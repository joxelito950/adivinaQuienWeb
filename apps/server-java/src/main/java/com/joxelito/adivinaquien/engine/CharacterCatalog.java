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
        character("Juan", QuestionKey.IS_MALE, QuestionKey.HAS_HAT, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Diego", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Andres", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Camila", QuestionKey.IS_FEMALE, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Paula", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Sofia", QuestionKey.IS_FEMALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Bruno", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Pablo", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Elena", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Marta", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Valeria", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Daniela", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Alicia", QuestionKey.IS_FEMALE, QuestionKey.IS_BALD, QuestionKey.HAS_EARRINGS),
        character("Laura", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Luna", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Natalia", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Mateo", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Nicolas", QuestionKey.IS_MALE, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Ines", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Marina", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Cesar", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Javier", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Ruben", QuestionKey.IS_MALE, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Hugo", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Clara", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Lucia", QuestionKey.IS_FEMALE, QuestionKey.HAS_HAT, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Gabriela", QuestionKey.IS_FEMALE, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_LONG_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Martin", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Adrian", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Sergio", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Tomas", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Ivan", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Raul", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_CURLY_HAIR),
        character("Guillermo", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Alvaro", QuestionKey.IS_MALE, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS, QuestionKey.HAS_SHORT_HAIR, QuestionKey.HAS_STRAIGHT_HAIR),
        character("Leo", QuestionKey.IS_MALE, QuestionKey.IS_BALD, QuestionKey.USES_GLASSES),
        character("Miguel", QuestionKey.IS_MALE, QuestionKey.IS_BALD, QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD)
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
        for (int i = 1; i <= DEFINITIONS.size(); i++) {
            CharacterDefinition definition = DEFINITIONS.get(i - 1);
            String imageUrl = "/characters/png/char-" + String.format("%02d", i) + ".png";
            list.add(new CharacterCard("char-" + i, definition.displayName(), imageUrl, Set.copyOf(definition.attributes())));
        }
        return list;
    }

    private static void validateDefinitions() {
        if (DEFINITIONS.size() != 37) {
            throw new IllegalStateException("Character catalog must define exactly 37 characters");
        }

        Set<String> displayNames = new HashSet<>();

        for (CharacterDefinition definition : DEFINITIONS) {
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
    }

    private static CharacterDefinition character(String displayName, QuestionKey... attributes) {
        Set<QuestionKey> values = new HashSet<>();
        for (QuestionKey key : attributes) {
            values.add(key);
        }
        return new CharacterDefinition(displayName, values);
    }

    private record CharacterDefinition(String displayName, Set<QuestionKey> attributes) {
    }
}

