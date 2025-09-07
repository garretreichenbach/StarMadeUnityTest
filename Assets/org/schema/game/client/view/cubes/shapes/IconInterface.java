package org.schema.game.client.view.cubes.shapes;

import org.schema.common.util.MemoryManager.MemFloatArray;
import org.schema.common.util.MemoryManager.MemIntArray;

public interface IconInterface {
	public void single(BlockRenderInfo ri, byte r, byte g, byte b, byte o, MemFloatArray buffer, AlgorithmParameters p);
	public void single(BlockRenderInfo ri, byte r, byte g, byte b, byte o, MemIntArray dataBuffer, AlgorithmParameters p);
}
