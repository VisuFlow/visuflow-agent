package de.unipaderborn.visuflow.agent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MonitorClient {

	private static MonitorClient instance = new MonitorClient();
	private Socket socket;
	private OutputStream out;
	private PrintWriter writer;
	private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
	private Thread sendThread;
	boolean running = true;

	private MonitorClient() {}
	public static MonitorClient getInstance() {
		return instance;
	}

	public void connect() throws UnknownHostException, IOException {
		socket = new Socket("localhost", 6543);
		out = socket.getOutputStream();
		writer = new PrintWriter(out);
	}

	public void start() {
		sendThread = new Thread() {
			@Override
			public void run() {
				while(running) {
					try {
						String msg = queue.take();
						writer.println(msg);
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

	public void send(String msg) throws IOException {
		writer.println(msg);
	}

	public void sendAsync(String msg) throws IOException, InterruptedException {
		queue.put(msg);
	}

	public void close() throws IOException {
		running = false;
		sendThread.interrupt();
		writer.flush();
		socket.close();
	}
}
