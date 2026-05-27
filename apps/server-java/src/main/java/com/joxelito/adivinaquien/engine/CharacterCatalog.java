package com.joxelito.adivinaquien.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.joxelito.adivinaquien.domain.CharacterCard;
import com.joxelito.adivinaquien.domain.QuestionKey;

@Component
public class CharacterCatalog {

    private static final List<CharacterDefinition> DEFINITIONS = List.of(
            // Catalogo curado manualmente: se explicitan atributos jugables por personaje.
            character(
                    1,
                    "Juan",
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_HAT,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    2,
                    "Maria",
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_FAIR_SKIN),
            character(3,
                    "Andres",
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_LONG_HAIR,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_CURLY_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    4,
                    "Camila",
                    QuestionKey.HAS_BLONDE_HAIR,
                    QuestionKey.HAS_LONG_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    5,
                    "Paula",
                    QuestionKey.HAS_HAT,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_CURLY_HAIR,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    6,
                    "Sofia",
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_LONG_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    7,
                    "Bruno",
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    8,
                    "Pablo",
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_CURLY_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    9,
                    "Elena",
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    10,
                    "Marta",
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    11,
                    "Valeria",
                    QuestionKey.HAS_LONG_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    12,
                    "Daniela",
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    13,
                    "Alicia",
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.IS_BALD,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    14,
                    "Laura",
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_HAT,
                    QuestionKey.HAS_LONG_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR),
            character(
                    15,
                    "Luna",
                    QuestionKey.HAS_HAT,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    16,
                    "Natalia",
                    QuestionKey.HAS_HAT,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    17,
                    "Mateo",
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    18,
                    "Nicolas",
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_LONG_HAIR,
                    QuestionKey.HAS_CURLY_HAIR),
            character(
                    19,
                    "Santiago",
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    20,
                    "Mariano",
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_FAIR_SKIN,
                    QuestionKey.HAS_EARRINGS),
            character(
                    21,
                    "Carolina",
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_HAT,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    22,
                    "Javier",
                    QuestionKey.IS_MALE,
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    23,
                    "Monica",
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_FAIR_SKIN,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.IS_BALD,
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    24,
                    "Hugo",
                    QuestionKey.IS_MALE,
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_CURLY_HAIR,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    25,
                    "Clara",
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_HAT,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    26,
                    "Lucia",
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_FAIR_SKIN),
            character(27,
                    "Gabriel",
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    28,
                    "Gabriela",
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    29,
                    "Adrian",
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    30,
                    "Sergio",
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    31,
                    "Tomas",
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_CURLY_HAIR,
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    32,
                    "Ivan",
                    QuestionKey.USES_GLASSES,
                    QuestionKey.IS_MALE,
                    QuestionKey.IS_BALD,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    33,
                    "Raul",
                    QuestionKey.IS_MALE,
                    QuestionKey.IS_BALD,
                    QuestionKey.HAS_DARK_SKIN),
            character(
                    34,
                    "Guillermo",
                    QuestionKey.IS_MALE,
                    QuestionKey.USES_GLASSES,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    35,
                    "Alvaro",
                    QuestionKey.IS_MALE,
                    QuestionKey.HAS_BEARD,
                    QuestionKey.HAS_EARRINGS,
                    QuestionKey.HAS_SHORT_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    36,
                    "Leo",
                    QuestionKey.IS_MALE,
                    QuestionKey.IS_BALD,
                    QuestionKey.HAS_FAIR_SKIN),
            character(
                    37,
                    "Marisol",
                    QuestionKey.HAS_LONG_HAIR,
                    QuestionKey.HAS_STRAIGHT_HAIR,
                    QuestionKey.IS_FEMALE,
                    QuestionKey.HAS_FAIR_SKIN));

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
            list.add(new CharacterCard("char-" + id, definition.displayName(), imageUrl,
                    Set.copyOf(definition.attributes())));
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
                throw new IllegalStateException(
                        "Duplicated display name in character catalog: " + definition.displayName());
            }

            boolean male = definition.attributes().contains(QuestionKey.IS_MALE);
            boolean female = definition.attributes().contains(QuestionKey.IS_FEMALE);
            if (male == female) {
                throw new IllegalStateException(
                        "Each character must have exactly one gender: " + definition.displayName());
            }

            boolean bald = definition.attributes().contains(QuestionKey.IS_BALD);
            boolean longHair = definition.attributes().contains(QuestionKey.HAS_LONG_HAIR);
            boolean shortHair = definition.attributes().contains(QuestionKey.HAS_SHORT_HAIR);
            boolean straightHair = definition.attributes().contains(QuestionKey.HAS_STRAIGHT_HAIR);
            boolean curlyHair = definition.attributes().contains(QuestionKey.HAS_CURLY_HAIR);

            if (bald) {
                if (longHair || shortHair || straightHair || curlyHair) {
                    throw new IllegalStateException(
                            "Bald character cannot include hair attributes: " + definition.displayName());
                }
                continue;
            }

            if (longHair == shortHair) {
                throw new IllegalStateException(
                        "Character must have exactly one hair length attribute: " + definition.displayName());
            }
            if (straightHair == curlyHair) {
                throw new IllegalStateException(
                        "Character must have exactly one hair texture attribute: " + definition.displayName());
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
