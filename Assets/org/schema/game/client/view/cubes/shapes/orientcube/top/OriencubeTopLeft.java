package org.schema.game.client.view.cubes.shapes.orientcube.top;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.bottom.OriencubeBottomLeft;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeTopLeft")
public class OriencubeTopLeft extends OrientCubeTop implements IconInterface {
	private final static Oriencube mirror = new OriencubeBottomLeft();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.TOP;
	}

	;

	@Override
	public byte getOrientCubeSecondaryOrientation() {
		return Element.LEFT;
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
