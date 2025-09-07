package org.schema.game.server.data;

public class PlayerNotFountException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public PlayerNotFountException(String playerName) {
		super("Player not found: \"" + playerName + "\"");
	}

}
