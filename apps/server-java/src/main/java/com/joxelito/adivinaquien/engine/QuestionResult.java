package com.joxelito.adivinaquien.engine;

public record QuestionResult(
	String gameId,
	String playerId,
	String questionKey,
	boolean answer,
	String nextTurnPlayerId,
	boolean timeoutFallback
) {
}

