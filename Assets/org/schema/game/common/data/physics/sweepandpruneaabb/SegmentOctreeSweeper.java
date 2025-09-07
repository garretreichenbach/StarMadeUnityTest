package org.schema.game.common.data.physics.sweepandpruneaabb;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.physics.octree.IntersectionCallback;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class SegmentOctreeSweeper extends Sweeper<Integer, IntersectionCallback>{
	private Vector3f outOuterMin = new Vector3f();
	private Vector3f outOuterMax = new Vector3f();
	
	@Override
	protected int fillAxis(IntersectionCallback shape, Transform trans,
			SweepPoint<Integer>[] axisToFill, List<Integer> aL,
			Comparator<SweepPoint<Integer>> comp, int startHash) {
		
		int i;
        IntArrayList aList = (IntArrayList)aL;
		int size = aList.size();
		
		axisToFill = ensureSize(axisToFill, size);
		for (i = 0; i < size; i++) {
			int segment = aList.getInt(i);
			shape.getAabbOnly(segment, outOuterMin, outOuterMax);
			
			axisToFill[i] = getPoint(segment, outOuterMin, outOuterMax, startHash + i);
		}
		
		Arrays.parallelSort(axisToFill, 0, i, comp);
		return i;
	}

	


}
