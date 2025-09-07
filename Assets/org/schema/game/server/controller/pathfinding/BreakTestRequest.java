package org.schema.game.server.controller.pathfinding;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.forms.BoundingBox;

public class BreakTestRequest extends AbstractPathRequest {

	private static Vector3i to = new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
	private SegmentPiece from;

	public BreakTestRequest(SegmentPiece from) {
		super();
		this.from = from;
	}

	@Override
	public SegmentController getSegmentController() {
		return from.getSegment().getSegmentController();
	}

	@Override
	public Vector3i getFrom(Vector3i out) {
		return from.getAbsolutePos(out);
	}

	@Override
	public Vector3i getTo(Vector3i out) {
		out.set(to);
		return to;
	}

	@Override
	public void refresh() {
		from.refresh();
	}

	@Override
	public Vector3i randomOrigin() {
		assert (false);
		return null;
	}

	@Override
	public BoundingBox randomRoamBB() {
		assert (false);
		return null;
	}

	@Override
	public Vector3i randomPathPrefferedDir() {
		assert (false);
		return null;
	}

	@Override
	public boolean random() {
		return false;
	}

	public int getType() {
		return from.getType();
	}

}
