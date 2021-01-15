/**
 * Copyright (c) 2011 - 2019, Coveo Solutions Inc.
 */
package codes.blitz.game.message.game;

public enum TileType {
	EMPTY, WALL, BASE, MINE;

	public static TileType getTileTypeFromString(String rawTile) {
		return TileType.valueOf(rawTile);
	}
}
