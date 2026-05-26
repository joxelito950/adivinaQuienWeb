package com.joxelito.adivinaquien.engine;

import com.joxelito.adivinaquien.domain.CharacterCard;
import com.joxelito.adivinaquien.domain.QuestionKey;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CharacterCatalog {

    private static final List<String> IMAGE_FILES = List.of(
            "chica-01.png",
            "chica-14.png",
            "chica-20.png",
            "chica-24.png",
            "chica-25.png",
            "chica-27.png",
            "chica-30.png",
            "chica-31.png",
            "chica-32.png",
            "chica-33.png",
            "chica-34.png",
            "chica-35.png",
            "chica-37.png",
            "chica-38.png",
            "chico-02.png",
            "chico-03.png",
            "Chico-04.png",
            "chico-05.png",
            "chico-06.png",
            "chico-07.png",
            "chico-08.png",
            "chico-09.png",
            "chico-10.png",
            "chico-11.png",
            "chico-12.png",
            "chico-13.png",
            "chico-15.png",
            "chico-16.png",
            "chico-17.png",
            "chico-18.png",
            "chico-21.png",
            "chico-22.png",
            "chico-23.png",
            "chico-25.png",
            "chico-28.png",
            "chico-29.png",
            "chico-36.png"
    );

            private static final List<String> DISPLAY_NAMES = List.of(
                "Camila",
                "Ines",
                "Valeria",
                "Sofia",
                "Paula",
                "Luna",
                "Marina",
                "Elena",
                "Daniela",
                "Natalia",
                "Alicia",
                "Laura",
                "Clara",
                "Marta",
                "Juan",
                "Diego",
                "Mateo",
                "Nicolas",
                "Tomas",
                "Andres",
                "Bruno",
                "Lucas",
                "Martin",
                "Adrian",
                "Sergio",
                "Pablo",
                "Hugo",
                "Ruben",
                "Cesar",
                "Javier",
                "Guillermo",
                "Alvaro",
                "Ivan",
                "Raul",
                "Leo",
                "Gabriel",
                "Miguel"
            );

            private static final Map<String, Set<QuestionKey>> ATTRIBUTE_OVERRIDES = Map.of(
                // Ajuste confirmado por UX: Chico 16 debe responder "si" a barba.
                "chico-16.png", EnumSet.of(QuestionKey.HAS_BEARD)
            );

    private final List<CharacterCard> characters;

    public CharacterCatalog() {
        this.characters = createCatalog();
    }

    public List<CharacterCard> allCharacters() {
        return List.copyOf(characters);
    }

    private List<CharacterCard> createCatalog() {
        List<CharacterCard> list = new ArrayList<>();
        QuestionKey[] keys = QuestionKey.values();
        for (int i = 1; i <= IMAGE_FILES.size(); i++) {
            String imageFile = IMAGE_FILES.get(i - 1);
            String displayName = DISPLAY_NAMES.get(i - 1);
            Set<QuestionKey> attrs = EnumSet.noneOf(QuestionKey.class);
            for (int bit = 0; bit < keys.length; bit++) {
                // Genera atributos de forma determinista para mantener tests estables.
                if (((i * 13) >> bit & 1) == 1) {
                    attrs.add(keys[bit]);
                }
            }

            // Normaliza rasgos mutuamente excluyentes para mantener consistencia del tablero.
            if (imageFile.toLowerCase().startsWith("chica-")) {
                attrs.remove(QuestionKey.IS_MALE);
                attrs.add(QuestionKey.IS_FEMALE);
            } else {
                attrs.remove(QuestionKey.IS_FEMALE);
                attrs.add(QuestionKey.IS_MALE);
            }

            if (i % 4 == 0) {
                attrs.remove(QuestionKey.HAS_FAIR_SKIN);
                attrs.add(QuestionKey.HAS_DARK_SKIN);
            } else {
                attrs.remove(QuestionKey.HAS_DARK_SKIN);
                attrs.add(QuestionKey.HAS_FAIR_SKIN);
            }

            if (i % 6 == 0) {
                attrs.add(QuestionKey.IS_BALD);
            } else {
                attrs.remove(QuestionKey.IS_BALD);
            }

            Set<QuestionKey> override = ATTRIBUTE_OVERRIDES.get(imageFile);
            if (override != null) {
                attrs.addAll(override);
            }
            String imageUrl = "/characters/png/" + imageFile;
            list.add(new CharacterCard("char-" + i, displayName, imageUrl, attrs));
        }
        return list;
    }
}

