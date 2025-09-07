package org.schema.game.common.data.missile;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public interface MissileManagerInterface {
	public Short2ObjectOpenHashMap<Missile> getMissiles();
}
