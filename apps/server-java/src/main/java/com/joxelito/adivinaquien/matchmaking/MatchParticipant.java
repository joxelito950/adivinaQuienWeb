package com.joxelito.adivinaquien.matchmaking;

import com.joxelito.adivinaquien.domain.PlayerType;

public record MatchParticipant(String playerId, String socketSessionId, PlayerType type) {
}

