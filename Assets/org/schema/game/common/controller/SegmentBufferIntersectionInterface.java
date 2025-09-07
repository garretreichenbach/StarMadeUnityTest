package org.schema.game.common.controller;

import org.schema.game.common.data.world.Segment;

public interface SegmentBufferIntersectionInterface {
	public boolean handle(Segment a, Segment b);
}
