package com.joxelito.adivinaquien.unit;

import com.joxelito.adivinaquien.domain.QuestionKey;
import com.joxelito.adivinaquien.engine.CharacterCatalog;
import com.joxelito.adivinaquien.engine.QuestionPolicy;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionPolicyTest {

    @Test
    void exposesOnlyQuestionKeysCoveredByTheCatalog() {
        QuestionPolicy policy = new QuestionPolicy(new CharacterCatalog());

        Set<QuestionKey> active = Set.copyOf(policy.activeQuestions());

        assertEquals(
                Set.of(
                        QuestionKey.USES_GLASSES,
                        QuestionKey.HAS_BEARD,
                        QuestionKey.HAS_HAT,
                        QuestionKey.HAS_LONG_HAIR,
                        QuestionKey.HAS_SHORT_HAIR,
                        QuestionKey.HAS_STRAIGHT_HAIR,
                        QuestionKey.HAS_CURLY_HAIR,
                        QuestionKey.HAS_EARRINGS,
                        QuestionKey.IS_MALE,
                        QuestionKey.IS_FEMALE,
                        QuestionKey.IS_BALD
                ),
                active
        );
        assertTrue(policy.isActive(QuestionKey.HAS_BEARD));
        assertTrue(policy.isActive(QuestionKey.IS_MALE));
        assertTrue(policy.isActive(QuestionKey.HAS_CURLY_HAIR));
        assertTrue(policy.isActive(QuestionKey.HAS_SHORT_HAIR));
    }
}
