package wss;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebSocketServer implements Runnable {

	private ServerSocket server;
	private WebSocketConnectionHandler handler;

	private int port;

	public WebSocketServer(WebSocketConnectionHandler pHandler, int pPort) {
		handler = pHandler;
		port = pPort;
		try {
			server = new ServerSocket(port);
			new Thread(this).start();
			System.out.println("Server started, port " + server.getLocalPort());
		} catch (IOException e) {
			System.out.println("ERROR: failed starting server");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		boolean stopFlag = false;
		while (!stopFlag) {
			try {
				Socket client = server.accept();
				System.out.println("client connected " + client.getInetAddress() + ":" + client.getPort());
				WebSocketConnection conn = new WebSocketConnection(client, handler);
				handler.addConnection(conn);
			} catch (IOException e) {
				stopFlag = true;
			}
		}

	}

	public boolean close() {
		try {
			server.close();
		} catch (IOException e) {
			System.out.println("ERROR: failed to close serverSocket");
			return false;
		}
		return true;
	}

	public int getPort() {
		return port;
	}
}
