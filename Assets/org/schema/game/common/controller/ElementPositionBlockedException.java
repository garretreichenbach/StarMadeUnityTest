package org.schema.game.common.controller;

public class ElementPositionBlockedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public Object userData;

	public ElementPositionBlockedException(Object userData) {
		this.userData = userData;
	}

}
