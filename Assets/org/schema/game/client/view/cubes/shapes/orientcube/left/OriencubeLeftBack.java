package org.schema.game.client.view.cubes.shapes.orientcube.left;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.right.OriencubeRightBack;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeLeftBack")
public class OriencubeLeftBack extends OrientCubeLeft implements IconInterface {
	private final static Oriencube mirror = new OriencubeRightBack();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.LEFT;
	}

	;

	@Override
	public byte getOrientCubeSecondaryOrientation() {
		return Element.BACK;
	}

	@Override
	public Oriencube getMirrorAlgo() {
		return mirror;
	}

	@Override
	public Transform getSecondaryTransform(Transform out) {
		out.setIdentity();
		out.basis.rotY(FastMath.PI);
		return out;
	}
}
