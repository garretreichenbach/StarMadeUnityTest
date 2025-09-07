package org.schema.game.server.controller.pathfinding;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.forms.BoundingBox;

public abstract class AbstractPathRequest {
	public abstract SegmentController getSegmentController();

	public abstract Vector3i getFrom(Vector3i out);

	public abstract Vector3i getTo(Vector3i out);

	public abstract void refresh();

	public abstract Vector3i randomOrigin();

	public abstract BoundingBox randomRoamBB();

	public abstract Vector3i randomPathPrefferedDir();

	public abstract boolean random();
}
