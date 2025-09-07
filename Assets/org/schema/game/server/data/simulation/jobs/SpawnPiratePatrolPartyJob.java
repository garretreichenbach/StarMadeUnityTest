package org.schema.game.server.data.simulation.jobs;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.simulation.SimulationManager;

public class SpawnPiratePatrolPartyJob extends SimulationJob {

	private final Vector3i from;
	private final Vector3i to;
	private int count;

	public SpawnPiratePatrolPartyJob(Vector3i from, Vector3i to, int count) {
		super();
		this.count = count;
		this.from = from;
		this.to = to;
	}

	@Override
	public int _getFaction() {
		return FactionManager.PIRATES_ID;
	}

	@Override
	public void execute(SimulationManager man) {
		man.createRandomPiratePatrolGroup(from, to, count);
	}

	@Override
	public Vector3i getStartLocation() {
		return from;
	}

}
