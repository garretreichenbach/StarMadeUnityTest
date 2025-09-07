package org.schema.game.client.view.cubes.shapes.orientcube.top;

import javax.vecmath.Vector3f;

import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;

import com.bulletphysics.linearmath.Transform;

public abstract class OrientCubeTop extends Oriencube {
	@Override
	public Transform getPrimaryTransform(Vector3f blockPosLocal, int move, Transform out) {
		out.setIdentity();
		out.origin.set(blockPosLocal);
		out.origin.y += move;

		return out;
	}

}
