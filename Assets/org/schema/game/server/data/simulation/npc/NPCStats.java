package org.schema.game.server.data.simulation.npc;

import java.sql.SQLException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.GameServerState;

public class NPCStats {
	private int fleetCreatorNumber;
	
	private int shipCreatorNumber;
	
	private boolean fetchedStats = false;
	
	public void incShipNumber(GameServerState state, int factionId, Vector3i idSys) {
		synchronized(this){
			shipCreatorNumber++;
			updateSpawnDb(state, factionId,idSys);		
		}
	}
	public void incFleetNumber(GameServerState state, int factionId, Vector3i idSys) {
		synchronized(this){
			fleetCreatorNumber++;
			updateSpawnDb(state, factionId,idSys);		
		}
	}
	private void updateSpawnDb(GameServerState state, int factionId, Vector3i idSys) {
		try {
			state.getDatabaseIndex().getTableManager().getNpcStatTable().updateNPCSpawns(
					factionId, idSys.x, idSys.y, idSys.z, 
					shipCreatorNumber, fleetCreatorNumber);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	private void fetch(GameServerState state, int factionId, Vector3i idSys){
		if(!fetchedStats){
			try {
				//will only insert if not exists
				state.getDatabaseIndex().getTableManager().getNpcStatTable().insertNPCSpawns(
						factionId, idSys.x, idSys.y, idSys.z,  shipCreatorNumber, fleetCreatorNumber);
				
				int[] sp = state.getDatabaseIndex().getTableManager().getNpcStatTable().getNPCFleetAndEntitySpawns(
						factionId, idSys.x, idSys.y, idSys.z);
				fleetCreatorNumber = sp[0];
				shipCreatorNumber = sp[1];
			} catch (SQLException e) {
				e.printStackTrace();
			}
			fetchedStats = true;
		}
	}
	public int getFleetCreatorNumber(GameServerState state, int factionId, Vector3i idSys) {
		synchronized(this){
			fetch(state, factionId,idSys);
			return fleetCreatorNumber;
		}
	}
	public int getShipCreatorNumber(GameServerState state, int factionId, Vector3i idSys) {
		synchronized(this){
			fetch(state, factionId,idSys);
			return shipCreatorNumber;
		}
	}
}
