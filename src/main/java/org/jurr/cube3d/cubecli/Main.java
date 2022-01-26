package org.jurr.cube3d.cubecli;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jurr.cube3d.cubecli.encoderdecoder.CubeEncoderDecoder;
import org.jurr.cube3d.cubecli.sender.CubeSender;
import org.jurr.cube3d.cubecli.sender.CubeSenderException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
	private static final int EXIT_CMDLINE_INVALID = 1;
	private static final int EXIT_EXCEPTION = 2;
	private static final int EXIT_OK = 0;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	static {
		final var stream = Main.class.getClassLoader().getResourceAsStream("logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(stream);
		} catch (SecurityException | IOException e) {
			LOGGER.log(Level.SEVERE, "Error setting up logger", e);
			throw new CubeSenderException("Error setting up logger", e);
		}
	}

	private static String getCurrentExecutable() {
		try {
			final var uri = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();

			// Are we an executable jar?
			final var innerURI = uri.replaceFirst("file:", "");
			final var currentExecutablePath = Paths.get(innerURI);
			if (!currentExecutablePath.toFile().isDirectory()) {
				final var fileName = currentExecutablePath.getFileName();
				if (fileName != null) {
					return fileName.toString();
				}
			}
		} catch (final URISyntaxException | RuntimeException e) {
			// Do nothing, just return the default
		}

		// We can not determine the jar file from which we run.
		// We are probably running the class directly from an IDE.
		// Default to returning the canonical name of this class.
		return Main.class.getCanonicalName();
	}

	public static void main(final String[] args) throws SecurityException, IOException {
		final var jCommander = JCommander.newBuilder().addObject(Settings.INSTANCE).build();
		try {
			jCommander.setProgramName(getCurrentExecutable());
			jCommander.parse(args);
		} catch (final ParameterException e) {
			LOGGER.severe(e.getLocalizedMessage());
			e.usage();
			System.exit(EXIT_CMDLINE_INVALID);
		}

		if (!(Settings.INSTANCE.isInquiry() ^ Settings.INSTANCE.isSendFile() ^ Settings.INSTANCE.isDecodeFile()
				^ Settings.INSTANCE.isEncodeFile())) {
			LOGGER.severe("Please use one (and only one) of -i, -f, -e and -d");
			jCommander.usage();
			System.exit(EXIT_CMDLINE_INVALID);
		}

		if (Settings.INSTANCE.isHelp()) {
			jCommander.usage();
			System.exit(EXIT_OK);
		}

		if (Settings.INSTANCE.isTraceOutput()) {
			Logger.getLogger("").setLevel(Level.FINEST);
			LOGGER.finest("Trace output enabled");
		} else if (Settings.INSTANCE.isVerboseOutput()) {
			Logger.getLogger("").setLevel(Level.CONFIG);
			LOGGER.config("Debug output enabled");
		}

		try {
			if (Settings.INSTANCE.isInquiry() || Settings.INSTANCE.isSendFile()) {
				new CubeSender().start();
			} else if (Settings.INSTANCE.isDecodeFile()) {
				try (var os = Settings.INSTANCE.isUseStandardOutput() ? System.out
						: new FileOutputStream(Settings.INSTANCE.getOutputFile().toFile())) {
					CubeEncoderDecoder.decode(Settings.INSTANCE.getFileToDecode(), os);
				}
			} else if (Settings.INSTANCE.isEncodeFile()) {
				try (var os = Settings.INSTANCE.isUseStandardOutput() ? System.out
						: new FileOutputStream(Settings.INSTANCE.getOutputFile().toFile())) {
					CubeEncoderDecoder.encode(Settings.INSTANCE.getFileToEncode(), os);
				}
			}
		} catch (final CubeSenderException e) {
			LOGGER.severe(e.getMessage());
			if (e.getCause() != null) {
				LOGGER.log(Level.FINE, "Stack trace: ", e.getCause());
			}
			System.exit(EXIT_EXCEPTION);
		} catch (final Throwable e) {
			LOGGER.severe(() -> "Unchecked exception thrown: " + e.getMessage());
			if (e.getCause() != null) {
				LOGGER.log(Level.FINE, "Stack trace: ", e.getCause());
			}
			throw e;
		}
	}
}