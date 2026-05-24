package com.joxelito.adivinaquien.engine;

import com.joxelito.adivinaquien.domain.Board;
import com.joxelito.adivinaquien.domain.CharacterCard;
import com.joxelito.adivinaquien.domain.Difficulty;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
public class BoardFactory {

    private final CharacterCatalog characterCatalog;
    private final Random random = new SecureRandom();

    public BoardFactory(CharacterCatalog characterCatalog) {
        this.characterCatalog = characterCatalog;
    }

    public Board createBoard(Difficulty difficulty) {
        List<CharacterCard> pool = new ArrayList<>(characterCatalog.allCharacters());
        Collections.shuffle(pool, random);
        int size = difficulty.boardSize();
        if (pool.size() < size) {
            throw new IllegalStateException("Catalog does not have enough characters for difficulty " + difficulty);
        }
        List<CharacterCard> boardCards = List.copyOf(pool.subList(0, size));
        return new Board(difficulty.getRows(), difficulty.getCols(), boardCards);
    }
}

