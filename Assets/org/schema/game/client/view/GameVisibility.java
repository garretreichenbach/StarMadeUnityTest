package org.schema.game.client.view;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SegmentDataManager;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Visability;

public class GameVisibility extends Visability {
	public static Vector3f visibility;
	public static float vislen;

	@Override
	public Vector3f getVisability() {
		return visibility;
	}

	@Override
	public float getVisLen() {
		return vislen;
	}

	@Override
	public void recalculateVisibility() {
		visibility = new Vector3f(
				getVisibleDistance() * SegmentData.SEG,
				getVisibleDistance() * SegmentData.SEG,
				getVisibleDistance() * SegmentData.SEG);
		vislen = visibility.length();
		SegmentDataManager.makeIterations();
	}
}
