package org.schema.game.common.controller.generator;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.RequestData;

public class ClientCreatorThread extends CreatorThread {

	public ClientCreatorThread(SegmentController c) {
		super(c);

	}

	@Override
	public int isConcurrent() {
		return LOCAL_CONCURRENT;
	}

	@Override
	public int loadFromDatabase(Segment ws) {
		return -1;
	}

	@Override
	public void onNoExistingSegmentFound(Segment ws, RequestData requestData) {
	}

	@Override
	public boolean predictEmpty(Vector3i pos) {
		return false;
	}

}
