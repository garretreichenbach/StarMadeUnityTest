package org.schema.game.server.controller.pathfinding;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.pathfinding.IslandCalculator;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;

public class SegmentBreaker extends AbstractPathFindingHandler<IslandCalculator, BreakTestRequest> {

	public SegmentBreaker(GameServerState state) {
		super(state, "SegmentBR", new IslandCalculator());
	}

	@Override
	protected void init() {
		if (ServerConfig.ENABLE_BREAK_OFF.isOn()) {
			start();
		}
	}

	@Override
	protected boolean canProcess(BreakTestRequest cr) {
		return cr.getType() != 0;
	}

	@Override
	protected void afterCalculate(boolean foundCore, BreakTestRequest cr) {
		if (!foundCore) {
			System.err.println("WAY TO CORE NOT FOUND: BREAKING UP");

			if (getIc().breakUp(segmentController)) {
				enqueueSynchedResponse();
			}
		}
	}

	@Override
	public void handleReturn() {
		getIc().send((SendableSegmentController) segmentController);
	}
}
