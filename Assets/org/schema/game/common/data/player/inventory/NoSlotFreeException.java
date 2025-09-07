package org.schema.game.common.data.player.inventory;

public class NoSlotFreeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public NoSlotFreeException() {
		super();
		// TODO Auto-generated constructor stub
	}

	//#RM1958 remove Java8 constructor
//	public NoSlotFreeException(String message, Throwable cause,
//			boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//		// TODO Auto-generated constructor stub
//	}

	public NoSlotFreeException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NoSlotFreeException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public NoSlotFreeException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
