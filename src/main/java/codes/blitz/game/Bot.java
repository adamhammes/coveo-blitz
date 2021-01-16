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
	private Terrain terrain;
	private Unit unit;

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
					this.unit = unit;
					terrain = new Terrain(gameMessage, unit);

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
			if (terrain.neighbors(unit.getPosition()).contains(base)) {
				// drop
				return new UnitAction(UnitActionType.DROP, unit.getId(), base);
			} else {
			    return generateMoveAction(unit, base);
			}
		}


		// pick a random miner
		var miners = myCrew.getUnits().stream().filter(u -> u.getType() == UnitType.MINER).collect(Collectors.toList());

		if (miners.isEmpty()) {
			return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());
		}

		var theChosenOne = terrain.closestPosition(miners.stream().map(Unit::getPosition).collect(Collectors.toList()));

		if (terrain.isNeighboring(theChosenOne)) {
			return generateNoneAction();
		}
		return generateMoveAction(unit, theChosenOne);
	}

	private Action generateNoneAction() {
		return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());
	}

	public Action minerLogic(Unit unit) {
		var unitPosition = unit.getPosition();
		var adjacentPositions = terrain.neighbors(unitPosition);

		var cartCount = myCrew.getUnits().stream().filter(u -> u.getType() == UnitType.CART).count();
		// no carts yet -> we need to return our blitzium
		if (cartCount < 1 && unit.getBlitzium() > 4) {
			// if we are adjacent to a base, and have blitzium:
				for (var adjPos: terrain.neighbors(unitPosition)) {
					if (positionHasType(adjPos, TileType.BASE)) {
						return new UnitAction(UnitActionType.DROP, unit.getId(), adjPos);
					}
				}

			// else, move towards a depot
			return generateMoveAction(unit, base);
		}



		// if we have at least 25 blitzium and are by a cart, drop the blitzium to that cart
		var adjacentCarts = friendlyAdjacentUnitPositions(unit.getPosition(),UnitType.CART);
		if (unit.getBlitzium() >= 25 && !adjacentCarts.isEmpty()) {
			return new UnitAction(UnitActionType.DROP, unit.getId(), adjacentCarts.get(0));
		}


		// else, check if we are by a mine with < 25 blitzium; mine
        if (unit.getBlitzium() < 50) {
			for (var pos: adjacentPositions) {
				if (positionHasType(pos, TileType.MINE)) {
					return new UnitAction(UnitActionType.MINE, unit.getId(), pos);
				}
			}
		}

		var mine = terrain.closestPositionOfType(TileType.MINE);

        if (!canMine(unit.getPosition()) && unit.getBlitzium() < 25) {
        	return generateMoveAction(unit, mine);
		} else {
        	return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());
		}
	}

	public Action generateMoveAction(Unit u, Position p) {
	    if (u.getPosition().equals(p)) {
	    	return new UnitAction(UnitActionType.NONE, u.getId(), u.getPosition());
		}

		var path = terrain.pathTo(p);
		return new UnitAction(UnitActionType.MOVE, u.getId(), path.get(1));
	}

	public List<Position> adjacentToTileType(Position pos, TileType type) {
		return terrain.neighbors(pos).stream().filter(p -> positionHasType(p, type)).collect(Collectors.toList());
	}

	public List<Position> friendlyAdjacentUnitPositions(Position pos, UnitType type) {
		var neighboringPositions = new ArrayList<Position>();

		var adjacentPositions = terrain.neighbors(pos);
		for (var unit: myCrew.getUnits()) {
			if (unit.getType() == type && adjacentPositions.contains( unit.getPosition())) {
			    neighboringPositions.add(unit.getPosition());
			}
		}

		return neighboringPositions;
	}


	public boolean positionHasType(Position p, TileType t) {
		try {
			return map.getTileTypeAt(p) == t;
		} catch (PositionOutOfMapException e) {
			return false;
		}
	}


	public boolean canMine(Position p) {
		var adjacentPositions = terrain.neighbors(p);
		for (var adjP: adjacentPositions) {
			try {
				if (map.getTileTypeAt(adjP) == TileType.MINE) {
					return true;
				}

			} catch (PositionOutOfMapException e) {}
		}
		return false;
	}
}