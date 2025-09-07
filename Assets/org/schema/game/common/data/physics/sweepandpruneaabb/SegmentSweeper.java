package org.schema.game.common.data.physics.sweepandpruneaabb;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.world.Segment;

import com.bulletphysics.linearmath.Transform;

public class SegmentSweeper extends Sweeper<Segment, SegmentAabbInterface>{
	private Vector3f outOuterMin = new Vector3f();
	private Vector3f outOuterMax = new Vector3f();
	private Vector3f localMinOut = new Vector3f();
	private Vector3f localMaxOut = new Vector3f();
	


	@Override
	protected int fillAxis(SegmentAabbInterface shape, Transform trans,
			SweepPoint<Segment>[] axisToFill, List<Segment> aList,
			Comparator<SweepPoint<Segment>> comp, int startHash) {
		int i;
		
		final int size = aList.size();
		axisToFill = ensureSize(axisToFill, size);
		for (i = 0; i < size; i++) {
			Segment segment = aList.get(i);
			
			shape.getSegmentAabb(segment, trans, outOuterMin, outOuterMax, localMinOut, localMaxOut, varSet);
			
			axisToFill[i] = getPoint(segment, outOuterMin, outOuterMax, startHash + i);
		}
		
		Arrays.parallelSort(axisToFill, 0, i, comp);
		return i;
	}
	


}
