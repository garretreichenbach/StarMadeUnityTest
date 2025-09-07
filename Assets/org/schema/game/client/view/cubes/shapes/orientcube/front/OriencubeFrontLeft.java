package org.schema.game.client.view.cubes.shapes.orientcube.front;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.back.OriencubeBackLeft;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeFrontLeft")
public class OriencubeFrontLeft extends OrientCubeFront implements IconInterface {
	private final static Oriencube mirror = new OriencubeBackLeft();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.FRONT;
	}

	;

	@Override
	public byte getOrientCubeSecondaryOrientation() {
		return Element.LEFT;
	}

	@Override
	public Oriencube getMirrorAlgo() {
		return mirror;
	}

	@Override
	public Transform getSecondaryTransform(Transform out) {
		out.setIdentity();
		out.basis.rotY(FastMath.HALF_PI);
		return out;
	}
}
