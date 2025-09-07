package org.schema.schine.auth.exceptions;

public class WrongUserNameOrPasswordException extends Exception {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WrongUserNameOrPasswordException(String message) {
		super(message);
	}

}
