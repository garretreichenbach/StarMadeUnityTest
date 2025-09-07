package org.schema.game.client.view.cubes.shapes.orientcube.left;

import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.right.OriencubeRightFront;
import org.schema.game.common.data.element.Element;

@BlockShape(name = "OriencubeLeftFront")
public class OriencubeLeftFront extends OrientCubeLeft implements IconInterface {
	private final static Oriencube mirror = new OriencubeRightFront();

	;

	@Override
	public byte getOrientCubePrimaryOrientation() {
		return Element.LEFT;
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
}
