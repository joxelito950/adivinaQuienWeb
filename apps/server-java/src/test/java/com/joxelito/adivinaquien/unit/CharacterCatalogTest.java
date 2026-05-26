package com.joxelito.adivinaquien.unit;

import com.joxelito.adivinaquien.engine.CharacterCatalog;
import com.joxelito.adivinaquien.domain.QuestionKey;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharacterCatalogTest {

    @Test
    void catalogHasOfficial37CharactersWithImageUrls() {
        CharacterCatalog catalog = new CharacterCatalog();
        var cards = catalog.allCharacters();

        assertEquals(37, cards.size());
        assertEquals("char-1", cards.get(0).characterId());
        assertEquals("char-37", cards.get(36).characterId());
        assertEquals("Chica 01", cards.get(0).displayName());
        assertTrue(cards.stream().noneMatch(card -> card.displayName().startsWith("Character ")));

        Set<String> ids = cards.stream().map(card -> card.characterId()).collect(java.util.stream.Collectors.toSet());
        assertEquals(37, ids.size());

        cards.forEach(card -> {
            assertNotNull(card.imageUrl());
            assertTrue(card.imageUrl().startsWith("/characters/png/"));
            assertTrue(card.imageUrl().endsWith(".png"));
        });

        var chico16 = cards.stream()
                .filter(card -> "chico 16".equalsIgnoreCase(card.displayName()))
                .findFirst()
                .orElseThrow();
        assertTrue(chico16.attributes().contains(QuestionKey.HAS_BEARD));
    }
}
