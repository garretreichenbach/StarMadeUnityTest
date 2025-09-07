package org.schema.game.common.data;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

public class SegmentSignature {

	public boolean empty = false;
	public SegmentController context;
	private Vector3i pos;

	private long lastChanged;
	private short size;

	public SegmentSignature() {

	}

	public SegmentSignature(Vector3i pos, long lastChanged, boolean empty, short size) {
		super();
		this.pos = pos;
		this.lastChanged = lastChanged;
		this.empty = empty;
		this.size = size;
	}

	/**
	 * @return the lastChanged
	 */
	public long getLastChanged() {
		return lastChanged;
	}

	/**
	 * @param lastChanged the lastChanged to set
	 */
	public void setLastChanged(long lastChanged) {
		this.lastChanged = lastChanged;
	}

	/**
	 * @return the pos
	 */
	public Vector3i getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(Vector3i pos) {
		this.pos = pos;
	}

	/**
	 * @return the size
	 */
	public short getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(short size) {
		this.size = size;
	}
}
