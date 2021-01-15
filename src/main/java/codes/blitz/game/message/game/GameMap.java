package codes.blitz.game.message.game;

import codes.blitz.game.message.exception.PositionOutOfMapException;

public class GameMap {
	private String[][] tiles;
	private Depot[] depots;

	public int getMapSize() {
		return this.tiles.length;
	}

	public TileType getTileTypeAt(Position position)
			throws PositionOutOfMapException {
		String rawTile = this.getRawTileValueAt(position);
		return TileType.getTileTypeFromString(rawTile);
	}

	public String getRawTileValueAt(Position position)
			throws PositionOutOfMapException {
		this.validateTileExists(position);
		return this.tiles[position.getX()][position.getY()];
	}

	public Depot[] getDepots() {
		return depots;
	}

	public void validateTileExists(Position position)
			throws PositionOutOfMapException {
		if (position.getX() < 0 || position.getY() < 0
				|| position.getX() >= this.getMapSize()
				|| position.getY() >= this.getMapSize()) {
			throw new PositionOutOfMapException(position, this.getMapSize());
		}
	}
}