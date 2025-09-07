package org.schema.game.client.controller.tutorial.states;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

public class TutorialMarker {
	public final Vector3i where;
	public final String markerText;
	public SegmentController context;
	public Vector3f absolute;

	public TutorialMarker(Vector3i where, String markerText) {
		super();
		this.where = where;
		this.markerText = markerText;
	}

}
