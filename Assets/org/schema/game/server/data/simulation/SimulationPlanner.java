package org.schema.game.server.data.simulation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation.SpaceStationType;
import org.schema.game.common.controller.database.SimDatabaseEntry;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.jobs.SpawnPiratePatrolPartyJob;
import org.schema.game.server.data.simulation.jobs.SpawnPirateRavengePartyJob;
import org.schema.game.server.data.simulation.jobs.SpawnTradingPartyJob;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerState;

public class SimulationPlanner extends Thread {

	private final SimulationManager manager;
	private final GameServerState state;

	private final Random random;
	private final ArrayList<PlayerState> players = new ArrayList<PlayerState>();
	private boolean shutdown;

	public SimulationPlanner(SimulationManager manager) {
		super("SimPlanner");
		this.setDaemon(true);
		this.manager = manager;
		this.state = manager.getState();
		this.random = new Random();
	}

	private void debugMessage(String message) {
		if (ServerConfig.DEBUG_FSM_STATE.isOn()) {
			state.getController().broadcastMessage(new Object[]{message}, ServerMessage.MESSAGE_TYPE_INFO);
		}
	}

	public PlayerState getRandomPlayer() {
		PlayerState rdmPlayer;
		synchronized (players) {
			if (players.isEmpty()) {
				return null;
			}
			rdmPlayer = players.get(random.nextInt(players.size()));
		}
		return rdmPlayer;
	}

	public void playerAdded(PlayerState playerState) {
		synchronized (players) {
			players.add(playerState);
		}
	}

	public void playerRemoved(PlayerState playerState) {
		synchronized (players) {
			players.remove(playerState);
		}
	}

