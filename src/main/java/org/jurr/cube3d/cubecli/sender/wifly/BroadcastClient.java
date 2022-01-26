package org.jurr.cube3d.cubecli.sender.wifly;

import java.net.SocketException;

public class BroadcastClient {
	private BroadcastMessage broadcastMessage;

	public BroadcastMessage autodetectWiFly() {
		broadcastMessage = null;

		final BroadcastListener broadcastListener;
		try {
			broadcastListener = new BroadcastListener();
		} catch (final SocketException e) {
			throw new BroadcastException("Error while listening for a WiFly board", e);
		}

		broadcastListener.addCallback(broadcastMessage -> {
			BroadcastClient.this.broadcastMessage = broadcastMessage;
			broadcastListener.stopListening();
			synchronized (BroadcastClient.class) {
				BroadcastClient.class.notifyAll();
			}
		});

		broadcastListener.start();
		synchronized (BroadcastClient.class) {
			try {
				while (broadcastMessage == null) {
					BroadcastClient.class.wait();
				}
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new BroadcastException("Interrupt while autodetecting a WiFly board", e);
			}
		}

		broadcastListener.stopListening();
		try {
			broadcastListener.join();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new BroadcastException("Interrupt after we discovered a WiFly board", e);
		}

		return broadcastMessage;
	}
}