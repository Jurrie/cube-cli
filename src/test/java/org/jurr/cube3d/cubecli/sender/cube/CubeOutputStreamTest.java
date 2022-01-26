package org.jurr.cube3d.cubecli.sender.cube;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CubeOutputStreamTest {
	private static Stream<Arguments> testSendBuildFileContentParams() {
		return Stream.of(Arguments.of(new byte[] { 0x01 }, new byte[] { 0x01 }, 257, "Simple test"),
				Arguments.of(new byte[] { (byte) CubeOutputStream.ESCAPE_CODE },
						new byte[] { (byte) CubeOutputStream.ESCAPE_CODE, 0x00 }, 65021, "Test with ESCAPE_CODE"),

				Arguments.of(new byte[] { (byte) CubeOutputStream.MESSAGE_BUILD_FILE_END },
						new byte[] { (byte) CubeOutputStream.ESCAPE_CODE, 0x01 }, 65278,
						"Test with MESSAGE_BUILD_FILE_END"),

				Arguments.of(new byte[] { (byte) CubeOutputStream.MESSAGE_START },
						new byte[] { (byte) CubeOutputStream.ESCAPE_CODE, 0x02 }, 0, "Test with MESSAGE_START"),

				Arguments.of(
						new byte[] { 0x01, (byte) CubeOutputStream.ESCAPE_CODE, 0x02,
								(byte) CubeOutputStream.MESSAGE_BUILD_FILE_END, 0x03,
								(byte) CubeOutputStream.MESSAGE_START, 0x04 },
						new byte[] { 0x01, (byte) CubeOutputStream.ESCAPE_CODE, 0x00, 0x02,
								(byte) CubeOutputStream.ESCAPE_CODE, 0x01, 0x03, (byte) CubeOutputStream.ESCAPE_CODE,
								0x02, 0x04 },
						3591, "Test with all escaped bytes"));
	}

	private void assertEquals(final byte[] expected, final byte[] actual) {
		Assertions.assertEquals(expected.length, actual.length, "Expected and actual do not have the same length.");
		for (var i = 0; i < expected.length; i++) {
			Assertions.assertEquals(expected[i], actual[i], "Byte " + i + " differs between expected and actual.");
		}
	}

	@ParameterizedTest(name = "{index}: {3}")
	@MethodSource("testSendBuildFileContentParams")
	void testSendBuildFileContent(final byte[] input, final byte[] expected, final int checksum, final String name)
			throws IOException {
		try (final var baos = new ByteArrayOutputStream();
				final var bfcos = new CubeOutputStream.BuildFileContentOutputStream(baos)) {
			bfcos.write(input);
			bfcos.flush();

			final var actual = baos.toByteArray();
			assertEquals(expected, actual);

			final var actualChecksum = bfcos.getChecksum();
			Assertions.assertEquals(checksum, actualChecksum);
		}
	}
}