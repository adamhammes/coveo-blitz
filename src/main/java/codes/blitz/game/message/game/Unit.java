package codes.blitz.game.message.game;

import java.util.List;

public class Unit {
	private String id;
	private UnitType type;
	private int blitzium;
	private Position position;
	private List<Position> path;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public UnitType getType() {
		return type;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

	public int getBlitzium() {
		return blitzium;
	}

	public void setBlitzium(int blitzium) {
		this.blitzium = blitzium;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public List<Position> getPath() {
		return path;
	}

	public void setPath(List<Position> path) {
		this.path = path;
	}
}
