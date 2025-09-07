package org.schema.game.client.view.cubes.shapes.orientcube.right;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.left.OriencubeLeftBack;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeRightBack")
public class OriencubeRightBack extends OrientCubeRight implements IconInterface {
	private final static Oriencube mirror = new OriencubeLeftBack();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.RIGHT;
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
