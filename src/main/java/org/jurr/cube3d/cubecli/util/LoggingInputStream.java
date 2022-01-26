package org.jurr.cube3d.cubecli.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingInputStream extends InputStream {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	public static InputStream forInputStream(final InputStream inputStream) {
		return forInputStream(inputStream, Level.FINEST);
	}

	public static InputStream forInputStream(final InputStream inputStream, final Level logLevel) {
		if (LOGGER.isLoggable(logLevel)) {
			return new LoggingInputStream(inputStream, logLevel);
		}
		return inputStream;
	}

	private final InputStream inputStream;
	private final Level logLevel;

	private LoggingInputStream(final InputStream inputStream, final Level logLevel) {
		this.inputStream = inputStream;
		this.logLevel = logLevel;
	}

	@Override
	public int read() throws IOException {
		final var result = inputStream.read();
		if (result == -1) {
			LOGGER.log(logLevel, () -> "→ [End of stream]");
		} else {
			LOGGER.log(logLevel, () -> "→ " + String.format("%02X", result));
		}
		return result;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		final var result = super.read(b, off, len);
		if (result == -1) {
			LOGGER.log(logLevel, () -> "→ [End of stream]");
		} else {
			final var sb = new StringBuilder(len * 2);
			for (var i = 0; i < len; i++) {
				final var byteToLog = b[off + i];
				sb.append(" ");
				sb.append(String.format("%02X", byteToLog));
			}
			LOGGER.log(logLevel, () -> ("→" + sb.toString() + " (" + len + " bytes)"));
		}
		return result;
	}
}