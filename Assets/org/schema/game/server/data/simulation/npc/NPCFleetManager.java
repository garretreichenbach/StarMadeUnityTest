package org.schema.game.server.data.simulation.npc;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.LogInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.trade.TradeManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;
import org.schema.game.network.objects.remote.FleetCommand;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.simulation.npc.geo.NPCSystem;
import org.schema.game.server.data.simulation.npc.geo.NPCSystemFleetManager;
import org.schema.game.server.data.simulation.npc.geo.NPCSystemFleetManager.FleetType;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class NPCFleetManager implements LogInterface {

	public static final String IDPREFIX_FLEET = "GNPCFLT#";
	public static final String IDPREFIX_OWNER = "GNPC#";
	private final NPCFaction faction;

	private final NPCGlobalStats stats;
	private boolean loaded;
	public List<Fleet> fleets = new ObjectArrayList<Fleet>();

	private class NPCGlobalStats extends NPCStats {
		private Vector3i idSys = new Vector3i(faction.getIdFaction(), Integer.MIN_VALUE, Integer.MIN_VALUE);

		public void incShipNumber() {
			super.incShipNumber((GameServerState) getFaction().getState(), getFaction().getIdFaction(), idSys);
		}

		public void incFleetNumber() {
			super.incFleetNumber((GameServerState) getFaction().getState(), getFaction().getIdFaction(), idSys);
		}

		public int getFleetCreatorNumber() {
			return super.getFleetCreatorNumber((GameServerState) getFaction().getState(), getFaction().getIdFaction(), idSys);
		}

		public int getShipCreatorNumber() {
			return super.getShipCreatorNumber((GameServerState) getFaction().getState(), getFaction().getIdFaction(), idSys);
		}
	}

	public NPCFleetManager(NPCFaction faction) {
		super();
		this.faction = faction;
		stats = new NPCGlobalStats();

	}

	public String getOwnerName() {
		return IDPREFIX_OWNER + faction.getIdFaction();
	}

	public Tag toTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, (byte) 0),
				FinishTag.INST
		}
		);
	}

	public void fromTag(Tag tag) {
		Tag[] t = tag.getStruct();
		byte version = t[0].getByte();
	}

	private NPCFaction getFaction() {
		return faction;
	}

	public boolean isType(Fleet f, FleetType type) {
		return f.getName().toLowerCase(Locale.ENGLISH).startsWith(getFleetPrefix(type).toLowerCase(Locale.ENGLISH));
	}

	private String getFleetPrefix(FleetType type) {
		return IDPREFIX_FLEET + type.name() + "#";
	}

	public String getName(int creationId, FleetType type) {
		return getFleetPrefix(type) + faction.getIdFaction() + "#" + creationId;
	}

	public FleetManager getFleetManager() {
		return getState().getFleetManager();
	}

	public GameServerState getState() {
		return faction.structure.getState();
	}

	private void load() {
		if(!loaded) {
			getFleetManager().loadByOwner(getOwnerName());
			recalcFleets();
			loaded = true;
		}
	}

	public void onUncachedFleet(Fleet f) {
		fleets.remove(f);
		this.loaded = false;

		log("UNCACHED FLEET " + f.getName() + "; Total System Fleets " + getState().getFleetManager().getAvailableFleets(getOwnerName()).size(), LogLevel.DEBUG);
	}

	public BlueprintClassification[] getClasses(FleetType type) {
		return faction.getConfig().getFleetClasses(type);
	}

	@Override
	public void log(String s, LogLevel lvl) {
		faction.log("[FLEETS]" + s, lvl);
	}

	public void lostEntity(long dbId, SegmentController entity) {
		for(Fleet f : fleets) {
			if(f.isMember(dbId)) {
				f.removeMemberByDbIdUID(dbId, false);
				if(f.isEmpty()) {
					f.removeFleet(true);
					fleets.remove(f);
					return;
				}
			}
		}
	}

	public static FleetType getTypeFromFleetName(String name) {

		String[] split = name.split("#");

		if(split.length > 2) {
			try {
				return FleetType.valueOf(split[1].trim());
			} catch(Exception e) {
				System.err.println("Exception STR: '" + name + "'");
				e.printStackTrace();
			}
		}

		return null;
	}

	public static int getFactionIdFromFleetName(String name) {
		String[] split = name.split("#");
		if(split.length > 3) {
			try {
				return Integer.parseInt(split[2]);
			} catch(NumberFormatException e) {
				System.err.println("Exception STR: '" + name + "'");
				e.printStackTrace();
			}
		}

		return 0;
	}

	public Fleet spawnTradingFleet(ElementCountMap c, Vector3i from, Vector3i to, boolean direct) {
		String fleetName = getName(stats.getFleetCreatorNumber(), NPCSystemFleetManager.FleetType.TRADING);
		getState().getFleetManager().requestCreateFleet(fleetName, getOwnerName());
		stats.incFleetNumber();
		recalcFleets();
		for(Fleet f : fleets) {
			if(f.getName().equals(fleetName)) {
				Vector3i pos = (direct ? from : getUnloadedSectorAround(from, new Vector3i()));
				fillTradeFleet(f, pos, c);
				FleetCommand com = new FleetCommand(FleetCommandTypes.TRADE_FLEET, f, to);
				getFleetManager().executeCommand(com);
				return f;
			}
		}
		throw new IllegalArgumentException("Couldn't spawn fleet " + fleetName);
	}

	public Vector3i getUnloadedSectorAround(Vector3i from, Vector3i out) {
		int distance = 2;

		out.set(from);

		while(getState().getUniverse().isSectorLoaded(out)) {

			int r = Universe.getRandom().nextInt(3);

			if(r == 0) {
				out.set(from.x + distance, from.y, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x - distance, from.y, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x, from.y + distance, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y - distance, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x, from.y, from.z + distance);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y, from.z - distance);
			} else if(r == 1) {

				out.set(from.x, from.y + distance, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y - distance, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x + distance, from.y, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x - distance, from.y, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x, from.y, from.z + distance);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y, from.z - distance);
			} else {

				out.set(from.x, from.y, from.z + distance);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y, from.z - distance);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x + distance, from.y, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x - distance, from.y, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x, from.y + distance, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y - distance, from.z);
				if(!getState().getUniverse().isSectorLoaded(out)) {
					break;
				}

			}

			distance++;

		}

		return out;
	}

	public void spawnAttackFleet(Vector3i target, int size) {
		FleetType t = FleetType.ATTACKING;

		String fleetName = getName(stats.getFleetCreatorNumber(), t);

		getState().getFleetManager().requestCreateFleet(fleetName, getOwnerName());
		stats.incFleetNumber();
		recalcFleets();

		for(Fleet f : fleets) {
			if(f.getName().equals(fleetName)) {

				Vector3i pos = findUnloadedOnWay(faction.getHomeSector(), target);
				boolean spawned = fillFleets(f, t, size, pos);
				if(spawned) {
					FleetCommand com = new FleetCommand(FleetCommandTypes.PATROL_FLEET, f, target);
					getFleetManager().executeCommand(com);
				} else {
					removeFleetAndPurgeShips(f);
					recalcFleets();
				}
				return;
			}
		}
		throw new IllegalArgumentException("Couldn't spawn fleet " + fleetName);
	}

	public void spawnScavengingFleet(Vector3i target, int size) {
		FleetType t = FleetType.SCAVENGE;

		String fleetName = getName(stats.getFleetCreatorNumber(), t);

		getState().getFleetManager().requestCreateFleet(fleetName, getOwnerName());
		stats.incFleetNumber();
		recalcFleets();

		for(Fleet f : fleets) {
			if(f.getName().equals(fleetName)) {

				Vector3i pos = findUnloadedOnWay(faction.getHomeSector(), target);
				boolean spawned = fillFleets(f, t, size, pos);
				if(spawned) {
					FleetCommand com = new FleetCommand(FleetCommandTypes.PATROL_FLEET, f, target);
					getFleetManager().executeCommand(com);
				} else {
					removeFleetAndPurgeShips(f);
					recalcFleets();
				}
				return;
			}
		}
		throw new IllegalArgumentException("Couldn't spawn fleet " + fleetName);
	}

	private boolean fillFleets(Fleet fleet, FleetType type, int size, Vector3i posToSpawn) {

		BluePrintController blueprintController = faction.getConfig().getPreset().blueprintController;

		log("Checking classes use for " + type.name() + ": " + faction.getConfig().getPreset().confFile.getParentFile().getName() + "; " + faction.getConfig().getPreset().blueprintController.entityBluePrintPath, LogLevel.DEBUG);
		List<BlueprintEntry> readBluePrints = blueprintController.readBluePrints();

		BlueprintClassification[] classes = getClasses(type);
		List<BlueprintEntry> bbs = new ObjectArrayList<BlueprintEntry>();
		for(BlueprintClassification bc : classes) {
			log("Checking class use for " + type.name() + ": " + bc.name(), LogLevel.DEBUG);
			for(BlueprintEntry b : readBluePrints) {
				log("Checking blueprint use for trading: " + b.getClassification().name() + "; " + b.getName(), LogLevel.DEBUG);
				if(bc == b.getClassification()) {
					bbs.add(b);
				}

			}
		}
		if(bbs.size() > 0) {
			//spawn in for empty fleets 
			for(int i = 0; i < size; i++) {

				BlueprintEntry blueprintEntry = bbs.get(i % bbs.size());

				String UID = "GFLTSHP_" + faction.getIdFaction() + "_" + stats.getShipCreatorNumber();
				stats.incShipNumber();

				long spawnInDB = spawnInDB(faction.structure.getState(), blueprintEntry, null, posToSpawn,
						UID, "Faction Attack Ship");
				if(spawnInDB > 0) {
					getFleetManager().requestShipAdd(fleet, spawnInDB);
				} else {
					System.err.println("Exception: FACTIONFLEETMANAGER: NOT ADDING SHIP " + UID);
				}
			}
			return true;
		} else {
			log("NO SHIPS FOR A FLEET OF TYPE " + type.name() + "; REMOVING FLEET", LogLevel.ERROR);
			return false;
		}

	}

	private Vector3i findUnloadedOnWay(Vector3i from, Vector3i to) {
		if(from.equals(to)) {
			Vector3i pos = new Vector3i(from);
			while(getState().getUniverse().isSectorLoaded(pos)) {
				pos.x++;
			}
		}
		//go into direction of the target to find an unlaoded sector to spawn in
		Vector3f f = new Vector3f(from.x, from.y, from.z);
		Vector3f t = new Vector3f(to.x, to.y, to.z);

		t.sub(f);
		t.normalize();
		int i = 0;
		Vector3i pos = new Vector3i(from);
		while(getState().getUniverse().isSectorLoaded(pos)) {
			f.add(t);
			pos.set(FastMath.round(f.x), FastMath.round(f.y), FastMath.round(f.z));
			i++;

			if(i > 1000) {
				throw new IllegalArgumentException("NO UNLOADED SECTOR WITHIN 1000 tries: " + from + " -> " + to + "; f: " + f + "; t: " + t);
			}
		}

		return pos;
	}

	private void recalcFleets() {
		ObjectArrayList<Fleet> availableFleets = getState().getFleetManager().getAvailableFleets(getOwnerName());
		fleets.clear();
		fleets.addAll(availableFleets);
	}

	private void fillTradeFleet(Fleet fleet, Vector3i pos, ElementCountMap c) {
		BluePrintController blueprintController = faction.getConfig().getPreset().blueprintController;

		List<BlueprintEntry> readBluePrints = blueprintController.readBluePrints();

		log("Checking trading classes use for Trading: " + faction.getConfig().getPreset().confFile.getParentFile().getName() + "; " + faction.getConfig().getPreset().blueprintController.entityBluePrintPath, LogLevel.DEBUG);
		BlueprintClassification[] classes = getClasses(FleetType.TRADING);
		boolean hasDefendOrAttack = false;
		assert (classes.length > 0);
		double totalCapacity = 0;
		List<BlueprintEntry> bbs = new ObjectArrayList<BlueprintEntry>();
		for(BlueprintClassification bc : classes) {
			log("Checking trading class use for trading: " + bc.name(), LogLevel.DEBUG);
			for(BlueprintEntry b : readBluePrints) {
				log("Checking trading class blueprint use for trading: " + b.getClassification().name() + "; " + b.getName(), LogLevel.DEBUG);
				if(bc == b.getClassification()) {
					bbs.add(b);
					totalCapacity += b.getTotalCapacity();
				}
				if(b.getClassification() == BlueprintClassification.ATTACK || b.getClassification() == BlueprintClassification.DEFENSE) {
					hasDefendOrAttack = true;
				}

			}
		}

		double capacity = c.getVolume();
		double defenseMass = 0;
		int cargoShips = 0;
		int defenseShips = 0;
		int count = 0;
		double cargoMass = c.getMass();
		while(bbs.size() > 0 && capacity > 0) {
			BlueprintEntry blueprintEntry = bbs.get(count % bbs.size());

			double totalCap = blueprintEntry.getTotalCapacity();

			ElementCountMap cargo = null;
			if(totalCap > 0) {
				cargo = new ElementCountMap();
				cargo.transferFrom(c, totalCap);
			}
			String UID = "GFLTSHP_" + faction.getIdFaction() + "_" + stats.getShipCreatorNumber();
			stats.incShipNumber();

			log("Spawning trading fleet member " + blueprintEntry.getName() + " for " + fleet.getName(), LogLevel.DEBUG);

			long spawnInDB = spawnInDB(faction.structure.getState(), blueprintEntry, cargo, pos,
					UID, faction.getName() + " Trade Fleet " + blueprintEntry.getClassification().getName() + " " + blueprintEntry.getName());
			if(spawnInDB < 0) {
				System.err.println("Exception: NPC FLEET MANAGER: NOT ADDING SHIP TO FLEET BECAUSE SPAWN DIDNT WORK: " + UID);
				continue;
			}

			getFleetManager().requestShipAdd(fleet, spawnInDB);
			capacity -= totalCap;
			count++;
			if(blueprintEntry.getClassification() == BlueprintClassification.ATTACK || blueprintEntry.getClassification() == BlueprintClassification.DEFENSE) {
				defenseShips++;
				defenseMass += blueprintEntry.getMass();
			} else if(blueprintEntry.getClassification() == BlueprintClassification.CARGO) {
				cargoShips++;
			}

			if(totalCapacity == 0) {
				break;
			}

		}
		if(hasDefendOrAttack) {
			/*
			 * add defense/attack ships to fleet as long as the combined mass of
			 * defend/attack ship doesn't exceeed the cargo mass with config multiplier.
			 * Cap on max config value and add at least minimum.
			 * * more cargo -> more defense.
			 * * no undefended cargo ships.
			 * * no spam.
			 */
			while((defenseShips < getConfig().tradingMinDefenseShips ||
					defenseMass < cargoMass * getConfig().tradingCargoMassVersusDefenseMass) &&
					defenseShips < cargoShips * getConfig().tradingMaxDefenseShipsPerCargo) {
				BlueprintEntry blueprintEntry = bbs.get(count % bbs.size());

				if(blueprintEntry.getClassification() == BlueprintClassification.ATTACK || blueprintEntry.getClassification() == BlueprintClassification.DEFENSE) {
					String UID = "GFLTSHP_" + faction.getIdFaction() + "_" + stats.getShipCreatorNumber();
					stats.incShipNumber();
					log("Spawning additional trading fleet defense member " + blueprintEntry.getName() + " for " + fleet.getName(), LogLevel.DEBUG);
					long spawnInDB = spawnInDB(faction.structure.getState(), blueprintEntry, null, pos,
							UID, faction.getName() + " Trade Fleet " + blueprintEntry.getClassification().getName() + " " + blueprintEntry.getName());

					getFleetManager().requestShipAdd(fleet, spawnInDB);
					defenseShips++;
					defenseMass += blueprintEntry.getMass();
				}
				count++;
				if(totalCapacity < 1) {
					break;
				}
			}
		}
		assert (count > 0) : totalCapacity + "; " + bbs.size();
	}

	public NPCFactionConfig getConfig() {
		return faction.getConfig();
	}

	private long spawnInDB(GameServerState state, BlueprintEntry blueprintEntry, ElementCountMap cMap, Vector3i pos, final String uid, final String realName) {
		BluePrintController c = faction.getConfig().getPreset().blueprintController;

		List<BlueprintEntry> readBluePrints = c.readBluePrints();
		long id = -1;
		if(blueprintEntry != null) {
			Transform t = new Transform();
			t.setIdentity();

			SegmentControllerOutline<?> loadBluePrint;
			try {

				if(cMap != null) {
					log("SPAWN IN DATABASE WITH BLOCKS: " + cMap.getTotalAmount() + "; UID: " + uid, LogLevel.NORMAL);
				} else {
					log("SPAWN IN DATABASE WITHOUT BLOCKS; UID: " + uid, LogLevel.NORMAL);
				}
				SegmentPiece toDockOn = null; //this is for spawning turrets manually by the player
				loadBluePrint = c.loadBluePrint(
						state,
						blueprintEntry.getName(),
						uid,
						t,
						-1,
						faction.getIdFaction(),
						readBluePrints,
						pos,
						null,
						"<system>",
						NPCFaction.buffer,
						true, toDockOn, new ChildStats(true));
				loadBluePrint.spawnSectorId = new Vector3i(pos);
				loadBluePrint.realName = realName;
				loadBluePrint.tradeNode = DatabaseEntry.removePrefixWOException(uid).equals(DatabaseEntry.removePrefixWOException(faction.getHomebaseUID()));
				loadBluePrint.itemsToSpawnWith = cMap;

				ChildStats childStats = new ChildStats(false);
				ObjectArrayList<String> added = new ObjectArrayList<String>();

				id = loadBluePrint.spawnInDatabase(pos, state, 0, added, childStats, true);

			} catch(EntityNotFountException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(EntityAlreadyExistsException e) {
				e.printStackTrace();
				String fuid = blueprintEntry.getEntityType().type.dbPrefix + DatabaseEntry.removePrefixWOException(uid);
				id = state.getDatabaseIndex().getTableManager().getEntityTable().getIdForFullUID(fuid);
				System.err.println("[SERVER] Exception caught sucessfully. DB entry: " + fuid + "; returned id: " + id);
			} catch(SQLException e) {
				e.printStackTrace();
			} catch(StateParameterNotFoundException e) {
				e.printStackTrace();
			}

		}

		return id;
	}

	public void onFinishedTrade(Fleet f) {
//		f.moveToUnload();
		removeFleetAndPurgeShips(f);
	}

	private void removeFleetAndPurgeShips(Fleet f) {
		List<FleetMember> mems = new ObjectArrayList<FleetMember>(f.getMembers());
		for(FleetMember m : mems) {
			f.removeMemberByDbIdUID(m.entityDbId, true);
		}
		faction.structure.getState().getFleetManager().removeFleet(f);
		fleets.remove(f);
	}

	public void update(long time) {
		if(!loaded) {
			load();
			for(int i = 0; i < fleets.size(); i++) {
				Fleet f = fleets.get(i);
				switch(f.getNpcType()) {
					case ATTACKING:
						log("removing inactive attack fleet", LogLevel.NORMAL);
						removeFleetAndPurgeShips(f);
						i--;
						break;
					case DEFENDING:
						break;
					case MINING:
						break;
					case SCAVENGE:
						log("removing inactive attack fleet", LogLevel.NORMAL);
						removeFleetAndPurgeShips(f);
						i--;
						break;
					case TRADING:
						TradeManager tradeManager = getState().getGameState().getTradeManager();
						if(!tradeManager.getTradeActiveMap().getFleetsInTrades().contains(f.dbid)) {
							log("removing trading fleet because no active trade found for it (probably due to crash before save)", LogLevel.ERROR);
							removeFleetAndPurgeShips(f);
							i--;
						} else {
							log("NOT removing trading fleet because active trade found for it", LogLevel.DEBUG);
						}

						break;
					default:
						break;

				}
			}
		}

	}

	public void fleetTurn() {
		List<Faction> enemies = faction.getEnemies();
		Random r = new Random();

		for(Faction efs : enemies) {
			int fleets = 3 + r.nextInt(8);

			if(efs.isPlayerFaction()) {

				List<PlayerState> online = efs.getOnlinePlayers();
				if(online.size() > 0) {
					Collections.shuffle(online);
					spawnAttackFleet(new Vector3i(online.get(0).getCurrentSector()), fleets);
				}
			} else if(efs.isNPC()) {
				NPCFaction npc = (NPCFaction) efs;

				List<PlayerState> allPlayer = new ObjectArrayList<PlayerState>(getState().getPlayerStatesByDbId().values());
				Collections.shuffle(allPlayer);

				boolean found = false;

				for(PlayerState s : allPlayer) {
					try {
						StellarSystem sys = getState().getUniverse().getStellarSystemFromStellarPos(s.getCurrentSystem());
						if(sys.getOwnerFaction() == efs.getIdFaction()) {
							found = true;
							spawnAttackFleet(new Vector3i(s.getCurrentSector()), fleets);
							/*
							 * go to player in the enemy territory for maximal chance of
							 *  an actual fight happening
							 */
							break;
						}
					} catch(IOException e) {
						e.printStackTrace();
					}
				}

				if(!found) {
					NPCSystem closest = npc.structure.findClosestFrom(faction.structure.getRoot(), 2);
					spawnAttackFleet(closest.getSystemBaseSector(), fleets);
				}
			}
		}

		Set<Vector3i> attackSector = new ObjectOpenHashSet<Vector3i>(getState().getUniverse().attackSector.keySet());
		for(int i = 0; i < faction.getConfig().scvengingFleetsPerTurn; i++) {
			Vector3i closest = null;
			float dist = 0;
			for(Vector3i sector : attackSector) {
				float d = Vector3i.getDisatance(faction.getHomeSector(), sector);
				if(closest == null || d < dist) {
					closest = sector;
					dist = d;
				}
			}

			if(closest != null) {
				attackSector.remove(closest);
				float range = Math.max(faction.getConfig().minScavenginRange, (faction.structure.getLastHabitatedLevel() * faction.getConfig().scavengingRangePerFactionLevel));

				if(Vector3i.getDisatance(faction.getHomeSector(), closest) / 16.0f < range) {
					spawnScavengingFleet(closest, 5);
				}
			}
		}
	}

}
