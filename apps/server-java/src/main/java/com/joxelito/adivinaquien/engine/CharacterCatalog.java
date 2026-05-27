package com.joxelito.adivinaquien.engine;

import com.joxelito.adivinaquien.domain.CharacterCard;
import com.joxelito.adivinaquien.domain.QuestionKey;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CharacterCatalog {

    private static final List<CharacterDefinition> DEFINITIONS = List.of(
        // Catalogo curado manualmente: se explicitan atributos jugables por personaje.
        character(1, "Juan", QuestionKey.IS_MALE, QuestionKey.HAS_HAT, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(2, "Diego", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(3, "Andres", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(4, "Camila", QuestionKey.IS_FEMALE, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(5, "Paula", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(6, "Sofia", QuestionKey.IS_FEMALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(7, "Bruno", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(8, "Pablo", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(9, "Elena", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(10, "Marta", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(11, "Valeria", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(12, "Daniela", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(13, "Alicia", QuestionKey.IS_FEMALE, QuestionKey.IS_BALD, QuestionKey.HAS_EARRINGS),
        character(14, "Laura", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(15, "Luna", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(16, "Natalia", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(17, "Mateo", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(18, "Nicolas", QuestionKey.IS_MALE, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(19, "Ines", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(20, "Marina", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(21, "Cesar", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(22, "Javier", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(23, "Ruben", QuestionKey.IS_MALE, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(24, "Hugo", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(25, "Clara", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(26, "Lucia", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(27, "Gabriela", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(28, "Martin", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(29, "Adrian", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(30, "Sergio", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(31, "Tomas", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(32, "Ivan", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(33, "Raul", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character(34, "Guillermo", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(35, "Alvaro", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character(36, "Leo", QuestionKey.IS_MALE, QuestionKey.IS_BALD, QuestionKey.USES_GLASSES),
        character(37, "Miguel", QuestionKey.IS_MALE, QuestionKey.IS_BALD, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD)
    );

    private final List<CharacterCard> characters;

    public CharacterCatalog() {
        validateDefinitions();
        this.characters = createCatalog();
    }

    public List<CharacterCard> allCharacters() {
        return List.copyOf(characters);
    }

    private List<CharacterCard> createCatalog() {
        List<CharacterCard> list = new ArrayList<>();
        for (CharacterDefinition definition : DEFINITIONS) {
            int id = definition.id();
            String imageUrl = "/characters/png/char-" + String.format("%02d", id) + ".png";
            list.add(new CharacterCard("char-" + id, definition.displayName(), imageUrl, Set.copyOf(definition.attributes())));
        }
        return list;
    }

    private static void validateDefinitions() {
        if (DEFINITIONS.size() != 37) {
            throw new IllegalStateException("Character catalog must define exactly 37 characters");
        }

        Set<String> displayNames = new HashSet<>();
        Set<Integer> ids = new HashSet<>();

        for (CharacterDefinition definition : DEFINITIONS) {
            if (!ids.add(definition.id())) {
                throw new IllegalStateException("Duplicated character id in catalog: " + definition.id());
            }

            if (!displayNames.add(definition.displayName())) {
                throw new IllegalStateException("Duplicated display name in character catalog: " + definition.displayName());
            }

            boolean male = definition.attributes().contains(QuestionKey.IS_MALE);
            boolean female = definition.attributes().contains(QuestionKey.IS_FEMALE);
            if (male == female) {
                throw new IllegalStateException("Each character must have exactly one gender: " + definition.displayName());
            }

            boolean bald = definition.attributes().contains(QuestionKey.IS_BALD);
            boolean longHair = definition.attributes().contains(QuestionKey.HAS_LONG_HAIR);
            boolean shortHair = definition.attributes().contains(QuestionKey.HAS_SHORT_HAIR);
            boolean straightHair = definition.attributes().contains(QuestionKey.HAS_STRAIGHT_HAIR);
            boolean curlyHair = definition.attributes().contains(QuestionKey.HAS_CURLY_HAIR);

            if (bald) {
                if (longHair || shortHair || straightHair || curlyHair) {
                    throw new IllegalStateException("Bald character cannot include hair attributes: " + definition.displayName());
                }
                continue;
            }

            if (longHair == shortHair) {
                throw new IllegalStateException("Character must have exactly one hair length attribute: " + definition.displayName());
            }
            if (straightHair == curlyHair) {
                throw new IllegalStateException("Character must have exactly one hair texture attribute: " + definition.displayName());
            }
        }

        for (int expectedId = 1; expectedId <= 37; expectedId++) {
            if (!ids.contains(expectedId)) {
                throw new IllegalStateException("Character catalog must contain id: " + expectedId);
            }
        }
    }

    private static CharacterDefinition character(int id, String displayName, QuestionKey... attributes) {
        Set<QuestionKey> values = new HashSet<>();
        for (QuestionKey key : attributes) {
            values.add(key);
        }
        return new CharacterDefinition(id, displayName, values);
    }

    private record CharacterDefinition(int id, String displayName, Set<QuestionKey> attributes) {
    }
}

