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
        assertEquals("Juan", cards.getFirst().displayName());
        assertTrue(cards.stream().noneMatch(card -> card.displayName().startsWith("Character ")));
        assertTrue(cards.stream().noneMatch(card -> card.displayName().toLowerCase().startsWith("chica ")));
        assertTrue(cards.stream().noneMatch(card -> card.displayName().toLowerCase().startsWith("chico ")));

        Set<String> ids = cards.stream().map(card -> card.characterId()).collect(java.util.stream.Collectors.toSet());
        assertEquals(37, ids.size());

        cards.forEach(card -> {
            assertNotNull(card.imageUrl());
            assertTrue(card.imageUrl().startsWith("/characters/png/"));
            assertTrue(card.imageUrl().endsWith(".png"));
            assertTrue(card.imageUrl().matches("/characters/png/char-\\d{2}\\.png"));
        });

        assertEquals("/characters/png/char-01.png", cards.getFirst().imageUrl());
        assertEquals("/characters/png/char-37.png", cards.get(36).imageUrl());

        var ruben = cards.stream()
            .filter(card -> "Ruben".equals(card.displayName()))
                .findFirst()
                .orElseThrow();
        assertTrue(ruben.attributes().contains(QuestionKey.HAS_BEARD));
        assertTrue(ruben.attributes().contains(QuestionKey.HAS_SHORT_HAIR));
        assertTrue(ruben.attributes().contains(QuestionKey.HAS_CURLY_HAIR));

        var camila = cards.stream()
            .filter(card -> "char-4".equals(card.characterId()))
                .findFirst()
                .orElseThrow();
        assertTrue(camila.attributes().contains(QuestionKey.IS_FEMALE));
        assertTrue(!camila.attributes().contains(QuestionKey.IS_MALE));
        assertTrue(!camila.attributes().contains(QuestionKey.USES_GLASSES));
        assertTrue(camila.attributes().contains(QuestionKey.HAS_LONG_HAIR));
        assertTrue(camila.attributes().contains(QuestionKey.HAS_STRAIGHT_HAIR));

        var luna = cards.stream()
                .filter(card -> "Luna".equals(card.displayName()))
                .findFirst()
                .orElseThrow();
        assertTrue(luna.attributes().contains(QuestionKey.HAS_HAT));
        assertTrue(luna.attributes().contains(QuestionKey.HAS_STRAIGHT_HAIR));

        var juan = cards.stream()
                .filter(card -> "Juan".equals(card.displayName()))
                .findFirst()
                .orElseThrow();
        assertTrue(juan.attributes().contains(QuestionKey.HAS_HAT));
        assertTrue(juan.attributes().contains(QuestionKey.USES_GLASSES));
        assertTrue(juan.attributes().contains(QuestionKey.HAS_BEARD));
        assertTrue(juan.attributes().contains(QuestionKey.HAS_SHORT_HAIR));

        var miguel = cards.stream()
                .filter(card -> "Miguel".equals(card.displayName()))
                .findFirst()
                .orElseThrow();
        assertTrue(miguel.attributes().contains(QuestionKey.IS_BALD));
        assertTrue(miguel.attributes().contains(QuestionKey.IS_MALE));
        assertTrue(miguel.attributes().contains(QuestionKey.USES_GLASSES));
        assertTrue(!miguel.attributes().contains(QuestionKey.HAS_SHORT_HAIR));

        var alicia = cards.stream()
            .filter(card -> "Alicia".equals(card.displayName()))
            .findFirst()
            .orElseThrow();
        assertTrue(alicia.attributes().contains(QuestionKey.IS_FEMALE));
        assertTrue(alicia.attributes().contains(QuestionKey.IS_BALD));
    }

    @Test
    void catalogMaintainsGenderAndHairIntegrity() {
        CharacterCatalog catalog = new CharacterCatalog();
        var cards = catalog.allCharacters();

        cards.forEach(card -> {
            boolean male = card.attributes().contains(QuestionKey.IS_MALE);
            boolean female = card.attributes().contains(QuestionKey.IS_FEMALE);
            boolean bald = card.attributes().contains(QuestionKey.IS_BALD);
            boolean longHair = card.attributes().contains(QuestionKey.HAS_LONG_HAIR);
            boolean shortHair = card.attributes().contains(QuestionKey.HAS_SHORT_HAIR);
            boolean straightHair = card.attributes().contains(QuestionKey.HAS_STRAIGHT_HAIR);
            boolean curlyHair = card.attributes().contains(QuestionKey.HAS_CURLY_HAIR);

            assertTrue(male ^ female, "Each card must contain exactly one gender attribute");

            if (bald) {
                assertTrue(!longHair && !shortHair && !straightHair && !curlyHair);
            } else {
                assertTrue(longHair ^ shortHair, "Non-bald characters must have one hair length");
                assertTrue(straightHair ^ curlyHair, "Non-bald characters must have one hair texture");
            }
        });
    }

    @Test
    void catalogAvoidsUncuratedQuestionKeysForNow() {
        CharacterCatalog catalog = new CharacterCatalog();
        var cards = catalog.allCharacters();

        List<QuestionKey> notCuratedYet = List.of(
                QuestionKey.HAS_FAIR_SKIN,
                QuestionKey.HAS_DARK_SKIN,
                QuestionKey.HAS_BLONDE_HAIR
        );

        notCuratedYet.forEach(key ->
                assertTrue(cards.stream().noneMatch(card -> card.attributes().contains(key)))
        );

        assertTrue(cards.stream().anyMatch(card -> card.attributes().contains(QuestionKey.HAS_LONG_HAIR)));
        assertTrue(cards.stream().anyMatch(card -> card.attributes().contains(QuestionKey.HAS_SHORT_HAIR)));
        assertTrue(cards.stream().anyMatch(card -> card.attributes().contains(QuestionKey.HAS_STRAIGHT_HAIR)));
        assertTrue(cards.stream().anyMatch(card -> card.attributes().contains(QuestionKey.HAS_CURLY_HAIR)));
    }
}
