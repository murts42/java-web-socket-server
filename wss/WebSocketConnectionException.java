package wss;

public class WebSocketConnectionException extends Exception {

    private static final long serialVersionUID = -7697077327357062912L;

    public WebSocketConnectionException(String message) {
	super(message);
    }

    public WebSocketConnectionException(Throwable cause) {
	super(cause);
    }

    public WebSocketConnectionException(String message, Throwable cause) {
	super(message, cause);
    }
}
