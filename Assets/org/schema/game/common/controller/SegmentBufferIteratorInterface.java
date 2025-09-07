package org.schema.game.common.controller;

import org.schema.game.common.data.world.Segment;

public interface SegmentBufferIteratorInterface {
	/**
	 * 
	 * @param s
	 * @param lastChanged
	 * @return false if iteration stops
	 */
	public boolean handle(Segment s, long lastChanged);

}
