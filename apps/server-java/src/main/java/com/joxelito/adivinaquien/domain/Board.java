package com.joxelito.adivinaquien.domain;

import java.util.List;

public record Board(int rows, int cols, List<CharacterCard> characters) {
}

