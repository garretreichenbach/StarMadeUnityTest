package org.schema.game.common.controller.io;

import org.schema.game.common.data.world.SegmentDataInterface;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public interface SegmentDummyProvider {
	public Int2ObjectOpenHashMap<SegmentDataInterface> getDummies();
}
