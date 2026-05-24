package com.joxelito.adivinaquien.websocket;

public record ServerMessage(String type, String correlationId, Object payload) {
}

