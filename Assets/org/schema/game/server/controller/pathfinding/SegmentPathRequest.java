package org.schema.game.server.controller.pathfinding;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.server.controller.SegmentPathCallback;
import org.schema.schine.graphicsengine.forms.BoundingBox;

public class SegmentPathRequest extends AbstractPathRequest {

	private final SegmentPathCallback callback;
	private Vector3i from = new Vector3i();
	private Vector3i to = new Vector3i();
	private Vector3i origin = new Vector3i();
	private BoundingBox roam = new BoundingBox();
	private Vector3i prefferedDir = new Vector3i(1, 0, 0);
	private boolean random;
	private SegmentController segmentController;
	private short type;
	private boolean active;

	public SegmentPathRequest(Vector3i from, Vector3i to, SegmentController controller, SegmentPathCallback callback) {
		this.from.set(from);
		this.to.set(to);
		this.callback = callback;
		this.type = (short) 0;
		this.active = false;
		this.segmentController = controller;
	}

	public SegmentPathRequest(SegmentPiece from, SegmentPiece to, SegmentPathCallback callback) {
		super();
		this.segmentController = from.getSegment().getSegmentController();
		this.from = from.getAbsolutePos(this.from);
		this.to = to.getAbsolutePos(this.to);
		this.callback = callback;
		this.type = from.getType();
		this.active = from.isActive();
	}

	public SegmentPathRequest(SegmentPiece from, Vector3i origin, BoundingBox roaming, Vector3i prefferedDir, SegmentPathCallback callback) {
		super();
		this.segmentController = from.getSegment().getSegmentController();
		this.from = from.getAbsolutePos(this.from);
		this.callback = callback;
		this.origin.set(origin);
		this.roam.set(roaming);
		this.prefferedDir.set(prefferedDir);
		this.random = true;
		this.type = from.getType();
		this.active = from.isActive();
	}

	@Override
	public SegmentController getSegmentController() {
		return segmentController;
	}

	@Override
	public Vector3i getFrom(Vector3i out) {
		out.set(from);
		return out;
	}

	@Override
	public Vector3i getTo(Vector3i out) {
		out.set(to);
		return out;
	}

	@Override
	public void refresh() {
		//		if(to != null){
		//			to.refresh();
		//		}
		//		from.refresh();
	}

	@Override
	public Vector3i randomOrigin() {
		return origin;
	}

	@Override
	public BoundingBox randomRoamBB() {
		return roam;
	}

	@Override
	public Vector3i randomPathPrefferedDir() {
		return prefferedDir;
	}

	@Override
	public boolean random() {
		return random;
	}

	public short getType() {
		return type;
	}

	public boolean isActive() {
		return active;
	}

	/**
	 * @return the callback
	 */
	public SegmentPathCallback getCallback() {
		return callback;
	}

}
