package org.schema.schine.input;

public class InputTypeParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean emptyLine;

	public InputTypeParseException(boolean emptyLine) {
		this.emptyLine = true;
	}
	public InputTypeParseException() {
		super();
	}

	public InputTypeParseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InputTypeParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public InputTypeParseException(String message) {
		super(message);
	}

	public InputTypeParseException(Throwable cause) {
		super(cause);
	}

}
