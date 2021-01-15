package codes.blitz.game.message.game;

public abstract class Action {
	private final ActionType type;

	protected Action(ActionType actionType) {
		this.type = actionType;
	}

	public ActionType getActionType() {
		return type;
	}
}
