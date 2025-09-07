package org.schema.game.client.view.cubes.shapes.orientcube.top;

import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.bottom.OriencubeBottomFront;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeTopFront")
public class OriencubeTopFront extends OrientCubeTop implements IconInterface {
	private final static Oriencube mirror = new OriencubeBottomFront();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.TOP;
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
