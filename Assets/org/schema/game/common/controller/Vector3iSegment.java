package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SegmentData;

public class Vector3iSegment extends Vector3i{

	public int getAbsX(){
		return x * SegmentData.SEG;
	}
	public int getAbsY(){
		return y * SegmentData.SEG;
	}
	public int getAbsZ(){
		return z * SegmentData.SEG;
	}
	public Vector3i getAbs(Vector3i out){
		out.set(getAbsX(), getAbsY(), getAbsZ());
		return out;
	}
	
	public boolean isSane(int x, int y, int z){
		return (Math.abs(x) < 512 && Math.abs(y) < 512 && Math.abs(z) < 512);
	}
	@Override
	public void set(int x, int y, int z) {
//		assert(isSane(x, y, z)):x+", "+y+", "+z;
		super.set(x, y, z);
	}
	@Override
	public void set(Vector3i pos) {
//		assert(isSane(x, y, z)):pos.x+", "+y+", "+pos.z;
		super.set(pos);
	}
	@Override
	public void set(Vector3b a) {
		assert(isSane(x, y, z)):a.x+", "+a.y+", "+a.z;
		super.set(a);
	}
	
	
}
