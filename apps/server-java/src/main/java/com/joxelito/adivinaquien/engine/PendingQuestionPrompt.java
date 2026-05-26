package com.joxelito.adivinaquien.engine;

public record PendingQuestionPrompt(
        String gameId,
        String askerPlayerId,
        String defenderPlayerId,
        String questionKey,
        long timeoutSeconds
) {
}
