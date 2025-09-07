package org.schema.game.common.data.physics.octree;

class TreeCache {
	byte[] lvlToIndex;

	boolean initialized;

	public TreeCache() {
		super();
		initialized = false;
		lvlToIndex = new byte[3]; // MAX LEVEL
	}

}