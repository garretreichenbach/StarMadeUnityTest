package org.schema.game.client.view.cubes.shapes.orientcube.front;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.back.OriencubeBackTop;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeFrontTop")
public class OriencubeFrontTop extends OrientCubeFront implements IconInterface {
	private final static Oriencube mirror = new OriencubeBackTop();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.FRONT;
	}

	;

	@Override
	public byte getOrientCubeSecondaryOrientation() {
		return Element.TOP;
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
