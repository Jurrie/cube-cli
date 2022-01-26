package org.jurr.cube3d.cubecli.sender.cube;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class CubeConnection {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private final Socket client;
	private final CubeInputStream cubeInputStream;
	private final CubeOutputStream cubeOutputStream;

	public CubeConnection(final String host, final int port) throws IOException {
		client = new Socket(host, port);

		cubeInputStream = new CubeInputStream(client.getInputStream());
		cubeOutputStream = new CubeOutputStream(client.getOutputStream());
	}

	public CubeType getCubeType() throws IOException {
		CubeType cubeType = null;
		synchronized (client) {
			cubeType = null;
			cubeOutputStream.requestCubeType();
			try {
				cubeType = cubeInputStream.readCubeTypeResponse();
			} catch (final IOException e) {
				LOGGER.severe("Error while retrieving Cube type");
				throw new CubeException("Error while retrieving Cube type", e);
			}
		}

		return cubeType;
	}

	public MaterialType getMaterialType() throws IOException {
		MaterialType materialType = null;
		synchronized (client) {
			materialType = null;
			cubeOutputStream.requestMaterialType();
			try {
				materialType = cubeInputStream.readMaterialTypeResponse();
			} catch (final IOException e) {
				LOGGER.severe("Error while retrieving material type");
				throw new CubeException("Error while retrieving material type", e);
			}
		}

		return materialType;
	}

	private void sendBuildFileChecksum(final int checksum) throws IOException {
		cubeOutputStream.sendBuildFileChecksum(checksum);
		try {
			cubeInputStream.readBuildFileChecksumResponse();
		} catch (final IOException e) {
			LOGGER.severe("Error while retrieving build file checksum response");
			throw new CubeException("Error while retrieving build file checksum response", e);
		}
	}

	private int sendBuildFileContent(final InputStream inputStream) throws IOException {
		final var checksum = cubeOutputStream.sendBuildFileContent(inputStream);
		try {
			cubeInputStream.readBuildFileContentResponse();
		} catch (final IOException e) {
			LOGGER.severe("Error while retrieving build file content response");
			throw new CubeException("Error while retrieving build file content response", e);
		}
		return checksum;
	}

	private void sendBuildFileName(final String fileName) throws IOException {
		cubeOutputStream.sendBuildFileName(fileName);
		try {
			cubeInputStream.readBuildFileNameResponse();
		} catch (final IOException e) {
			LOGGER.severe("Error while retrieving build file name response");
			throw new CubeException("Error while retrieving build file name response", e);
		}
	}

	private void sendBuildFileSize(final long fileSize) throws IOException {
		cubeOutputStream.sendBuildFileSize(fileSize);
		try {
			cubeInputStream.readBuildFileSizeResponse();
		} catch (final IOException e) {
			LOGGER.severe("Error while retrieving build file size response");
			throw new CubeException("Error while retrieving build file size response", e);
		}
	}

	public void sendCubeFile(final InputStream inputStream, final String fileName, final long fileSize)
			throws IOException {
		sendBuildFileSize(fileSize);
		sendBuildFileName(fileName);
		final var checksum = sendBuildFileContent(inputStream);
		sendBuildFileChecksum(checksum);
	}

	public void sendCubeFile(final Path cubeFile) throws IOException {
		sendBuildFileSize(Files.size(cubeFile));
		sendBuildFileName(cubeFile.getFileName().toString());
		try (final var is = new FileInputStream(cubeFile.toFile())) {
			final var checksum = sendBuildFileContent(is);
			sendBuildFileChecksum(checksum);
		}
	}
}