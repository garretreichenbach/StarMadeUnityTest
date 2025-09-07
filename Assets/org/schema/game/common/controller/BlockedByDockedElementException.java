package org.schema.game.common.controller;

import org.schema.game.common.data.SegmentPiece;

public class BlockedByDockedElementException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public SegmentPiece to;

	public BlockedByDockedElementException(SegmentPiece to) {
		this.to = to;
	}
}
