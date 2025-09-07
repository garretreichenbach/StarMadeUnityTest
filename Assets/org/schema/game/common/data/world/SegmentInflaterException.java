package org.schema.game.common.data.world;

public class SegmentInflaterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * how many bytes were actually inflated
	 */
	public int inflate;
	/**
	 * how many bytes should have been inflated
	 */
	public int shouldBeInflate;

	public SegmentInflaterException(int inflate, int shouldBeInflate) {
		this.inflate = inflate;
		this.shouldBeInflate = shouldBeInflate;
	}

}
