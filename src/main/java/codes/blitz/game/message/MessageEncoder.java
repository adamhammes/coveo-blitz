package codes.blitz.game.message;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import codes.blitz.game.message.bot.BotMessage;

public class MessageEncoder implements Encoder.Text<BotMessage> {
	private static Gson gson = new GsonBuilder().create();

	@Override
	public String encode(BotMessage message) throws EncodeException {
		return gson.toJson(message);
	}

	@Override
	public void init(EndpointConfig endpointConfig) {
		// Custom initialization logic
	}

	@Override
	public void destroy() {
		// Close resources
	}
}