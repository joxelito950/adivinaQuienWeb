package com.joxelito.adivinaquien.websocket;

import com.joxelito.adivinaquien.config.AppProperties;
import com.joxelito.adivinaquien.domain.*;
import com.joxelito.adivinaquien.dummy.DummyPlayerService;
import com.joxelito.adivinaquien.engine.GameActionException;
import com.joxelito.adivinaquien.engine.GameService;
import com.joxelito.adivinaquien.engine.GuessResult;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final DummyPlayerService dummyPlayerService;
    private final AppProperties appProperties;

    private final Map<String, WebSocketSession> sessionById = new ConcurrentHashMap<>();
    private final Map<String, String> playerBySessionId = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdByPlayer = new ConcurrentHashMap<>();
    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public GameWebSocketHandler(
            ObjectMapper objectMapper,
            MatchmakingService matchmakingService,
            GameService gameService,
            DummyPlayerService dummyPlayerService,
            AppProperties appProperties
    ) {
        this.objectMapper = objectMapper;
        this.matchmakingService = matchmakingService;
        this.gameService = gameService;
        this.dummyPlayerService = dummyPlayerService;
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

        QuestionResult result = gameService.askQuestion(gameId, playerId, questionKey);
        GameSession game = gameService.getGameById(gameId);

        broadcast(game, "question_answered", Map.of(
                GAME_ID, gameId,
                PLAYER_ID, playerId,
                QUESTION_KEY, result.questionKey(),
                ANSWER, result.answer()
        ));

        broadcast(game, TURN_CHANGED, Map.of(GAME_ID, gameId, CURRENT_TURN_PLAYER_ID, result.nextTurnPlayerId()));
        maybeTriggerDummyTurn(game);
    }

    private void handleGuessCharacter(JsonNode payload) {
        String gameId = getRequiredText(payload, GAME_ID);
        String playerId = getRequiredText(payload, PLAYER_ID);
        String characterId = getRequiredText(payload, CHARACTER_ID);

        GuessResult result = gameService.guessCharacter(gameId, playerId, characterId);
        GameSession game = gameService.getGameById(gameId);

        broadcast(game, GUESS_RESULT, Map.of(
                GAME_ID, gameId,
                PLAYER_ID, playerId,
                CHARACTER_ID, characterId,
                "correct", result.correct()
        ));

        if (result.finished()) {
            broadcast(game, "game_finished", Map.of(GAME_ID, gameId, "winnerPlayerId", result.winnerPlayerId()));
            return;
        }

        broadcast(game, TURN_CHANGED, Map.of(GAME_ID, gameId, CURRENT_TURN_PLAYER_ID, result.nextTurnPlayerId()));
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
    }

    private void onMatchStarted(MatchStarted matchStarted) {
        GameSession game = gameService.createFromMatch(matchStarted);

        sendGameStarted(game, matchStarted.first());
        sendGameStarted(game, matchStarted.second());
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

        virtualExecutor.submit(() -> {
            try {
                Thread.sleep(appProperties.getDummyActionDelayMillis());
                QuestionKey key = dummyPlayerService.chooseQuestion(game.getDifficulty());
                QuestionResult questionResult = gameService.askQuestion(game.getGameId(), current.getPlayerId(), key);
                GameSession updatedGame = gameService.getGameById(game.getGameId());
                broadcast(updatedGame, "question_answered", Map.of(
                        GAME_ID, game.getGameId(),
                        PLAYER_ID, current.getPlayerId(),
                        QUESTION_KEY, questionResult.questionKey(),
                        ANSWER, questionResult.answer()
                ));
                broadcast(updatedGame, TURN_CHANGED, Map.of(
                        GAME_ID, game.getGameId(),
                        CURRENT_TURN_PLAYER_ID, questionResult.nextTurnPlayerId()
                ));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                logger.warn("Dummy action failed", ex);
            }
        });
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
        if (node == null || node.isNull() || node.asText().isBlank()) {
            throw new GameActionException("Missing field: " + field);
        }
        return node.asText();
    }

    @PreDestroy
    public void shutdown() {
        virtualExecutor.shutdownNow();
    }
}



