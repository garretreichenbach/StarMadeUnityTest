package org.schema.game.server.data.blueprint;

import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Sector;
import org.schema.game.server.data.GameServerState;

public abstract class SegmentControllerSpawnCallback {
	public final Sector sector;
	public SegmentControllerSpawnCallback(GameServerState state, Vector3i sectorPos) throws IOException{
		if (sectorPos != null) {
			this.sector = state.getUniverse().getSector(sectorPos);
		}else{
			this.sector = null;
		}
	}
	public SegmentControllerSpawnCallback(){
		this.sector = null;
	}
	
	
	public abstract void onSpawn(SegmentController c);
	public void onNullSector(SegmentController c) {
	}
	public abstract void onNoDocker();
}
