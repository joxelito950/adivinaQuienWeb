package com.joxelito.adivinaquien.unit;

import com.joxelito.adivinaquien.domain.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DifficultyTest {

    @Test
    void boardSizesMatchSpec() {
        assertEquals(12, Difficulty.SMALL.boardSize());
        assertEquals(20, Difficulty.MEDIUM.boardSize());
        assertEquals(36, Difficulty.LARGE.boardSize());
    }
}

