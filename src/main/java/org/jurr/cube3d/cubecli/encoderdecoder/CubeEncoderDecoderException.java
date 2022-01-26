package org.jurr.cube3d.cubecli.encoderdecoder;

public class CubeEncoderDecoderException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	CubeEncoderDecoderException(final String message) {
		super(message);
	}

	CubeEncoderDecoderException(final Throwable cause) {
		super(cause);
	}
}