package org.schema.game.client.controller.element.world;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SegmentRequester {

	private static final Random rand = new Random();
	private final SegmentController segmentController;
	private final BoundingBox requestBox = new BoundingBox();
	private final Vector3f camSegHelper = new Vector3f();
	private final Vector3i posHelper = new Vector3i();
	private final Vector3f camPosLocal = new Vector3f();
	private final LongArrayList requestBuffer = new LongArrayList();
	private final LongArrayList requestBufferPers = new LongArrayList();
	private final Vector3i tmpPos = new Vector3i();
	private final ObjectArrayList<DrawableRemoteSegment> newlyAdded = new ObjectArrayList<DrawableRemoteSegment>();
	private final ObjectArrayList<DrawableRemoteSegment> currentAdded = new ObjectArrayList<DrawableRemoteSegment>();
	private final BoundingBox self = new BoundingBox();
	private final BoundingBox inter = new BoundingBox();
	private final Vector3f cTmp = new Vector3f();
	Vector3i posOutTmp = new Vector3i();
	Vector3i posOutTmp2 = new Vector3i();
	Vector3i posTmp = new Vector3i();
	Vector3i posTmp2 = new Vector3i();

	public SegmentRequester(ClientSegmentProvider segmentProvider) {
		super();
		this.segmentController = segmentProvider.getSegmentController();
	}

	/**
	 * @return the segmentController
	 */
	public SegmentController getSegmentController() {
		return segmentController;
	}
	private final Vector3i assertTmp = new Vector3i();
	public void onAddedSegment(Segment s) {
		newlyAdded.add((DrawableRemoteSegment) s);
//		System.err.println("NNNAAA "+newlyAdded.size()+"; "+currentAdded.size());
	}
	public GameClientState getState(){
		return (GameClientState) segmentController.getState();
	}
	
	

}
