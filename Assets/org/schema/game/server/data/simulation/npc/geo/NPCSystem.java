package org.schema.game.server.data.simulation.npc.geo;


import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.CompareTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.network.objects.remote.RemoteNPCSystem;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GalaxyTmpVars;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFaction.NPCFactionControlCommandType;
import org.schema.game.server.data.simulation.npc.NPCFactionConfig;
import org.schema.game.server.data.simulation.npc.geo.NPCEntityContingent.NPCEntitySpecification;
import org.schema.schine.network.server.ServerStateInterface;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class NPCSystem extends NPCSystemStub {


	protected final NPCSystemStructure structure;

	protected final NPCCreator creator;

	protected final GameServerState state;
	private final NPCSystemFleetManager fleetManager;
	public ShortSet takenSecs;
	private boolean changed;
	private short level;
	private Vector3i tmp = new Vector3i();
	private int loadedSecs;
	private int total = -1;

	public NPCSystem(GameServerState state, short lvl, NPCSystemStructure structure) {
		super();
		this.level = lvl;
		this.state = state;
		this.structure = structure;
		this.creator = new NPCCreator(this);
		this.fleetManager = new NPCSystemFleetManager(this);
	}

	public static short getLocalIndexFromSector(int x, int y, int z) {
		return getLocalIndex(ByteUtil.modU16(x), ByteUtil.modU16(y), ByteUtil.modU16(z));
	}

	public static short getLocalIndex(int x, int y, int z) {
		assert (x >= 0 && x < VoidSystem.SYSTEM_SIZE && y >= 0 && y < VoidSystem.SYSTEM_SIZE && z >= 0 && z < VoidSystem.SYSTEM_SIZE) : x + ", " + y + ", " + z;
		return (short) (z * 256 + y * 16 + x);
	}

	public void onStatusChanged(double oldStatus) {
		if(status < getFaction().getConfig().abandonSystemOnStatus) {
			log("NPC System status low: " + oldStatus + " -> " + status + " (min " + getFaction().getConfig().abandonSystemOnStatus, LogLevel.NORMAL);
			abandon();
		}
		getFaction().sendCommand(NPCFactionControlCommandType.AWNSER_STATUS, system.x, system.y, system.z, (float) status);
	}

	public void removeSystem() {
		abandon();
	}

	public void abandon() {
		if(!getConfig().doesAbandonSystems) {
			log("not abandoning system due to config value...", LogLevel.NORMAL);
			return;
		}
		if(isRoot() && !getConfig().doesAbandonHome) {
			log("not abandoning home system due to config value...", LogLevel.NORMAL);
			return;
		}
		state.getFactionManager().getNpcFactionNews().lostSystem(getFaction().getIdFaction(), system);
		log("abandoning...", LogLevel.NORMAL);
		this.abandoned = true;

		structure.removeSystem(this);
	}

	public NPCFactionConfig getConfig() {
		return getFaction().getConfig();
	}

	public Vector3i getHomeBase() {
		return structure.getHomeBase();
	}

	@Override
	public float getWeight() {
		assert (structure.totalWeight > 0);
		return NPCSystemStructure.getLevelWeight(level) / structure.totalWeight;
	}

	public void setChangedNT() {
		this.changed = true;
		structure.setChangedNT();
	}

	@Override
	public void log(String string, LogLevel l) {
		structure.log("[" + system.toStringPure() + "]" + string, l);
	}

	public void generate() {


		assert (system != null) : "System null!!";
		if(level == 0) {
			//this is the root system
			distanceFactor = 0.25f;
		} else {
			distanceFactor = Math.max(0.25f, Vector3i.getDisatance(structure.getRoot(), system));
		}

		Galaxy galaxy = state.getUniverse().getGalaxy(getFaction().serverGalaxyPos);
		galaxy.getSystemResources(system, resources, new GalaxyTmpVars());

		//assign system base
		seed = calcSeed();
	}

	public long getResourcesTotal() {
		Galaxy galaxy = state.getUniverse().getGalaxyFromSystemPos(system);

		if(total < 0) {
			total = 0;
			if(!galaxy.isVoidAbs(system)) {
				for(int i = 0; i < VoidSystem.RAW_RESOURCES; i++) {
					byte r = resources.res[i];
					log("System resources per type: " + r, LogLevel.DEBUG);
					float val = getFaction().getConfig().minimumResourceMining;
					if(r > 0) {
						val = Math.max(val, (float) (r) / (float) (127));
					}

					total += val * getFaction().getConfig().resourceAvaililityMultiplicator;
				}
			}
		}
		return total;
	}

	public long getResourcesAvailable() {
		return getResourcesTotal() - minedResources;
	}

	public NPCCreator getCreator() {
		return creator;
	}

	public GameServerState getState() {
		return state;
	}

	public NPCSystemStructure getStructure() {
		return structure;
	}

	public long getLastApplied() {
		return lastApplied;
	}

	public void applyToDatabase(boolean forced) {
		if(lastApplied <= 0 || forced) {
			try {


				StellarSystem overwriteSystem = state.getUniverse().overwriteSystem(system, creator, true);
				int wasFaction = overwriteSystem.getOwnerFaction();
				assert (getFactionId() != 0);
				overwriteSystem.setOwnerFaction(getFactionId());
				overwriteSystem.setOwnerPos(systemBase);
				overwriteSystem.setOwnerUID(systemBaseUID);

				Galaxy galaxy = state.getUniverse().getGalaxyFromSystemPos(system);

				galaxy.getSystemResources(system, overwriteSystem.systemResources, new GalaxyTmpVars());


				System.err.println("[SERVER][NPCSYSTEM] taken system: " + system + ": OwnerPos " + systemBase + "; UID " + systemBaseUID + "; Faction: " + getFactionId());

				state.getDatabaseIndex().getTableManager().getSystemTable().updateOrInsertSystemIfChanged(overwriteSystem, true);

				state.getUniverse().getGalaxyFromSystemPos(system).getNpcFactionManager().onSystemOwnershipChanged(wasFaction, getFactionId(), system);


				state.getGameState().sendGalaxyModToClients(overwriteSystem, systemBase);

				lastApplied = System.currentTimeMillis();

			} catch(IOException e) {
				e.printStackTrace();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}


	}

	public void calculateContingent(int level, int maxLevel, float totalLevelFill) {
		try {
			boolean changed = lastContingentMaxLevel >= 0 && (this.lastContingentMaxLevel != maxLevel || Math.abs(this.lastContingentTotalLevelFill - totalLevelFill) > 0.01f);

			List<NPCEntitySpecification> oldCont = new ObjectArrayList<NPCEntitySpecification>(getContingent().entities);

			this.lastContingentMaxLevel = maxLevel;
			this.lastContingentTotalLevelFill = totalLevelFill;
			getContingent().clearContignet();
			structure.faction.getConfig().getWeightedContingent(level, maxLevel, totalLevelFill, getContingent());
			log("ReCalculated contingent... Total Entities: " + getContingent().getTotalAmount(), LogLevel.NORMAL);


			if(changed || this.markedChangedContingent) {
				//check if contingent actually changed
				if(getContingent().isEqualTo(oldCont)) {
					this.markedChangedContingent = true;

					getFaction().structure.scheduleSystemContingentChange(this);
				} else {
					this.markedChangedContingent = false;
				}
			}

		} catch(EntityNotFountException e) {
			e.printStackTrace();
		}


	}

	public Vector3i getSystemBaseSector() {
		return systemBase;
	}

	public String getSystemBaseUID() {
		return systemBaseUID;
	}

	@Override
	public int getFactionId() {
		return structure.faction.getIdFaction();
	}

	public void checkNPCFactionSending(SendableGameState g, boolean force) {
		if(changed || force) {
			g.getNetworkObject().npcSystemBuffer.add(new RemoteNPCSystem(this, g.getNetworkObject()));
			if(!force) {
				changed = false;
			}
		}
	}

	@Override
	public short getLevel() {
		return level;
	}

	public int getTotalAmountClass(BlueprintClassification... c) {
		return getContingent().getTotalAmountClass(c);
	}

	private boolean isTaken(int x, int y, int z) {
		return takenSecs != null && takenSecs.contains(NPCSystem.getLocalIndexFromSector(x, y, z));
	}

	public int getTotalStationsProjected() {
		int totalStations = 0;
		for(NPCEntitySpecification e : getContingent().entities) {

			if(e.type == EntityType.SPACE_STATION) {
				totalStations += e.count;
			}
		}
		return totalStations;
	}

	public ShortArrayList createStationPositions(final Random r, int stationsToAdd) {
		ShortArrayList newTaken = null;

		SystemRange range = SystemRange.get(system);
		try {
			takenSecs = state.getDatabaseIndex().getTableManager().getEntityTable().getPlayerTakenSectorsLocalCoords(range.start, range.end);

		} catch(SQLException e1) {
			e1.printStackTrace();
		}
		if(takenSecs == null) {
			takenSecs = new ShortOpenHashSet();
		}

		boolean allTaken = takenSecs.size() >= (VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE) - 64;


		r.setSeed(getFactionId() * structure.faction.getSeed() + system.hashCode());


		boolean first;
		int totalStations;
		if(stationsToAdd > 0) {
			newTaken = new ShortArrayList(stationsToAdd);
			totalStations = stationsToAdd;
			first = false; //we are adding stations. home already exists
		} else {
			totalStations = getTotalStationsProjected();
			first = true;
		}

		if(totalStations == 0) {
			throw new RuntimeException("NPC Faction has not available Stations to spawn: " + getFaction().getConfig().getPreset().factionPresetName);
		}


		/*
		 * make sure trade station is used first (for root system first stations) if possible
		 */
		Collections.sort(getContingent().entities, (o1, o2) -> {
			int a = o1.c == BlueprintClassification.TRADE_STATION ? 1 : 0;
			int b = o2.c == BlueprintClassification.TRADE_STATION ? 1 : 0;
			//trade stations first
			return CompareTools.compare(b, a);
		});
		for(NPCEntitySpecification e : getContingent().entities) {

			if(e.type == EntityType.SPACE_STATION) {
				for(int i = 0; i < e.getLeft(); i++) {

					byte x;
					byte y;
					byte z;
					boolean triedForFirst = false;
					do {
						if(!triedForFirst && first && system.equals(0, 0, 0)) {
							x = 4;
							y = 4;
							z = 4;
							triedForFirst = true;
						} else {
							x = (byte) (1 + r.nextInt(VoidSystem.SYSTEM_SIZE - 2));
							y = (byte) (1 + r.nextInt(VoidSystem.SYSTEM_SIZE - 2));
							z = (byte) (1 + r.nextInt(VoidSystem.SYSTEM_SIZE - 2));

							if(Math.abs(x - VoidSystem.SYSTEM_SIZE_HALF) < 2 && Math.abs(y - VoidSystem.SYSTEM_SIZE_HALF) < 2 && Math.abs(z - VoidSystem.SYSTEM_SIZE_HALF) < 2) {
								switch(r.nextInt(3)) {
									case (0) -> x += r.nextBoolean() ? 3 : -3;
									case (1) -> y += r.nextBoolean() ? 3 : -3;
									case (2) -> z += r.nextBoolean() ? 3 : -3;
								}
							}
						}
					} while((!allTaken && isTaken(x, y, z)) || stationMap.containsKey(getLocalIndex(x, y, z)));


					if(first) {
						systemBase = new Vector3i(system);
						systemBase.scale(VoidSystem.SYSTEM_SIZE);
						systemBase.add(x, y, z);
						if(isRoot()) {


							//faction will apply that name to getHomeBaseUID() after this method finished
							systemBaseUID = "NPC-HOMEBASE_" + (systemBase.x) + "_" + (systemBase.y) + "_" + (systemBase.z);
						} else {
							systemBaseUID = "NPC-SYSTEMBASE_" + (systemBase.x) + "_" + (systemBase.y) + "_" + (systemBase.z);
						}
						first = false;
					}
					short local = getLocalIndex(x, y, z);
					stationMap.put(local, e.bbName);
					if(newTaken != null) {
						newTaken.add(local);
					}
					if(takenSecs.size() > (VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE) / 2) {
						log("ERROR: SYSTEM TO OVERCROWDED WITH PLAYER STATIONS AND SHIPS" + getContingent().toString(), LogLevel.ERROR);
						return newTaken;
					}
					if(stationMap.size() > (VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE) / 2) {
						log("ERROR: TOO MANY STATIONS IN THIS SYSTEM " + getContingent().toString(), LogLevel.ERROR);
						return newTaken;
					}
				}
			}
		}
		return newTaken;
	}

	public boolean isRoot() {
		return this.system.equals(structure.getRoot());
	}

	public void populateSpaceStation(Sector sector, Random random) {

		if(sector.pos.equals(getHomeBase())) {
			sector.loadUIDs(sector.getState());
			boolean found = false;
			for(EntityUID e : sector.entityUids) {
				if(e.id == structure.faction.getTradeNode().getEntityDBId()) {
					found = true;
					assert (DatabaseEntry.removePrefixWOException(e.uid).equals(DatabaseEntry.removePrefixWOException(structure.faction.getHomebaseUID()))) : DatabaseEntry.removePrefixWOException(e.uid) + "; " + structure.faction.getHomebaseUID();
					break;
				}
			}
			assert (found) : sector.entityUids;
			return;
		}


		BluePrintController c = structure.faction.getConfig().getPreset().blueprintController;
		List<BlueprintEntry> readBluePrints = c.readBluePrints();


		NPCEntitySpecification spec = getContingent().getStation(sector.getSeed(), random);


		BlueprintEntry blueprintEntry = null;

		if(spec != null) {
			for(BlueprintEntry e : readBluePrints) {
				if(spec.bbName.toLowerCase(Locale.ENGLISH).equals(e.getName().toLowerCase(Locale.ENGLISH))) {
					blueprintEntry = e;
					break;
				}
			}
		}
		if(blueprintEntry != null) {

			Transform t = new Transform();
			t.setIdentity();

			SegmentControllerOutline<?> loadBluePrint;
			try {
				StellarSystem sys = sector.getState().getUniverse().getStellarSystemFromSecPos(sector.pos);

				String UID = sys.getName() + "_" + sector.pos.x + "_" + sector.pos.y + "_" + sector.pos.z + "_ID";
				if(sector.pos.equals(systemBase)) {
					UID = systemBaseUID;
				}
				SegmentPiece toDockOn = null; //this is for spawning turrets manually by the player
				loadBluePrint = c.loadBluePrint(state, blueprintEntry.getName(), UID, t, -1, structure.faction.getIdFaction(), readBluePrints, sector.pos, null, "<system>", Sector.buffer, true, toDockOn, new ChildStats(true));
				if(sector.pos.equals(systemBase)) {
					loadBluePrint.realName = spec.bbName + " Main ";
				} else {
					loadBluePrint.realName = spec.bbName + " " + spawedStations + " ";
					spawedStations++;
				}
				loadBluePrint.checkForChilds(structure.faction.getIdFaction());

				loadBluePrint.scrap = false;
				loadBluePrint.spawnSectorId = new Vector3i(sector.pos);
				loadBluePrint.npcSystem = this;
				loadBluePrint.npcSpec = spec;
				synchronized(state.getBluePrintsToSpawn()) {
					state.getBluePrintsToSpawn().add(loadBluePrint);
				}
			} catch(EntityNotFountException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(EntityAlreadyExistsException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isLocalCoordinateSectorLoadedServer(short localCol) {
		assert (state instanceof ServerStateInterface);
		Vector3i sec = getPosSectorFromLocalCoordinate(localCol, tmp);
		return state.getUniverse().isSectorLoaded(sec);
	}

	public Vector3i getPosSectorFromLocalCoordinate(short localCol, Vector3i out) {
		int z = ((localCol >> 8) & 0xF);
		int y = ((localCol >> 4) & 0xF);
		int x = (localCol & 0xF);
		out.set(x, y, z);
		out.add(system.x * VoidSystem.SYSTEM_SIZE, system.y * VoidSystem.SYSTEM_SIZE, system.z * VoidSystem.SYSTEM_SIZE);
		return out;
	}


	private void onActiveSystem() {
		log("-> Became active", LogLevel.NORMAL);
		fleetManager.onAciveSystem();
	}

	private void onInactiveSystem() {
		log("<- Became inactive", LogLevel.NORMAL);
		fleetManager.onInactiveSystem();
	}

	public void onLoadedSec(Sector sec) {
		boolean wasActive = isActive();

		loadedSecs++;
		assert (loadedSecs > 0) : this;
		if(!wasActive && isActive()) {
			onActiveSystem();
		}
	}

	public boolean isActive() {
		return loadedSecs > 0;
	}

	public void onUnloadedSec(Sector sec) {
		boolean wasActive = isActive();

		loadedSecs--;
		assert (loadedSecs >= 0) : this;
		if(!wasActive && isActive()) {
			onInactiveSystem();
		}

	}

	public void onCommandPartFinished(Fleet fleet, FleetState currentState) {
		fleetManager.onCommandPartFinished(fleet, currentState);
	}

	public void lostEntity(BlueprintClassification c, String bbName, ElementCountMap cargo, long dbId, SegmentController entity) {
		try {
			BlueprintEntry blueprint = getFaction().getConfig().getPreset().blueprintController.getBlueprint(bbName);

			if(cargo != null) {
				getFaction().lostResources(cargo);
			}
			fleetManager.lostEntity(dbId, entity);
			setChangedNT();
		} catch(EntityNotFountException e) {
			e.printStackTrace();
		}
	}

	public NPCFaction getFaction() {
		return structure.faction;
	}


	public void onRemovedCleanUp() {
		log("cleaning up after losing control " + system, LogLevel.NORMAL);
		try {
			state.getDatabaseIndex().getTableManager().getSystemTable().setSystemOwnership(system, 0, "", 0, 0, 0);

			state.getUniverse().getGalaxyFromSystemPos(system).getNpcFactionManager().onSystemOwnershipChanged(getFactionId(), 0, system);


			state.getGameState().sendGalaxyModToClients(0, "", systemBase);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		fleetManager.cleanUpAllFleets();
		getContingent().despawnAllSpawned(this);
	}

	/**
	 * used when contigent changes, so they respawn on next visit
	 */
	public void clearFleetsAndContingentsOnChange() {
		assert (!isActive());
		log("cleaning up since contingent changed (this sys is inactive) " + system, LogLevel.NORMAL);
		fleetManager.cleanUpAllFleets();
		getContingent().despawnAllSpawned(this);
	}

	public void replenish(long time) {
		long replenish = (long) ((double) getFaction().getConfig().replenishResourceRate * (double) getResourcesTotal());
		double res = resourcesAvailable;

		minedResources = Math.max(0L, minedResources - replenish);
		if(getResourcesTotal() <= 0) {
			resourcesAvailable = 0;
		} else {
			resourcesAvailable = (float) (1.0d - Math.min(1d, (double) minedResources / (double) getResourcesTotal()));
		}
		log("Replenished Resources: " + res + " -> " + resourcesAvailable + "; Replenished Resources: " + replenish + "; available now: " + getResourcesAvailable() + " / " + getResourcesTotal(), LogLevel.NORMAL);
	}

	public void mine(long time) {

		replenish(time);


		if(resourcesAvailable < getFaction().getConfig().minimumMinableResources) {
			log("MINE DONE: Not mining because available resources too low: " + resourcesAvailable + " " + "< " + getFaction().getConfig().minimumMinableResources + "; (mined: " + minedResources + ")", LogLevel.DEBUG);
			return;
		}
		double score = getContingent().getMinerScore();
		IntOpenHashSet slt = new IntOpenHashSet();
		long available = getResourcesAvailable();
		float totalVal = 0;
		if(available > 0) {
			for(int i = 0; i < VoidSystem.RAW_RESOURCES; i++) {
				if(ElementKeyMap.getInfo(ElementKeyMap.resources[i]).deprecated) continue;
				byte r = resources.res[i];
				float val = (float) (r) / (float) (127);
				totalVal += val;

				//making any negative value positive to calculate resourceAmount
				val = Math.max(getFaction().getConfig().minimumResourceMining, val);

				int resourceAmount = (int) Math.min(Integer.MAX_VALUE, (long) Math.min(available, val * getFaction().getConfig().resourcesPerMinerScore * score));

				log(String.format("MINE (raw ress): %-25s x %-8d value: %5.2f resourceByte: %4d", ElementKeyMap.toString(ElementKeyMap.resources[i]), resourceAmount, val, resources.res[i]), LogLevel.DEBUG);
				//log("MINE (raw ress): "+ElementKeyMap.toString(ElementKeyMap.resources[i])+" x "+resourceAmount + " value: " + val + " resourceByte: " + resources.res[i], LogLevel.DEBUG);

				slt.add(getFaction().getInventory().incExistingOrNextFreeSlotWithoutException(ElementKeyMap.resources[i], resourceAmount));
				minedResources += resourceAmount;
			}
			//removed if statement (totalVal > 0), it's always positive or 0 now
			Math.abs(totalVal);
			log("MINE (misc): Total value: " + totalVal, LogLevel.DEBUG);
			Random r = new Random(time);
			for(short type : ElementKeyMap.typeList()) {
				ElementInformation info = ElementKeyMap.getInfoFast(type);
				if(!info.isOre() && !info.isCapsule() && info.isShoppable() && info.getConsistence().isEmpty()) {
					int resourceAmount = (int) Math.min(Integer.MAX_VALUE, (long) Math.min(available, totalVal * getFaction().getConfig().resourcesPerMinerScore * score));

					if(resourceAmount > 0) {
						int base = resourceAmount / 3;
						int randomAdd = 2 * (resourceAmount / 3);
						resourceAmount = base + (randomAdd > 0 ? r.nextInt(randomAdd + 1) : 0);

						log(String.format("MINE (misc): %-25s x %-8d", ElementKeyMap.toString(type), resourceAmount), LogLevel.DEBUG);
						//log("MINE (misc): "+ElementKeyMap.toString(type)+" x "+resourceAmount + " Total value: " + totalVal, LogLevel.DEBUG);


						slt.add(getFaction().getInventory().incExistingOrNextFreeSlotWithoutException(type, resourceAmount));
						minedResources += resourceAmount * getFaction().getConfig().minedResourcesAddedFromMisc;
					}
				}
			}


			if(slt.size() > 0) {
				getFaction().getInventory().sendInventoryModification(slt);
			}
			minedResources = Math.min(minedResources, getResourcesTotal());
			resourcesAvailable = (float) (1.0d - Math.min(1d, (double) minedResources / (double) getResourcesTotal()));
			if(totalVal == 0) {
				log("WARNING; MINING: RAW RES 0: " + Arrays.toString(resources.res), LogLevel.DEBUG);
			}
		}
		log("MINE DONE: (MiningScore: " + score + ") Resources Available: " + resourcesAvailable + " (totAvailable " + available + ") (resourcesRaw: " + totalVal + "); (mined: " + minedResources + ")", LogLevel.DEBUG);

		getFaction().setChangedNT();

	}

	public void resupply(long time) {
		if(!isActive()) {
			getContingent().resupply(this, time);
		} else {
			log("Not Resupplying System since it's active", LogLevel.NORMAL);
		}
	}

	public void consume(long time) {
		getContingent().consume(this, time);
	}

	public void populateAsteroids(Sector sector, SectorType sectorType, Random random) throws IOException {
		Random randomT = new Random(seed + sector.getSeed());
		Random randomAsteroidLocal = new Random(seed + sector.getSeed());


		int asteroidCount = FastMath.round(sectorType.getAsteroidCountMax() * resourcesAvailable);
		//log("Asteroid count: " + asteroidCount + " Asteroid max: " + sectorType.getAsteroidCountMax() + " Resource available: " + resourcesAvailable , LogLevel.DEBUG);

		if(asteroidCount > 0) {
			int rock_count = randomT.nextInt(asteroidCount);
			if(sectorType == SectorType.ASTEROID) {
				rock_count++;
			}
			int maxSize = FastMath.round(ServerConfig.ASTEROID_RADIUS_MAX.getInt() * resourcesAvailable);

			for(int i = 0; i < rock_count; i++) {
				//create seed here so sizes/radius etc can be replicated out of seed
				long seed = randomT.nextLong();

				randomAsteroidLocal.setSeed(seed);

				int sizeX = randomAsteroidLocal.nextInt(maxSize) + Sector.rockSize;
				int sizeY = randomAsteroidLocal.nextInt(maxSize) + Sector.rockSize;
				int sizeZ = randomAsteroidLocal.nextInt(maxSize) + Sector.rockSize;

				sector.addRandomRock(sector.getState(), seed, sizeX, sizeY, sizeZ, randomAsteroidLocal, i);
			}
		}
	}

	public void onUnachedFleet(Fleet f) {
		fleetManager.onUncachedFleet(f);
	}

	public NPCSystemFleetManager getFleetManager() {
		return fleetManager;
	}

	public void onAttackedFaction(Fleet fleet, EditableSendableSegmentController seg, Damager from) {
		fleetManager.onAttacked(fleet, seg, from);
	}
}
