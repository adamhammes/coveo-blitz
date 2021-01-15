package codes.blitz.game.message.game;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameMessage {
	private int tick;
	private int totalTick;
	private String crewId;
	private List<Crew> crews;
	private GameMap map;
	private Map<String, Crew> crewsMapById;
	private Rules rules;

	public int getTick() {
		return tick;
	}

	public int getTotalTick() {
		return totalTick;
	}

	public String getCrewId() {
		return crewId;
	}

	public List<Crew> getCrews() {
		return crews;
	}

	public GameMap getGameMap() {
		return map;
	}

	public Map<String, Crew> getCrewsMapById() {
		if (crewsMapById == null) {
			crewsMapById = crews.stream().collect(
					Collectors.toMap(Crew::getId, Function.identity()));
		}
		return crewsMapById;
	}

	public Rules getRules() {
		return rules;
	}
}
