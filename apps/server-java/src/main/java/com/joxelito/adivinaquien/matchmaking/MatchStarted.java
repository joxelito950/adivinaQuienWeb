package com.joxelito.adivinaquien.matchmaking;

import com.joxelito.adivinaquien.domain.Difficulty;

public record MatchStarted(Difficulty difficulty, MatchParticipant first, MatchParticipant second) {
}

