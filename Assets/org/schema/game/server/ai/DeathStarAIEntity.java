package org.schema.game.server.ai;

import org.schema.game.common.controller.TeamDeathStar;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.graphicsengine.core.Timer;

public class DeathStarAIEntity extends SegmentControllerAIEntity<TeamDeathStar> {

	/**
	 *
	 */
	

	public DeathStarAIEntity(String name, MachineProgram<?> program, TeamDeathStar s) {
		super(name, s);
	}

	@Override
	public void updateAIClient(Timer timer) {
	}

	@Override
	public void updateAIServer(Timer timer) {
	}

}
