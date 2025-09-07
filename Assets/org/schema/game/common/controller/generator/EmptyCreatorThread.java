package org.schema.game.common.controller.generator;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.RequestData;

public class EmptyCreatorThread extends CreatorThread {

	public EmptyCreatorThread(SegmentController world) {
		super(world);
		//		System.err.println("[EMPTY-CREATOR-THREAD] starting thread");
	}

	@Override
	public int isConcurrent() {
		return FULL_CONCURRENT;
	}

	@Override
	public int loadFromDatabase(Segment ws) {
		//TODO
		return 0;
	}

	@Override
	public void onNoExistingSegmentFound(Segment ws, RequestData requestData) {
		//no autocreate needed
	}

	@Override
	public boolean predictEmpty(Vector3i pos) {
		return true;
	}

}
