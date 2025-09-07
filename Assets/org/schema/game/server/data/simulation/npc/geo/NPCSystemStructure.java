package org.schema.game.server.data.simulation.npc.geo;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.LogInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation.SpaceStationType;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GalaxyTmpVars;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction.DiplActionType;
import org.schema.game.server.data.simulation.npc.geo.NPCEntityContingent.NPCEntitySpecification;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class NPCSystemStructure implements LogInterface {

	private static final byte VERSION = 0;
	private static final int EXPANSION_SEARCH_SYSTEMS = 5;
	public final Long2ObjectOpenHashMap<Vector3i> spawnedEntitiesPerSystem = new Long2ObjectOpenHashMap<Vector3i>();
	public final long[] totalResources = new long[VoidSystem.RESOURCES];
	private final Set<Vector3i> taken = new ObjectOpenHashSet<Vector3i>();
	public float totalDistanceWeight;
	public float totalWeight;
	NPCFaction faction;
	private GameServerState state;
	private NPCSystemLevel[] levels;
	private int totalSystems;
	private int idGen;
	private Long2DoubleOpenHashMap valueCache = new Long2DoubleOpenHashMap();
	private Vector3i tmp3 = new Vector3i();
	private Set<NPCSystem> scheduledChanges = new ObjectOpenHashSet<NPCSystem>();

	private long lastChangeUpdate;

	private boolean systemsGrown;

	public NPCSystemStructure(GameServerState state, NPCFaction faction) {
		super();
		this.state = state;
		this.faction = faction;
	}

	public static float getLevelWeight(int level) {
		return 1f / (level + 1f);
	}

	private static int getLevelSize(int lvl) {
		assert (lvl >= 0);
		if(lvl == 0) {
			return 1;
		}
		int l = lvl - 1;

		int a = ((lvl * 2) + 1);
		int b = ((l * 2) + 1);

		int max = (a * a * a) - (b * b * b);
//		System.err.println("LEVEL: "+(lvl-1)+" -> "+max+"; :::: "+(a*a*a)+" :: "+(b*b*b));
		return max;
	}

	public boolean isCreated() {
		return levels != null;
	}

	public void createNew() {
		levels = new NPCSystemLevel[1];
		levels[0] = new NPCSystemLevel((short) 0);
		NPCSystem rootSys = new NPCSystem(state, (short) 0, this);
		rootSys.system = new Vector3i(faction.npcFactionHomeSystem);
		rootSys.generate();
		levels[0].add(rootSys);
		recalc();

		faction.setHomebaseRealName(faction.getName() + " Home");

		rootSys.createStationPositions(new Random(), -1);
		if(rootSys.getHomeBase() == null) {
			throw new NullPointerException("Home system null " + faction);
		}
		faction.setHomebaseUID(EntityType.SPACE_STATION.dbPrefix + DatabaseEntry.removePrefixWOException(rootSys.getSystemBaseUID()));
		faction.getHomeSector().set(getHomeBase());

		String mainStationBB = rootSys.stationMap.get(NPCSystem.getLocalIndex(ByteUtil.modU16(rootSys.getHomeBase().x), ByteUtil.modU16(rootSys.getHomeBase().y), ByteUtil.modU16(rootSys.getHomeBase().z)));


		NPCEntitySpecification mainStation = rootSys.getContingent().getSpec(mainStationBB);

		long baseStationId = spawnInDB(state, mainStation, rootSys, faction.getHomeSector(), DatabaseEntry.removePrefixWOException(faction.getHomebaseUID()), faction.getHomebaseRealName());
		try {
			if(baseStationId >= 0) {
				System.err.println("[SERVER][NPC] Creating tradenode for home system: " + rootSys);
				faction.createTradeNode(state, baseStationId);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}


		Random r = new Random(faction.getIdFaction());
		int grow;
		if(faction.initialSpawn >= 0) {
			grow = faction.initialSpawn;
		} else {
			grow = Math.max(0, faction.getConfig().initialGrowBaseDefault + (faction.getConfig().initialGrowAddedDefaultRandom > 0 ? r.nextInt(faction.getConfig().initialGrowAddedDefaultRandom) : 0));
		}
		log("INITIAL SYTEM COUNT: " + grow, LogLevel.NORMAL);
		for(int i = 0; i < grow; i++) {
			grow(false);
		}
	}

	public void populateSpaceStation(Sector sector, Random random) {
		NPCSystem system = getSystem(VoidSystem.getContainingSystem(sector.pos, new Vector3i()));
		if(system != null) {
			system.populateSpaceStation(sector, random);
		}
	}

	public void populateAsteroids(Sector sector, SectorType sectorType, Random random) throws IOException {
		NPCSystem system = getSystem(VoidSystem.getContainingSystem(sector.pos, new Vector3i()));
		if(system != null) {
			system.populateAsteroids(sector, sectorType, random);
		}

	}

	public long spawnInDB(GameServerState state, NPCEntitySpecification spec, NPCSystem sys, Vector3i pos, final String uid, final String realName) {
		BluePrintController c = faction.getConfig().getPreset().blueprintController;


		List<BlueprintEntry> readBluePrints = c.readBluePrints();
		BlueprintEntry blueprintEntry = null;

		assert (spec != null);
		assert (spec.bbName != null);

		for(BlueprintEntry e : readBluePrints) {
			if(spec.bbName.toLowerCase(Locale.ENGLISH).equals(e.getName().toLowerCase(Locale.ENGLISH))) {
				blueprintEntry = e;
				break;
			}
		}
		long id = -1;
		if(blueprintEntry != null) {
			Transform t = new Transform();
			t.setIdentity();
			SegmentPiece toDockOn = null; //this is for spawning turrets manually by the player
			SegmentControllerOutline<?> loadBluePrint;
			try {
				loadBluePrint = c.loadBluePrint(state, blueprintEntry.getName(), uid, t, -1, faction.getIdFaction(), readBluePrints, pos, null, "<system>", NPCFaction.buffer, true, toDockOn, new ChildStats(true));
				loadBluePrint.spawnSectorId = new Vector3i(pos);
				loadBluePrint.realName = realName;
				//rechecking, otherwise childs don't use the realName parameter but the original uid provided in c.loadBlueprint
				loadBluePrint.checkForChilds(faction.getIdFaction());
				loadBluePrint.tradeNode = DatabaseEntry.removePrefixWOException(uid).equals(DatabaseEntry.removePrefixWOException(faction.getHomebaseUID()));

				ChildStats childStats = new ChildStats(false);
				ObjectArrayList<String> added = new ObjectArrayList<String>();

				id = loadBluePrint.spawnInDatabase(pos, state, 0, added, childStats, true);

				System.err.println("SPAWN IN DB: ADDED: " + added);

			} catch(EntityNotFountException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(EntityAlreadyExistsException e) {
				e.printStackTrace();
				String fuid = spec.type.dbPrefix + DatabaseEntry.removePrefixWOException(uid);
				id = state.getDatabaseIndex().getTableManager().getEntityTable().getIdForFullUID(fuid);
				System.err.println("[SERVER] Exception caught sucessfully. DB entry: " + fuid + "; returned id: " + id);
			} catch(SQLException e) {
				e.printStackTrace();
			} catch(StateParameterNotFoundException e) {
				e.printStackTrace();
			}

		}
		if(id > 0) {
			sys.getContingent().spawn(spec, id);
		}
		return id;
	}

	private long getLocalKey(Vector3i sys) {
		tmp3.sub(faction.npcFactionHomeSystem, sys);
		return ElementCollection.getIndex(tmp3);
	}

	public void iterateTaken(LvlIteratorTakenCallback b) {
		for(int i = 0; i < levels.length; i++) {
			levels[i].iterateTaken(b);
		}
	}

	public void iterateAllNonFull(LvlIteratorCallback b) {
		for(int i = 0; i < levels.length; i++) {
			if(!levels[i].isFull()) {
				levels[i].iterate(b);
			}
		}
	}

	public void grow(boolean inRuntime) {

		int highestLevel = -1;
		for(int i = 0; i < levels.length; i++) {
			if(!levels[i].isEmpty()) {
				highestLevel = i;
			}
		}

		while(highestLevel > levels.length - EXPANSION_SEARCH_SYSTEMS) {
			addLevel();
		}

		BestExpansionIterator b = new BestExpansionIterator();

		iterateAllNonFull(b);

		if(b.found()) {

			NPCSystemLevel addToLevel = levels[b.bestLvl];
			log("GROWING TO " + b.bestPos, LogLevel.NORMAL);
			NPCSystem sys = addSystemOnGrow(addToLevel, b.bestPos);
			if(inRuntime) {
				sys.applyToDatabase(true);
				try {
					assert (state.getUniverse().getStellarSystemFromStellarPos(b.bestPos).getOwnerFaction() == faction.getIdFaction());
				} catch(IOException e) {
					e.printStackTrace();
				}
				this.systemsGrown = true;
				state.getFactionManager().markedChangedContingentFactions.add(faction);

				state.getUniverse().getGalaxyManager().markZoneDirty(sys.system);
			}
			state.getFactionManager().getNpcFactionNews().grown(faction.getIdFaction(), sys.system);

		} else {
			onNoSystemFoundToGrow();
		}
	}

	private void onNoSystemFoundToGrow() {

	}

	protected double getExpansionValue(Vector3i system, short lvl) {
		return faction.getSystemEvaluator().getSystemValue(system, lvl);
	}

	public int getLevel(Vector3i pos) {
		int x = pos.x - getRoot().x;
		int y = pos.y - getRoot().y;
		int z = pos.z - getRoot().z;
		return Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z));

	}

	private void addLevel() {
		int newLvl = getLast().lvl;
		NPCSystemLevel[] old = this.levels;
		this.levels = new NPCSystemLevel[old.length + 1];
		System.arraycopy(old, 0, this.levels, 0, old.length);
		this.levels[this.levels.length - 1] = new NPCSystemLevel((short) (newLvl + 1));
	}

	private NPCSystem addSystemOnGrow(NPCSystemLevel lvl, Vector3i pos) {
		NPCSystem newSys = new NPCSystem(state, lvl.lvl, this);
		newSys.system = new Vector3i(pos);
		newSys.generate();
		newSys.calculateContingent(lvl.lvl, getLastHabitatedLevel() + 1, getTotalLevelFill());
		lvl.add(newSys);
		newSys.createStationPositions(new Random(newSys.system.hashCode() * lvl.lvl), -1);

		return newSys;
	}

	public void recalcSystem(NPCSystem sys) {
		sys.calculateContingent(sys.getLevel(), getLastHabitatedLevel() + 1, getTotalLevelFill());
	}

	/**
	 * updates inactive system
	 * @param sys
	 */
	private void onChangedSystem(NPCSystem sys) {
		assert (!sys.isActive()) : "System active while updating!";

		int stationsNow = sys.getTotalStationsProjected();
		int toAddStations = stationsNow - sys.stationMap.size();
		if(toAddStations > 0) {
			ShortArrayList local = sys.createStationPositions(new Random(sys.system.hashCode() * sys.getLevel()), toAddStations);

			StellarSystem stellar;
			try {
				stellar = state.getUniverse().getStellarSystemFromStellarPos(sys.system);
				for(short l : local) {


					Vector3i secPos = sys.getPosSectorFromLocalCoordinate(l, new Vector3i());

					//reset sector
					state.getDatabaseIndex().getTableManager().getSectorTable().removeSector(secPos);

					Vector3i localCoordinates = VoidSystem.getLocalCoordinates(secPos, new Vector3i());
					int index = stellar.getIndex(localCoordinates);

					stellar.setSectorType(index, SectorType.SPACE_STATION);
					stellar.setStationType(index, SpaceStationType.FACTION);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}

			state.getGameState().sendGalaxyModToClients(faction.getIdFaction(), sys.systemBaseUID, sys.systemBase);
		}

		sys.clearFleetsAndContingentsOnChange();

		boolean forced = false;
		sys.setChangedNT();

	}

	/**
	 * removes a system furthest from the center and cleans up any empty levels
	 * @return the system removed
	 */
	private NPCSystem removeSystemFromLast() {
		final int lastLvl = levels.length - 1;
		for(int i = lastLvl; i >= 0; i++) {

			if(levels[i].isEmpty()) {
				//remove now empty level
				NPCSystemLevel[] old = this.levels;
				this.levels = new NPCSystemLevel[old.length - 1];
				System.arraycopy(old, 0, this.levels, 0, this.levels.length);
			} else {
				NPCSystem lastAdded = levels[i].getLastAdded();
				levels[i].remove(lastAdded);
				if(levels[i].isEmpty()) {
					//remove now empty level
					NPCSystemLevel[] old = this.levels;
					this.levels = new NPCSystemLevel[old.length - 1];
					System.arraycopy(old, 0, this.levels, 0, this.levels.length);
				}
				lastAdded.onRemovedCleanUp();
				return lastAdded;
			}
		}
		throw new IllegalArgumentException("NPC faction is empty");
	}

	public void removeSystem(NPCSystem lastAdded) {

		log("Removing NPC System: " + lastAdded, LogLevel.NORMAL);
		levels[lastAdded.getLevel()].remove(lastAdded);

		if(levels[lastAdded.getLevel()].isEmpty() && levels.length - 1 == lastAdded.getLevel()) {
			//remove now empty level
			NPCSystemLevel[] old = this.levels;
			this.levels = new NPCSystemLevel[old.length - 1];
			System.arraycopy(old, 0, this.levels, 0, this.levels.length);
		}
		lastAdded.onRemovedCleanUp();
	}

	public void shrink() {
		removeSystemFromLast();
	}

	public NPCSystemLevel getLast() {
		return levels[levels.length - 1];
	}

	public NPCSystemLevel getOneBeforeLast() {
		return levels[levels.length - 2];
	}

	public void applyNonExisting() {
		for(NPCSystemLevel l : levels) {
			l.applyNonExisting();
		}
	}

	public void recalc() {
		for(NPCSystemLevel l : levels) {
			l.recalc();
		}
	}

	public Vector3i getRoot() {
		return levels[0].s[0].system;
	}

	public Tag toTagStructure() {

		Tag[] tg = new Tag[levels.length + 1];

		for(int i = 0; i < levels.length; i++) {
			tg[i] = levels[i].toTagStructure();
		}
		tg[levels.length] = FinishTag.INST;

		Tag struct = new Tag(Type.STRUCT, null, tg);

		return new Tag(Type.STRUCT, null, new Tag[] {new Tag(Type.BYTE, null, VERSION), new Tag(Type.INT, null, idGen), new Tag(Type.INT, null, (getLastHabitatedLevel() + 1)), new Tag(Type.FLOAT, null, getTotalLevelFill()), struct, new Tag(Type.BYTE, null, systemsGrown ? (byte) 1 : (byte) 0), FinishTag.INST});
	}

	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		byte version = t[0].getByte();
		idGen = t[1].getInt();

		int maxLevel = t[2].getInt();
		float maxFillLevel = t[3].getFloat();

		Tag[] lvlTags = t[4].getStruct();

		levels = new NPCSystemLevel[lvlTags.length - 1];
		for(int i = 0; i < lvlTags.length - 1; i++) {
			levels[i] = new NPCSystemLevel((short) i);
			levels[i].fromTagStructure(lvlTags[i]);
		}

		this.systemsGrown = t[5].getByte() != 0;
//		assert(false);
//		if(faction.getName().contains("Trade")){
//			iterateTaken(new LvlIteratorTakenCallback() {
//				@Override
//				public void it(NPCSystem s) {
//					System.err.println("TAKEN BY "+faction.getName()+": "+s.system);
//				}
//			});
//		}
//		List<Vector3i> systems = getState().getDatabaseIndex().getSystemsByFaction(faction.getIdFaction(), new ObjectArrayList<Vector3i>());
//		for(Vector3i sys : systems){
//			if(!taken.contains(sys)){
//				assert(false);
//			}
//		}
	}

	public int getLastLevel() {
		return levels.length - 1;
	}

	public int getTotalSystems() {
		return totalSystems;
	}

	public Vector3i getHomeBase() {
		assert (levels[0] != null);
		assert (levels[0].s[0] != null);
		assert (levels[0].s[0].getSystemBaseSector() != null);
		return levels[0].s[0].getSystemBaseSector();
	}

	public int getLastHabitatedLevel() {

		for(int i = levels.length - 1; i >= 0; i--) {
			if(levels[i].size > 0) {
				return i;
			}
		}
		return 0;
	}

	public void setChangedNT() {
		faction.setChangedNT();
	}

	public void checkNPCFactionSending(SendableGameState g, boolean force) {
		for(NPCSystemLevel lvl : levels) {
			lvl.checkNPCFactionSending(g, force);
		}
	}

	public float getTotalLevelFill() {
		float fill = 0;
		for(NPCSystemLevel lvl : levels) {
			fill += lvl.getFill();
		}
		return fill;
	}

	@Override
	public void log(String string, LogLevel l) {
		faction.log("[GEO]" + string, l);
	}

	public void onLoadedSector(Sector sec) {
		Vector3i system = VoidSystem.getContainingSystem(sec.pos, tmp3);
		if(taken.contains(system)) {
			NPCSystem sys = getSystem(system);

			if(sys != null) {
				sys.onLoadedSec(sec);
			}
		}
	}

	public void onUnloadedSector(Sector sec) {
		Vector3i system = VoidSystem.getContainingSystem(sec.pos, tmp3);
		if(taken.contains(system)) {
			NPCSystem sys = getSystem(system);
			if(sys != null) {
				sys.onUnloadedSec(sec);
			}
		}
	}

	public void produce() {
		int factoryEntities = getTotalAmountClass(BlueprintClassification.FACTORY_STATION, BlueprintClassification.NONE);

		log("Factory entities: " + factoryEntities, LogLevel.DEBUG);
		IntOpenHashSet mod = new IntOpenHashSet();

		produceRaw(mod);
		Short2IntOpenHashMap diff = new Short2IntOpenHashMap();
		faction.getDemandDiff(diff);
		//factoryEntities needs to change to total output of all factories combined
		produceFac(diff, factoryEntities, mod);

		if(mod.size() > 0) {
			faction.getInventory().sendInventoryModification(mod);
		}
	}

	private int getTotalAmountClass(BlueprintClassification... c) {
		int cnt = 0;

		for(NPCSystemLevel lvl : levels) {
			cnt += lvl.getTotalAmountClass(c);
		}
		return cnt;
	}

	//productionLimit should be something like the entire sum of each factory's output
	private void produceFac(Short2IntOpenHashMap diff, int productionLimit, IntOpenHashSet mod) {
		ShortArrayList types = new ShortArrayList(ElementKeyMap.keySet);
		Inventory inventory = faction.getInventory();

		int productionMulti = faction.getConfig().productionMultiplier;

		int steps = faction.getConfig().productionStepts;
		int maxAmount = productionLimit * productionMulti;
		int stepAmount = maxAmount / steps;

		log("Number of Factory Steps: " + steps + ", production amount limit: " + maxAmount + ", amount per step: " + stepAmount, LogLevel.DEBUG);

		int st = 0;
		while(st < steps) {
			//decrease production every step
			log("Step " + st, LogLevel.DEBUG);

			int amountRemaining = produceFacStep(diff, stepAmount, types, inventory, mod);
			if(amountRemaining == stepAmount) {
				log("Step: Nothing produced previous step so cancelling other steps", LogLevel.DEBUG);
				break;
			}
			st++;

//			if(pd <= 0.0001){
//				log("ERROR: COULDN'T PRODUCE; "+productionLimit+"; "+st, LogLevel.ERROR);
//				return;
//			}
		}


//		float pLim = (float)productionLimit / (float)steps;
//		float to = 0;
//		//do it in smaller steps, so that more diverse stuff gets made, and not just mases of one thing
//		while(to < 1f){
//			to+= pLim;
//		}
	}

	private int produceFacStep(Short2IntOpenHashMap diff, int productionLimit, ShortArrayList types, Inventory inventory, IntOpenHashSet mod) {

		Collections.shuffle(types);
		for(short type : types) {
			ElementInformation info = ElementKeyMap.getInfoFast(type);

			//filtering out the non craftable stuff
			if(!info.isOre() && !info.isCapsule() && !info.getConsistence().isEmpty()) {

				int maxProduced = -1;
				//Production weight * trade Multi - what you already have
				int demand = diff.get(type);

				if(demand > 0) {

					for(FactoryResource f : info.getConsistence()) {

						//how much is the surplus of the input,
						//subtracting the lowest demand needed of the input itself to allow it to expand or the bare minimum needed for other production stages
						int over = Math.max(0, inventory.getOverallQuantity(f.type) - Math.min(faction.getDemand(f.type, faction.getConfig().expensionDemandMult), faction.getProdWeightedDemand(f.type, faction.getConfig().expensionDemandMult)));

						//how many items can be produced with that surplus
						int mAm = over / f.count;

//					log("CHECK RES: to prod: "+ElementKeyMap.toString(type)+" Checking "+ElementKeyMap.toString(f.type)+"; canProd: "+mAm+"; (have: "+over+") Needed for one: "+f.count);
						if(maxProduced < 0 || mAm < maxProduced) {
							maxProduced = mAm;
						}
					}
					log("In Demand of: " + ElementKeyMap.toString(type) + " x " + demand + ", max able to produce: " + maxProduced + ", prodLim: " + productionLimit, LogLevel.DEBUG);
				} else {
					if(demand != 0) {
//					log("surplus of: "+ElementKeyMap.toString(type)+" ("+demand+")");
					}
				}
				if(maxProduced > 0 && productionLimit > 0 && demand > 0) {
					//actually only produce as many as demanded plus half
					int produced = Math.min(productionLimit, Math.min(demand, maxProduced));

					log("PRODUCING FAC: " + ElementKeyMap.toString(type) + "x" + produced, LogLevel.DEBUG);

					if(produced > 0) {
						StringBuffer ffFrom = new StringBuffer();
						for(FactoryResource f : info.getConsistence()) {
							inventory.decreaseBatch(f.type, f.count * produced, mod);
							ffFrom.append(ElementKeyMap.toString(f.type) + "x" + f.count * produced + ", ");
						}

						log("Produced FAC: " + ElementKeyMap.toString(type) + "x" + produced + " from " + ffFrom + "; demand(" + demand + ", maxProd: " + maxProduced + ")", LogLevel.FINE);
						mod.add(inventory.incExistingOrNextFreeSlot(type, produced));
					}

					productionLimit -= produced;

					if(productionLimit == 0) {
						break;
					}
					if(productionLimit < 0) {
						throw new IllegalArgumentException();
					}
				}


			}

		}

		if(productionLimit > 0) {
			log("----- Remaining limit at the end of step: " + productionLimit, LogLevel.DEBUG);
		}
		return productionLimit;
	}

	private void produceRaw(IntOpenHashSet mod) {

		int factoryEntities = getTotalAmountClass(BlueprintClassification.FACTORY_STATION, BlueprintClassification.NONE);

		Inventory inventory = faction.getInventory();
		for(short type : ElementKeyMap.typeList()) {
			ElementInformation info = ElementKeyMap.getInfoFast(type);
			if(info.consistence == null || info.consistence.isEmpty()) {
				produceRaw(mod, type, inventory, factoryEntities);
			}
		}
	}

	private void produceRaw(IntOpenHashSet mod, short type, Inventory inventory, int factoryEntities) {
		for(FixedRecipeProduct r : ElementKeyMap.capsuleRecipe.recipeProducts) {
			if(r.input[0].type == type) {
				int sourceAmount = inventory.getOverallQuantity(type);
				log("Raw Res available: " + ElementKeyMap.toString(type) + "x" + sourceAmount, LogLevel.FINE);

				//Uses total production value of factories instead, divided for each of the 16 types
				int conversionAmount = Math.min(factoryEntities * faction.getConfig().productionMultiplier / 16, sourceAmount);
				if(conversionAmount > 0) {
					for(FactoryResource o : r.output) {
						int outputAmount = o.count * conversionAmount;
						mod.add(inventory.incExistingOrNextFreeSlot(o.type, outputAmount));

						log("Produced from RAW: " + ElementKeyMap.toString(o.type) + "x" + outputAmount + " from " + ElementKeyMap.toString(type) + "x" + conversionAmount, LogLevel.DEBUG);
					}

					inventory.decreaseBatch(type, conversionAmount, mod);
				}
			}
		}
	}

	public NPCSystem getSystem(Vector3i system) {

		if(system != null && taken.contains(system)) {
			int level = getLevel(system);
			if(level < levels.length) {
				NPCSystem sys = levels[level].get(system);
				return sys;
			}

		}
		log("ERROR: System not found: " + system, LogLevel.DEBUG);
		return null;
	}

	public boolean removeEntity(long dbId, SegmentController entity, boolean lost) {
		Vector3i system = spawnedEntitiesPerSystem.get(dbId);
		if(system != null) {
			NPCSystem sys = getSystem(system);
			if(sys != null) {

				return sys.getContingent().spawnedEntities.remove(dbId, entity, lost);

			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	public GameServerState getState() {
		return state;
	}

	public void onCommandPartFinished(Fleet fleet, FleetState currentState) {
		Vector3i npcSystem = fleet.getNpcSystem();
		NPCSystem system = getSystem(npcSystem);
		if(system != null) {
			system.onCommandPartFinished(fleet, currentState);
		} else {
			log("Patrol ERROR: fleet system not found: " + system + " for " + fleet, LogLevel.ERROR);
		}
	}

	public void mine(long time) {
		for(NPCSystemLevel l : levels) {
			l.mine(time);
		}
	}

	public void resupply(long time) {
		for(NPCSystemLevel l : levels) {
			l.resupply(time);
		}
	}

	public void consume(long time) {
		for(NPCSystemLevel l : levels) {
			l.consume(time);
		}
	}

	public void scheduleSystemContingentChange(NPCSystem npcSystem) {
		this.scheduledChanges.add(npcSystem);
		state.getFactionManager().markedChangedContingentFactions.add(this.faction);
	}

	public boolean updateChangedSystems(long time) {

		if(this.systemsGrown) {

			iterateTaken(this::recalcSystem);
			this.systemsGrown = false;

			return true;
		}

		if(this.scheduledChanges.isEmpty()) {
			return false;
		}

		if(time - lastChangeUpdate > (60000 + Math.random() * 5000 - 2500)) {


			for(NPCSystem sys : this.scheduledChanges) {

				if(!sys.isActive()) {
					//only update inactive systems
					onChangedSystem(sys);
					lastChangeUpdate = time;
					break;
				} else {
					lastChangeUpdate = time;
				}
			}
		}

		return !this.scheduledChanges.isEmpty();
	}

	public NPCSystem findClosestFrom(Vector3i root, int levelsFromLastHabitated) {
		int lvlStart = getLastHabitatedLevel();
		levelsFromLastHabitated = Math.max(0, levelsFromLastHabitated);
		NPCSystem closest = null;
		float dist = 0;
		for(int lvl = lvlStart; lvl >= Math.max(0, lvlStart - levelsFromLastHabitated); lvl--) {
			NPCSystemLevel sysLvl = levels[lvl];

			for(int sg = 0; sg < sysLvl.size; sg++) {
				NPCSystem sys = sysLvl.s[sg];
				float nDist = Vector3i.getDisatance(sys.system, root);
				if(closest == null || nDist < dist) {
					dist = nDist;
					closest = sys;
				}
			}
		}

		return closest;
	}

	private class BestExpansionIterator implements LvlIteratorCallback {
		Vector3i bestPos = new Vector3i();
		double best = Double.NEGATIVE_INFINITY;
		short bestLvl = 0;
		private Vector3i tmp = new Vector3i();

		private Vector3i tmp2 = new Vector3i();

		public BestExpansionIterator() {
			assert (-123894398234d > best);
		}

		@Override
		public void handleTaken(int x, int y, int z, short lvl) {
		}

		private boolean containsLvl(Vector3i sys) {
			int range = 1;
			tmp2.set(1, 0, 0);
			tmp2.add(sys);
			if(taken.contains(tmp2)) {
				return true;
			}
			tmp2.set(-1, 0, 0);
			tmp2.add(sys);
			if(taken.contains(tmp2)) {
				return true;
			}
			tmp2.set(0, 1, 0);
			tmp2.add(sys);
			if(taken.contains(tmp2)) {
				return true;
			}
			tmp2.set(0, -1, 0);
			tmp2.add(sys);
			if(taken.contains(tmp2)) {
				return true;
			}
			tmp2.set(0, 0, 1);
			tmp2.add(sys);
			if(taken.contains(tmp2)) {
				return true;
			}
			tmp2.set(0, 0, -1);
			tmp2.add(sys);
			if(taken.contains(tmp2)) {
				return true;
			}
			return false;
		}

		@Override
		public void handleFree(int x, int y, int z, short lvl) {

			tmp.set(x, y, z);
			if(containsLvl(tmp)) {
				tmp.set(x, y, z);
				double exp;
				long localKey = getLocalKey(tmp);
				if(!valueCache.containsKey(localKey)) {
					exp = getExpansionValue(tmp, lvl);
					valueCache.put(localKey, exp);
				} else {
					exp = valueCache.get(localKey);
				}

				if(exp == NPCFactionSystemEvaluator.OTHER_FACTION_CODE) {
					Galaxy galaxy = getState().getUniverse().getGalaxyFromSystemPos(tmp);
					//that system is taken by someone else
					StarSystemResourceRequestContainer systemResources = state.getUniverse().updateSystemResourcesWithDatabaseValues(tmp, galaxy, new StarSystemResourceRequestContainer(), new GalaxyTmpVars());

					if(systemResources.factionId != 0 && systemResources.factionId != faction.getIdFaction()) {
						state.getFactionManager().diplomacyAction(DiplActionType.TERRITORY, faction.getIdFaction(), systemResources.factionId);
						log("someone has territory close to us. we might not like that (TERRITORY diplomacy action triggered)", LogLevel.NORMAL);
					}
				}
				if(exp > best) {
					best = exp;
					bestPos.set(tmp);
					bestLvl = lvl;
				}
			}
		}

		public boolean found() {
			return best != Double.NEGATIVE_INFINITY && best != NPCFactionSystemEvaluator.OTHER_FACTION_CODE;
		}
	}

	private class NPCSystemLevel {
		private final int maxSize;
		private short lvl;
		private int size;
		private NPCSystem[] s;


		public NPCSystemLevel(short lvl) {
			this.lvl = lvl;
			this.maxSize = getLevelSize(lvl);
			s = new NPCSystem[maxSize];
		}

		public void iterateTaken(LvlIteratorTakenCallback b) {
			for(int i = 0; i < size; i++) {
				b.it(s[i]);
			}
		}

		public void applyNonExisting() {
			for(int i = 0; i < size; i++) {
				NPCSystem sys = s[i];
				sys.applyToDatabase(false);
			}
		}

		public void recalc() {
			for(int i = 0; i < size; i++) {
				NPCSystem sys = s[i];
				sys.calculateContingent(lvl, getLastHabitatedLevel() + 1, getTotalLevelFill());
			}
		}

		public float getFill() {
			return (float) size / (float) maxSize;
		}

		public NPCSystem getLastAdded() {
			return s[size - 1];
		}

		public void iterate(LvlIteratorCallback it) {
			if(lvl == 0) {
				if(taken.contains(getRoot())) {
					it.handleTaken(getRoot().x, getRoot().y, getRoot().z, lvl);
				} else {
					it.handleFree(getRoot().x, getRoot().y, getRoot().z, lvl);
				}
				return;
			}
			int c = 0;
			Vector3i tmp = new Vector3i();
			for(int x = -lvl; x <= lvl; x++) {
				for(int z = -lvl; z <= lvl; z++) {
					int y = lvl;
					tmp.set(x, y, z);
					tmp.add(getRoot());
					if(taken.contains(tmp)) {
						it.handleTaken(tmp.x, tmp.y, tmp.z, lvl);
					} else {
						it.handleFree(tmp.x, tmp.y, tmp.z, lvl);
					}
					c++;
					y = -lvl;
					tmp.set(x, y, z);
					tmp.add(getRoot());
					if(taken.contains(tmp)) {
						it.handleTaken(tmp.x, tmp.y, tmp.z, lvl);
					} else {
						it.handleFree(tmp.x, tmp.y, tmp.z, lvl);
					}
					c++;
				}
			}

			for(int y = -lvl + 1; y <= lvl - 1; y++) {
				for(int x = -lvl; x <= lvl; x++) {
					int z = lvl;
					tmp.set(x, y, z);
					tmp.add(getRoot());
					if(taken.contains(tmp)) {
						it.handleTaken(tmp.x, tmp.y, tmp.z, lvl);
					} else {
						it.handleFree(tmp.x, tmp.y, tmp.z, lvl);
					}
					c++;
					z = -lvl;
					tmp.set(x, y, z);
					tmp.add(getRoot());
					if(taken.contains(tmp)) {
						it.handleTaken(tmp.x, tmp.y, tmp.z, lvl);
					} else {
						it.handleFree(tmp.x, tmp.y, tmp.z, lvl);
					}
					c++;
				}
				//don't include first and last one on Z, because that is already covered by the X iteration
				for(int z = -lvl + 1; z <= lvl - 1; z++) {
					int x = lvl;
					tmp.set(x, y, z);
					tmp.add(getRoot());

					if(taken.contains(tmp)) {
						it.handleTaken(tmp.x, tmp.y, tmp.z, lvl);
					} else {
						it.handleFree(tmp.x, tmp.y, tmp.z, lvl);
					}
					c++;
					x = -lvl;
					tmp.set(x, y, z);
					tmp.add(getRoot());
					if(taken.contains(tmp)) {
						it.handleTaken(tmp.x, tmp.y, tmp.z, lvl);
					} else {
						it.handleFree(tmp.x, tmp.y, tmp.z, lvl);
					}
					c++;
				}
			}
			assert (c == maxSize) : lvl + " -> " + c + "/" + maxSize;
		}

		public boolean isFull() {
			return maxSize == size;
		}

		public boolean isEmpty() {
			return size == 0;
		}

		private void add(NPCSystem sys) {
			if(size == s.length) {
				throw new IndexOutOfBoundsException("Index: " + size);
			}
			s[size] = sys;
			if(sys.id < 0) {
				sys.id = ++idGen;
			}
			taken.add(sys.system);
			addResourcesToTotal(sys);
			size++;
			totalSystems++;
			totalWeight += getLevelWeight(lvl);
			assert (sys.distanceFactor >= 0);
			totalDistanceWeight += sys.distanceFactor;
		}

		private void addResourcesToTotal(NPCSystem sys) {
			for(int i = 0; i < VoidSystem.RESOURCES; i++) {
				totalResources[i] += sys.resources.res[i];
			}
		}

		private void removeResourcesFromTotal(NPCSystem sys) {
			for(int i = 0; i < VoidSystem.RESOURCES; i++) {
				totalResources[i] += sys.resources.res[i];
			}
		}

		public void remove(NPCSystem sys) {
			if(size == 0) {
				throw new IndexOutOfBoundsException("Index: " + size);
			}
			NPCSystem[] old = s;
			s = new NPCSystem[old.length];
			int c = 0;
			for(int i = 0; i < size; i++) {
				if(!sys.equals(old[i])) {
					s[c] = old[i];
					c++;
				}
			}
			removeResourcesFromTotal(sys);
			taken.remove(sys.system);
			totalWeight -= getLevelWeight(lvl);
			totalDistanceWeight -= sys.distanceFactor;
			size--;

			totalSystems--;
		}

		public void fromTagStructure(Tag tag) {
			Tag[] t = (Tag[]) tag.getValue();

			lvl = (Short) t[0].getValue();
			size = (Integer) t[1].getValue();


			Tag[] lg = t[2].getStruct();
			assert (lg != null);
			for(int i = 0; i < size; i++) {
				assert (lg[i] != null);
				NPCSystem sys = new NPCSystem(state, lvl, NPCSystemStructure.this);
				sys.fromTagStructure(lg[i], lvl);
				s[i] = sys;
				addResourcesToTotal(s[i]);
				taken.add(s[i].system);
				totalSystems++;
				totalWeight += getLevelWeight(lvl);
				assert (sys.distanceFactor >= 0);
				totalDistanceWeight += sys.distanceFactor;
			}
		}

		public Tag toTagStructure() {

			Tag[] tg = new Tag[size + 1];

			for(int i = 0; i < size; i++) {
				tg[i] = s[i].toTagStructure();
			}
			tg[size] = FinishTag.INST;

			Tag struct = new Tag(Type.STRUCT, null, tg);

			return new Tag(Type.STRUCT, null, new Tag[] {new Tag(Type.SHORT, null, lvl), new Tag(Type.INT, null, size), struct, FinishTag.INST});

		}

		public void checkNPCFactionSending(SendableGameState g, boolean force) {
			for(int i = 0; i < size; i++) {
				s[i].checkNPCFactionSending(g, force);
			}
		}

		public int getTotalAmountClass(BlueprintClassification... c) {
			int cnt = 0;
			for(int i = 0; i < size; i++) {
				cnt += s[i].getTotalAmountClass(c);
			}
			return cnt;
		}

		public NPCSystem get(Vector3i system) {
			for(int i = 0; i < size; i++) {
				if(s[i].system.equals(system)) {
					return s[i];
				}
			}
			return null;
		}

		public void mine(long time) {
			for(int i = 0; i < size; i++) {
				NPCSystem sys = s[i];
				sys.mine(time);
			}

		}

		public void resupply(long time) {
			for(int i = 0; i < size; i++) {
				NPCSystem sys = s[i];
				sys.resupply(time);
			}

		}

		public void consume(long time) {
			for(int i = 0; i < size; i++) {
				NPCSystem sys = s[i];
				sys.consume(time);
			}
		}
	}


}
