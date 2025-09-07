package org.schema.game.common.data.player;

import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.database.FogOfWarReceiver;
import org.schema.game.common.data.world.GalaxyManager;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FogOfWarController {

	private final FogOfWarReceiver ownRec;
	private final Object2BooleanOpenHashMap<Vector3i> cachedFogOfWar = new Object2BooleanOpenHashMap<Vector3i>();
	private final Vector3i tmpSystem = new Vector3i();
	private final Vector3i tmpSystemFrom = new Vector3i();
	private final Vector3i tmpSystemTo = new Vector3i();
	private final Vector3i currentSystem = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	private Object2ObjectOpenHashMap<Vector3i, Set<Vector3i>> scannedSys = new Object2ObjectOpenHashMap<Vector3i, Set<Vector3i>>(); 
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	public FogOfWarController(FogOfWarReceiver r) {
		this.ownRec = r;
	}
	
	public boolean isOnServer(){
		return ownRec.getState() instanceof ServerStateInterface;
	}
	private boolean isVisibleSector(Vector3i sec) {
		return isOnServer() ? isVisibleSectorServer(sec) : isVisibleSectorClient(sec);
	}
	public boolean isVisibleSystem(Vector3i sys) {
		return isOnServer() ? isVisibleSystemServer(sys) : isVisibleSystemClient(sys);
	}
	public boolean isVisibleSectorClient(Vector3i sectorPos) {
		assert(!isOnServer());
		Vector3i sys = VoidSystem.getContainingSystem(sectorPos, tmpSystem);
		
		return isVisibleSystemClient(sys);
	}
	public boolean isVisibleSystemClient(Vector3i sys) {
		return ((GameClientState)ownRec.getState()).getController().
				getClientChannel().getGalaxyManagerClient().isSystemVisiblyClient(sys);
	}
	public boolean isVisibleSectorServer(Vector3i sectorPos) {
		assert(isOnServer());
		Vector3i sys = VoidSystem.getContainingSystem(sectorPos, tmpSystem);
		
		return isVisibleSystemServer(sys);
	}
	public boolean isVisibleSystemServer(Vector3i sys) {
		assert(isOnServer());
		try{
			lock.readLock().lock();
			if(!cachedFogOfWar.containsKey(sys)){
				lock.readLock().unlock();
				updateFogOfWar(sys);
				lock.readLock().lock();
			}
			return cachedFogOfWar.getBoolean(sys);
		}finally{
			lock.readLock().unlock();
		}
	}
	public void updateFogOfWar(Vector3i sys){
		assert(isOnServer());
		try {
			lock.writeLock().lock();
			boolean visibileSystem = ((GameServerState)ownRec.getState()).getDatabaseIndex().getTableManager().getTradeNodeTable().isVisibileSystem(ownRec, sys.x, sys.y, sys.z);
			cachedFogOfWar.put(sys, visibileSystem);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			lock.writeLock().unlock();
		}
	}
	public void scan(Vector3i sysTo) {
		assert(isOnServer());
		boolean ok = false;
		try {
			System.err.println("[SERVER][FOW] "+ownRec+" initializing scan "+sysTo);
			lock.writeLock().lock();
			
			((GameServerState)ownRec.getState()).getDatabaseIndex().getTableManager().getVisibilityTable().updateVisibileSystem(ownRec, sysTo.x, sysTo.y, sysTo.z, System.currentTimeMillis());
			cachedFogOfWar.put(new Vector3i(sysTo), true);
			ok = true;
		} catch (SQLException e) {
			e.printStackTrace();
			((GameServerState)ownRec.getState()).getController().broadcastMessageAdmin(Lng.astr("SQL error (FogOfWar Insert):\n%s: %s",e.getClass().getSimpleName(), e.getMessage()), ServerMessage.MESSAGE_TYPE_ERROR);
		}finally{
			lock.writeLock().unlock();
			if(ok){
				ownRec.sendFowResetToClient(new Vector3i(sysTo));
				System.err.println("[SERVER][FOW] "+ownRec+" SUCCESSFULLY scanned system: "+sysTo);
			}else{
				System.err.println("[SERVER][FOW] "+ownRec+" FAILED scanned system: "+sysTo);
			}
		}
	}
	public void merge(FogOfWarReceiver from) {
		assert(isOnServer());
		try {
			lock.writeLock().lock();
			((GameServerState)ownRec.getState()).getDatabaseIndex().getTableManager().getVisibilityTable().mergeVisibility(from, ownRec);
			for(Entry<Vector3i, Boolean> s : from.getFogOfWar().cachedFogOfWar.entrySet()){
				if(s.getValue()){
					cachedFogOfWar.put(s.getKey(), true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			((GameServerState)ownRec.getState()).getController().broadcastMessageAdmin(Lng.astr("SQL error (FogOfWar Merge):\n%s: %s",e.getClass().getSimpleName(), e.getMessage()), ServerMessage.MESSAGE_TYPE_ERROR);
		}finally{
			lock.writeLock().unlock();
		}
	}
	public void onSectorSwitch(Vector3i from, Vector3i to){
		Vector3i sysFrom = VoidSystem.getContainingSystem(from, tmpSystemFrom);
		Vector3i sysTo = VoidSystem.getContainingSystem(to, tmpSystemTo);
		if(!isVisibleSector(to)){
			if(!scannedSys.containsKey(sysTo)){
				scannedSys.put(new Vector3i(sysTo), new ObjectOpenHashSet<Vector3i>());
			}
			scannedSys.get(sysTo).add(new Vector3i(to));
			
			
			if(scannedSys.get(sysTo).size() >= getSectorsVisitedForScan()){
				if(isOnServer()){
					scan(sysTo);
				}else{
					if(((GameClientState)ownRec.getState()).getPlayer().getFogOfWar() == this){
						//the fow is owned by this client (either player or its faction)
						//reset client visibility for this system so it's re-requested
						GalaxyManager galaxyManagerClient = ((GameClientState)ownRec.getState()).getController().getClientChannel().getGalaxyManagerClient();
						galaxyManagerClient.resetClientVisibilitySystem(sysTo);
						
						((GameClientState)ownRec.getState()).getController().popupGameTextMessage(Lng.str("Manual scan of system %s completed!", sysTo.toStringPure()), 0);
					}
				}
				scannedSys.remove(sysTo);
			}
		}else{
			scannedSys.remove(sysTo);
		}
	}
	

	public float scannedPercent(Vector3i sys){
		return isVisibleSystemServer(sys) ? 1f : getScannedCountForSystem(sys) / (float)getSectorsVisitedForScan();
	}
	
	private int getSectorsVisitedForScan() {
		return ((GameStateInterface)ownRec.getState()).getGameState().getSectorsToExploreForSystemScan();
	}

	public int getScannedCountForSystem(Vector3i sys) {
		
		if(scannedSys.containsKey(sys)){
			return scannedSys.get(sys).size();
		}
		return 0;
	}
}
