package org.schema.game.client.view.cubes.shapes.orientcube.back;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;

import com.bulletphysics.linearmath.Transform;

public abstract class OrientCubeBack extends Oriencube {
	@Override
	public Transform getPrimaryTransform(Vector3f blockPosLocal, int move, Transform out) {
		out.setIdentity();
		out.basis.rotX(-FastMath.HALF_PI);
		out.origin.set(blockPosLocal);
		out.origin.z -= move;

		return out;
	}

}
