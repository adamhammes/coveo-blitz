package codes.blitz.game;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.client.ClientManager;

public class Application {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		CountDownLatch latch = new CountDownLatch(1);

		String uri = "ws://127.0.0.1:8765";
		
		//Pour Alack
		uri = "ws://159.203.94.148:8765";
		
		ClientManager client = ClientManager.createClient();
		try {
			client.connectToServer(new WebsocketClient(latch),
					new URI(uri));
			latch.await();
		} catch (DeploymentException | URISyntaxException | InterruptedException
				| IOException e) {
			throw new RuntimeException(e);
		}
	}
}