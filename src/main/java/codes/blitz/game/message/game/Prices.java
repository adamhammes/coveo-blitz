package codes.blitz.game.message.game;

import com.google.gson.annotations.SerializedName;

public class Prices {
	@SerializedName("MINER")
	private int minerPrice;
	@SerializedName("OUTLAW")
	private int outlawPrice;
	@SerializedName("CART")
	private int cartPrice;

	public int getMinerPrice() {
		return minerPrice;
	}

	public int getOutlawPrice() {
		return outlawPrice;
	}

	public int getCartPrice() {
		return cartPrice;
	}
}