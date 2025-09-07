package org.schema.game.common.controller.rules.rules;

public class RuleParserException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3462162891269365149L;

	public RuleParserException() {
		super();
	}

	public RuleParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RuleParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public RuleParserException(String message) {
		super(message);
	}

	public RuleParserException(Throwable cause) {
		super(cause);
	}
	

}
