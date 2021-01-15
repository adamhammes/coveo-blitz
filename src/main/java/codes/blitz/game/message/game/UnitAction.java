package codes.blitz.game.message.game;

public class UnitAction extends Action {
	private Position target;
	private UnitActionType action;
	private String unitId;

	public UnitAction(UnitActionType action, String unitId, Position target) {
		super(ActionType.UNIT);
		this.target = target;
		this.action = action;
		this.unitId = unitId;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public UnitActionType getAction() {
		return action;
	}

	public void setAction(UnitActionType action) {
		this.action = action;
	}

	public Position getTarget() {
		return target;
	}

	public void setTarget(Position target) {
		this.target = target;
	}
}
