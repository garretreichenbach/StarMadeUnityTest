package org.schema.game.common.data.physics.octree;

public interface SegmentDataTraverseInterface {

	public void handle(byte x, byte y, byte z, int octreeNodeIndex, int octreeLocalIndex);

}
