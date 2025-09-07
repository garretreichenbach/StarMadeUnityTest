package org.schema.game.common.data.player;

public class PlayerControlledTransformableNotFound extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public PlayerControlledTransformableNotFound(PlayerState playerState) {
		super("[ERROR] no transformable for  " + playerState);
	}

}
