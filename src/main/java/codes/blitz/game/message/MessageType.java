package codes.blitz.game.message;

import com.google.gson.annotations.SerializedName;

public enum MessageType {
	@SerializedName("COMMAND")
	COMMAND, @SerializedName("REGISTER")
	REGISTER
}
