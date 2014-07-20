package org.juniversal.core;

@SuppressWarnings("serial")
public class JUniversalException extends RuntimeException {

	public JUniversalException(Throwable cause) {
		super(cause);
	}

	public JUniversalException(String message) {
		super(message);
	}

	public JUniversalException(String message, Throwable cause) {
		super(message, cause);
	}
}
