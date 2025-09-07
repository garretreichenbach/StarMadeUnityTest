package org.schema.game.common.controller.generator;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.world.factory.ships.WorldCreatorTurretFactory;

public class StationTurretCreatorThread extends CreatorThread {

	private WorldCreatorTurretFactory creator;

	public StationTurretCreatorThread(SegmentController world, int difficulty) {
		super(world);
		creator = new WorldCreatorTurretFactory(world.getSeed(), difficulty);
	}

	@Override
	public int isConcurrent() {
		return LOCAL_CONCURRENT;
	}

	@Override
	public int loadFromDatabase(Segment ws) {
		return 0;
	}

	@Override
	public void onNoExistingSegmentFound(Segment ws, RequestData requestData) {
		creator.createWorld(ws.getSegmentController(), ws, requestData);
	}

	@Override
	public boolean predictEmpty(Vector3i pos) {
		return false;
	}
}
