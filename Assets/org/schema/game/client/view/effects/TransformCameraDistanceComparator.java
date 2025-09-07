package org.schema.game.client.view.effects;

import java.util.Comparator;

import org.schema.game.common.controller.elements.BeamState;

public class TransformCameraDistanceComparator implements Comparator<BeamState> {

	boolean end;

	public TransformCameraDistanceComparator(boolean b) {
		end = b;
	}

	@Override
	public int compare(BeamState a, BeamState b) {
		if (end) {
			return Float.compare(b.camDistEnd, a.camDistEnd);
		} else {
			return Float.compare(b.camDistStart, a.camDistStart);
		}
	}

}
