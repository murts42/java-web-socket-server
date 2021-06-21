package wss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	private final WebSocketServer server;
	private final ConnectionHandler handler;

	public Main(int pPort) {
		handler = new ConnectionHandler();
		server = new WebSocketServer(handler, pPort);

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean stop = false;
		do {
			String[] command = null;
			try {
				command = reader.readLine().split(" ", 2);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (command != null) {
				String arg = (command.length > 1) ? command[1] : null;
				switch (command[0]) {
				case "stop":
					stop = true;
					break;

				case "send":
					send(arg);
					break;

				case "time":
					timeCmd(arg);
					break;

				default:
					System.out.println("unknown command");
				}
			}
		} while (!stop);
		server.close();
		handler.closeConnections();
		System.out.println("server closed");
	}

	private void send(String message) {
		if (message != null)
			handler.sendToAll(message);
		else
			System.out.println("usage: send <message>");
	}

	private void timeCmd(String arg) {
		if (arg != null) {
			int startnummer = Integer.parseInt(arg);
			send(startnummer + "/" + getCurTime());
		} else
			System.out.println("usage: time <startnummer>");
	}

	private long getCurTime() {
		return Math.round(System.currentTimeMillis() / 10D);
	}

	public static void main(String[] args) {
		System.out.println("build 1");
		if (args.length >= 1) {
			try {
				int port = Integer.parseInt(args[0]);
				new Main(port);
			} catch (NumberFormatException e) {
				System.out.println("only accpets a number as argument");
			}
		} else {
			new Main(1001);
		}
	}
}
