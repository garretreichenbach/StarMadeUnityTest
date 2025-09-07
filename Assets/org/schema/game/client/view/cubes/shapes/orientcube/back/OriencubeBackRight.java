package org.schema.game.client.view.cubes.shapes.orientcube.back;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.front.OriencubeFrontRight;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeBackRight")
public class OriencubeBackRight extends OrientCubeBack implements IconInterface {
	private final static Oriencube mirror = new OriencubeFrontRight();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.BACK;
	}

	;

	@Override
	public byte getOrientCubeSecondaryOrientation() {
		return Element.RIGHT;
	}

	@Override
	public Oriencube getMirrorAlgo() {
		return mirror;
	}

	@Override
	public Transform getSecondaryTransform(Transform out) {
		out.setIdentity();
		out.basis.rotY(FastMath.HALF_PI * 3);
		return out;
	}
}
