package com.joxelito.adivinaquien.unit;

import com.joxelito.adivinaquien.domain.QuestionKey;
import com.joxelito.adivinaquien.engine.QuestionPolicy;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionPolicyTest {

    @Test
    void exposesAllQuestionKeysInPhaseThree() {
        QuestionPolicy policy = new QuestionPolicy();

        Set<QuestionKey> active = Set.copyOf(policy.activeQuestions());
        Set<QuestionKey> all = Set.of(QuestionKey.values());

        assertEquals(all.size(), active.size());
        assertTrue(active.contains(QuestionKey.USES_GLASSES));
        assertTrue(active.contains(QuestionKey.HAS_BEARD));
        assertTrue(active.contains(QuestionKey.HAS_HAT));
        assertTrue(active.contains(QuestionKey.HAS_BLONDE_HAIR));
        assertTrue(active.contains(QuestionKey.HAS_BLUE_EYES));
        assertTrue(active.contains(QuestionKey.HAS_EARRINGS));
        assertTrue(active.contains(QuestionKey.IS_MALE));
        assertTrue(active.contains(QuestionKey.IS_FEMALE));
        assertTrue(active.contains(QuestionKey.IS_BALD));
        assertTrue(active.contains(QuestionKey.HAS_FAIR_SKIN));
        assertTrue(active.contains(QuestionKey.HAS_DARK_SKIN));
        assertEquals(all, active);
    }
}
