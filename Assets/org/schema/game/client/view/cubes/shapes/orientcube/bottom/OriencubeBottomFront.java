package org.schema.game.client.view.cubes.shapes.orientcube.bottom;

import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.top.OriencubeTopFront;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeBottomFront")
public class OriencubeBottomFront extends OrientCubeBottom implements IconInterface {
	private final static Oriencube mirror = new OriencubeTopFront();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.BOTTOM;
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
