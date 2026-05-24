package com.joxelito.adivinaquien.dummy;

import com.joxelito.adivinaquien.domain.Difficulty;
import com.joxelito.adivinaquien.domain.QuestionKey;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
public class DummyPlayerService {

    private final Random random = new SecureRandom();

    public QuestionKey chooseQuestion(Difficulty difficulty) {
        QuestionKey[] all = QuestionKey.values();
        int bias = switch (difficulty) {
            case SMALL -> 0;
            case MEDIUM -> 1;
            case LARGE -> 2;
        };
        int index = Math.floorMod(random.nextInt(all.length) + bias, all.length);
        return all[index];
    }
}

