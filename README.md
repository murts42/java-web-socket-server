# Java Web Socket Server

**A crappy and unmaintained implementation of a Web Socket Server using Java.**

---

## Usage

Yout need a custom ConnectionHandler that implements `WebSocketConnectionHandler`.

```java
public class EchoConnectionHandler implements WebSocketCOnnectionHandler {

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
```

Then you need to instantiate a `WebSocketServer` with the handler from above. The constructor automatically starts a background thread (which is probably bad design) which listens to incoming connections. It creates `WebSocketConnections`for each new connections and hands them over to the handler.

## Crappiness

This thing basically does not care how the initial handshake from the client looks, as long as it starts with `GET`, ends with `\r\n\r\n` and contains the `Sec-WebSocket-Key` header. It also does not support custom protocols and Secure Web Socket (wss://).