package org.schema.game.common.data.physics;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class SimpleIntList extends IntArrayList{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	public int max;
	
	
	@Override
	public int size() {
		return max;
	}

	@Override
	public int getInt(int index) {
		return index;
	}

	@Override
	public Integer get(int index) {
		return index;
	}

	
}
