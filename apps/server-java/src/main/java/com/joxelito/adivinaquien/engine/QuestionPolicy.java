package com.joxelito.adivinaquien.engine;

import com.joxelito.adivinaquien.domain.QuestionKey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class QuestionPolicy {

    // Fase 3: reactivar todas las preguntas del dominio.
    private static final List<QuestionKey> ACTIVE_QUESTIONS = List.of(QuestionKey.values());

    private static final Set<QuestionKey> ACTIVE_QUESTION_SET = Set.copyOf(ACTIVE_QUESTIONS);

    public List<QuestionKey> activeQuestions() {
        return ACTIVE_QUESTIONS;
    }

    public boolean isActive(QuestionKey key) {
        return ACTIVE_QUESTION_SET.contains(key);
    }
}