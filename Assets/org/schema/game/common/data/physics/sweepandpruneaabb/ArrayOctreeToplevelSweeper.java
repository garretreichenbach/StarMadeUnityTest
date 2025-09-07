package org.schema.game.common.data.physics.sweepandpruneaabb;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.game.common.data.physics.octree.OctreeVariableSet;
import org.schema.game.common.data.world.Segment;

import com.bulletphysics.linearmath.Transform;

public class ArrayOctreeToplevelSweeper extends Sweeper<Integer, Segment>{
	private Vector3f outOuterMin = new Vector3f();
	private Vector3f outOuterMax = new Vector3f();
	
	public int targetLevel = 0;
	public float margin;
	public OctreeVariableSet set;
	public Segment seg0;
	public Matrix3f abs0;
	public Matrix3f abs1;
	
	@Override
	protected int fillAxis(Segment shape, Transform trans,
			SweepPoint<Integer>[] axisToFill, List<Integer> aL,
			Comparator<SweepPoint<Integer>> comp, int startHash) {
		
		assert(set != null);
		
		Segment cb = shape;
		
		int count = 0;
		
		Matrix3f absoluteMat;
		if(seg0 == cb){
			absoluteMat = abs0;
		}else{
			absoluteMat = abs1;
		}
		
		for (int i = 0; i < 8; i++) {
			
			
			boolean contains = cb.getSegmentData().getOctree().getAABB(i, 0, set, cb, 
					trans, absoluteMat, margin, outOuterMin, outOuterMax);
//			System.err.println("IN "+(seg0 == cb)+": "+i+"; "+outOuterMin+", "+outOuterMax);
			if(contains){
				axisToFill[count] = getPoint(i, outOuterMin, outOuterMax, startHash + count);
				count++;
			}
		}
		
		Arrays.sort(axisToFill, 0, count, comp);
		return count;
	}


}