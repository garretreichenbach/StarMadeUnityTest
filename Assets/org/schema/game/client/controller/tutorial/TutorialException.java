package org.schema.game.client.controller.tutorial;

public class TutorialException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public TutorialException() {
		super();
	}

	// #RM1958 remove Java 8 constructor
//	public TutorialException(String message, Throwable cause,
//			boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

	public TutorialException(String message, Throwable cause) {
		super(message, cause);
	}

	public TutorialException(String message) {
		super(message);
	}

	public TutorialException(Throwable cause) {
		super(cause);
	}

}
