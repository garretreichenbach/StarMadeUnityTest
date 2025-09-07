package org.schema.game.server.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.catalog.CatalogWavePermission;
import org.schema.game.server.controller.BluePrintController;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class ShipSpawnWave {
	public static final int MAX_LEVEL = 10;
	public BluePrintController bluePrintController;
	public Vector3i sectorId;
	private int waveFaction;
	private int level;
	private int timeInSecs;
	private long timeInitiatedInMS;
	private ArrayList<CatalogPermission> printsToSpawn;

	public ShipSpawnWave(int waveFaction, int level, BluePrintController bluePrintController, int timeinSecs, Vector3i sectorId) {
		super();
		this.waveFaction = waveFaction;
		this.level = level;
		this.sectorId = sectorId;
		this.timeInSecs = timeinSecs;
		this.bluePrintController = bluePrintController;
		this.timeInitiatedInMS = System.currentTimeMillis();
	}

	public void createWave(GameServerState state, int shipCountToSpawn) {
		Collection<CatalogPermission> catalog = state.getCatalogManager().getCatalog();

		int closestDiffOverall = -1;
		int closestOverall = Integer.MAX_VALUE;
		Object2ObjectOpenHashMap<CatalogPermission, CatalogWavePermission> toPossiblySpawn = new Object2ObjectOpenHashMap<CatalogPermission, CatalogWavePermission>();
		
		for (CatalogPermission c : catalog) {
			CatalogWavePermission clWp = null;
			int closest = Integer.MAX_VALUE;
			for (CatalogWavePermission wp : c.wavePermissions) {
				
				if(wp.factionId == waveFaction){
					
					int d = Math.abs(wp.difficulty - level);
					if(d < closest){
						System.err.println("[AI] closest difficulty now: "+wp.difficulty+"; distance "+d+"; clsoest bef: "+closest+"; level asked: "+level);
						
						if(d < closestOverall){
							closestDiffOverall = wp.difficulty;
							closestOverall = d;
						}
						clWp = wp;
						closest = d;
					}
					
				}
			}
			if(clWp != null){
				toPossiblySpawn.put(c, clWp);
			}
		}
		ObjectIterator<Entry<CatalogPermission, CatalogWavePermission>> iterator = toPossiblySpawn.entrySet().iterator();
		ArrayList<CatalogPermission> c = new ArrayList<CatalogPermission>();
		while(iterator.hasNext()){
			Entry<CatalogPermission, CatalogWavePermission> next = iterator.next();
			
			if(next.getValue().difficulty != closestDiffOverall){
				iterator.remove();
			}else{
				for(int i = 0; i < next.getValue().amount; i++){
					c.add(next.getKey());
				}
			}
		}
		if(toPossiblySpawn.size() > 0){
			printsToSpawn = c;
		}else{
			createWaveOld(state, shipCountToSpawn);
		}
		
	}
	public void createWaveOld(GameServerState state, int shipCountToSpawn) {
		state.getController();
		Collection<CatalogPermission> catalog = state.getCatalogManager().getCatalog();

		
		ArrayList<CatalogPermission> toPossiblySpawn = new ArrayList<CatalogPermission>();
		for (CatalogPermission c : catalog) {

			if (c.enemyUsable()) {
				toPossiblySpawn.add(c);
			}

		}
		if (toPossiblySpawn.isEmpty()) {
			System.err.println("[WAVE] Server will not spawn any waves, the catalog is empty");
			return;
		}
		Collections.sort(toPossiblySpawn, (o1, o2) -> (int) (o1.price - o2.price));
		
		
		
		float d = (float) toPossiblySpawn.size() / (float) MAX_LEVEL;
		int toIndex = (int) Math.min(toPossiblySpawn.size() - 1, Math.ceil(d * level));
		printsToSpawn = new ArrayList<CatalogPermission>();

		for (int i = 0; i < shipCountToSpawn; i++) {
			int index = Math.min(toPossiblySpawn.size() - 1, Math.max(0, toIndex - 2 + i));

			printsToSpawn.add(toPossiblySpawn.get(index));
		}
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}


	/**
	 * @return the printsToSpawn
	 */
	public ArrayList<CatalogPermission> getPrintsToSpawn() {
		return printsToSpawn;
	}

	/**
	 * @param printsToSpawn the printsToSpawn to set
	 */
	public void setPrintsToSpawn(ArrayList<CatalogPermission> printsToSpawn) {
		this.printsToSpawn = printsToSpawn;
	}

	/**
	 * @return the timeInitiatedInMS
	 */
	public long getTimeInitiatedInMS() {
		return timeInitiatedInMS;
	}

	/**
	 * @param timeInitiatedInMS the timeInitiatedInMS to set
	 */
	public void setTimeInitiatedInMS(long timeInitiatedInMS) {
		this.timeInitiatedInMS = timeInitiatedInMS;
	}

	/**
	 * @return the timeInSecs
	 */
	public int getTimeInSecs() {
		return timeInSecs;
	}

	/**
	 * @param timeInSecs the timeInSecs to set
	 */
	public void setTimeInSecs(int time) {
		this.timeInSecs = time;
	}

	/**
	 * @return the waveTeam
	 */
	public int getWaveTeam() {
		return waveFaction;
	}


	/**
	 * @param waveTeam the waveTeam to set
	 */
	public void setWaveFaction(int waveTeam) {
		this.waveFaction = waveTeam;
	}

}
