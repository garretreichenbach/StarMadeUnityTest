package org.schema.game.client.view.cubes.shapes.orientcube.bottom;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;

import com.bulletphysics.linearmath.Transform;

public abstract class OrientCubeBottom extends Oriencube {
	@Override
	public Transform getPrimaryTransform(Vector3f blockPosLocal, int move, Transform out) {
		out.basis.rotZ(-FastMath.PI);
		out.origin.set(blockPosLocal);
		out.origin.y -= move;

		return out;
	}

}
