package org.schema.game.common.data.physics.octree;



public class ArrayOctreeEmpty extends ArrayOctree{

	public ArrayOctreeEmpty(boolean onServer) {
		super(onServer);
		
	}

	@Override
	public void insertAABB16(byte x, byte y, byte z, int index) {
		assert(false);
		//do nothing because we already filled
	}

	@Override
	public void insert(byte x, byte y, byte z, int index) {
		assert(false);
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
