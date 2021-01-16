package codes.blitz.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import codes.blitz.game.message.exception.PositionOutOfMapException;
import codes.blitz.game.message.game.*;

public class Bot {
    private GameMessage gameMessage;
    private Crew myCrew;
	private GameMap map;
	private Position base;

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
		this.gameMessage = gameMessage;
		this.myCrew = gameMessage.getCrewsMapById().get(gameMessage.getCrewId());
		this.map = gameMessage.getGameMap();
		this.base = myCrew.getHomeBase();


		// #x###########
		// ###o##########
		// ###o#########
		// ###o######o##
		// #############
		// #############

		List<Action> actions = myCrew.getUnits().stream()
				.map(unit -> {
				    if (unit.getType() == UnitType.MINER) {
						return minerLogic(unit);
					} else if (unit.getType() == UnitType.CART) {
				    	return cartLogic(unit);
					} else {
				        return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());
					}
				})
				.collect(Collectors.toList());

		var numCarts = myCrew.getUnits().stream().filter(unit -> unit.getType() == UnitType.CART).count();
		var cartCost = myCrew.getPrices().getMinerPrice();

		System.out.println("num carts " + numCarts);
		System.out.println("cart cost" + cartCost);
		System.out.println("my blitzium" + myCrew.getBlitzium());

		if (numCarts < 1 && cartCost <= myCrew.getBlitzium()) {
			var createMiner = new BuyAction(UnitType.CART);
			actions.add(createMiner);
		}

		return actions;

	}

	public Action cartLogic(Unit unit) {
		// if we have blitzium
	    if (unit.getBlitzium() > 24) {
	        // and are next to a base
			if (getAdjacentPositions(unit.getPosition()).contains(base)) {
				// drop
				return new UnitAction(UnitActionType.DROP, unit.getId(), base);
			} else {
				var moveTo = getAdjacentPositions(base).get(0);
				return new UnitAction(UnitActionType.MOVE, unit.getId(), moveTo);
			}
		}

		// pick a random miner
		var miners = myCrew.getUnits().stream().filter(u -> u.getType() == UnitType.MINER).collect(Collectors.toList());

		if (miners.isEmpty()) {
			return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());

		}


		var theChosenOne = miners.get(0);

		var moveTo = getAdjacentPositions(theChosenOne.getPosition()).get(0);
		return new UnitAction(UnitActionType.MOVE, unit.getId(), moveTo);
	}

	public Action minerLogic(Unit unit) {
		var unitPosition = unit.getPosition();
		var adjacentPositions = getAdjacentPositions(unitPosition);

		var cartCount = myCrew.getUnits().stream().filter(u -> u.getType() == UnitType.CART).count();
		// no carts yet -> we need to return our blitzium
		if (cartCount < 1 && unit.getBlitzium() > 4) {
			// if we are adjacent to a base, and have blitzium:
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


		var mine = getAllPositions().stream().filter(pos -> positionHasType(pos, TileType.MINE)).collect(Collectors.toList()).get(0);
		final var walkTo = getAdjacentPositions(mine).get(0);

		// if we have at least 25 blitzium and are by a cart, drop the blitzium to that cart
		var adjacentCarts = friendlyAdjacentUnitPositions(unit.getPosition(),UnitType.CART);
		if (unit.getBlitzium() >= 25 && !adjacentCarts.isEmpty()) {
			return new UnitAction(UnitActionType.DROP, unit.getId(), adjacentCarts.get(0));
		}


		// else, check if we are by a mine with < 25 blitzium; mine
        if (unit.getBlitzium() < 50) {
			for (var pos: adjacentPositions) {
				if (positionHasType(pos, TileType.MINE)) {
					System.out.println("mine!");
					return new UnitAction(UnitActionType.MINE, unit.getId(), pos);
				}
			}
		}

        if (!canMine(unit.getPosition()) && unit.getBlitzium() < 25) {
			// else, try to walk towards a mine
			System.out.println("walk towards mine");
			return new UnitAction(UnitActionType.MOVE, unit.getId(), walkTo);

		} else {
        	return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());
		}
	}

	public List<Position> adjacentToTileType(Position pos, TileType type) {
		return getAdjacentPositions(pos).stream().filter(p -> positionHasType(p, type)).collect(Collectors.toList());
	}

	public List<Position> friendlyAdjacentUnitPositions(Position pos, UnitType type) {
		var neighboringPositions = new ArrayList<Position>();

		var adjacentPositions = getAdjacentPositions(pos);
		for (var unit: myCrew.getUnits()) {
			if (unit.getType() == type && adjacentPositions.contains( unit.getPosition())) {
			    neighboringPositions.add(unit.getPosition());
			}
		}

		return neighboringPositions;
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