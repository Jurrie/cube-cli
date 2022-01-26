package org.jurr.cube3d.cubecli.sender.cube;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Logger;

import org.jurr.cube3d.cubecli.util.LoggingOutputStream;

class CubeOutputStream {
	static class BuildFileContentOutputStream extends OutputStream {
		private int num;
		private int num2;

		private final OutputStream outputStream;

		BuildFileContentOutputStream(final OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		int getChecksum() {
			return num2 << 8 | num;
		}

		@Override
		public void write(final byte[] b, final int off, final int len) throws IOException {
			Objects.checkFromIndexSize(off, len, b.length);

			var numberOfBytesToWrite = len;
			for (var i = 0; i < len; i++) {
				final var currentByte = b[off + i] & 0xFF;

				// Update checksum intermediates
				num = (num + currentByte) % 255;
				num2 = (num2 + num) % 255;

				// NOTE: Should we try to escape '$' as well, as "$$$" will trigger WiFly command mode? I wasn't able to get it to break.

				if (currentByte == ESCAPE_CODE || currentByte == MESSAGE_BUILD_FILE_END
						|| currentByte == MESSAGE_START) {
					numberOfBytesToWrite++;
				}
			}

			final var bytesWithEscapeCodes = new byte[numberOfBytesToWrite];
			var indexInOutputBuffer = 0;
			for (var i = 0; i < len; i++) {
				final var currentByte = b[off + i] & 0xFF;

				if (currentByte == ESCAPE_CODE || currentByte == MESSAGE_BUILD_FILE_END
						|| currentByte == MESSAGE_START) {
					bytesWithEscapeCodes[indexInOutputBuffer] = (byte) ESCAPE_CODE;
					indexInOutputBuffer++;
					bytesWithEscapeCodes[indexInOutputBuffer] = (byte) (currentByte - ESCAPE_CODE);
				} else {
					bytesWithEscapeCodes[indexInOutputBuffer] = (byte) currentByte;
				}
				indexInOutputBuffer++;
			}

			outputStream.write(bytesWithEscapeCodes, 0, numberOfBytesToWrite);
		}

		@Override
		public void write(final int currentByte) throws IOException {
			// We opted to override write(byte[], int, int) because of performance reasons
			write(new byte[] { (byte) currentByte }, 0, 1);
		}
	}

	static final int ESCAPE_CODE = 0xFD;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	static final int MESSAGE_BUILD_FILE_END = 0xFE;

	private static final int MESSAGE_END = 0x00;
	private static final int MESSAGE_GET_CUBE_TYPE = 0x54;
	private static final int MESSAGE_GET_MATERIAL_TYPE = 0x4D;
	private static final int MESSAGE_SEND_BUILD_FILE_CHECKSUM = 0x43;
	private static final int MESSAGE_SEND_BUILD_FILE_NAME = 0x4E;
	private static final int MESSAGE_SEND_BUILD_FILE_SIZE = 0x53;

	static final int MESSAGE_START = 0xFF;

	private final OutputStream outputStream;

	CubeOutputStream(final OutputStream outputStream) {
		this.outputStream = LoggingOutputStream.forOutputStream(outputStream);
	}

	void requestCubeType() throws IOException {
		LOGGER.config("Sending 'request Cube type'");
		outputStream.write(MESSAGE_START);
		outputStream.write(MESSAGE_GET_CUBE_TYPE);
		outputStream.write(MESSAGE_END);
	}

	void requestMaterialType() throws IOException {
		LOGGER.config("Sending 'request material type'");
		outputStream.write(MESSAGE_START);
		outputStream.write(MESSAGE_GET_MATERIAL_TYPE);
		outputStream.write(MESSAGE_END);
	}

	public void sendBuildFileChecksum(final int checksum) throws IOException {
		LOGGER.config(() -> "Sending 'build file checksum' (" + checksum + ")");
		outputStream.write(MESSAGE_START);
		outputStream.write(MESSAGE_SEND_BUILD_FILE_CHECKSUM);
		// The checksum is written as four bytes hexadecimal, upper case, with leading zeroes.
		outputStream.write(String.format("%04X", checksum).getBytes(StandardCharsets.ISO_8859_1));
		outputStream.write(MESSAGE_END);
	}

	public int sendBuildFileContent(final InputStream is) throws IOException {
		LOGGER.config("Sending 'build file content'");
		try (final var os = new BuildFileContentOutputStream(outputStream)) {
			is.transferTo(os);
			outputStream.write(MESSAGE_BUILD_FILE_END);
			return os.getChecksum();
		}
	}

	public void sendBuildFileName(final String name) throws IOException {
		LOGGER.config(() -> "Sending 'build file name' (" + name + ")");
		outputStream.write(MESSAGE_START);
		outputStream.write(MESSAGE_SEND_BUILD_FILE_NAME);
		outputStream.write(name.getBytes(StandardCharsets.ISO_8859_1));
		outputStream.write(MESSAGE_END);
	}

	public void sendBuildFileSize(final long size) throws IOException {
		LOGGER.config(() -> "Sending 'build file size' (" + size + " bytes)");
		outputStream.write(MESSAGE_START);
		outputStream.write(MESSAGE_SEND_BUILD_FILE_SIZE);
		outputStream.write(Long.toString(size).getBytes(StandardCharsets.ISO_8859_1));
		outputStream.write(MESSAGE_END);
	}
}