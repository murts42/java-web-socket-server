package wss;

public interface WebSocketConnectionHandler {

	public void addConnection(WebSocketConnection conn);

	public void processMessage(WebSocketConnection con, String message);

	public void processClose(WebSocketConnection con);
}
