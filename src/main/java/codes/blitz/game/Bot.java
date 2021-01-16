package codes.blitz.game;

import java.util.ArrayList;
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

		// #x###########
		// ###o##########
		// ###o#########
		// ###o######o##
		// #############
		// #############

		List<Action> actions = myCrew.getUnits().stream()
				.map(unit -> {
					var unitPosition = unit.getPosition();
					var adjacentPositions = getAdjacentPositions(unitPosition);


					// if we have blitzium
					if (unit.getBlitzium() > 4) {
					    // if we are adjacent to a base:
						for (var adjPos: getAdjacentPositions(unitPosition)) {
							if (positionHasType(adjPos, TileType.BASE)) {
								return new UnitAction(UnitActionType.DROP, unit.getId(), adjPos);
							}
						}

						// else, move towards a depot
					    var myBase = gameMessage.getCrewsMapById().get(myCrew.getId()).getHomeBase();
					    var moveTo = getAdjacentPositions(myBase).get(0);
						return new UnitAction(UnitActionType.MOVE, unit.getId(), moveTo);
					}

					// else, check if we are by a mine; if so, mine!!!
					for (var pos: adjacentPositions) {
						if (positionHasType(pos, TileType.MINE)) {
							System.out.println("mine!");
							return new UnitAction(UnitActionType.MINE, unit.getId(), pos);
						}
					}

					// else, try to walk towards a mine
					System.out.println("walk towards mine");
					return new UnitAction(UnitActionType.MOVE, unit.getId(), walkTo);
				})
				.collect(Collectors.toList());

		return actions;

	}

	public List<Position> getAllPositions() {
	    var positions = new ArrayList<Position>();

		var mapSize = this.map.getMapSize();
		for (int x = 0; x < mapSize; x++) {
			for (int y = 0; y < mapSize; y++) {
				var position = new Position(x, y);



				try {
					map.validateTileExists(position);
					positions.add(position);
				} catch (PositionOutOfMapException e) {}
			}
		}

		return positions;
	}

	public boolean positionHasType(Position p, TileType t) {
		try {
			return map.getTileTypeAt(p) == t;
		} catch (PositionOutOfMapException e) {
			return false;
		}
	}

	// Return adjacent positions to `p`. Skips positions that are off the map.
	public List<Position> getAdjacentPositions(Position p) {
		var positions = new ArrayList<Position>();

		positions.add(new Position(p.getX() - 1, p.getY()));
		positions.add(new Position(p.getX() + 1, p.getY()));
		positions.add(new Position(p.getX(), p.getY() - 1));
		positions.add(new Position(p.getX(), p.getY() + 1));

		var reachablePositions = new ArrayList<Position>();
		for (var pos: positions) {
			try {
				map.validateTileExists(pos);
				reachablePositions.add(pos);

			} catch (PositionOutOfMapException e) {}
		}

		return reachablePositions;
	}

	public Position getWalkablePositionAround(Position p) {
	    var adjacentPositions = getAdjacentPositions(p);
	    for (var adjP: adjacentPositions) {
	    	try {
				if (map.getTileTypeAt(adjP) == TileType.EMPTY) {
					return adjP;
				}

			} catch (PositionOutOfMapException e) {}
		}
		return new Position(0, 0);// todo: find a better way of handling no walkable tiles;
	}

	public boolean canMine(Position p) {
		var adjacentPositions = getAdjacentPositions(p);
		for (var adjP: adjacentPositions) {
			try {
				if (map.getTileTypeAt(adjP) == TileType.MINE) {
					return true;
				}

			} catch (PositionOutOfMapException e) {}
		}
		return false;
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