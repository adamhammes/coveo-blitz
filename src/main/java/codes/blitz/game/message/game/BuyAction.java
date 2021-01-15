package codes.blitz.game.message.game;

public class BuyAction extends Action {
	private UnitType unitType;

	public BuyAction(UnitType unitType) {
		super(ActionType.BUY);
		this.unitType = unitType;
	}

	public UnitType getUnitType() {
		return unitType;
	}

	public void setUnitType(UnitType unitType) {
		this.unitType = unitType;
	}
}
