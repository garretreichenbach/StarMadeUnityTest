package org.schema.game.server.controller;

public class NoIPException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public NoIPException(String playerIp) {
		super(playerIp);
	}

}
