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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final ScheduledExecutorService scheduler;

    private final Map<String, GameSession> gameById = new ConcurrentHashMap<>();
    private final Map<String, String> gameIdByPlayer = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> abandonTaskByPlayer = new ConcurrentHashMap<>();

    public GameService(BoardFactory boardFactory, GameLockManager gameLockManager, AppProperties appProperties) {
        this.boardFactory = boardFactory;
        this.gameLockManager = gameLockManager;
        this.reconnectTimeoutSeconds = appProperties.getReconnectTimeoutSeconds();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
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
        String p1Secret = cards.getFirst().characterId();
        String p2Secret = cards.getLast().characterId();
        if (p1Secret.equals(p2Secret) && cards.size() > 1) {
            p2Secret = cards.get(1).characterId();
        }

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

    public QuestionResult askQuestion(String gameId, String playerId, QuestionKey questionKey) {
        return gameLockManager.executeSerialized(gameId, () -> {
            GameSession game = getActiveGame(gameId, playerId);
            enforceTurn(game, playerId);

            PlayerState opponent = game.opponentOf(playerId);
            String opponentSecret = game.getSecretByPlayer().get(opponent.getPlayerId());
            CharacterCard opponentCard = game.getBoard().characters().stream()
                    .filter(card -> card.characterId().equals(opponentSecret))
                    .findFirst()
                    .orElseThrow(() -> new GameActionException("Secret character not found"));

            boolean answer = opponentCard.hasAttribute(questionKey);
            game.setCurrentTurnPlayerId(opponent.getPlayerId());

            return new QuestionResult(gameId, playerId, questionKey.toWireValue(), answer, game.getCurrentTurnPlayerId());
        });
    }

    public GuessResult guessCharacter(String gameId, String playerId, String guessedCharacterId) {
        return gameLockManager.executeSerialized(gameId, () -> {
            GameSession game = getActiveGame(gameId, playerId);
            enforceTurn(game, playerId);

            String opponentId = Objects.requireNonNull(game.opponentOf(playerId)).getPlayerId();
            String expected = game.getSecretByPlayer().get(opponentId);
            boolean correct = expected.equals(guessedCharacterId);
            if (correct) {
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
            game.setWinnerPlayerId(opponent.getPlayerId());
            game.setStatus(GameStatus.ABANDONED);
        });
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}



