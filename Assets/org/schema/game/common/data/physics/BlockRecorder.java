package org.schema.game.common.data.physics;

import org.schema.game.common.data.world.SegmentData;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockRecorder {
	public BlockRecorder(CubeRayVariableSet cubeRayVariableSet){
		boundVarSet = cubeRayVariableSet;
		
	}
	private final CubeRayVariableSet boundVarSet;
	
	public final LongArrayList blockAbsIndices = new LongArrayList(128);
	public final IntArrayList blockLocalIndices = new IntArrayList(128);
	public final ObjectArrayList<SegmentData> datas = new ObjectArrayList<SegmentData>(128);
	
	
	public void clear(){
		blockAbsIndices.clear();
		blockLocalIndices.clear();
		datas.clear();
	}


	public int size() {
		assert(!blockAbsIndices.isEmpty() || (blockLocalIndices.isEmpty() && datas.isEmpty()));
		return blockAbsIndices.size();
	}
	public boolean isEmpty() {
		assert(!blockAbsIndices.isEmpty() || (blockLocalIndices.isEmpty() && datas.isEmpty()));
		return blockAbsIndices.isEmpty();
	}


	public void free() {
		clear();
		boundVarSet.freeBlockRecorder(this);		
	}
}
