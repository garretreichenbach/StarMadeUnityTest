package org.schema.game.common.data.blockeffects.config;

public class EffectException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EffectException() {
		super();
	}

	public EffectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public EffectException(String message, Throwable cause) {
		super(message, cause);
	}

	public EffectException(String message) {
		super(message);
	}

	public EffectException(Throwable cause) {
		super(cause);
	}

}
