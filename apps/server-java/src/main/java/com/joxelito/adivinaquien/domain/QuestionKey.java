package com.joxelito.adivinaquien.domain;

public enum QuestionKey {
    USES_GLASSES,
    HAS_BEARD,
    HAS_HAT,
    HAS_BLONDE_HAIR,
    HAS_BLUE_EYES,
    HAS_EARRINGS;

    public static QuestionKey fromWireValue(String value) {
        return QuestionKey.valueOf(value.trim().toUpperCase());
    }

    public String toWireValue() {
        return name().toLowerCase();
    }
}

