package com.joxelito.adivinaquien.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class PlayerState {

    private final String playerId;
    private final PlayerType type;
    @Setter
    private String socketSessionId;
    @Setter
    private boolean connected;
    @Setter
    private Instant disconnectedAt;

    public PlayerState(String playerId, PlayerType type, String socketSessionId, boolean connected) {
        this.playerId = Objects.requireNonNull(playerId);
        this.type = Objects.requireNonNull(type);
        this.socketSessionId = socketSessionId;
        this.connected = connected;
    }

}

