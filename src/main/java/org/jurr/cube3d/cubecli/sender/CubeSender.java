package org.jurr.cube3d.cubecli.sender;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.jurr.cube3d.cubecli.Settings;
import org.jurr.cube3d.cubecli.encoderdecoder.CubeEncoderDecoder;
import org.jurr.cube3d.cubecli.encoderdecoder.CubeFileUtils;
import org.jurr.cube3d.cubecli.sender.cube.CubeConnection;
import org.jurr.cube3d.cubecli.sender.wifly.BroadcastClient;
import org.jurr.cube3d.cubecli.sender.wifly.BroadcastMessage;

public class CubeSender {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	public CubeSender() {
		// Before trying to connect to a Cube, let's verify the parameters

		if (Settings.INSTANCE.isSendFile()) {
			final var cubeFile = Settings.INSTANCE.getFileToSend();
			if (!Files.isReadable(cubeFile)) {
				throw new CubeSenderException("File " + cubeFile.toAbsolutePath().toString() + " is not readable");
			}
		}
	}

	private BroadcastMessage autodetectCube() {
		while (true) {
			final var broadcastMessage = new BroadcastClient().autodetectWiFly();
			if (broadcastMessage.getPort() == 2000 && broadcastMessage.getDeviceId().startsWith("Cube-")) {
				Settings.INSTANCE.setHost(broadcastMessage.getAddress().getHostAddress());
				Settings.INSTANCE.setPort(broadcastMessage.getCallbackPort());
				LOGGER.info("Found a Cube on " + Settings.INSTANCE.getHost() + ":" + Settings.INSTANCE.getPort());
				return broadcastMessage;
			}
			LOGGER.fine("Found a WiFly device, but not a Cube on " + Settings.INSTANCE.getHost() + ":"
					+ Settings.INSTANCE.getPort());
		}
	}

	private void doInquiry(final BroadcastMessage broadcastMessage, final CubeConnection cubeConnection)
			throws IOException {
		if (broadcastMessage != null) {
			LOGGER.info(() -> "Cube hostname: " + broadcastMessage.getDeviceId());
			LOGGER.info(
					() -> "Connected to accesspoint " + macAddressToString(broadcastMessage.getAccesspointMacAddress())
							+ " with signal strength " + broadcastMessage.getReceivedSignalStrengthIndicator());
			LOGGER.info(() -> "System time: " + broadcastMessage.getTime());
			LOGGER.info(() -> "Boot time: " + broadcastMessage.getBootTime());
		}

		final var cubeType = cubeConnection.getCubeType();
		LOGGER.info(() -> "Connected to a " + cubeType);

		final var materialType = cubeConnection.getMaterialType();
		LOGGER.info(() -> "Cube material: " + materialType);
	}

	private void doSendCubeFile(final CubeConnection cubeConnection, final Path fileToSend) throws IOException {
		LOGGER.info(() -> "Sending " + fileToSend);
		cubeConnection.sendCubeFile(fileToSend);
	}

	private void doSendGCodeFile(final CubeConnection cubeConnection, final Path fileToSend) throws IOException {
		LOGGER.info(() -> "Encoding " + fileToSend + " to .cube file, and sending");
		final var fileName = fileToSend.getFileName().toString();
		final var fileSize = Files.size(fileToSend);
		try (var pis = new PipedInputStream()) {
			final var pipeThread = new Thread(() -> {
				try (var pos = new PipedOutputStream(pis)) {
					CubeEncoderDecoder.encode(fileToSend, pos);
				} catch (final IOException e) {
					throw new CubeSenderException("Unable to encode .gcode file to .cube file before sending to Cube",
							e);
				}
			});
			pipeThread.start();

			cubeConnection.sendCubeFile(pis, fileName, fileSize + fileSize % CubeEncoderDecoder.getBlockSize());

			try {
				pipeThread.join();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new CubeSenderException("Interrupt after we've sent the file to the Cube", e);
			}
		}
	}

	private String macAddressToString(final int[] macAddress) {
		final var result = new StringBuilder(17);
		result.append(String.format("%02X", macAddress[0]));
		for (var i = 1; i < macAddress.length; i++) {
			result.append(':');
			result.append(String.format("%02X", macAddress[i]));
		}
		return result.toString();
	}

	public void start() throws IOException {
		final BroadcastMessage broadcastMessage;

		if (Settings.INSTANCE.isAutodetectEnabled()) {
			LOGGER.info("Scanning for Cube printer...");
			synchronized (CubeSender.class) {
				broadcastMessage = autodetectCube();
			}
		} else {
			broadcastMessage = null;
		}

		LOGGER.info(() -> "Connecting to Cube on " + Settings.INSTANCE.getHost() + ":" + Settings.INSTANCE.getPort());
		final var cubeConnection = new CubeConnection(Settings.INSTANCE.getHost(), Settings.INSTANCE.getPort());

		if (Settings.INSTANCE.isInquiry()) {
			doInquiry(broadcastMessage, cubeConnection);
		} else if (Settings.INSTANCE.getFileToSend() != null) {
			final var fileToSend = Settings.INSTANCE.getFileToSend();

			// If it's a regular .gcode file, then first encode
			if (CubeFileUtils.isGCodeFile(fileToSend)) {
				doSendGCodeFile(cubeConnection, fileToSend);
			} else if (CubeFileUtils.isCubeFile(fileToSend)) {
				doSendCubeFile(cubeConnection, fileToSend);
			} else {
				throw new CubeSenderException(
						"File " + fileToSend + " is neither a valid .cube file nor a valid .gcode file");
			}

			LOGGER.info(() -> "File sent successfully, now press [✓] on your Cube");
		}
	}
}