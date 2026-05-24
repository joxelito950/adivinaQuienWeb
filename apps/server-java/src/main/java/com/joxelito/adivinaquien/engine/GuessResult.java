package com.joxelito.adivinaquien.engine;

public record GuessResult(
        String gameId,
        String playerId,
        String guessedCharacterId,
        boolean correct,
        String nextTurnPlayerId,
        String winnerPlayerId,
        boolean finished
) {
}

