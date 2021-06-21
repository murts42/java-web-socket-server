package wss;

import java.util.Vector;

public class ConnectionHandler implements WebSocketConnectionHandler {

	private Vector<WebSocketConnection> connections;

	public ConnectionHandler() {
		connections = new Vector<>();
	}

	public void closeConnections() {
		for (int i = 0; i < connections.size(); i++) {
			connections.elementAt(i).close();
		}
	}

	public void sendToAll(String message) {
		for (WebSocketConnection conn : connections) {
			conn.send(message);
		}
	}

	@Override
	public void addConnection(WebSocketConnection conn) {
		connections.add(conn);
	}

	@Override
	public void processMessage(WebSocketConnection conn, String message) {
		System.out.println("message \"" + message + "\" from " + conn.getInetAddress() + ":" + conn.getPort());
		conn.send(message);
	}

	@Override
	public void processClose(WebSocketConnection conn) {
		System.out.println("socket " + conn.getInetAddress() + ":" + conn.getPort() + " closed");
		connections.remove(conn);
	}
}