	@Override
	public void run() {

		while (!ServerState.isShutdown() && !shutdown) {
			try {
				Thread.sleep(ServerConfig.SIMULATION_SPAWN_DELAY.getInt() * 1000);
			} catch (InterruptedException e1) {
				System.err.println("[SIMPLANNER] Sleep has been interrupted");
				if(shutdown){
					return;
				}
			}
			if(EngineSettings.SECRET.getString().toLowerCase(Locale.ENGLISH).contains("nomobs")){
				continue;
			}
			int playerCountUnsave = players.size();
			if (ServerConfig.ENABLE_SIMULATION.isOn() && playerCountUnsave > 0) {

				try {

					spawnPlanning();
					npcSystemStep();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
	public void shutdown() {
		this.shutdown = true;
		interrupt();
	}
	private void npcSystemStep(){
		
	}
	private void spawnPlanning() throws SQLException {
		PlayerState randomPlayer = getRandomPlayer();
		if (randomPlayer == null) {
			//no players in game
			return;
		}
		Vector3i sec = randomPlayer.getCurrentSector();

		Sector sector = state.getUniverse().getSector(randomPlayer.getCurrentSectorId());
		if (sector == null) {
			return;
		}
		float fac = 0.3f;
		boolean safeZone = false;

		int range = 4;

		Vector3i lastShop = null;

		Vector3i start = new Vector3i(sec.x - range, sec.y - range, sec.z - range);
		Vector3i end = new Vector3i(sec.x + range, sec.y + range, sec.z + range);

		System.err.println("[SIMULATION] checking sectors to plan activity.... " + start + " to " + end);

		int[] types = new int[]{
				EntityType.SHOP.dbTypeId,
				EntityType.SPACE_STATION.dbTypeId,
				//				DatabaseEntry.TYPE_PLANET
		};
		long t = System.currentTimeMillis();
		List<SimDatabaseEntry> bySectorRange = state.getDatabaseIndex().getTableManager().getEntityTable().getBySectorRangeSim(start, end, types, 0);
		long dbTime = System.currentTimeMillis() - t;
		if (dbTime > 500) {
			System.err.println("[SIMULATION] (simthread) WARNING requesting sectors to plan simulation took long: " + dbTime + "ms; requested all sectors from  " + start + " to " + end + "; retrieved " + bySectorRange.size() + " entities");
		}

		int inJuristiction = 0;
		for (int i = 0; i < bySectorRange.size(); i++) {
			SimDatabaseEntry entry = bySectorRange.get(i);
			if (entry.type == EntityType.SPACE_STATION.dbTypeId && entry.faction > 0) {
				inJuristiction = entry.faction;
				break;
			}
			if (entry.type == EntityType.SHOP.dbTypeId && entry.sectorPos.equals(sec)) {
				safeZone = true;
				break;
			}
		}
		debugMessage("In safe zone: " + safeZone + "\nread entities: " + bySectorRange.size() + "\nfrom " + start + "\nto " + end + "\nQUERY: " + dbTime + "ms");
		for (int i = 0; i < bySectorRange.size(); i++) {
			SimDatabaseEntry entry = bySectorRange.get(i);
			if (!state.getUniverse().isSectorLoaded(entry.sectorPos)) {
				//only spawn in unloaded sectors

				if (!safeZone && random.nextInt((int)Math.ceil(25f * fac)) == 0) {
					if (entry.type == EntityType.SPACE_STATION.dbTypeId && entry.creatorID == SpaceStationType.PIRATE.ordinal()) {
						//found pirate base
						debugMessage("Spawning Pirate Scan\nBase: " + entry.sectorPos);
						manager.addJob(new SpawnPirateRavengePartyJob(new Vector3i(entry.sectorPos), random.nextInt(5) + 1));
					}
				}
				if (random.nextInt((int)Math.ceil(19f * fac)) == 0) {
					if (entry.type == EntityType.SHOP.dbTypeId) {
						if (lastShop == null) {
							lastShop = new Vector3i(entry.sectorPos);
						} else {
							debugMessage("Spawning Trading Route\n" + entry.sectorPos + " -> " + lastShop);
							manager.addJob(new SpawnTradingPartyJob(new Vector3i(entry.sectorPos), lastShop, 3));
							lastShop = new Vector3i(entry.sectorPos);
						}
					}
				}
			} else {
				if (random.nextInt((int)Math.ceil(20f * fac)) == 0) {
					if (entry.type == EntityType.SHOP.dbTypeId && lastShop != null) {
						debugMessage("Spawning Trading Route\n" + lastShop + " -> " + entry.sectorPos);
						manager.addJob(new SpawnTradingPartyJob(new Vector3i(lastShop), new Vector3i(entry.sectorPos), 3));
					}
				}
			}

		}
		if (random.nextInt((int)Math.ceil(22f * fac)) == 0) {

			Vector3i randomSecFrom = new Vector3i(
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range
			);
			Vector3i randomSecTo = new Vector3i(
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range
			);
			randomSecFrom.add(sec);
			if (!state.getUniverse().isSectorLoaded(randomSecFrom)) {
				debugMessage("Spawning Trading Patrol");
				manager.addJob(new SpawnTradingPartyJob(randomSecFrom, randomSecTo, random.nextInt(4) + 1));
			}
		}

		if (!safeZone && inJuristiction <= 0 && random.nextInt((int)Math.ceil(40f * fac)) == 0) {

			Vector3i randomSecFrom = new Vector3i(
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range
			);
			Vector3i randomSecTo = new Vector3i(
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range
			);
			randomSecFrom.add(sec);
			if (!state.getUniverse().isSectorLoaded(randomSecFrom)) {
				debugMessage("Spawning Pirate Patrol");
				manager.addJob(new SpawnPiratePatrolPartyJob(randomSecFrom, randomSecTo, random.nextInt(4) + 1));
			}
		}

		if (!safeZone && inJuristiction <= 0 && random.nextInt((int)Math.ceil(200f * fac)) == 0) {

			Vector3i randomSecFrom = new Vector3i(
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range,
					random.nextInt(range * 2) - range
			);

			randomSecFrom.add(sec);
			if (!state.getUniverse().isSectorLoaded(randomSecFrom)) {
				debugMessage("Spawning Battle");
				manager.addJob(new SpawnTradingPartyJob(randomSecFrom, randomPlayer.getCurrentSector(), random.nextInt(4) + 10));
				manager.addJob(new SpawnPiratePatrolPartyJob(randomSecFrom, randomPlayer.getCurrentSector(), random.nextInt(4) + 10));
			}
		}
	}

	
}
