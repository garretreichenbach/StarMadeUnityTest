package org.schema.game.common.controller;

import org.schema.game.common.data.element.ElementCollection;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class PositionControl {
	private final LongOpenHashSet controlMap = new LongOpenHashSet();
	private final LongOpenHashSet controlPosMap = new LongOpenHashSet();
	private short type;

	public void clear() {
		controlMap.clear();
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof PositionControl) {
			return type == ((PositionControl) o).type;
		}
		return false;
	}

	/**
	 * @return the controlMap
	 */
	public LongOpenHashSet getControlMap() {
		return controlMap;
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(short type) {
		this.type = type;
	}

	public void addControlled(LongOpenHashSet m) {
		controlMap.addAll(m);
		for (long l : m) {
			controlPosMap.add(ElementCollection.getPosIndexFrom4(l));
		}
	}

	/**
	 * @return the controlPosMap
	 */
	public LongOpenHashSet getControlPosMap() {
		return controlPosMap;
	}
}
