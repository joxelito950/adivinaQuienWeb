package com.joxelito.adivinaquien.domain;

import java.util.Set;

public record CharacterCard(String characterId, String displayName, String imageUrl, Set<QuestionKey> attributes) {

    public boolean hasAttribute(QuestionKey key) {
        return attributes.contains(key);
    }
}

