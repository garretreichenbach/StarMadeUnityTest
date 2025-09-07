package org.schema.game.server.data.blueprint;

import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.GameServerState;

public abstract class SegmentControllerSpawnCallbackDirect extends SegmentControllerSpawnCallback{

	private final GameServerState state;

	public SegmentControllerSpawnCallbackDirect(GameServerState state) {
		super();
		this.state = state;
	}

	public SegmentControllerSpawnCallbackDirect(GameServerState state, Vector3i sectorPos) throws IOException {
		super(state, sectorPos);
		this.state = state;
	}

	@Override
	public void onSpawn(SegmentController c) {
		System.err.println("[SPAWN] "+c.getState()+" ADDING synchronized object queued: "+c);
		state.getController().getSynchController().addNewSynchronizedObjectQueued(c);
	}
	public void onNullSector(SegmentController c) {
		System.err.println("[SPAWN] "+c.getState()+" NOT ADDING synchronized object queued since it has no sector: "+c);
	}
}
