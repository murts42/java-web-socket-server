package wss;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;

public class WebSocketConnection extends Thread {

	private Socket socket;
	private WebSocketConnectionHandler connectionHandler;
	private boolean connected = false;

	public WebSocketConnection(Socket pSocket,
			WebSocketConnectionHandler pConnectionHandler) {
		socket = pSocket;
		connectionHandler = pConnectionHandler;
		doHandshake();
	}

	private void doHandshake() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					InputStream in = socket.getInputStream();
					OutputStream out = socket.getOutputStream();

					Scanner scanner = new Scanner(in);
					String data = scanner.useDelimiter("\r\n\r\n").next();

					Matcher get = Pattern.compile("^GET").matcher(data);
					if (get.find()) {
						// GET request
						Matcher match = Pattern
								.compile("Sec-WebSocket-Key: (.*)")
								.matcher(data);
						match.find();

						String magicBytes = match.group(1);

						byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
								+ "Connection: Upgrade\r\n"
								+ "Upgrade: websocket\r\n"
								+ "Sec-WebSocket-Accept: "
								+ Base64.getEncoder().encodeToString(
										MessageDigest.getInstance("SHA-1")
												.digest((magicBytes
														+ "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
																.getBytes(
																		"UTF-8")))
								+ "\r\n\r\n").getBytes("UTF-8");

						out.write(response, 0, response.length);
						out.flush();
						System.out.println("handshake with "
								+ socket.getInetAddress() + ":"
								+ socket.getPort() + " successful");
					} else {
						// no GET request
						scanner.close();
						socket.close();
					}
				} catch (IOException | NoSuchAlgorithmException e) {
					connected = false;
					e.printStackTrace();
				}
				connected = true;
				startReading();
			}
		}).start();
	}

	@Override
	public void run() {
		try {
			InputStream in = socket.getInputStream();
			while (!socket.isClosed()) {
				try {
					int first = in.read();
					if (first == 129) {
						int second = in.read();
						int length = 0;
						if (second - 128 <= 125) {
							length = second - 128;
						} else if (second - 128 == 126) {
							// length equal to next 2 bytes
							byte[] b = new byte[2];
							in.read(b);
							length = unsignedShortToInt(b);
						} else if (second - 128 == 127) {
							// length equal to next 8 bytes
							byte[] b = new byte[8];
							in.read(b);
							long l = unsignedIntToLong(b);
							if (l < Integer.MAX_VALUE) {
								length = (int) l;
							} else {
								System.out.println(
										"WARNING: message to long (length > Integer.MAX_VALUE), will be truncated at index "
												+ (Integer.MAX_VALUE - 1));
								length = Integer.MAX_VALUE;
							}
						}
						byte[] key = new byte[4];
						in.read(key);
						byte[] encoded = new byte[length];
						in.read(encoded);
						byte[] decoded = decode(encoded, key);
						String message = new String(decoded, "UTF-8");
						connectionHandler.processMessage(this, message);
					} else if (first == -1) {
						System.out.println("here");
						close();
					}
				} catch (IOException e) {
					// socket probably got closed -> ignore
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startReading() {
		start();
	}

	private byte[] decode(byte[] encoded, byte[] key) {
		byte[] decoded = new byte[encoded.length];

		for (int i = 0; i < encoded.length; i++) {
			decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
		}
		return decoded;
	}

	public void send(String message) {
		try {
			int opcode = 0x1;
			OutputStream out = socket.getOutputStream();
			if (message.length() <= 125) {
				byte[] data = new byte[message.length() + 2];
				data[0] = (byte) (opcode | 0x80);
				data[1] = (byte) message.length();
				byte[] messageBytes = message.getBytes();
				for (int i = 2; i < data.length; i++) {
					data[i] = messageBytes[i - 2];
				}
				out.write(data);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}

	public int getPort() {
		return socket.getPort();
	}

	public void close() {
		try {
			socket.close();
			connectionHandler.processClose(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int unsignedShortToInt(byte[] b) {
		if (b.length == 2) {
			int i = 0;
			i |= b[0] & 0xFF;
			i <<= 8;
			i |= b[1] & 0xFF;
			return i;
		} else {
			return -1;
		}
	}

	private long unsignedIntToLong(byte[] b) {
		if (b.length == 4) {
			int i = 0;
			i |= b[0] & 0xFF;
			i <<= 8;
			i |= b[1] & 0xFF;
			i <<= 8;
			i |= b[2] & 0xFF;
			i <<= 8;
			i |= b[3] & 0xFF;
			return i;
		} else {
			return -1;
		}
	}

	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof WebSocketConnection) {
			WebSocketConnection conn = (WebSocketConnection) o;
			return conn.getInetAddress().equals(getInetAddress())
					&& conn.getPort() == getPort();
		}
		return false;
	}
}
