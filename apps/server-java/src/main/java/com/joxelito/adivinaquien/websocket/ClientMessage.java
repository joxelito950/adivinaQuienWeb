package com.joxelito.adivinaquien.websocket;

import tools.jackson.databind.JsonNode;

public record ClientMessage(String type, String correlationId, JsonNode payload) {
}

