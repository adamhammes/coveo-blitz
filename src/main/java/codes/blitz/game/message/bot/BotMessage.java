package codes.blitz.game.message.bot;

import java.util.List;

import codes.blitz.game.message.MessageType;
import codes.blitz.game.message.game.Action;

public class BotMessage {
	private MessageType type;
	private List<Action> actions;
	private String crewName;
	private String token;
	private Integer tick;

	public Integer getTick() {
		return this.tick;
	}

	public void setTick(Integer tick) {
		this.tick = tick;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> action) {
		this.actions = action;
	}

	public String getCrewName() {
		return crewName;
	}

	public void setCrewName(String crewName) {
		this.crewName = crewName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result
				+ ((crewName == null) ? 0 : crewName.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BotMessage other = (BotMessage) obj;
		if (actions != other.actions) {
			return false;
		}
		if (crewName == null) {
			if (other.crewName != null) {
				return false;
			}
		} else if (!crewName.equals(other.crewName)) {
			return false;
		}
		if (token == null) {
			if (other.token != null) {
				return false;
			}
		} else if (!token.equals(other.token)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BotMessage [type=" + type + ", action=" + actions + ", name="
				+ crewName + ", token=" + token + "]";
	}
}
