package org.jurr.cube3d.cubecli.sender.wifly;

public class BroadcastException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	BroadcastException(final String message) {
		super(message);
	}

	BroadcastException(final String message, final Throwable cause) {
		super(message, cause);
	}
}