package org.schema.game.common.data.explosion;

import java.util.Comparator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;

import com.bulletphysics.linearmath.Transform;

public class ExplosionCubeConvexBlockCallback implements Comparable<ExplosionCubeConvexBlockCallback>, Comparator<ExplosionCubeConvexBlockCallback> {
	public static final int SEGMENT_CONTROLLER = 0;
	public static final int CHARACTER = 1;
	public final Vector3i blockPos = new Vector3i();
	public final Vector3i segmentPos = new Vector3i();
	public final Transform boxTransform = new Transform();
	public final boolean[][] dodecaOverlap = new boolean[12][6];
	public final Vector3f boxPosToCenterOfExplosion = new Vector3f();
	public int segDataIndex;
	public byte segPosX;
	public byte segPosY;
	public byte segPosZ;
	public int type;
	public int blockHp;
	public int blockHpOrig;
	public float boxDistToCenterOfExplosion;
	public int segEntityId;
	public SegmentData data; //direct reference to segment data for usage only in synched
	public short blockId;
	public boolean blockActive;
	

	public ExplosionCubeConvexBlockCallback() {
		super();
	}

	@Override
	public int compare(ExplosionCubeConvexBlockCallback o1,
	                   ExplosionCubeConvexBlockCallback o2) {
		return o1.compareTo(o2);
	}

	@Override
	public int compareTo(ExplosionCubeConvexBlockCallback o) {
		return Float.compare(boxDistToCenterOfExplosion, o.boxDistToCenterOfExplosion);
	}

	public ReentrantReadWriteLock update(ReentrantReadWriteLock currentLock) {
		if (data != null) {
			if (currentLock != data.rwl) {
				if (currentLock != null) {
					currentLock.readLock().unlock();
				}
				currentLock = data.rwl;
				currentLock.readLock().lock();
			}
			//else we are already locked 

			blockId = data.getType(segDataIndex);
			if(blockId != 0) {
				blockHp = ElementKeyMap.convertToFullHP(blockId, data.getHitpointsByte(segDataIndex));
			}else {
				blockHp = 0;
			}
			blockHpOrig = blockHp;
		} else {
			blockId = 0;
			blockHp = 0;
			blockHpOrig = 0;
		}
		return currentLock;
	}

}
