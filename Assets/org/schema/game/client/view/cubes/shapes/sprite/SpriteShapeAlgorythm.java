package org.schema.game.client.view.cubes.shapes.sprite;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;

public abstract class SpriteShapeAlgorythm extends BlockShapeAlgorithm {
	@Override
	public boolean hasValidShape() {
		return false;
	}

	@Override
	protected Vector3i[] getSideByNormal(int sideId, int normal) {
		return super.getSideByNormal(sideId%6, sideId%6);
	}
	@Override
	protected int getAngledSideLightRepresentitive(int sideId, int normal) {
		return sideId%6;
	}
	@Override
	public boolean isAngled(int sideId) {
		return true;
	}
	public abstract byte getPrimaryOrientation();
}
