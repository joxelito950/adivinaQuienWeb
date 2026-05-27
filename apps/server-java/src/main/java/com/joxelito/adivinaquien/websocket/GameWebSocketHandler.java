package com.joxelito.adivinaquien.websocket;

import com.joxelito.adivinaquien.config.AppProperties;
import com.joxelito.adivinaquien.domain.*;
import com.joxelito.adivinaquien.engine.GameActionException;
import com.joxelito.adivinaquien.engine.GameService;
import com.joxelito.adivinaquien.engine.GuessResult;
import com.joxelito.adivinaquien.engine.PendingQuestionPrompt;
import com.joxelito.adivinaquien.engine.QuestionPolicy;
import com.joxelito.adivinaquien.engine.QuestionResult;
import com.joxelito.adivinaquien.matchmaking.MatchParticipant;
import com.joxelito.adivinaquien.matchmaking.MatchStarted;
import com.joxelito.adivinaquien.matchmaking.MatchmakingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketHandler.class);
    protected static final String CHARACTER_ID = "characterId";
    protected static final String PLAYER_ID = "playerId";
    protected static final String GAME_ID = "gameId";
    protected static final String TURN_CHANGED = "turn_changed";
    protected static final String CURRENT_TURN_PLAYER_ID = "currentTurnPlayerId";
    protected static final String QUESTION_KEY = "questionKey";
    protected static final String ANSWER = "answer";
    protected static final String GUESS_RESULT = "guess_result";

    private final ObjectMapper objectMapper;
    private final MatchmakingService matchmakingService;
    private final GameService gameService;
    private final QuestionPolicy questionPolicy;
    private final AppProperties appProperties;

    private final Map<String, WebSocketSession> sessionById = new ConcurrentHashMap<>();
    private final Map<String, String> playerBySessionId = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdByPlayer = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> questionTimeoutTaskByGame = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> playerInactivityTaskByGame = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Set<String>>> candidateCharacterIdsByGame = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Set<QuestionKey>>> askedQuestionKeysByGame = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public GameWebSocketHandler(
            ObjectMapper objectMapper,
            MatchmakingService matchmakingService,
            GameService gameService,
            QuestionPolicy questionPolicy,
            AppProperties appProperties
    ) {
        this.objectMapper = objectMapper;
        this.matchmakingService = matchmakingService;
        this.gameService = gameService;
        this.questionPolicy = questionPolicy;
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() {
        matchmakingService.registerListener(this::onMatchStarted);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionById.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ClientMessage clientMessage = objectMapper.readValue(message.getPayload(), ClientMessage.class);
            dispatchCommand(session, clientMessage);
        } catch (GameActionException ex) {
            send(session, "invalid_action", null, Map.of("reason", ex.getMessage()));
        } catch (Exception ex) {
            logger.warn("Invalid WebSocket message", ex);
            send(session, "error", null, Map.of("reason", "invalid_message"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionById.remove(session.getId());
        String playerId = playerBySessionId.remove(session.getId());
        if (playerId == null) {
            return;
        }

        sessionIdByPlayer.remove(playerId);
        GameSession game = gameService.disconnectPlayer(playerId);
        if (game == null) {
            return;
        }

        broadcast(game, "player_disconnected", Map.of(PLAYER_ID, playerId, "at", Instant.now().toString()));
        if (game.getStatus() == GameStatus.ABANDONED || game.getStatus() == GameStatus.FINISHED) {
            cancelQuestionTimeout(game.getGameId());
            cancelPlayerInactivityTimeout(game.getGameId());
            candidateCharacterIdsByGame.remove(game.getGameId());
            askedQuestionKeysByGame.remove(game.getGameId());
            broadcast(game, "game_finished", Map.of(GAME_ID, game.getGameId(), "winnerPlayerId", game.getWinnerPlayerId()));
        }
    }

    private void dispatchCommand(WebSocketSession session, ClientMessage message) {
        String type = message.type();
        if (type == null) {
            throw new GameActionException("Command type is required");
        }

        switch (type) {
            case "join_queue" -> handleJoinQueue(session, message.payload());
            case "leave_queue" -> handleLeaveQueue(session, message.payload());
            case "ask_question" -> handleAskQuestion(message.payload());
            case "answer_question" -> handleAnswerQuestion(message.payload());
            case "guess_character" -> handleGuessCharacter(message.payload());
            case "reconnect_game" -> handleReconnectGame(session, message.payload());
            case "ping" -> send(session, "pong", message.correlationId(), Map.of("ts", Instant.now().toString()));
            default -> throw new GameActionException("Unsupported command: " + type);
        }
    }

    private void handleJoinQueue(WebSocketSession session, JsonNode payload) {
        String playerId = getRequiredText(payload, PLAYER_ID);
        Difficulty difficulty = Difficulty.fromWireValue(getRequiredText(payload, "difficulty"));

        sessionIdByPlayer.put(playerId, session.getId());
        playerBySessionId.put(session.getId(), playerId);

        boolean accepted = matchmakingService.joinQueue(playerId, session.getId(), difficulty);
        if (!accepted) {
            throw new GameActionException("Player is already waiting in queue");
        }

        send(session, "queue_joined", null, Map.of("difficulty", difficulty.toWireValue()));
        send(session, "queue_waiting", null, Map.of("timeoutSeconds", appProperties.getMatchTimeoutSeconds()));
    }

    private void handleLeaveQueue(WebSocketSession session, JsonNode payload) {
        String playerId = getRequiredText(payload, PLAYER_ID);
        matchmakingService.leaveQueue(playerId);
        send(session, "queue_left", null, Map.of(PLAYER_ID, playerId));
    }

    private void handleAskQuestion(JsonNode payload) {
        String gameId = getRequiredText(payload, GAME_ID);
        String playerId = getRequiredText(payload, PLAYER_ID);
        QuestionKey questionKey = QuestionKey.fromWireValue(getRequiredText(payload, QUESTION_KEY));

        if (!questionPolicy.isActive(questionKey)) {
            throw new GameActionException("Question is temporarily disabled: " + questionKey.toWireValue());
        }

        cancelPlayerInactivityTimeout(gameId);

        PendingQuestionPrompt prompt = gameService.askQuestion(gameId, playerId, questionKey);
        GameSession game = gameService.getGameById(gameId);
        if (game == null) {
            return;
        }

        rememberAskedQuestion(gameId, playerId, questionKey);

        broadcast(game, "question_asked", Map.of(
            GAME_ID, gameId,
            PLAYER_ID, playerId,
            QUESTION_KEY, prompt.questionKey()
        ));

        broadcast(game, "question_pending", Map.of(
                GAME_ID, prompt.gameId(),
                "askerPlayerId", prompt.askerPlayerId(),
                "defenderPlayerId", prompt.defenderPlayerId(),
                QUESTION_KEY, prompt.questionKey(),
                "timeoutSeconds", prompt.timeoutSeconds()
        ));

        scheduleQuestionTimeout(prompt);

        PlayerState defender = game.playerById(prompt.defenderPlayerId());
        if (defender != null && defender.getType() == PlayerType.DUMMY) {
            scheduleDummyAnswer(prompt);
        }
    }

    private void handleAnswerQuestion(JsonNode payload) {
        String gameId = getRequiredText(payload, GAME_ID);
        String playerId = getRequiredText(payload, PLAYER_ID);
        boolean answer = getRequiredBoolean(payload, ANSWER);

        QuestionResult result = gameService.answerQuestion(gameId, playerId, answer, false);
        cancelQuestionTimeout(gameId);

        GameSession game = gameService.getGameById(gameId);
        if (game == null) {
            return;
        }

        broadcast(game, "question_answered", Map.of(
                GAME_ID, gameId,
            PLAYER_ID, result.playerId(),
                QUESTION_KEY, result.questionKey(),
                ANSWER, result.answer(),
                "timeoutFallback", result.timeoutFallback()
        ));

        broadcast(game, TURN_CHANGED, Map.of(GAME_ID, gameId, CURRENT_TURN_PLAYER_ID, result.nextTurnPlayerId()));
        updateCandidatesFromAnswer(game, result.playerId(), result.questionKey(), result.answer());
        sendCandidatesUpdated(game, result.playerId());
        schedulePlayerInactivityTimeout(game);
        maybeTriggerDummyTurn(game);
    }

    private void handleGuessCharacter(JsonNode payload) {
        String gameId = getRequiredText(payload, GAME_ID);
        String playerId = getRequiredText(payload, PLAYER_ID);
        String characterId = getRequiredText(payload, CHARACTER_ID);
        cancelPlayerInactivityTimeout(gameId);

        GuessResult result = gameService.guessCharacter(gameId, playerId, characterId);
        GameSession game = gameService.getGameById(gameId);

        broadcast(game, GUESS_RESULT, Map.of(
                GAME_ID, gameId,
                PLAYER_ID, playerId,
                CHARACTER_ID, characterId,
                "correct", result.correct()
        ));

        if (result.finished()) {
            cancelQuestionTimeout(gameId);
            cancelPlayerInactivityTimeout(gameId);
            candidateCharacterIdsByGame.remove(gameId);
            askedQuestionKeysByGame.remove(gameId);
            broadcast(game, "game_finished", Map.of(GAME_ID, gameId, "winnerPlayerId", result.winnerPlayerId()));
            return;
        }

        removeCandidateGuess(game, playerId, characterId);
        sendCandidatesUpdated(game, playerId);

        broadcast(game, TURN_CHANGED, Map.of(GAME_ID, gameId, CURRENT_TURN_PLAYER_ID, result.nextTurnPlayerId()));
        schedulePlayerInactivityTimeout(game);
        maybeTriggerDummyTurn(game);
    }

    private void handleReconnectGame(WebSocketSession session, JsonNode payload) {
        String gameId = getRequiredText(payload, GAME_ID);
        String playerId = getRequiredText(payload, PLAYER_ID);

        sessionIdByPlayer.put(playerId, session.getId());
        playerBySessionId.put(session.getId(), playerId);

        GameSession game = gameService.reconnectPlayer(gameId, playerId, session.getId());
        send(session, "reconnected", null, Map.of(
                GAME_ID, gameId,
                CURRENT_TURN_PLAYER_ID, game.getCurrentTurnPlayerId(),
                "status", game.getStatus().name().toLowerCase()
        ));

        PendingQuestionPrompt pending = gameService.getPendingQuestion(gameId);
        if (pending != null) {
            send(session, "question_pending", null, Map.of(
                GAME_ID, pending.gameId(),
                "askerPlayerId", pending.askerPlayerId(),
                "defenderPlayerId", pending.defenderPlayerId(),
                QUESTION_KEY, pending.questionKey(),
                "timeoutSeconds", pending.timeoutSeconds()
            ));
        } else {
            schedulePlayerInactivityTimeout(game);
        }

        sendCandidatesUpdated(game, playerId);
    }

    private void onMatchStarted(MatchStarted matchStarted) {
        GameSession game = gameService.createFromMatch(matchStarted);
        initializeCandidateTracking(game);
        initializeAskedQuestionTracking(game);

        sendGameStarted(game, matchStarted.first());
        sendGameStarted(game, matchStarted.second());
        sendCandidatesUpdated(game, game.getPlayerOne().getPlayerId());
        sendCandidatesUpdated(game, game.getPlayerTwo().getPlayerId());
        schedulePlayerInactivityTimeout(game);
        maybeTriggerDummyTurn(game);
    }

    private void sendGameStarted(GameSession game, MatchParticipant participant) {
        if (participant.type() == PlayerType.DUMMY) {
            return;
        }
        WebSocketSession session = sessionById.get(participant.socketSessionId());
        if (session == null) {
            return;
        }

        Board board = game.getBoard();
        List<Map<String, Object>> characters = board.characters().stream()
                .map(card -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put(CHARACTER_ID, card.characterId());
                    data.put("displayName", card.displayName());
                    data.put("imageUrl", card.imageUrl());
                    return data;
                })
                .toList();

        PlayerState opponent = game.opponentOf(participant.playerId());

        Map<String, Object> payload = new HashMap<>();
        payload.put(GAME_ID, game.getGameId());
        payload.put("difficulty", game.getDifficulty().toWireValue());
        payload.put("opponentType", opponent.getType().name().toLowerCase());
        payload.put("board", Map.of("rows", board.rows(), "cols", board.cols(), "characters", characters));
        payload.put("yourSecretCharacterId", game.getSecretByPlayer().get(participant.playerId()));
        payload.put("firstTurnPlayerId", game.getCurrentTurnPlayerId());

        send(session, "game_started", null, payload);
    }

    private void maybeTriggerDummyTurn(GameSession game) {
        if (game == null || game.getStatus() != GameStatus.IN_PROGRESS) {
            return;
        }

        PlayerState current = game.playerById(game.getCurrentTurnPlayerId());
        if (current == null || current.getType() != PlayerType.DUMMY) {
            return;
        }

        cancelPlayerInactivityTimeout(game.getGameId());

        virtualExecutor.submit(() -> {
            try {
                Thread.sleep(appProperties.getDummyActionDelayMillis());
                int candidateCount = candidateCountForPlayer(game, current.getPlayerId());
                if (candidateCount <= 2) {
                    String guessedCharacterId = randomCandidateCharacterId(game, current.getPlayerId());
                    GuessResult result = gameService.guessCharacter(game.getGameId(), current.getPlayerId(), guessedCharacterId);
                    GameSession updatedGame = gameService.getGameById(game.getGameId());
                    if (updatedGame == null) {
                        return;
                    }

                    broadcast(updatedGame, GUESS_RESULT, Map.of(
                            GAME_ID, game.getGameId(),
                            PLAYER_ID, current.getPlayerId(),
                            CHARACTER_ID, guessedCharacterId,
                            "correct", result.correct()
                    ));

                    if (result.finished()) {
                        cancelQuestionTimeout(game.getGameId());
                        cancelPlayerInactivityTimeout(game.getGameId());
                        candidateCharacterIdsByGame.remove(game.getGameId());
                        askedQuestionKeysByGame.remove(game.getGameId());
                        broadcast(updatedGame, "game_finished", Map.of(GAME_ID, game.getGameId(), "winnerPlayerId", result.winnerPlayerId()));
                        return;
                    }

                    removeCandidateGuess(updatedGame, current.getPlayerId(), guessedCharacterId);
                    broadcast(updatedGame, TURN_CHANGED, Map.of(
                            GAME_ID, game.getGameId(),
                            CURRENT_TURN_PLAYER_ID, result.nextTurnPlayerId()
                    ));
                    schedulePlayerInactivityTimeout(updatedGame);
                    maybeTriggerDummyTurn(updatedGame);
                    return;
                }

                QuestionKey key = chooseBestQuestionForPlayer(game, current.getPlayerId());
                PendingQuestionPrompt prompt = gameService.askQuestion(game.getGameId(), current.getPlayerId(), key);
                rememberAskedQuestion(game.getGameId(), current.getPlayerId(), key);
                GameSession updatedGame = gameService.getGameById(game.getGameId());
                if (updatedGame == null) {
                    return;
                }

                broadcast(updatedGame, "question_asked", Map.of(
                        GAME_ID, prompt.gameId(),
                        PLAYER_ID, prompt.askerPlayerId(),
                        QUESTION_KEY, prompt.questionKey()
                ));
                broadcast(updatedGame, "question_pending", Map.of(
                        GAME_ID, prompt.gameId(),
                        "askerPlayerId", prompt.askerPlayerId(),
                        "defenderPlayerId", prompt.defenderPlayerId(),
                        QUESTION_KEY, prompt.questionKey(),
                        "timeoutSeconds", prompt.timeoutSeconds()
                ));

                scheduleQuestionTimeout(prompt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                logger.warn("Dummy action failed", ex);
            }
        });
    }

    private void scheduleQuestionTimeout(PendingQuestionPrompt prompt) {
        cancelQuestionTimeout(prompt.gameId());
        Future<?> task = virtualExecutor.submit(() -> {
            try {
                Thread.sleep(prompt.timeoutSeconds() * 1000L);
                QuestionResult result = gameService.resolvePendingQuestionWithCorrectAnswer(prompt.gameId(), true);
                if (result == null) {
                    return;
                }
                GameSession game = gameService.getGameById(prompt.gameId());
                if (game == null) {
                    return;
                }

                broadcast(game, "question_answered", Map.of(
                        GAME_ID, prompt.gameId(),
                        PLAYER_ID, result.playerId(),
                        QUESTION_KEY, result.questionKey(),
                        ANSWER, result.answer(),
                        "timeoutFallback", result.timeoutFallback()
                ));
                updateCandidatesFromAnswer(game, result.playerId(), result.questionKey(), result.answer());
                sendCandidatesUpdated(game, result.playerId());
                broadcast(game, TURN_CHANGED, Map.of(
                        GAME_ID, prompt.gameId(),
                        CURRENT_TURN_PLAYER_ID, result.nextTurnPlayerId()
                ));
                schedulePlayerInactivityTimeout(game);
                maybeTriggerDummyTurn(game);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                logger.warn("Question timeout handling failed", ex);
            } finally {
                questionTimeoutTaskByGame.remove(prompt.gameId());
            }
        });
        questionTimeoutTaskByGame.put(prompt.gameId(), task);
    }

    private void scheduleDummyAnswer(PendingQuestionPrompt prompt) {
        virtualExecutor.submit(() -> {
            try {
                Thread.sleep(appProperties.getDummyActionDelayMillis());
                QuestionResult result = gameService.resolvePendingQuestionWithCorrectAnswer(prompt.gameId(), false);
                if (result == null) {
                    return;
                }
                cancelQuestionTimeout(prompt.gameId());

                GameSession game = gameService.getGameById(prompt.gameId());
                if (game == null) {
                    return;
                }

                broadcast(game, "question_answered", Map.of(
                        GAME_ID, prompt.gameId(),
                        PLAYER_ID, result.playerId(),
                        QUESTION_KEY, result.questionKey(),
                        ANSWER, result.answer(),
                        "timeoutFallback", result.timeoutFallback()
                ));
                updateCandidatesFromAnswer(game, result.playerId(), result.questionKey(), result.answer());
                sendCandidatesUpdated(game, result.playerId());
                broadcast(game, TURN_CHANGED, Map.of(
                        GAME_ID, prompt.gameId(),
                        CURRENT_TURN_PLAYER_ID, result.nextTurnPlayerId()
                ));
                schedulePlayerInactivityTimeout(game);
                maybeTriggerDummyTurn(game);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                logger.warn("Dummy answer failed", ex);
            }
        });
    }

    private void cancelQuestionTimeout(String gameId) {
        Future<?> previous = questionTimeoutTaskByGame.remove(gameId);
        if (previous != null) {
            previous.cancel(true);
        }
    }

    private void initializeCandidateTracking(GameSession game) {
        if (game == null) {
            return;
        }

        Map<String, Set<String>> candidatesByPlayer = new ConcurrentHashMap<>();
        candidatesByPlayer.put(game.getPlayerOne().getPlayerId(), allCandidateIdsExceptOwnSecret(game, game.getPlayerOne().getPlayerId()));
        candidatesByPlayer.put(game.getPlayerTwo().getPlayerId(), allCandidateIdsExceptOwnSecret(game, game.getPlayerTwo().getPlayerId()));
        candidateCharacterIdsByGame.put(game.getGameId(), candidatesByPlayer);
    }

    private Set<String> allCandidateIdsExceptOwnSecret(GameSession game, String playerId) {
        String ownSecret = game.getSecretByPlayer().get(playerId);
        Set<String> ids = new LinkedHashSet<>();
        for (CharacterCard card : game.getBoard().characters()) {
            if (!card.characterId().equals(ownSecret)) {
                ids.add(card.characterId());
            }
        }
        return ids;
    }

    private void updateCandidatesFromAnswer(GameSession game, String askerPlayerId, String questionKeyWire, boolean answer) {
        if (game == null || askerPlayerId == null || questionKeyWire == null) {
            return;
        }

        Map<String, Set<String>> byPlayer = candidateCharacterIdsByGame.computeIfAbsent(
                game.getGameId(),
                ignored -> new ConcurrentHashMap<>()
        );

        Set<String> current = byPlayer.computeIfAbsent(
                askerPlayerId,
                ignored -> allCandidateIdsExceptOwnSecret(game, askerPlayerId)
        );

        QuestionKey key;
        try {
            key = QuestionKey.fromWireValue(questionKeyWire);
        } catch (Exception ignored) {
            return;
        }

        Set<String> filtered = new HashSet<>();
        for (CharacterCard card : game.getBoard().characters()) {
            if (!current.contains(card.characterId())) {
                continue;
            }
            if (card.hasAttribute(key) == answer) {
                filtered.add(card.characterId());
            }
        }

        if (!filtered.isEmpty()) {
            byPlayer.put(askerPlayerId, filtered);
        }
    }

    private int candidateCountForPlayer(GameSession game, String playerId) {
        if (game == null || playerId == null) {
            return Integer.MAX_VALUE;
        }
        Map<String, Set<String>> byPlayer = candidateCharacterIdsByGame.get(game.getGameId());
        if (byPlayer == null || byPlayer.get(playerId) == null || byPlayer.get(playerId).isEmpty()) {
            return allCandidateIdsExceptOwnSecret(game, playerId).size();
        }
        return byPlayer.get(playerId).size();
    }

    private void removeCandidateGuess(GameSession game, String playerId, String guessedCharacterId) {
        if (game == null || playerId == null || guessedCharacterId == null) {
            return;
        }

        Map<String, Set<String>> byPlayer = candidateCharacterIdsByGame.get(game.getGameId());
        if (byPlayer == null) {
            return;
        }
        Set<String> candidates = byPlayer.get(playerId);
        if (candidates == null) {
            return;
        }
        candidates.remove(guessedCharacterId);
    }

    private String randomCandidateCharacterId(GameSession game, String playerId) {
        Map<String, Set<String>> byPlayer = candidateCharacterIdsByGame.get(game.getGameId());
        Set<String> candidates = byPlayer == null ? null : byPlayer.get(playerId);
        List<String> pool = (candidates == null || candidates.isEmpty())
                ? List.copyOf(allCandidateIdsExceptOwnSecret(game, playerId))
                : List.copyOf(candidates);

        if (pool.isEmpty()) {
            return game.getBoard().characters().getFirst().characterId();
        }
        return pool.get(random.nextInt(pool.size()));
    }

    private void schedulePlayerInactivityTimeout(GameSession game) {
        if (game == null) {
            return;
        }

        String gameId = game.getGameId();
        cancelPlayerInactivityTimeout(gameId);

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            return;
        }
        if (gameService.getPendingQuestion(gameId) != null) {
            return;
        }

        PlayerState current = game.playerById(game.getCurrentTurnPlayerId());
        if (current == null || current.getType() != PlayerType.HUMAN) {
            return;
        }

        Future<?> task = virtualExecutor.submit(() -> {
            try {
                Thread.sleep(appProperties.getPlayerInactivityTimeoutSeconds() * 1000L);

                GameSession latest = gameService.getGameById(gameId);
                if (latest == null || latest.getStatus() != GameStatus.IN_PROGRESS) {
                    return;
                }
                if (gameService.getPendingQuestion(gameId) != null) {
                    return;
                }

                String currentPlayerId = latest.getCurrentTurnPlayerId();
                PlayerState latestCurrent = latest.playerById(currentPlayerId);
                if (latestCurrent == null || latestCurrent.getType() != PlayerType.HUMAN) {
                    return;
                }

                int candidateCount = candidateCountForPlayer(latest, currentPlayerId);
                if (candidateCount <= 4) {
                    String guessedCharacterId = randomCandidateCharacterId(latest, currentPlayerId);
                    GuessResult result = gameService.guessCharacter(gameId, currentPlayerId, guessedCharacterId);
                    GameSession updated = gameService.getGameById(gameId);
                    if (updated == null) {
                        return;
                    }

                    broadcast(updated, "auto_action_triggered", Map.of(
                            GAME_ID, gameId,
                            PLAYER_ID, currentPlayerId,
                            "action", "guess_character",
                            CHARACTER_ID, guessedCharacterId,
                            "candidateCount", candidateCount,
                            "correct", result.correct()
                    ));

                    broadcast(updated, GUESS_RESULT, Map.of(
                            GAME_ID, gameId,
                            PLAYER_ID, currentPlayerId,
                            CHARACTER_ID, guessedCharacterId,
                            "correct", result.correct()
                    ));

                    if (result.finished()) {
                        cancelQuestionTimeout(gameId);
                        cancelPlayerInactivityTimeout(gameId);
                        candidateCharacterIdsByGame.remove(gameId);
                        askedQuestionKeysByGame.remove(gameId);
                        broadcast(updated, "game_finished", Map.of(GAME_ID, gameId, "winnerPlayerId", result.winnerPlayerId()));
                        return;
                    }

                    broadcast(updated, TURN_CHANGED, Map.of(GAME_ID, gameId, CURRENT_TURN_PLAYER_ID, result.nextTurnPlayerId()));
                    schedulePlayerInactivityTimeout(updated);
                    maybeTriggerDummyTurn(updated);
                    return;
                }

                QuestionKey randomQuestion = chooseBestQuestionForPlayer(latest, currentPlayerId);
                PendingQuestionPrompt prompt = gameService.askQuestion(gameId, currentPlayerId, randomQuestion);
                rememberAskedQuestion(gameId, currentPlayerId, randomQuestion);
                GameSession updated = gameService.getGameById(gameId);
                if (updated == null) {
                    return;
                }

                broadcast(updated, "auto_action_triggered", Map.of(
                        GAME_ID, gameId,
                        PLAYER_ID, currentPlayerId,
                        "action", "ask_question",
                        QUESTION_KEY, prompt.questionKey(),
                        "candidateCount", candidateCount
                ));

                broadcast(updated, "question_asked", Map.of(
                        GAME_ID, prompt.gameId(),
                        PLAYER_ID, prompt.askerPlayerId(),
                        QUESTION_KEY, prompt.questionKey()
                ));
                broadcast(updated, "question_pending", Map.of(
                        GAME_ID, prompt.gameId(),
                        "askerPlayerId", prompt.askerPlayerId(),
                        "defenderPlayerId", prompt.defenderPlayerId(),
                        QUESTION_KEY, prompt.questionKey(),
                        "timeoutSeconds", prompt.timeoutSeconds()
                ));

                scheduleQuestionTimeout(prompt);
                PlayerState defender = updated.playerById(prompt.defenderPlayerId());
                if (defender != null && defender.getType() == PlayerType.DUMMY) {
                    scheduleDummyAnswer(prompt);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (GameActionException ignored) {
                // Si el estado cambió por una acción concurrente, no es un error operativo.
            } catch (Exception ex) {
                logger.warn("Player inactivity handling failed", ex);
            } finally {
                playerInactivityTaskByGame.remove(gameId);
            }
        });

        playerInactivityTaskByGame.put(gameId, task);
    }

    private void cancelPlayerInactivityTimeout(String gameId) {
        Future<?> previous = playerInactivityTaskByGame.remove(gameId);
        if (previous != null) {
            previous.cancel(true);
        }
    }

    private void initializeAskedQuestionTracking(GameSession game) {
        if (game == null) {
            return;
        }

        Map<String, Set<QuestionKey>> askedByPlayer = new ConcurrentHashMap<>();
        askedByPlayer.put(game.getPlayerOne().getPlayerId(), ConcurrentHashMap.newKeySet());
        askedByPlayer.put(game.getPlayerTwo().getPlayerId(), ConcurrentHashMap.newKeySet());
        askedQuestionKeysByGame.put(game.getGameId(), askedByPlayer);
    }

    private void rememberAskedQuestion(String gameId, String playerId, QuestionKey key) {
        askedQuestionKeysByGame
                .computeIfAbsent(gameId, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(key);
    }

    private Set<QuestionKey> askedQuestionsForPlayer(String gameId, String playerId) {
        Map<String, Set<QuestionKey>> byPlayer = askedQuestionKeysByGame.get(gameId);
        if (byPlayer == null) {
            return Set.of();
        }
        Set<QuestionKey> asked = byPlayer.get(playerId);
        return asked == null ? Set.of() : Set.copyOf(asked);
    }

    private QuestionKey chooseBestQuestionForPlayer(GameSession game, String playerId) {
        List<QuestionKey> active = questionPolicy.activeQuestions();
        Set<QuestionKey> asked = askedQuestionsForPlayer(game.getGameId(), playerId);

        List<QuestionKey> available = active.stream()
                .filter(key -> !asked.contains(key))
                .toList();

        if (available.isEmpty()) {
            askedQuestionKeysByGame
                    .computeIfAbsent(game.getGameId(), ignored -> new ConcurrentHashMap<>())
                    .put(playerId, ConcurrentHashMap.newKeySet());
            available = active;
        }

        List<String> candidates = candidatePoolForPlayer(game, playerId);
        if (candidates.size() <= 1) {
            return available.get(random.nextInt(available.size()));
        }

        int bestScore = -1;
        List<QuestionKey> ties = new ArrayList<>();

        for (QuestionKey key : available) {
            int yesCount = 0;
            for (CharacterCard card : game.getBoard().characters()) {
                if (candidates.contains(card.characterId()) && card.hasAttribute(key)) {
                    yesCount++;
                }
            }

            int noCount = candidates.size() - yesCount;
            int score = Math.min(yesCount, noCount);

            if (score > bestScore) {
                bestScore = score;
                ties.clear();
                ties.add(key);
            } else if (score == bestScore) {
                ties.add(key);
            }
        }

        if (!ties.isEmpty()) {
            return ties.get(random.nextInt(ties.size()));
        }

        return available.get(random.nextInt(available.size()));
    }

    private List<String> candidatePoolForPlayer(GameSession game, String playerId) {
        Map<String, Set<String>> byPlayer = candidateCharacterIdsByGame.get(game.getGameId());
        Set<String> candidates = byPlayer == null ? null : byPlayer.get(playerId);
        if (candidates == null || candidates.isEmpty()) {
            return List.copyOf(allCandidateIdsExceptOwnSecret(game, playerId));
        }
        return List.copyOf(candidates);
    }

    private void sendCandidatesUpdated(GameSession game, String playerId) {
        if (game == null) {
            return;
        }

        PlayerState player = game.playerById(playerId);
        if (player == null || player.getType() == PlayerType.DUMMY) {
            return;
        }

        String ownSecret = game.getSecretByPlayer().get(playerId);
        Set<String> activeCandidates = new HashSet<>(candidatePoolForPlayer(game, playerId));
        List<String> eliminated = game.getBoard().characters().stream()
                .map(CharacterCard::characterId)
                .filter(id -> !id.equals(ownSecret))
                .filter(id -> !activeCandidates.contains(id))
                .toList();

        sendToPlayer(player, "candidates_updated", Map.of(
                GAME_ID, game.getGameId(),
                PLAYER_ID, playerId,
                "eliminatedCharacterIds", eliminated,
                "candidateCount", activeCandidates.size()
        ));
    }

    private void broadcast(GameSession game, String type, Object payload) {
        if (game == null) {
            return;
        }
        sendToPlayer(game.getPlayerOne(), type, payload);
        sendToPlayer(game.getPlayerTwo(), type, payload);
    }

    private void sendToPlayer(PlayerState player, String type, Object payload) {
        if (player.getType() == PlayerType.DUMMY) {
            return;
        }
        String sessionId = sessionIdByPlayer.get(player.getPlayerId());
        if (sessionId == null) {
            return;
        }
        WebSocketSession session = sessionById.get(sessionId);
        if (session == null) {
            return;
        }
        send(session, type, null, payload);
    }

    private void send(WebSocketSession session, String type, String correlationId, Object payload) {
        try {
            if (!session.isOpen()) {
                return;
            }
            String json = objectMapper.writeValueAsString(new ServerMessage(type, correlationId, payload));
            synchronized (session) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException ex) {
            logger.warn("Failed to send WebSocket event {}", type, ex);
        }
    }

    private String getRequiredText(JsonNode payload, String field) {
        JsonNode node = payload == null ? null : payload.get(field);
        if (node == null || node.isNull() || !node.isTextual() || node.textValue().isBlank()) {
            throw new GameActionException("Missing field: " + field);
        }
        return node.textValue();
    }

    private boolean getRequiredBoolean(JsonNode payload, String field) {
        JsonNode node = payload == null ? null : payload.get(field);
        if (node == null || node.isNull() || !node.isBoolean()) {
            throw new GameActionException("Missing boolean field: " + field);
        }
        return node.asBoolean();
    }

    @PreDestroy
    public void shutdown() {
        questionTimeoutTaskByGame.values().forEach(task -> task.cancel(true));
        questionTimeoutTaskByGame.clear();
        playerInactivityTaskByGame.values().forEach(task -> task.cancel(true));
        playerInactivityTaskByGame.clear();
        candidateCharacterIdsByGame.clear();
        askedQuestionKeysByGame.clear();
        virtualExecutor.shutdownNow();
    }
}



