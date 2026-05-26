package com.joxelito.adivinaquien.dummy;

import com.joxelito.adivinaquien.domain.Difficulty;
import com.joxelito.adivinaquien.domain.QuestionKey;
import com.joxelito.adivinaquien.engine.QuestionPolicy;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Service
public class DummyPlayerService {

    private final Random random = new SecureRandom();
    private final QuestionPolicy questionPolicy;

    public DummyPlayerService(QuestionPolicy questionPolicy) {
        this.questionPolicy = questionPolicy;
    }

    public QuestionKey chooseQuestion(Difficulty difficulty) {
        List<QuestionKey> all = questionPolicy.activeQuestions();
        int bias = switch (difficulty) {
            case SMALL -> 0;
            case MEDIUM -> 1;
            case LARGE -> 2;
        };
        int index = Math.floorMod(random.nextInt(all.size()) + bias, all.size());
        return all.get(index);
    }
}

