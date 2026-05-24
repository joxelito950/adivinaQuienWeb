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
        for (int i = 1; i <= 48; i++) {
            Set<QuestionKey> attrs = EnumSet.noneOf(QuestionKey.class);
            for (int bit = 0; bit < keys.length; bit++) {
                // Genera atributos de forma determinista para mantener tests estables.
                if (((i * 13) >> bit & 1) == 1) {
                    attrs.add(keys[bit]);
                }
            }
            list.add(new CharacterCard("char-" + i, "Character " + i, attrs));
        }
        return list;
    }
}

