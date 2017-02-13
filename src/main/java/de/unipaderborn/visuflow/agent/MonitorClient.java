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
					} catch (InterruptedException e) {
						// fine, we silently stop sending results
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		sendThread.setName("MonitorClient Async Message Dispatcher");
		sendThread.start();
	}

	public void send(String msg) throws IOException {
		writer.println(msg);
	}

	public void sendAsync(String msg) throws IOException, InterruptedException {
		queue.put(msg);
	}

	public void close() throws IOException {
		// FIXME ?!? if the queue is not empty, we might loose some results,
		// because they are not sent. we might have to think about a better
		// solution to close the client
		running = false;
		sendThread.interrupt();
		writer.flush();
		socket.close();
	}
}
