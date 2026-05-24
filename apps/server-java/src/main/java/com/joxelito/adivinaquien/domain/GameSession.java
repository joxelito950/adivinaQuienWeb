package com.joxelito.adivinaquien.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Getter
public class GameSession {

    private final String gameId;
    private final Difficulty difficulty;
    private final PlayerState playerOne;
    private final PlayerState playerTwo;
    private final Board board;
    private final Map<String, String> secretByPlayer;

    private GameStatus status;
    private String currentTurnPlayerId;
    private String winnerPlayerId;
    private Instant updatedAt;

    public GameSession(
            String gameId,
            Difficulty difficulty,
            PlayerState playerOne,
            PlayerState playerTwo,
            Board board,
            Map<String, String> secretByPlayer,
            String firstTurnPlayerId
    ) {
        this.gameId = Objects.requireNonNull(gameId);
        this.difficulty = Objects.requireNonNull(difficulty);
        this.playerOne = Objects.requireNonNull(playerOne);
        this.playerTwo = Objects.requireNonNull(playerTwo);
        this.board = Objects.requireNonNull(board);
        this.secretByPlayer = Objects.requireNonNull(secretByPlayer);
        this.currentTurnPlayerId = Objects.requireNonNull(firstTurnPlayerId);
        this.status = GameStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public void setStatus(GameStatus status) {
        this.status = status;
        touch();
    }

    public void setCurrentTurnPlayerId(String currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
        touch();
    }

    public void setWinnerPlayerId(String winnerPlayerId) {
        this.winnerPlayerId = winnerPlayerId;
        touch();
    }

    public boolean containsPlayer(String playerId) {
        return playerOne.getPlayerId().equals(playerId) || playerTwo.getPlayerId().equals(playerId);
    }

    public PlayerState playerById(String playerId) {
        if (playerOne.getPlayerId().equals(playerId)) {
            return playerOne;
        }
        if (playerTwo.getPlayerId().equals(playerId)) {
            return playerTwo;
        }
        return null;
    }

    public PlayerState opponentOf(String playerId) {
        if (playerOne.getPlayerId().equals(playerId)) {
            return playerTwo;
        }
        if (playerTwo.getPlayerId().equals(playerId)) {
            return playerOne;
        }
        return null;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}

