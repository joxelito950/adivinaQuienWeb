package com.joxelito.adivinaquien.unit;

import com.joxelito.adivinaquien.engine.CharacterCatalog;
import com.joxelito.adivinaquien.domain.QuestionKey;
import org.junit.jupiter.api.Test;

import java.util.List;
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
        assertEquals("char-1", cards.getFirst().characterId());
        assertEquals("char-37", cards.get(36).characterId());
        assertEquals("Camila", cards.getFirst().displayName());
        assertTrue(cards.stream().noneMatch(card -> card.displayName().startsWith("Character ")));
        assertTrue(cards.stream().noneMatch(card -> card.displayName().toLowerCase().startsWith("chica ")));
        assertTrue(cards.stream().noneMatch(card -> card.displayName().toLowerCase().startsWith("chico ")));

        Set<String> ids = cards.stream().map(card -> card.characterId()).collect(java.util.stream.Collectors.toSet());
        assertEquals(37, ids.size());

        cards.forEach(card -> {
            assertNotNull(card.imageUrl());
            assertTrue(card.imageUrl().startsWith("/characters/png/"));
            assertTrue(card.imageUrl().endsWith(".png"));
        });

        var chico16 = cards.stream()
            .filter(card -> "/characters/png/chico-16.png".equals(card.imageUrl()))
                .findFirst()
                .orElseThrow();
        assertTrue(chico16.attributes().contains(QuestionKey.HAS_BEARD));

        var chica01 = cards.stream()
                .filter(card -> "/characters/png/chica-01.png".equals(card.imageUrl()))
                .findFirst()
                .orElseThrow();
        assertTrue(chica01.attributes().contains(QuestionKey.USES_GLASSES));
        assertTrue(chica01.attributes().contains(QuestionKey.IS_FEMALE));

        var chica27 = cards.stream()
                .filter(card -> "/characters/png/chica-27.png".equals(card.imageUrl()))
                .findFirst()
                .orElseThrow();
        assertTrue(chica27.attributes().contains(QuestionKey.HAS_HAT));

        var chico02 = cards.stream()
                .filter(card -> "/characters/png/chico-02.png".equals(card.imageUrl()))
                .findFirst()
                .orElseThrow();
        assertTrue(chico02.attributes().contains(QuestionKey.USES_GLASSES));
        assertTrue(chico02.attributes().contains(QuestionKey.HAS_BEARD));

        var chico36 = cards.stream()
                .filter(card -> "/characters/png/chico-36.png".equals(card.imageUrl()))
                .findFirst()
                .orElseThrow();
        assertTrue(chico36.attributes().contains(QuestionKey.IS_BALD));
    }

    @Test
    void catalogMaintainsGenderIntegrityByImagePrefix() {
        CharacterCatalog catalog = new CharacterCatalog();
        var cards = catalog.allCharacters();

        cards.forEach(card -> {
            boolean male = card.attributes().contains(QuestionKey.IS_MALE);
            boolean female = card.attributes().contains(QuestionKey.IS_FEMALE);

            assertTrue(male ^ female, "Each card must contain exactly one gender attribute");

            String image = card.imageUrl().toLowerCase();
            if (image.contains("/chica-")) {
                assertTrue(female);
            }
            if (image.contains("/chico-")) {
                assertTrue(male);
            }
        });
    }

    @Test
    void catalogAvoidsUncuratedQuestionKeysForNow() {
        CharacterCatalog catalog = new CharacterCatalog();
        var cards = catalog.allCharacters();

        List<QuestionKey> notCuratedYet = List.of(
                QuestionKey.HAS_BLUE_EYES,
                QuestionKey.HAS_FAIR_SKIN,
                QuestionKey.HAS_DARK_SKIN,
                QuestionKey.HAS_BLONDE_HAIR
        );

        notCuratedYet.forEach(key ->
                assertTrue(cards.stream().noneMatch(card -> card.attributes().contains(key)))
        );
    }
}
