package org.schema.game.common.data.physics.octree;

import org.schema.game.common.data.world.Segment;



public class ArrayOctreeFull extends ArrayOctree{

	public ArrayOctreeFull(boolean onServer) {
		super(onServer);
		
		int i = 0;
		for(byte z = 0; z < Segment.DIM; z++){
			for(byte y = 0; y < Segment.DIM; y++){
				for(byte x = 0; x < Segment.DIM; x++){
					super.insert(x, y, z, i);
					super.insertAABB16(x, y, z, i);
					
					i++;
				}
			}
		}
	}

	@Override
	public void insertAABB16(byte x, byte y, byte z, int index) {
		//do nothing because we already filled
	}

	@Override
	public void insert(byte x, byte y, byte z, int index) {
		//do nothing because we already filled
	}

	@Override
	public void resetAABB16() {
		//do nothing because we already filled
	}

	@Override
	public void delete(byte x, byte y, byte z, int index, short type) {
		throw new RuntimeException("Cannot delete from this");
	}

	@Override
	public void reset() {
		//do nothing because we already filled
	}

}
