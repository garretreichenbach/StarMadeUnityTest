package org.schema.game.client.view.cubes.shapes.orientcube.front;

import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.back.OriencubeBackBottom;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeFrontBottom")
public class OriencubeFrontBottom extends OrientCubeFront implements IconInterface {
	private final static Oriencube mirror = new OriencubeBackBottom();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.FRONT;
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
		return out;
	}
}
