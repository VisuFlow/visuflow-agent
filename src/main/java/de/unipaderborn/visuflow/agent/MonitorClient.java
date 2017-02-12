package de.unipaderborn.visuflow.agent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MonitorClient {

	private static MonitorClient instance = new MonitorClient();
	private Socket socket;
	private OutputStream out;
	private DataOutputStream writer;
	private BlockingQueue<String[]> queue = new LinkedBlockingQueue<>();
	private Thread sendThread;
	boolean running = true;

	private MonitorClient() {}
	public static MonitorClient getInstance() {
		return instance;
	}

	public void connect() throws UnknownHostException, IOException {
		socket = new Socket("localhost", 6543);
		out = socket.getOutputStream();
		writer = new DataOutputStream(out);
	}

	public void start() {
		sendThread = new Thread() {
			@Override
			public void run() {
				while(running) {
					try {
						String[] msg = queue.take();
						writer.writeUTF(msg[0]); // fqn
						writer.writeUTF(msg[1]); // inset
						writer.writeUTF(msg[2]); // outset
						writer.flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		sendThread.setName("MonitorClient Async Message Dispatcher");
		sendThread.setDaemon(true);
		sendThread.start();
	}

	public void send(String[] msg) throws IOException {
		writer.writeUTF(msg[0]); // fqn
		writer.writeUTF(msg[1]); // inset
		writer.writeUTF(msg[2]); // outset
	}

	public void sendAsync(String fqn, String inset, String outset) throws IOException, InterruptedException {
		String[] msg = new String[3];
		msg[0] = fqn;
		msg[1] = inset;
		msg[2] = outset;
		queue.put(msg);
	}

	public void close() throws IOException {
		running = false;
		sendThread.interrupt();
		writer.flush();
		socket.close();
	}
}
