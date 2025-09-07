package org.schema.game.client.view.cubes.shapes.orientcube.top;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.bottom.OriencubeBottomRight;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeTopRight")
public class OriencubeTopRight extends OrientCubeTop implements IconInterface {
	private final static Oriencube mirror = new OriencubeBottomRight();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.TOP;
	}

	;

	@Override
	public byte getOrientCubeSecondaryOrientation() {
		return Element.RIGHT;
	}

	@Override
	public Oriencube getMirrorAlgo() {
		return mirror;
	}

	@Override
	public Transform getSecondaryTransform(Transform out) {
		out.setIdentity();
		out.basis.rotY(FastMath.HALF_PI * 3);
		return out;
	}
}
