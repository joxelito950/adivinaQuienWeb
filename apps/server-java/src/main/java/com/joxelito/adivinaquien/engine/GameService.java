package com.joxelito.adivinaquien.engine;

import com.joxelito.adivinaquien.concurrency.GameLockManager;
import com.joxelito.adivinaquien.config.AppProperties;
import com.joxelito.adivinaquien.domain.Board;
import com.joxelito.adivinaquien.domain.CharacterCard;
import com.joxelito.adivinaquien.domain.Difficulty;
import com.joxelito.adivinaquien.domain.GameSession;
import com.joxelito.adivinaquien.domain.GameStatus;
import com.joxelito.adivinaquien.domain.PlayerState;
import com.joxelito.adivinaquien.domain.PlayerType;
import com.joxelito.adivinaquien.domain.QuestionKey;
import com.joxelito.adivinaquien.matchmaking.MatchParticipant;
import com.joxelito.adivinaquien.matchmaking.MatchStarted;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GameService {

    private final BoardFactory boardFactory;
    private final GameLockManager gameLockManager;
    private final long reconnectTimeoutSeconds;
    private final long questionResponseTimeoutSeconds;
    private final ScheduledExecutorService scheduler;
    private final Random random;

    private final Map<String, GameSession> gameById = new ConcurrentHashMap<>();
    private final Map<String, String> gameIdByPlayer = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> abandonTaskByPlayer = new ConcurrentHashMap<>();
    private final Map<String, PendingQuestionState> pendingQuestionByGame = new ConcurrentHashMap<>();

    public GameService(BoardFactory boardFactory, GameLockManager gameLockManager, AppProperties appProperties) {
        this.boardFactory = boardFactory;
        this.gameLockManager = gameLockManager;
        this.reconnectTimeoutSeconds = appProperties.getReconnectTimeoutSeconds();
        this.questionResponseTimeoutSeconds = appProperties.getQuestionResponseTimeoutSeconds();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.random = new SecureRandom();
    }

    public GameSession createFromMatch(MatchStarted matchStarted) {
        Difficulty difficulty = matchStarted.difficulty();
        Board board = boardFactory.createBoard(difficulty);

        MatchParticipant first = matchStarted.first();
        MatchParticipant second = matchStarted.second();

        PlayerState p1 = new PlayerState(first.playerId(), first.type(), first.socketSessionId(), true);
        PlayerState p2 = new PlayerState(
                second.playerId(),
                second.type(),
                second.socketSessionId(),
                second.type() == PlayerType.DUMMY || second.socketSessionId() != null
        );

        List<CharacterCard> cards = board.characters();
        int p1SecretIndex = random.nextInt(cards.size());
        int p2SecretIndex = random.nextInt(cards.size() - 1);
        if (p2SecretIndex >= p1SecretIndex) {
            p2SecretIndex++;
        }
        String p1Secret = cards.get(p1SecretIndex).characterId();
        String p2Secret = cards.get(p2SecretIndex).characterId();

        String gameId = "g-" + UUID.randomUUID();
        GameSession gameSession = new GameSession(
                gameId,
                difficulty,
                p1,
                p2,
                board,
                Map.of(p1.getPlayerId(), p1Secret, p2.getPlayerId(), p2Secret),
                p1.getPlayerId()
        );

        gameById.put(gameId, gameSession);
        gameIdByPlayer.put(p1.getPlayerId(), gameId);
        gameIdByPlayer.put(p2.getPlayerId(), gameId);
        return gameSession;
    }

    public PendingQuestionPrompt askQuestion(String gameId, String playerId, QuestionKey questionKey) {
        return gameLockManager.executeSerialized(gameId, () -> {
            GameSession game = getActiveGame(gameId, playerId);
            enforceTurn(game, playerId);
            ensureNoPendingQuestion(gameId);

            PlayerState opponent = game.opponentOf(playerId);
            PendingQuestionState pending = new PendingQuestionState(
                    playerId,
                    opponent.getPlayerId(),
                    questionKey,
                    Instant.now(),
                    questionResponseTimeoutSeconds
            );
            pendingQuestionByGame.put(gameId, pending);

            return new PendingQuestionPrompt(
                    gameId,
                    playerId,
                    opponent.getPlayerId(),
                    questionKey.toWireValue(),
                    questionResponseTimeoutSeconds
            );
        });
    }

    public QuestionResult answerQuestion(String gameId, String defenderId, boolean answer, boolean timeoutFallback) {
        return gameLockManager.executeSerialized(gameId, () -> {
            GameSession game = getActiveGame(gameId, defenderId);
            PendingQuestionState pending = pendingQuestionByGame.get(gameId);
            if (pending == null) {
                throw new GameActionException("There is no pending question");
            }
            if (!pending.defenderPlayerId().equals(defenderId)) {
                throw new GameActionException("Only defender can answer pending question");
            }
            return resolvePendingQuestion(gameId, game, pending, answer, timeoutFallback);
        });
    }

    public QuestionResult resolvePendingQuestionWithCorrectAnswer(String gameId, boolean timeoutFallback) {
        return gameLockManager.executeSerialized(gameId, () -> {
            GameSession game = gameById.get(gameId);
            if (game == null || game.getStatus() != GameStatus.IN_PROGRESS) {
                return null;
            }
            PendingQuestionState pending = pendingQuestionByGame.get(gameId);
            if (pending == null) {
                return null;
            }

            boolean answer = computeCorrectAnswer(game, pending.defenderPlayerId(), pending.questionKey());
            return resolvePendingQuestion(gameId, game, pending, answer, timeoutFallback);
        });
    }

    public PendingQuestionPrompt getPendingQuestion(String gameId) {
        PendingQuestionState pending = pendingQuestionByGame.get(gameId);
        if (pending == null) {
            return null;
        }

        long elapsed = Duration.between(pending.askedAt(), Instant.now()).toSeconds();
        long remaining = Math.max(0, pending.timeoutSeconds() - elapsed);
        return new PendingQuestionPrompt(
                gameId,
                pending.askerPlayerId(),
                pending.defenderPlayerId(),
                pending.questionKey().toWireValue(),
                remaining
        );
    }

    public GuessResult guessCharacter(String gameId, String playerId, String guessedCharacterId) {
        return gameLockManager.executeSerialized(gameId, () -> {
            GameSession game = getActiveGame(gameId, playerId);
            enforceTurn(game, playerId);
            ensureNoPendingQuestion(gameId);

            String opponentId = Objects.requireNonNull(game.opponentOf(playerId)).getPlayerId();
            String expected = game.getSecretByPlayer().get(opponentId);
            boolean correct = expected.equals(guessedCharacterId);
            if (correct) {
                pendingQuestionByGame.remove(gameId);
                game.setWinnerPlayerId(playerId);
                game.setStatus(GameStatus.FINISHED);
                return new GuessResult(gameId, playerId, guessedCharacterId, true, null, playerId, true);
            }

            game.setCurrentTurnPlayerId(opponentId);
            return new GuessResult(gameId, playerId, guessedCharacterId, false, opponentId, null, false);
        });
    }

    public GameSession disconnectPlayer(String playerId) {
        String gameId = gameIdByPlayer.get(playerId);
        if (gameId == null) {
            return null;
        }
        return gameLockManager.executeSerialized(gameId, () -> {
            GameSession game = gameById.get(gameId);
            if (game == null || game.getStatus() != GameStatus.IN_PROGRESS) {
                return game;
            }
            PlayerState player = game.playerById(playerId);
            if (player == null) {
                return game;
            }
            player.setConnected(false);
            player.setDisconnectedAt(Instant.now());
            ScheduledFuture<?> future = scheduler.schedule(
                    () -> abandonIfStillDisconnected(gameId, playerId),
                    reconnectTimeoutSeconds,
                    TimeUnit.SECONDS
            );
            ScheduledFuture<?> prev = abandonTaskByPlayer.put(playerId, future);
            if (prev != null) {
                prev.cancel(false);
            }
            return game;
        });
    }

    public GameSession reconnectPlayer(String gameId, String playerId, String sessionId) {
        return gameLockManager.executeSerialized(gameId, () -> {
            GameSession game = gameById.get(gameId);
            if (game == null) {
                throw new GameActionException("Game not found");
            }
            PlayerState player = game.playerById(playerId);
            if (player == null) {
                throw new GameActionException("Player does not belong to game");
            }
            if (game.getStatus() != GameStatus.IN_PROGRESS) {
                throw new GameActionException("Game is not active");
            }
            player.setConnected(true);
            player.setSocketSessionId(sessionId);
            player.setDisconnectedAt(null);

            ScheduledFuture<?> future = abandonTaskByPlayer.remove(playerId);
            if (future != null) {
                future.cancel(false);
            }
            return game;
        });
    }

    public GameSession getGameById(String gameId) {
        return gameById.get(gameId);
    }

    public GameSession findByPlayerId(String playerId) {
        String gameId = gameIdByPlayer.get(playerId);
        return gameId == null ? null : gameById.get(gameId);
    }

    private GameSession getActiveGame(String gameId, String playerId) {
        GameSession game = gameById.get(gameId);
        if (game == null) {
            throw new GameActionException("Game not found");
        }
        if (!game.containsPlayer(playerId)) {
            throw new GameActionException("Player does not belong to game");
        }
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameActionException("Game is not active");
        }
        return game;
    }

    private void enforceTurn(GameSession game, String playerId) {
        if (!playerId.equals(game.getCurrentTurnPlayerId())) {
            throw new GameActionException("It is not your turn");
        }
    }

    private void ensureNoPendingQuestion(String gameId) {
        if (pendingQuestionByGame.containsKey(gameId)) {
            throw new GameActionException("Pending question must be answered first");
        }
    }

    private QuestionResult resolvePendingQuestion(
            String gameId,
            GameSession game,
            PendingQuestionState pending,
            boolean answer,
            boolean timeoutFallback
    ) {
        pendingQuestionByGame.remove(gameId);
        game.setCurrentTurnPlayerId(pending.defenderPlayerId());
        return new QuestionResult(
                gameId,
                pending.askerPlayerId(),
                pending.questionKey().toWireValue(),
                answer,
                game.getCurrentTurnPlayerId(),
                timeoutFallback
        );
    }

    private boolean computeCorrectAnswer(GameSession game, String defenderPlayerId, QuestionKey questionKey) {
        String defenderSecret = game.getSecretByPlayer().get(defenderPlayerId);
        CharacterCard defenderCard = game.getBoard().characters().stream()
                .filter(card -> card.characterId().equals(defenderSecret))
                .findFirst()
                .orElseThrow(() -> new GameActionException("Secret character not found"));
        return defenderCard.hasAttribute(questionKey);
    }

    private void abandonIfStillDisconnected(String gameId, String playerId) {
        gameLockManager.executeSerialized(gameId, () -> {
            GameSession game = gameById.get(gameId);
            if (game == null || game.getStatus() != GameStatus.IN_PROGRESS) {
                return;
            }
            PlayerState player = game.playerById(playerId);
            if (player == null || player.isConnected()) {
                return;
            }
            PlayerState opponent = game.opponentOf(playerId);
                pendingQuestionByGame.remove(gameId);
            game.setWinnerPlayerId(opponent.getPlayerId());
            game.setStatus(GameStatus.ABANDONED);
        });
    }

            private record PendingQuestionState(
                String askerPlayerId,
                String defenderPlayerId,
                QuestionKey questionKey,
                Instant askedAt,
                long timeoutSeconds
            ) {
            }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}



