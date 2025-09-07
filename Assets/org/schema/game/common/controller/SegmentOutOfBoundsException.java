package org.schema.game.common.controller;

import org.schema.game.common.data.world.SegmentData;

public class SegmentOutOfBoundsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public SegmentOutOfBoundsException(int x, int y, int z, SegmentController c) {
		super("segment out of bounds: " + x + ", " + y + ", " + z + "; ["+SegmentData.SEG+"x -> " + c.getMinPos() + " - " + c.getMaxPos() + "] on " + c + " (" + c.getState() + ")");
	}

}
