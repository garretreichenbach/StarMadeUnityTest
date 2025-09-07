package org.schema.game.server.controller.world.factory;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;

public class WorldCreatorEmptySpaceStationFactory extends WorldCreatorFactory {
	public static int HEIGHT = 15;
	Vector3i p = new Vector3i();
	public WorldCreatorEmptySpaceStationFactory(long seed) {
	}

	private void createFromCorner(Segment w, SegmentController world) throws SegmentDataWriteException {
		byte start = 0;
		byte end = SegmentData.SEG;
		if (w.pos.equals(0, 0, 0)) {
			for (byte z = start; z < end; z++) {
				for (byte x = start; x < end; x++) {
					placeSolid(x, 0, z, w, ElementKeyMap.HULL_COLOR_RED_ID);
				}
			}
		}
		world.getSegmentBuffer().updateBB(w);
	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {

		try {
			createFromCorner(w, world);
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean predictEmpty() {
		return false;
	}

}
