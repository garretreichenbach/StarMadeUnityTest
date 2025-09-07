package org.schema.game.common.data.gamemode;

public class GameModeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public GameModeException() {
		super();
	}

	// #RM1958 remove Java8 constructor
//	public GameModeException(String message, Throwable cause,
//			boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

	public GameModeException(String message, Throwable cause) {
		super(message, cause);
	}

	public GameModeException(String message) {
		super(message);
	}

	public GameModeException(Throwable cause) {
		super(cause);
	}

}
