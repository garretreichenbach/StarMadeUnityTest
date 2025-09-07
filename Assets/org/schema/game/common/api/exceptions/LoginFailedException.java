package org.schema.game.common.api.exceptions;

public class LoginFailedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public LoginFailedException(int responseCode) {
		super("server response code: " + responseCode);
	}
}
