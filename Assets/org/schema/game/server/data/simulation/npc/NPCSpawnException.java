package org.schema.game.server.data.simulation.npc;

public class NPCSpawnException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NPCSpawnException() {
		super();
	}

	public NPCSpawnException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NPCSpawnException(String message, Throwable cause) {
		super(message, cause);
	}

	public NPCSpawnException(String message) {
		super(message);
	}

	public NPCSpawnException(Throwable cause) {
		super(cause);
	}

}
