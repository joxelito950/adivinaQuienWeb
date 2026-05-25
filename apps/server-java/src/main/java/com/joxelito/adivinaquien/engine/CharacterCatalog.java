package com.joxelito.adivinaquien.engine;

import com.joxelito.adivinaquien.domain.CharacterCard;
import com.joxelito.adivinaquien.domain.QuestionKey;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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
            Set<QuestionKey> attrs = EnumSet.noneOf(QuestionKey.class);
            for (int bit = 0; bit < keys.length; bit++) {
                // Genera atributos de forma determinista para mantener tests estables.
                if (((i * 13) >> bit & 1) == 1) {
                    attrs.add(keys[bit]);
                }
            }
            String imageUrl = "/characters/png/" + IMAGE_FILES.get(i - 1);
            list.add(new CharacterCard("char-" + i, "Character " + i, imageUrl, attrs));
        }
        return list;
    }
}

