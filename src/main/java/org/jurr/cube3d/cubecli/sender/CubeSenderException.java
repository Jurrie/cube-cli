package org.jurr.cube3d.cubecli.sender;

public class CubeSenderException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CubeSenderException(final String message) {
		super(message);
	}

	public CubeSenderException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CubeSenderException(final Throwable cause) {
		super(cause);
	}
}