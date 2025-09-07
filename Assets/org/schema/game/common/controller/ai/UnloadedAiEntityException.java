package org.schema.game.common.controller.ai;

public class UnloadedAiEntityException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public final String uid;

	public UnloadedAiEntityException(String uid) {
		super(uid);
		this.uid = uid;
	}

}
