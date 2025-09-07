package org.schema.game.common.data.physics.sweepandpruneaabb;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.physics.CubesCompoundShape;
import org.schema.game.common.data.physics.CubesCompoundShapeChild;

import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.linearmath.Transform;

public class CompoundCollisionObjectSweeper extends Sweeper<CompoundShapeChild, CubesCompoundShape>{
	private Vector3f outOuterMin = new Vector3f();
	private Vector3f outOuterMax = new Vector3f();
	@Override
	protected int fillAxis(CubesCompoundShape shape, Transform trans,
			SweepPoint<CompoundShapeChild>[] axisToFill,
			List<CompoundShapeChild> aList,
			Comparator<SweepPoint<CompoundShapeChild>> comp, int startHash) {
		int i;
		
		final int size = aList.size();
		axisToFill = ensureSize(axisToFill, size);
		for (i = 0; i < size; i++) {
			CubesCompoundShapeChild child = (CubesCompoundShapeChild) aList.get(i);
			child.tmpChildIndex = i;
			
			shape.getAABB(i,
					trans, outOuterMin, outOuterMax, varSet);
			
			axisToFill[i] = getPoint(child, outOuterMin, outOuterMax, startHash + i);
		}
		
		Arrays.sort(axisToFill, 0, i, comp);
		return i;
	}

}
