package org.jurr.cube3d.cubecli.sender.wifly;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class BroadcastListener extends Thread {
	interface ListenerCallback {
		void deviceDiscovered(BroadcastMessage message);
	}

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private final List<ListenerCallback> callbacks;
	private boolean running;
	private final DatagramSocket socket;

	BroadcastListener() throws SocketException {
		socket = new DatagramSocket(55555);
		socket.setBroadcast(true);
		socket.setSoTimeout(1000);

		callbacks = new ArrayList<>();
	}

	void addCallback(final ListenerCallback callback) {
		callbacks.add(callback);
	}

	void removeCallback(final ListenerCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public void run() {
		running = true;

		while (running) {
			try {
				final var recvBuf = new byte[BroadcastMessage.UDP_PACKET_SIZE];
				final var packet = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(packet);

				final var message = BroadcastMessage.fromDatagramPacket(packet);
				callbacks.forEach(callback -> callback.deviceDiscovered(message));
			} catch (final SocketTimeoutException e) {
				// This is ok, we just do not want to wait infinitely
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "Error while listening for WiFly devices", e);
				running = false;
			}
		}
	}

	void stopListening() {
		running = false;
	}
}