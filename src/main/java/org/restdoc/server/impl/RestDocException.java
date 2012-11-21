package org.restdoc.server.impl;

/**
 * Exception thrown by the framework on errors
 * 
 * @author hoegertn
 */
public class RestDocException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 *            the detail message (which is saved for later retrieval by the getMessage() method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that
	 *            the cause is nonexistent or unknown.)
	 */
	public RestDocException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 *            the detail message (which is saved for later retrieval by the getMessage() method).
	 */
	public RestDocException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 *            the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that
	 *            the cause is nonexistent or unknown.)
	 */
	public RestDocException(final Throwable cause) {
		super(cause);
	}

}
