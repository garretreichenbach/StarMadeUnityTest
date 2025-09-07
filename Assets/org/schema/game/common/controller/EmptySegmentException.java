package org.schema.game.common.controller;

public class EmptySegmentException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public int x;
	public int y;
	public int z;

	public EmptySegmentException(int x, int y, int z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

}
