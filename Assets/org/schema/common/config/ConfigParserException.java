package org.schema.common.config;

public class ConfigParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public ConfigParserException(String message, Throwable cause) {
		super(message + "\n(FixSuggestion: check your xml config files)", cause);
	}

	public ConfigParserException(String message) {
		super(message + "\n(FixSuggestion: check your xml config files)");
	}

}
