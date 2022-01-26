package org.jurr.cube3d.cubecli.encoderdecoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class CubeFileUtils {
	private static final byte[] VALID_CHARS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
			'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', '/', '.', '+', '-', '=', ' ', '[', ']', '{', '}', '^', ':', '#', '\t', '\r',
			'\n' };

	static {
		Arrays.sort(VALID_CHARS);
	}

	// Check if the file adheres to the Blowfish blocksize of 8 bytes.
	// This is of course *very* rudimentary.
	public static boolean isCubeFile(final Path cubeFile) throws IOException {
		return Files.size(cubeFile) % CubeEncoderDecoder.getBlockSize() == 0;
	}

	// Read first kilobyte and check if there are only the valid gcode characters.
	// This is of course a *very* rudimentary detection of a .gcode file.
	public static boolean isGCodeFile(final Path gcodeFile) throws IOException {
		try (var is = Files.newInputStream(gcodeFile)) {
			final var buffer = is.readNBytes(1024);
			for (final byte b : buffer) {
				if (Arrays.binarySearch(VALID_CHARS, b) < 0) {
					return false;
				}
			}
		}

		return true;
	}

	private CubeFileUtils() {
	}
}