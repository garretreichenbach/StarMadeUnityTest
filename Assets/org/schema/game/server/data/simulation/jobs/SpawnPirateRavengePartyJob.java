package org.schema.game.server.data.simulation.jobs;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.simulation.SimulationManager;

public class SpawnPirateRavengePartyJob extends SimulationJob {

	private final Vector3i from;
	private int count;

	public SpawnPirateRavengePartyJob(Vector3i from, int count) {
		super();
		this.from = from;
		this.count = count;
	}

	@Override
	public int _getFaction() {
		return FactionManager.PIRATES_ID;
	}

	@Override
	public void execute(SimulationManager man) {
		man.createRandomPirateGroup(from, count);
	}

	@Override
	public Vector3i getStartLocation() {
		return this.from;
	}

}
