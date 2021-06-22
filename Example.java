import java.util.Vector;

import wss.WebSocketConnection;
import wss.WebSocketConnectionHandler;
import wss.WebSocketServer;

public class Example {
    
    public Example() {
        WebSocketConnectionHandler handler = new EchoConnectionHandler();
        WebSocketServer server = new WebSocketServer(handler, 1001); // automatically starts background thread, that listens for new connections
    }

    public static void main(String[] args) {
        new Example();
    }

    private class EchoConnectionHandler implements WebSocketConnectionHandler {

        private Vector<WebSocketConnection> connections = new Vector<WebSocketConnection>();

        @Override
        public void addConnection(WebSocketConnection connection) {
            connections.add(connection);
        }

        @Override
        public void processMessage(WebSocketConnection connection, String message) {
            connection.send(message); // echo the incoming message
        }

        @Override
        public void processClose(WebSocketConnection connection) {
            connections.remove(connection);
        }

    }
}