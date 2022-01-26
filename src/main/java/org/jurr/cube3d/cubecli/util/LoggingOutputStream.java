package org.jurr.cube3d.cubecli.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingOutputStream extends OutputStream {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	public static OutputStream forOutputStream(final OutputStream outputStream) {
		return forOutputStream(outputStream, Level.FINEST);
	}

	public static OutputStream forOutputStream(final OutputStream outputStream, final Level logLevel) {
		if (LOGGER.isLoggable(logLevel)) {
			return new LoggingOutputStream(outputStream, logLevel);
		}
		return outputStream;
	}

	private final Level logLevel;
	private final OutputStream outputStream;

	private LoggingOutputStream(final OutputStream outputStream, final Level logLevel) {
		this.outputStream = outputStream;
		this.logLevel = logLevel;
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		final var sb = new StringBuilder(len * 2);
		for (var i = 0; i < len; i++) {
			final var byteToLog = b[off + i];
			sb.append(" ");
			sb.append(String.format("%02X", byteToLog));
		}
		LOGGER.log(logLevel, () -> ("←" + sb.toString() + " (" + len + " bytes)"));
		outputStream.write(b, off, len);
	}

	@Override
	public void write(final int b) throws IOException {
		LOGGER.log(logLevel, () -> "← " + String.format("%02X", b));
		outputStream.write(b);
	}
}