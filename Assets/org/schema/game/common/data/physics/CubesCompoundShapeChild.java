package org.schema.game.common.data.physics;

import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.linearmath.Transform;

public class CubesCompoundShapeChild extends CompoundShapeChild {

	public final Transform transformPivoted = new Transform();
	public int tmpChildIndex;
	@Override
	public String toString() {
		return super.toString()+"("+childShape+")";
	}

}
