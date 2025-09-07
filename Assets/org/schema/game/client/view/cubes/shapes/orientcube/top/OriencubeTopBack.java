package org.schema.game.client.view.cubes.shapes.orientcube.top;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.bottom.OriencubeBottomBack;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeTopBack")
public class OriencubeTopBack extends OrientCubeTop implements IconInterface {
	private final static Oriencube mirror = new OriencubeBottomBack();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.TOP;
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
