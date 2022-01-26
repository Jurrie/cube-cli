package org.jurr.cube3d.cubecli.sender.cube;

public class CubeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	CubeException(final String message) {
		super(message);
	}

	CubeException(final String message, final Throwable cause) {
		super(message, cause);
	}
}