package org.jurr.cube3d.cubecli.sender.cube;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jurr.cube3d.cubecli.util.LoggingInputStream;

class CubeInputStream {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static final int MESSAGE_CUBE_TYPE_CUBE_1 = 0x31;
	private static final int MESSAGE_CUBE_TYPE_CUBE_2 = 0x32;

	private static final int MESSAGE_END = 0x00;
	private static final int MESSAGE_MATERIAL_TYPE_ABS = 0x30;
	private static final int MESSAGE_MATERIAL_TYPE_NONE = 0x2F;
	private static final int MESSAGE_MATERIAL_TYPE_PLA = 0x31;
	private static final int MESSAGE_MATERIAL_TYPE_PLA_ABS = 0x32;

	private static final int MESSAGE_RESPONSE = 0x52;
	private static final int MESSAGE_RESPONSE_CODE_CHECKSUMFAIL = 0x36;
	private static final int MESSAGE_RESPONSE_CODE_DUPLICATEFILENAME = 0x35;
	private static final int MESSAGE_RESPONSE_CODE_ERROR = 0x31;
	private static final int MESSAGE_RESPONSE_CODE_INSUFFICIENTSTORAGESPACE = 0x34;
	private static final int MESSAGE_RESPONSE_CODE_NOMEMORYMEDIA = 0x33;
	private static final int MESSAGE_RESPONSE_CODE_NOTREADY = 0x32;

	private static final int MESSAGE_RESPONSE_CODE_OK = 0x30;
	private static final int MESSAGE_START = 0xFF;

	private final InputStream inputStream;

	private boolean readingFirstMessage = true;

	CubeInputStream(final InputStream inputStream) {
		this.inputStream = LoggingInputStream.forInputStream(inputStream);
	}

	public void readBuildFileChecksumResponse() throws IOException {
		LOGGER.config("Reading 'build file checksum' response");
		readResponse();
	}

	public void readBuildFileContentResponse() throws IOException {
		LOGGER.config("Reading 'build file content' response");
		readResponse();
	}

	public void readBuildFileNameResponse() throws IOException {
		LOGGER.config("Reading 'build file name' response");
		readResponse();
	}

	public void readBuildFileSizeResponse() throws IOException {
		LOGGER.config("Reading 'build file size' response");
		readResponse();
	}

	private int readByte() throws IOException {
		final var result = inputStream.read();
		if (result == -1) {
			throw new IllegalStateException("Input stream was closed");
		}
		return result;
	}

	public CubeType readCubeTypeResponse() throws IOException {
		LOGGER.config("Reading 'Cube type' response");

		final CubeType response;

		readMessageStartByte();
		readMessageResponseByte();

		final var cubeTypeByte = readByte();
		response = switch (cubeTypeByte) {
		case MESSAGE_CUBE_TYPE_CUBE_1 -> CubeType.CUBE_1;
		case MESSAGE_CUBE_TYPE_CUBE_2 -> CubeType.CUBE_2;
		default -> throw new IllegalArgumentException(
				"Unknown Cube type response value " + String.format("%02X", cubeTypeByte));
		};

		readMessageEndByte();

		return response;
	}

	public MaterialType readMaterialTypeResponse() throws IOException {
		LOGGER.config("Reading 'material type' response");

		final MaterialType response;

		readMessageStartByte();
		readMessageResponseByte();

		final var materialTypeByte = readByte();
		response = switch (materialTypeByte) {
		case MESSAGE_MATERIAL_TYPE_PLA -> MaterialType.PLA;
		case MESSAGE_MATERIAL_TYPE_ABS -> MaterialType.ABS;
		case MESSAGE_MATERIAL_TYPE_PLA_ABS -> MaterialType.PLA_ABS;
		case MESSAGE_MATERIAL_TYPE_NONE -> MaterialType.NONE;
		default -> throw new IllegalArgumentException(
				"Unknown material type response value " + String.format("%02X", materialTypeByte));
		};

		readMessageEndByte();

		return response;
	}

	private void readMessageEndByte() throws IOException {
		var b = readByte();

		while (b != MESSAGE_END) {
			LOGGER.log(Level.FINE, "Invalid end of message. Received {}, expected {}",
					new Object[] { String.format("%02X", b), String.format("%02X", MESSAGE_END) });
			b = readByte();
		}
	}

	private void readMessageResponseByte() throws IOException {
		final var b = readByte();
		if (b != MESSAGE_RESPONSE) {
			LOGGER.log(Level.SEVERE, "Received {}, expected {} after {}", new Object[] { String.format("%02X", b),
					String.format("%02X", MESSAGE_RESPONSE), String.format("%02X", MESSAGE_START) });
			throw new IllegalStateException("Did not receive message response byte");
		}
	}

	private void readMessageResponseCodeByte() throws IOException {
		final var responseCode = readByte();

		switch (responseCode) {
		case MESSAGE_RESPONSE_CODE_ERROR:
			throw new IllegalStateException("Response was 'ERROR'");
		case MESSAGE_RESPONSE_CODE_NOTREADY:
			throw new IllegalStateException("Response was 'NOT READY'");
		case MESSAGE_RESPONSE_CODE_NOMEMORYMEDIA:
			throw new IllegalStateException("Response was 'NO MEMORY MEDIA'");
		case MESSAGE_RESPONSE_CODE_INSUFFICIENTSTORAGESPACE:
			throw new IllegalStateException("Response was 'INSUFFICIENT STORAGE SPACE'");
		case MESSAGE_RESPONSE_CODE_DUPLICATEFILENAME:
			throw new IllegalStateException("Response was 'DUPLICATE FILENAME'");
		case MESSAGE_RESPONSE_CODE_CHECKSUMFAIL:
			throw new IllegalStateException("Response was 'CHECKSUM FAIL'");
		case MESSAGE_RESPONSE_CODE_OK:
		default:
			break;
		}
	}

	private void readMessageStartByte() throws IOException {
		var b = readByte();

		while (b != MESSAGE_START) {
			if (readingFirstMessage) {
				LOGGER.log(Level.FINE,
						"Received garbage prior to first message. Probably the WiFly card greeting. Received {}",
						String.format("%02X", b));
			} else {
				LOGGER.log(Level.SEVERE, "Invalid start of message. Received {}, expected {}",
						new Object[] { String.format("%02X", b), String.format("%02X", MESSAGE_START) });
			}
			b = readByte();
		}
		readingFirstMessage = false;
	}

	private void readResponse() throws IOException {
		readMessageStartByte();
		readMessageResponseByte();
		readMessageResponseCodeByte();
		readMessageEndByte();
	}
}