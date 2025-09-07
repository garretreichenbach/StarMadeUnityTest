package org.schema.game.common.data.player.faction;

public class FactionNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public FactionNotFoundException(Integer factionId) {
		super("ID: " + factionId);
	}

}
