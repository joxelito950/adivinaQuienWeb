package com.joxelito.adivinaquien.domain;

public enum QuestionKey {
    USES_GLASSES,
    HAS_BEARD,
    HAS_HAT,
    HAS_BLONDE_HAIR,
    HAS_LONG_HAIR,
    HAS_SHORT_HAIR,
    HAS_STRAIGHT_HAIR,
    HAS_CURLY_HAIR,
    HAS_EARRINGS,
    IS_MALE,
    IS_FEMALE,
    IS_BALD,
    HAS_FAIR_SKIN,
    HAS_DARK_SKIN;

    public static QuestionKey fromWireValue(String value) {
        return QuestionKey.valueOf(value.trim().toUpperCase());
    }

    public String toWireValue() {
        return name().toLowerCase();
    }
}

