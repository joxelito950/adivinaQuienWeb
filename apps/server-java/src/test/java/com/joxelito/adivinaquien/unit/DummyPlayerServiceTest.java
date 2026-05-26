package com.joxelito.adivinaquien.unit;

import com.joxelito.adivinaquien.domain.Difficulty;
import com.joxelito.adivinaquien.domain.QuestionKey;
import com.joxelito.adivinaquien.dummy.DummyPlayerService;
import com.joxelito.adivinaquien.engine.QuestionPolicy;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyPlayerServiceTest {

    @Test
    void choosesOnlyActiveQuestions() {
        QuestionPolicy questionPolicy = new QuestionPolicy();
        DummyPlayerService service = new DummyPlayerService(questionPolicy);

        Set<QuestionKey> active = Set.copyOf(questionPolicy.activeQuestions());

        for (int i = 0; i < 200; i++) {
            assertTrue(active.contains(service.chooseQuestion(Difficulty.SMALL)));
            assertTrue(active.contains(service.chooseQuestion(Difficulty.MEDIUM)));
            assertTrue(active.contains(service.chooseQuestion(Difficulty.LARGE)));
        }
    }
}
