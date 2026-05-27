package com.joxelito.adivinaquien.engine;

import com.joxelito.adivinaquien.domain.QuestionKey;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class QuestionPolicy {

    private final List<QuestionKey> activeQuestions;
    private final Set<QuestionKey> activeQuestionSet;

    public QuestionPolicy(CharacterCatalog characterCatalog) {
        List<QuestionKey> active = new ArrayList<>();
        for (QuestionKey key : QuestionKey.values()) {
            if (isCoveredByCatalog(characterCatalog, key)) {
                active.add(key);
            }
        }
        this.activeQuestions = List.copyOf(active);
        this.activeQuestionSet = Set.copyOf(activeQuestions);
    }

    public List<QuestionKey> activeQuestions() {
        return activeQuestions;
    }

    public boolean isActive(QuestionKey key) {
        return activeQuestionSet.contains(key);
    }

    private boolean isCoveredByCatalog(CharacterCatalog characterCatalog, QuestionKey key) {
        boolean seenTrue = false;
        boolean seenFalse = false;

        for (var card : characterCatalog.allCharacters()) {
            if (card.hasAttribute(key)) {
                seenTrue = true;
            } else {
                seenFalse = true;
            }

            if (seenTrue && seenFalse) {
                return true;
            }
        }

        return false;
    }
}