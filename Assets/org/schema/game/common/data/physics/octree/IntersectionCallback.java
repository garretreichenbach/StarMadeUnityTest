package org.schema.game.common.data.physics.octree;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.data.physics.AABBVarSet;
import org.schema.game.common.data.physics.sweepandpruneaabb.SegmentAabbInterface;
import org.schema.game.common.data.world.Segment;

import com.bulletphysics.linearmath.Transform;

public class IntersectionCallback implements SegmentAabbInterface{
	public int leafCalcs;
	public int hitCount;
	public boolean initialized = false;
	public long aabbTest;
	public long aabbRetrieve;
	private Vector3f[] hits;
	private Vector3b[] range;
	private int[] mask;
	private int[] nodeIndex;

	//	public void addHit(Vector3f min, Vector3f max, Vector3b start, Vector3b end){
	//		hits[hitCount*2].set(min);
	//		hits[hitCount*2+1].set(max);
	//		range[hitCount*2].set(start);
	//		range[hitCount*2+1].set(end);
	//		hitCount++;
	//	}
	public void addHit(Vector3f min, Vector3f max, byte sX, byte sY, byte sZ, byte eX, byte eY, byte eZ, int mask, int nodeIndex) {
		hits[hitCount * 2].set(min);
		hits[hitCount * 2 + 1].set(max);
		range[hitCount * 2].set(sX, sY, sZ);
		range[hitCount * 2 + 1].set(eX, eY, eZ);
		this.mask[hitCount] = mask;
		this.nodeIndex[hitCount] = nodeIndex;

		hitCount++;
	}

	public void createHitCache(int possibleHitCount) {
		hits = new Vector3f[possibleHitCount * 2];
		range = new Vector3b[possibleHitCount * 2];
		for (int i = 0; i < hits.length; i++) {
			hits[i] = new Vector3f();
			range[i] = new Vector3b();
		}
		mask = new int[possibleHitCount];
		nodeIndex = new int[possibleHitCount];
		initialized = true;
	}

	public int getNodeIndexHit(int index) {
		return nodeIndex[index];
	}

	public void getAabbOnly(int index, Vector3f minOut, Vector3f maxOut) {
		minOut.set(hits[index * 2]);
		maxOut.set(hits[index * 2 + 1]);
	}
	public int getHit(int index, Vector3f minOut, Vector3f maxOut, Vector3b startOut, Vector3b endOut) {
		minOut.set(hits[index * 2]);
		maxOut.set(hits[index * 2 + 1]);
		startOut.set(range[index * 2]);
		endOut.set(range[index * 2 + 1]);
		return mask[index];
	}

	public void reset() {
		hitCount = 0;
		leafCalcs = 0;
		aabbTest = 0;
		aabbRetrieve = 0;
	}

	@Override
	public void getSegmentAabb(Segment s, Transform trans,
			Vector3f outOuterMin, Vector3f outOuterMax, Vector3f localMinOut,
			Vector3f localMaxOut, AABBVarSet varSet) {
				
	}

	@Override
	public void getAabb(Transform tmpAABBTrans0, Vector3f min, Vector3f max) {
				
	}

	@Override
	public void getAabbUncached(Transform t, Vector3f aabbMin,
			Vector3f aabbMax, boolean cache) {
				
	}

	@Override
	public void getAabbIdent(Vector3f min, Vector3f max) {
				
	}

}
