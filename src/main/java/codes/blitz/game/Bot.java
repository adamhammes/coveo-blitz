package codes.blitz.game;

import java.util.*;
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
	private Set<Position> requestedMiningLocations;
	private Map<UnitType, Integer> unitTypeCounts;
	private List<Unit> surplusMiners;
	private Map<Unit, Unit> cartAssignations;


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
		this.requestedMiningLocations = new HashSet<>();

		this.unitTypeCounts = new HashMap<>();
		for (var unit: myCrew.getUnits()) {
			if (unitTypeCounts.containsKey(unit.getType())) {
				unitTypeCounts.put(unit.getType(), unitTypeCounts.get(unit.getType()) + 1);
			} else {
				unitTypeCounts.put(unit.getType(), 1);
			}
		}

		var minerSurplus = unitTypeCounts.getOrDefault(UnitType.MINER, 0) - unitTypeCounts.getOrDefault(UnitType.CART, 0);
		surplusMiners = myCrew.getUnits().stream().filter(u -> u.getType() == UnitType.MINER).collect(Collectors.toList());
		Collections.reverse(surplusMiners);
		surplusMiners = surplusMiners.stream().limit(minerSurplus).collect(Collectors.toList());

		this.assignCarts();

		// #x###########
		// ###o##########
		// ###o#########
		// ###o######o##
		// #############
		// #############
		Map<Unit, Position> desiredCartDestinations = new HashMap<>();

		List<Action> actions = myCrew.getUnits().stream()
				.sorted(Comparator.comparing(Unit::getId))
				.map(unit -> {
					this.unit = unit;
					terrain = new Terrain(gameMessage, unit);

				    if (unit.getType() == UnitType.MINER) {
						var minerAction = minerLogic(unit);
						if (minerAction.getAction() == UnitActionType.MOVE) {
							desiredCartDestinations.put(unit, minerAction.getTarget());
							return null;
						} else {
							return minerAction;
						}
					} else if (unit.getType() == UnitType.CART) {
				    	var cartAction = cartLogic(unit);
				    	if (cartAction.getAction() == UnitActionType.MOVE) {
				    		desiredCartDestinations.put(unit, cartAction.getTarget());
				    		return null;
						} else {
				    		return cartAction;
						}
					} else {
				        return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		var cartActions = coordinateCarts(desiredCartDestinations);
		actions.addAll(cartActions);

		var buyAction = buyLogic();
		if (buyAction != null) {
			actions.add(buyAction);
		}

		return actions;
	}

	public List<UnitAction> coordinateCarts(Map<Unit, Position> cartDestinations) {
	    Set<Position> restrictedPositions = new HashSet<>();
	    List<UnitAction> cartMoves = new ArrayList<>();

	    // TODO: sort the carts by priority
	    for (var entry: cartDestinations.entrySet()) {
	    	var cart = entry.getKey();
	    	var destination = entry.getValue();

	    	var path = terrain.pathTo(cart.getPosition(), destination, restrictedPositions);
	    	if (path != null) {
	    		var action = new UnitAction(UnitActionType.MOVE, cart.getId(), path.get(1));
	    		cartMoves.add(action);

	    		path.stream().limit(3).forEach(restrictedPositions::add);
			}
		}

		return cartMoves;
	}

	public Action buyLogic() {
		var numCarts = myCrew.getUnits().stream().filter(unit -> unit.getType() == UnitType.CART).count();
		var numMiners = myCrew.getUnits().stream().filter(unit -> unit.getType() == UnitType.MINER).count();

	    var MAX_MINERS = Math.min(6, terrain.getMineablePositions(base).size() + numMiners);
	    var MAX_CARTS = 6;


		var cartCost = myCrew.getPrices().getCartPrice();
		var minerCost = myCrew.getPrices().getMinerPrice();

		if (numCarts < MAX_CARTS && surplusMiners.size() > 0 && cartCost <= myCrew.getBlitzium()) {
			return new BuyAction(UnitType.CART);
		}

		if (numMiners < MAX_MINERS && minerCost <= myCrew.getBlitzium()) {
			return new BuyAction(UnitType.MINER);
		}
		return null;
	}

	public void assignCarts() {
		cartAssignations = new HashMap<>();

		var miners = myCrew.getUnits()
				.stream()
				.filter(u -> u.getType() == UnitType.MINER)
				.collect(Collectors.toList());

		if (miners.isEmpty()) {
			return;
		}

		var carts = myCrew.getUnits().stream().filter(u -> u.getType() == UnitType.CART).collect(Collectors.toList());
		for (int i = 0; i < carts.size(); i++) {
			var cart = carts.get(i);
			var miner = miners.get(i % miners.size());
			cartAssignations.put(cart, miner);
		}
	}


	public UnitAction cartLogic(Unit unit) {
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


		var miners = myCrew.getUnits().stream()
				.filter(u -> u.getType() == UnitType.MINER)
				.collect(Collectors.toList());

		if (miners.isEmpty()) {
			return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());
		}

		if (miners.size() == 1) {
			return generateMoveAction(unit, miners.get(0).getPosition());
		}

		var theChosenOne = cartAssignations.get(unit);
		if (theChosenOne == null) {
			return generateNoneAction();
		}

		var chosenPosition = theChosenOne.getPosition();
		if (!terrain.reachable(chosenPosition) || terrain.isNeighboring(chosenPosition)) {
			return generateNoneAction();
		}
		return generateMoveAction(unit, chosenPosition);
	}

	private UnitAction generateNoneAction() {
		return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());
	}


	public UnitAction minerLogic(Unit unit) {
		var isSurplusMiner = surplusMiners.contains(unit);

		var unitPosition = unit.getPosition();
		var adjacentPositions = terrain.neighbors(unitPosition);

		if (isSurplusMiner && unit.getBlitzium() > 4) {
			// if we are adjacent to a base, and have blitzium:
			if (terrain.isNeighboring(base)) {
				return new UnitAction(UnitActionType.DROP, unit.getId(), base);

			}

			// else, move towards a depot
			return generateMoveAction(unit, base);
		}



		// if we have at least 25 blitzium and are by our assigned cart, drop the blitzium to that cart
        Unit assignedCart = null;
		for (var entry: cartAssignations.entrySet()) {
			if (entry.getValue().equals(unit)) {
				assignedCart = entry.getKey();
			}
		}
		if (assignedCart != null && unit.getBlitzium() >= 25 && terrain.isNeighboring(assignedCart.getPosition())) {
			return new UnitAction(UnitActionType.DROP, unit.getId(), assignedCart.getPosition());
		}


		// else, check if we are by a mine with < 25 blitzium; mine
        if (unit.getBlitzium() < 50) {
			for (var pos: adjacentPositions) {
				if (positionHasType(pos, TileType.MINE)) {
					requestedMiningLocations.add(pos);
					return new UnitAction(UnitActionType.MINE, unit.getId(), pos);
				}
			}
		}

		var mines = terrain.getMineablePositions().stream().filter(m -> !requestedMiningLocations.contains(m)).collect(Collectors.toList());

        if (!mines.isEmpty() && !canMine(unit.getPosition()) && unit.getBlitzium() < 25) {
        	var mine = mines.stream().findFirst().orElseThrow();
			requestedMiningLocations.add(mine);
			System.out.println("Trying to mine at " + mine.toString());
			return generateMoveAction(unit, mine);
		} else {
        	return new UnitAction(UnitActionType.NONE, unit.getId(), unit.getPosition());
		}
	}

	public UnitAction generateMoveAction(Unit u, Position p) {
	    if (u.getPosition().equals(p)) {
	    	return new UnitAction(UnitActionType.NONE, u.getId(), u.getPosition());
		}

	    if (terrain.pathTo(p) == null) {
	    	return generateNoneAction();
		}

		return new UnitAction(UnitActionType.MOVE, u.getId(), p);
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