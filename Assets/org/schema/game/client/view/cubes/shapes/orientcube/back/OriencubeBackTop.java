package org.schema.game.client.view.cubes.shapes.orientcube.back;

import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.front.OriencubeFrontTop;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeBackTop")
public class OriencubeBackTop extends OrientCubeBack implements IconInterface {
	private final static Oriencube mirror = new OriencubeFrontTop();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.BACK;
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

		return out;
	}
}
