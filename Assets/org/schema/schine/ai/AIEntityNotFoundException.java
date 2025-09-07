package org.schema.schine.ai;

public class AIEntityNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public AIEntityNotFoundException(int receiver_ID) {
		super("ID: " + receiver_ID);
	}

}
