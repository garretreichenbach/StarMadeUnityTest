package org.schema.game.common.controller;

public interface SegmentBufferIteratorEmptyInterface extends SegmentBufferIteratorInterface {
	public boolean handleEmpty(int posX, int posY, int posZ, long lastChanged);
}
