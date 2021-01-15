package codes.blitz.game;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import codes.blitz.game.message.exception.PositionOutOfMapException;
import codes.blitz.game.message.game.*;

public class Bot {
	private GameMap map;
	public Bot() {
		// initialize some variables you will need throughout the game here
	}

    /*
    * Here is where the magic happens, for now the moves are random. I bet you can do better ;)
    *
    * No path finding is required, you can simply send a destination per unit and the game will move your unit towards
    * it in the next turns.
    */
	public List<Action> getNextActions(GameMessage gameMessage) {

		Crew myCrew = gameMessage.getCrewsMapById()
				.get(gameMessage.getCrewId());

		var map = gameMessage.getGameMap();
		this.map = map;

		var mine = this.getMinePosition();
		final var walkTo = this.getWalkablePositionAround(mine);


		List<Action> actions = myCrew.getUnits().stream()
				.map(unit -> new UnitAction(UnitActionType.MOVE, unit.getId(),
						walkTo))
				.collect(Collectors.toList());

		return actions;

	}

	public Position getWalkablePositionAround(Position p) {
		var dx = new int[] { -1, 1};
		var dy = new int[] { -1, 1};

		for (var x: dx) {
			for (var y: dy) {
				var new_x = p.getX() + x;
				var new_y = p.getY() + y;

				var position = new Position(new_x, new_y);

				try {
					if (map.getTileTypeAt(position) == TileType.EMPTY) {
						return position;
					}

				} catch (PositionOutOfMapException e) {}
			}
		}

		return new Position(0, 0);// todo: find a better way of handling no walkable tiles;
	}

	public Position getMinePosition() {
		var mapSize = this.map.getMapSize();
		var minePosition = new Position(0, 0);
		for (int x = 0; x < mapSize; x++) {
			for (int y = 0; y < mapSize; y++) {
				var position = new Position(x, y);

				try {
					var tileType = map.getTileTypeAt(position);

					if (tileType == TileType.MINE) {
						minePosition = position;
					}

				} catch (PositionOutOfMapException e) {}
			}
		}

		return minePosition;
	}
}