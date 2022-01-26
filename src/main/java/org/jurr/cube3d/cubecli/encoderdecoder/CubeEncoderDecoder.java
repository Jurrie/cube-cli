package org.jurr.cube3d.cubecli.encoderdecoder;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public final class CubeEncoderDecoder {
	private static final String CIPHER = "Blowfish/ECB/NoPadding";
	private static final SecretKeySpec KEY = new SecretKeySpec("221BBakerMycroft".getBytes(ISO_8859_1), "Blowfish");

	public static void decode(final Path cubeFile, final OutputStream os) throws IOException {
		if (!Files.isReadable(cubeFile)) {
			throw new CubeEncoderDecoderException("File " + cubeFile.toAbsolutePath() + " is not readable");
		}

		if (!CubeFileUtils.isCubeFile(cubeFile)) {
			throw new CubeEncoderDecoderException("File " + cubeFile + " is not a valid .cube file");
		}

		try (var is = Files.newInputStream(cubeFile)) {
			runFileThroughCipher(is, os, Cipher.DECRYPT_MODE);
		} catch (final NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| IllegalBlockSizeException | BadPaddingException e) {
			throw new CubeEncoderDecoderException(e.getMessage());
		}
	}

	public static void encode(final Path gcodeFile, final OutputStream os) throws IOException {
		if (!Files.isReadable(gcodeFile)) {
			throw new CubeEncoderDecoderException("File " + gcodeFile.toAbsolutePath() + " is not readable");
		}

		if (!CubeFileUtils.isGCodeFile(gcodeFile)) {
			throw new CubeEncoderDecoderException("File " + gcodeFile + " is not a valid .gcode file");
		}

		try (var is = Files.newInputStream(gcodeFile)) {
			runFileThroughCipher(is, os, Cipher.ENCRYPT_MODE);
		} catch (final NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| IllegalBlockSizeException | BadPaddingException e) {
			throw new CubeEncoderDecoderException(e.getMessage());
		}
	}

	public static int getBlockSize() {
		try {
			return Cipher.getInstance(CIPHER).getBlockSize();
		} catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
			throw new CubeEncoderDecoderException(e);
		}
	}

	private static void runFileThroughCipher(final InputStream is, final OutputStream os, final int cipherOpmode)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException,
			IllegalBlockSizeException, BadPaddingException {
		final var cipher = Cipher.getInstance(CIPHER);
		cipher.init(cipherOpmode, KEY);

		final var buffer = new byte[cipher.getBlockSize()];
		int length;
		while ((length = is.read(buffer)) != -1) {
			final var swappedBuffer = swapByteOrderToConformWithBrokenEncoding(buffer);

			final var decodedBlock = cipher.update(swappedBuffer, 0, length);
			if (decodedBlock.length > 0) {
				os.write(swapByteOrderToConformWithBrokenEncoding(decodedBlock));
			}
		}

		os.write(cipher.doFinal());
	}

	// See https://github.com/fritzw/cube-utils/blob/master/blowfish.c#L257
	private static byte[] swapByteOrderToConformWithBrokenEncoding(final byte[] buffer) {
		return new byte[] { buffer[3], buffer[2], buffer[1], buffer[0], buffer[7], buffer[6], buffer[5], buffer[4] };
	}

	private CubeEncoderDecoder() {
	}
}