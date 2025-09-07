package org.schema.game.client.view.cubes.shapes.orientcube.right;

import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.left.OriencubeLeftFront;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeRightFront")
public class OriencubeRightFront extends OrientCubeRight implements IconInterface {
	private final static Oriencube mirror = new OriencubeLeftFront();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.RIGHT;
	}

	;

	@Override
	public byte getOrientCubeSecondaryOrientation() {
		return Element.FRONT;
	}

	@Override
	public Oriencube getMirrorAlgo() {
		return mirror;
	}

	@Override
	public Transform getSecondaryTransform(Transform out) {
		out.setIdentity();
		return out;
	}
}
