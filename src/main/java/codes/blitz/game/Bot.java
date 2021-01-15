package codes.blitz.game;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import codes.blitz.game.message.game.Action;
import codes.blitz.game.message.game.Crew;
import codes.blitz.game.message.game.GameMessage;
import codes.blitz.game.message.game.Position;
import codes.blitz.game.message.game.UnitAction;
import codes.blitz.game.message.game.UnitActionType;

public class Bot {
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
		int mapSize = gameMessage.getGameMap().getMapSize();

		List<Action> actions = myCrew.getUnits().stream()
				.map(unit -> new UnitAction(UnitActionType.MOVE, unit.getId(),
						getRandomPosition(mapSize)))
				.collect(Collectors.toList());

		return actions;

	}

	public Position getRandomPosition(int size) {
		Random rand = ThreadLocalRandom.current();
		return new Position(rand.nextInt(size), rand.nextInt(size));
	}
}