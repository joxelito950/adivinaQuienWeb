package com.joxelito.adivinaquien.engine;

import com.joxelito.adivinaquien.domain.CharacterCard;
import com.joxelito.adivinaquien.domain.QuestionKey;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CharacterCatalog {

    private static final List<CharacterDefinition> DEFINITIONS = List.of(
            // Chicas
            female("chica-01.png", "Camila", QuestionKey.USES_GLASSES),
            female("chica-14.png", "Ines", QuestionKey.HAS_EARRINGS),
            female("chica-20.png", "Valeria", QuestionKey.HAS_EARRINGS),
            female("chica-24.png", "Sofia", QuestionKey.HAS_EARRINGS),
            female("chica-25.png", "Paula", QuestionKey.IS_BALD, QuestionKey.HAS_EARRINGS),
            female("chica-27.png", "Luna", QuestionKey.HAS_HAT, QuestionKey.HAS_EARRINGS),
            female("chica-30.png", "Marina", QuestionKey.HAS_HAT, QuestionKey.HAS_EARRINGS),
            female("chica-31.png", "Elena", QuestionKey.HAS_HAT, QuestionKey.HAS_EARRINGS),
            female("chica-32.png", "Daniela", QuestionKey.HAS_HAT),
            female("chica-33.png", "Natalia", QuestionKey.HAS_HAT),
            female("chica-34.png", "Alicia", QuestionKey.HAS_HAT),
            female("chica-35.png", "Laura", QuestionKey.HAS_HAT),
            female("chica-37.png", "Clara"),
            female("chica-38.png", "Marta"),

            // Chicos
            male("chico-02.png", "Juan", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-03.png", "Diego", QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("Chico-04.png", "Mateo", QuestionKey.USES_GLASSES),
            male("chico-05.png", "Nicolas", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-06.png", "Tomas", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-07.png", "Andres", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-08.png", "Bruno", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-09.png", "Lucas", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-10.png", "Martin", QuestionKey.HAS_HAT, QuestionKey.USES_GLASSES, QuestionKey.HAS_EARRINGS),
            male("chico-11.png", "Adrian", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-12.png", "Sergio", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-13.png", "Pablo", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-15.png", "Hugo", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-16.png", "Ruben", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-17.png", "Cesar", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-18.png", "Javier", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-21.png", "Guillermo", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-22.png", "Alvaro", QuestionKey.USES_GLASSES, QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-23.png", "Ivan", QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-25.png", "Raul", QuestionKey.HAS_BEARD, QuestionKey.HAS_EARRINGS),
            male("chico-28.png", "Leo", QuestionKey.IS_BALD, QuestionKey.USES_GLASSES),
            male("chico-29.png", "Gabriel"),
            male("chico-36.png", "Miguel", QuestionKey.IS_BALD)
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
            String imageUrl = "/characters/png/" + definition.imageFile();
            list.add(new CharacterCard("char-" + i, definition.displayName(), imageUrl, Set.copyOf(definition.attributes())));
        }
        return list;
    }

    private static void validateDefinitions() {
        if (DEFINITIONS.size() != 37) {
            throw new IllegalStateException("Character catalog must define exactly 37 characters");
        }

        Set<String> imageFiles = new HashSet<>();
        Set<String> displayNames = new HashSet<>();

        for (CharacterDefinition definition : DEFINITIONS) {
            if (!imageFiles.add(definition.imageFile())) {
                throw new IllegalStateException("Duplicated image file in character catalog: " + definition.imageFile());
            }
            if (!displayNames.add(definition.displayName())) {
                throw new IllegalStateException("Duplicated display name in character catalog: " + definition.displayName());
            }

            boolean male = definition.attributes().contains(QuestionKey.IS_MALE);
            boolean female = definition.attributes().contains(QuestionKey.IS_FEMALE);
            if (male == female) {
                throw new IllegalStateException("Each character must have exactly one gender: " + definition.imageFile());
            }

            String normalizedFile = definition.imageFile().toLowerCase();
            if (normalizedFile.startsWith("chica-") && !female) {
                throw new IllegalStateException("Female image must contain IS_FEMALE: " + definition.imageFile());
            }
            if (normalizedFile.startsWith("chico-") && !male) {
                throw new IllegalStateException("Male image must contain IS_MALE: " + definition.imageFile());
            }
        }
    }

    private static CharacterDefinition female(String imageFile, String displayName, QuestionKey... extra) {
        Set<QuestionKey> attributes = EnumSet.of(QuestionKey.IS_FEMALE);
        for (QuestionKey key : extra) {
            attributes.add(key);
        }
        return new CharacterDefinition(imageFile, displayName, attributes);
    }

    private static CharacterDefinition male(String imageFile, String displayName, QuestionKey... extra) {
        Set<QuestionKey> attributes = EnumSet.of(QuestionKey.IS_MALE);
        for (QuestionKey key : extra) {
            attributes.add(key);
        }
        return new CharacterDefinition(imageFile, displayName, attributes);
    }

    private record CharacterDefinition(String imageFile, String displayName, Set<QuestionKey> attributes) {
    }
}

