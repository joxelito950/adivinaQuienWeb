package com.joxelito.adivinaquien.domain;

import lombok.Getter;

@Getter
public enum Difficulty {
	SMALL(3, 4),
	MEDIUM(4, 5),
	LARGE(6, 6);

	private final int rows;
	private final int cols;

	Difficulty(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
	}

    public int boardSize() {
		return rows * cols;
	}

	public static Difficulty fromWireValue(String value) {
		return Difficulty.valueOf(value.trim().toUpperCase());
	}

	public String toWireValue() {
		return name().toLowerCase();
	}
}

