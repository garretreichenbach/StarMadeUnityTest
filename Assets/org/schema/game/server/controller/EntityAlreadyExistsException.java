package org.schema.game.server.controller;

public class EntityAlreadyExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public EntityAlreadyExistsException(String ident) {
		super(ident);
	}

}
