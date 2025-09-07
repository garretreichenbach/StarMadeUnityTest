package org.schema.game.client.view.cubes.shapes.orientcube.left;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.right.OriencubeRightBottom;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeLeftBottom")
public class OriencubeLeftBottom extends OrientCubeLeft implements IconInterface {
	private final static Oriencube mirror = new OriencubeRightBottom();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.LEFT;
	}

	;

	@Override
	public byte getOrientCubeSecondaryOrientation() {
		return Element.BOTTOM;
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
