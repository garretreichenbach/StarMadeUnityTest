package org.schema.game.server.controller;

public class DebugMarking extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public DebugMarking(String string) {
		super("(do not report this! this is a debug marker)" + string);
	}

}
