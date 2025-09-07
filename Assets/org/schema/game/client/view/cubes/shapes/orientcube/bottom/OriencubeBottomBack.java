package org.schema.game.client.view.cubes.shapes.orientcube.bottom;

import org.schema.common.FastMath;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.top.OriencubeTopBack;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.Transform;

@BlockShape(name = "OriencubeBottomBack")
public class OriencubeBottomBack extends OrientCubeBottom implements IconInterface {
	private final static Oriencube mirror = new OriencubeTopBack();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.BOTTOM;
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
