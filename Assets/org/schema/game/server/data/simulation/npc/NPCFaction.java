package org.schema.game.server.data.simulation.npc;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.config.ConfigParserException;
import org.schema.common.util.LogInterface;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.trade.TradeManager;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.NPCFactionInventory;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.TradePrice;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.network.objects.remote.RemoteSimpelCommand;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction.DiplActionType;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyReaction;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacy;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacy.NPCDipleExecType;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacyEntity;
import org.schema.game.server.data.simulation.npc.geo.NPCFactionSystemEvaluator;
import org.schema.game.server.data.simulation.npc.geo.NPCSystem;
import org.schema.game.server.data.simulation.npc.geo.NPCSystemStructure;
import org.schema.game.server.data.simulation.npc.geo.NPCTradeNode;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.*;


public class NPCFaction extends Faction implements LogInterface {

	public static final ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);
	private static final byte VERSION = 0;
	private final IntOpenHashSet sentFullDebugUpdate = new IntOpenHashSet();
	public Vector3i npcFactionHomeSystem = new Vector3i(0, 0, 0);
	public NPCSystemStructure structure;
	public long accumulatedTimeNPCFactionTurn;
	public Vector3i serverGalaxyPos;
	public int initialSpawn = -1;
	private NPCFactionSystemEvaluator systemEvaluator;
	private NPCFactionInventory inventory;
	private long seed;
	private Random random;
	private NPCFactionConfig config;
	private boolean changedNT;
	private long baseStationId;
	private NPCTradeController tradeController;
	private boolean initialized;
	private NPCDiplomacy diplomacy;
	private Int2IntOpenHashMap closeTerritoryFactions = new Int2IntOpenHashMap();
	private NPCFleetManager fleetManager;
	private Turn[] turns;
	private int turnPoint = 0;
	private FileHandler fileHandler;
	private Logger logger;
	private Int2ObjectOpenHashMap<String> clientReactions = new Int2ObjectOpenHashMap<String>();

	public NPCFaction(StateInterface state, int factionID) {
		super(state);
		this.setIdFaction(factionID);
		if(!isOnServer()) {
			if(((GameClientState) state).getGameState() != null) {
				Inventory inv = ((GameClientState) state).getGameState().getInventories().get(factionID);
				if(inv != null) {
					this.inventory = (NPCFactionInventory) inv;
				} else {
					this.inventory = new NPCFactionInventory(((GameStateInterface) state), factionID);
				}
			} else {
				this.inventory = new NPCFactionInventory(((GameStateInterface) state), factionID);
			}
		} else {
			this.inventory = new NPCFactionInventory(((GameStateInterface) state), factionID);
		}
		this.setAutoDeclareWar(true);
		diplomacy = new NPCDiplomacy(this);

		createTurns();

		if(isOnServer()) {
			this.fleetManager = new NPCFleetManager(this);
		}
	}

	public NPCFaction(StateInterface state, int factionId, String name, String description, Vector3i npcFactionHome) {
		super(state, factionId, name, description);
		if(!isOnServer()) {
			if(((GameClientState) state).getGameState() != null) {
				Inventory inv = ((GameClientState) state).getGameState().getInventories().get(factionId);
				if(inv != null) {
					this.inventory = (NPCFactionInventory) inv;
				} else {
					this.inventory = new NPCFactionInventory(((GameStateInterface) state), factionId);
				}
			} else {
				this.inventory = new NPCFactionInventory(((GameStateInterface) state), factionId);
			}
		} else {
			this.inventory = new NPCFactionInventory(((GameStateInterface) state), factionId);
		}
		this.npcFactionHomeSystem.set(npcFactionHome);
		this.setAutoDeclareWar(true);
		diplomacy = new NPCDiplomacy(this);
		createTurns();
		if(isOnServer()) {
			this.fleetManager = new NPCFleetManager(this);
		}
	}

	@Override
	public void initialize() {
		initialized = true;
		this.seed = (long) getName().hashCode() * (long) getIdFaction() + npcFactionHomeSystem.hashCode();
		random = new Random(seed);


		assert (getState() instanceof GameClientState || config != null);
		if(config != null) {
			config.initialize();
		}
	}

	public void getDemandDiff(Short2IntOpenHashMap out) {
		out.clear();
		for(short type : ElementKeyMap.keySet) {
			int have = inventory.getOverallQuantity(type);
			int want = Math.max(getProdWeightedDemand(type, config.expensionDemandMult), getDemand(type, config.expensionDemandMult));
			out.addTo(type, want - have);
		}
	}

	public int getDemand(short type, float multi) {
		if(ElementKeyMap.getInfoFast(type).isShoppable()) {
			int demandAmount = config.getDemandAmount(type);
			return (int) (Math.max(0, ((float) demandAmount) + (float) demandAmount) * multi);
		} else {
			return 0;
		}
	}

	public int getProdWeightedDemand(short type, float multi) {
		if(ElementKeyMap.getInfoFast(type).isShoppable()) {
			int demandAmount = config.getWeightedAmountWithProduction(type);
			return (int) (Math.max(0, demandAmount + demandAmount * multi));
		} else {
			return 0;
		}
	}

	public int getTradeDemand(short type, float multi) {
		if(ElementKeyMap.getInfoFast(type).isShoppable()) {
			//Take the highest demand of either normal demand (the actual blocks you need to expand)
			//or the blocks you need to fabricate the ones you need
			int normalDemand = config.getDemandAmount(type);
			int prodDemand = config.getWeightedAmountWithProduction(type);
			int demandAmount = Math.max(normalDemand, prodDemand);
			return (int) (Math.max(structure.getTotalSystems() * config.minDemandPerSystem, demandAmount + demandAmount * multi));
		} else {
			return 0;
		}
	}

	public boolean canExpand() {
		boolean canExpand = true;
		for(short type : ElementKeyMap.typeList()) {
			int demand = getDemand(type, config.expensionDemandMult);
			int available = getInventory().getOverallQuantity(type);
			if(available < demand) {
				log(String.format("Cannot expand, need resource: %-40s current: %8d demand: %8d --> %8d", ElementKeyMap.toString(type), available, demand, (demand - available)), LogLevel.NORMAL);
				//log("Cannot expand, Need Resource: "+ElementKeyMap.toString(type)+" x "+available+" / "+demand+" needed ("+(demand - available)+")", LogLevel.NORMAL);

				canExpand = false;
			}
		}
		return canExpand;
	}

	public void inventoryToDatabase() {
		TradePrices pp = getTradeNode().getTradePricesInstance((GameServerState) getState());
		List<TradePriceInterface> prices = pp.getPrices();
		Short2ObjectOpenHashMap<TradePriceInterface> bMap = new Short2ObjectOpenHashMap<TradePriceInterface>();
		Short2ObjectOpenHashMap<TradePriceInterface> sMap = new Short2ObjectOpenHashMap<TradePriceInterface>();
		for(TradePriceInterface p : prices) {
			if(p.isBuy()) {
				bMap.put(p.getType(), p);
			} else {
				sMap.put(p.getType(), p);
			}
		}
		for(short type : ElementKeyMap.typeList()) {
			int q = getInventory().getOverallQuantity(type);

			if(q == 0) {
				TradePriceInterface tb = bMap.get(type);
				if(tb != null) {
					tb.setAmount(0);
				}
				tb = sMap.get(type);
				if(tb != null) {
					tb.setAmount(0);
				}
			} else {
				TradePriceInterface tb = bMap.get(type);
				if(tb != null) {
					tb.setAmount(q);
				} else {
					prices.add(new TradePrice(type, q, -1, -1, false));
				}
				tb = sMap.get(type);
				if(tb != null) {
					tb.setAmount(q);
				} else {
					prices.add(new TradePrice(type, q, -1, -1, true));
				}
			}
		}
		try {
			((GameServerState) getState()).getDatabaseIndex().getTableManager().getTradeNodeTable().setTradePrices(getTradeNode().getEntityDBId(), getInventory().getVolume(), getCredits(), prices);
		} catch(SQLException e1) {
			e1.printStackTrace();
		}


	}

	public void expandTurn() {
		if(canExpand()) {
			log("EXPANDING! (will consume " + (StringTools.formatPointZero(config.expensionDemandConsumeMult * 100f)) + "% of demand)", LogLevel.NORMAL);

			IntOpenHashSet mod = new IntOpenHashSet();
			for(short type : ElementKeyMap.typeList()) {
				int demand = getDemand(type, config.expensionDemandConsumeMult);
				if(getInventory().getOverallQuantity(type) > 0) {
					getInventory().decreaseBatch(type, demand, mod);
					if(demand != 0) {
						log(String.format("EXPANDING! Removing from inventory: %-40s removing %8d", ElementKeyMap.toString(type), demand), LogLevel.NORMAL);
					}
				}
			}


			getInventory().sendInventoryModification(mod);

			inventoryToDatabase();

			structure.grow(true);
		} else {
			log("Cannot expand, Not enough resources", LogLevel.NORMAL);
		}
	}

	private void createTurns() {
		turns = new Turn[TurnType.values().length];
		for(int i = 0; i < TurnType.values().length; i++) {
			turns[i] = new Turn();
			turns[i].active = true;
			turns[i].type = TurnType.values()[i];
		}
	}

	public Turn getTurn(TurnType t) {
		for(int i = 0; i < turns.length; i++) {
			if(turns[i].type == t) {
				return turns[i];
			}
		}
		return null;
	}

	public Tag getTurnsTag() {
		Tag[] t = new Tag[turns.length + 1];
		t[t.length - 1] = FinishTag.INST;
		for(int i = 0; i < turns.length; i++) {
			t[i] = new Tag(Type.STRUCT, null, new Tag[] {new Tag(Type.STRING, null, turns[i].type.name()), new Tag(Type.BYTE, null, turns[i].active ? (byte) 1 : (byte) 0), FinishTag.INST,});
		}
		return new Tag(Type.STRUCT, null, null);
	}

	public void getTurnsTag(Tag tag) {
		Tag[] t = tag.getStruct();

		for(int i = 0; i < t.length - 1; i++) {
			Tag[] m = t[i].getStruct();
			TurnType c = TurnType.valueOf(m[0].getString());
			if(c != null) {
				Turn turn = getTurn(c);
				if(turn != null) {
					turn.active = m[1].getByte() != 0;
				}
			}
		}
	}

	/**
	 *
	 * executes all parts of a turn in order over multuiple update
	 * to reduce lag
	 *
	 * @param time
	 * @return true if the full turn is done
	 */
	public boolean turn(long time) {
		assert (initialized);
		if(!initialized) {
			System.err.println("Exception: Faction " + this + " not initialized");
		}
		if(getTradeNode() == null) {
			try {
				throw new Exception("Can't do turn because trade node missing on " + this);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		if(turnPoint == 0) {
			log("----------STARTING TURN", LogLevel.NORMAL);
		} else {
			log("----------CONTINUING TURN", LogLevel.NORMAL);
		}

		if(turnPoint < turns.length) {
			turns[turnPoint].execute(time);
			turnPoint = (turnPoint + 1) % turns.length;
		}

		if(turnPoint == 0) {
			log("----------ENDED TURN", LogLevel.NORMAL);
			return true;
		}
		return false;
	}

	public void turnFull(long time) {
		assert (initialized);
		log("----------STARTING TURN", LogLevel.NORMAL);

		for(Turn t : turns) {
			t.execute(time);
		}

		log("----------ENDED TURN", LogLevel.NORMAL);
	}

	public long getCredits() {
		return getTradeNode().getCredits();
	}

	public void createTradeNode(GameServerState state, long baseStationId) throws SQLException, IOException {
		NPCTradeNode node = new NPCTradeNode(this);
		node.setCapacity(getInventory().getCapacity());
		node.setVolume(getInventory().getVolume());
		node.setCredits(random.nextInt(Math.max(1, config.randomCredits + 1)) + config.baseCredits);
		node.setEntityDBId(baseStationId);
		node.setFactionId(getIdFaction());
		node.setOwners(new ObjectOpenHashSet<String>());
		node.setTradePermission(TradeManager.PERM_ALL_BUT_ENEMY);
		node.setSector(getHomeSector());
		node.setStationName(getHomebaseRealName());
		node.setSystem(VoidSystem.getContainingSystem(getHomeSector(), new Vector3i()));
		state.getDatabaseIndex().getTableManager().getTradeNodeTable().insertOrUpdateTradeNode(node);
		assert (node.getEntityDBId() != Long.MIN_VALUE);
		state.getUniverse().tradeNodesDirty.enqueue(baseStationId);

		this.baseStationId = baseStationId;

		state.getUniverse().getGalaxyManager().sendDirectTradeUpdateOnServer(baseStationId);
	}

	@Override
	public boolean isNPC() {
		return true;
	}

	@Override
	protected Tag toTagAdditionalInfo() {
		Tag versiontag = new Tag(Type.BYTE, null, VERSION);
		Tag npcHomeSysTag = new Tag(Type.VECTOR3i, null, npcFactionHomeSystem);
		Tag structureTag = structure.toTagStructure();
		Tag inventoryTag = inventory.toTagStructure();
		Tag tradeNodeTag = new Tag(Type.LONG, null, getTradeNode() != null ? getTradeNode().getEntityDBId() : Long.MIN_VALUE);
		Tag accTag = new Tag(Type.LONG, null, accumulatedTimeNPCFactionTurn);
		Tag galaxPosTag = new Tag(Type.VECTOR3i, null, serverGalaxyPos);
		return new Tag(Type.STRUCT, null, new Tag[] {versiontag, npcHomeSysTag, structureTag, inventoryTag, tradeNodeTag, accTag, galaxPosTag, new Tag(Type.BYTE, null, (byte) 0), //placeholder
				diplomacy.toTag(), FinishTag.INST});
	}

	public TradeNodeStub getTradeNode() {
		TradeNodeStub tradeNodeStub = ((GameServerState) getState()).getUniverse().getGalaxyManager().getTradeNodeDataById().get(baseStationId);
		if(tradeNodeStub == null) {
			try {
				throw new Exception("NPC TRADE NODE OF " + this + " NOT FOUND: " + baseStationId + "; ALL NODES; " + ((GameServerState) getState()).getUniverse().getGalaxyManager().getTradeNodeDataById());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return ((GameServerState) getState()).getUniverse().getGalaxyManager().getTradeNodeDataById().get(baseStationId);
	}

	@Override
	protected void fromTagAdditionalInfo(Tag tag) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
		Tag[] t = tag.getStruct();
		byte version = t[0].getByte();
		npcFactionHomeSystem = t[1].getVector3i();
		structure.fromTagStructure(t[2]);
		inventory.fromTagStructure(t[3]);
		baseStationId = t[4].getLong();
		accumulatedTimeNPCFactionTurn = t[5].getLong();
		serverGalaxyPos = t[6].getVector3i();
//		fromCloseTerritoryTag(t[7]);
		diplomacy.fromTag(t[8]);
	}

	@Override
	public void initializeWithState(GameServerState state) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
		super.initializeWithState(state);
		this.structure = new NPCSystemStructure(state, this);
		this.systemEvaluator = new NPCFactionSystemEvaluator(this);
		this.tradeController = new NPCTradeController(this, state);
	}

	public void onCreated() {
		addHook = () -> {
			if(!structure.isCreated()) {
				config.generate(NPCFaction.this);
				structure.createNew();
				structure.recalc();
				structure.applyNonExisting();
				assert (getTradeNode().getEntityDBId() != Long.MIN_VALUE);
				tradeController.fillInitialInventoryAndTrading(random);
			} else {
				structure.recalc();
			}
		};
	}

	@Override
	public void onEntityDestroyedServer(SegmentController segmentController) {
		onLostEntity(segmentController.dbId, segmentController, true);
	}

	@Override
	public void onEntityOverheatingServer(SegmentController segmentController) {
		onLostEntity(segmentController.dbId, segmentController, true);
	}

	public void onLostEntity(long dbId, SegmentController entity, boolean lost) {
		structure.removeEntity(dbId, entity, lost);
	}

	public NPCFactionSystemEvaluator getSystemEvaluator() {
		assert (getState() instanceof ServerStateInterface);
		return systemEvaluator;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(NPCFactionInventory inventory) {
		this.inventory = inventory;
	}

	public NPCFactionConfig getConfig() {
		return config;
	}

	public void setConfig(NPCFactionConfig config) {
		assert (this.config == null);
		this.config = config;

	}

	public void setChangedNT() {
		assert (getState() instanceof ServerStateInterface);
		this.changedNT = true;
		if(((FactionState) getState()).getFactionManager() != null) {
			((FactionState) getState()).getFactionManager().setNPCFactionChanged();
		}
	}

	public void checkNPCFactionSendingDebug(SendableGameState g, boolean force) {
		if(changedNT || force) {

			structure.checkNPCFactionSending(g, force);


			if(!force) {
				changedNT = false;
			}
		}
	}

	public void checkNPCDiplomacyNT(SendableGameState g, boolean force) {

		diplomacy.checkNPCFactionSending(g, force);

	}

	public boolean isLogMode(LogMode l) {
		return ServerConfig.NPC_LOG_MODE.getInt() == l.ordinal();
	}

	@Override
	public void log(String string, LogLevel l) {
		if(getName() == null || !isOnServer()) {
			if(l == LogLevel.ERROR || l == LogLevel.NORMAL || l == LogLevel.DEBUG) {
				System.err.println(!isOnServer() ? "[CLIENT]" : "[SERVER]" + "[NPC][" + getName() + "]" + string);
			}
			return;
		}

		if(isLogMode(LogMode.FILE) && fileHandler == null) {
			File dir = new File("./logs/npc/");
			dir.mkdirs();
			try {
				final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
				final Date now = new Date();
				String loggerName = getName() + "_" + getIdFaction();
				fileHandler = new FileHandler("./logs/npc/lognpc_" + loggerName + ".%g.log", 1024 * 1024 * 4, 20);
				fileHandler.setFormatter(new Formatter() {
					@Override
					public String format(LogRecord record) {
						now.setTime(record.getMillis());
						return "[" + sdfDate.format(now) + "] " + record.getMessage() + "\n";
					}
				});
				LogManager man = LogManager.getLogManager();
				logger = Logger.getLogger(loggerName);
				assert (logger != null);
				logger.addHandler(fileHandler);
				logger.setLevel(Level.ALL);
				man.addLogger(logger);

			} catch(SecurityException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		if(l == LogLevel.ERROR || l == LogLevel.NORMAL) {
			if(logger == null) {
				System.err.println("[NPC][" + getName() + "]" + string);
			} else {
				logger.log(Level.INFO, string);
			}
		}
	}

	public NPCTradeController getTradeController() {
		assert (getState() instanceof GameServerState);
		return tradeController;
	}

	public long getSeed() {
		return seed;
	}

	public void populateAsteroids(Sector sector, SectorType sectorType, Random random) throws IOException {
		structure.populateAsteroids(sector, sectorType, random);
	}

	public void populateSpaceStation(Sector sector, Random random) {
		structure.populateSpaceStation(sector, random);
	}

	public void populateAfterAsteroids(Sector sector, SectorType sectorType, Random random) {
		//maybe outposts?
	}

	public void populateGasPlanet(Sector sector, SectorType sectorType, SectorInformation.GasPlanetType planetType, Random random) {
		sector.populateGasPlanetSector(sector.getState());
	}

	public void populatePlanet(Sector sector, SectorType sectorType, PlanetType planetType, Random random) {
		sector.populatePlanetIcoSector(sector.getState());
	}

	public void getWeightedInventory(ElementCountMap reservedResources, float weight) {

	}

	@Override
	public void onAddedSectorSynched(Sector sec) {
		structure.onLoadedSector(sec);
	}

	@Override
	public void onRemovedSectorSynched(Sector sec) {
		structure.onUnloadedSector(sec);
	}

	public void onCommandPartFinished(Fleet fleet, FleetState currentState) {
		assert (isOnServer());
		structure.onCommandPartFinished(fleet, currentState);
	}

	public void lostResources(ElementCountMap cargo) {
		IntOpenHashSet changed = new IntOpenHashSet();
		getInventory().decreaseBatchIgnoreAmount(cargo, changed);
		getInventory().sendInventoryModification(changed);


		log("Lost resources: " + cargo.getTotalAmount(), LogLevel.NORMAL);
	}

	public long getTimeBetweenFactionTurns() {
		return config.timeBetweenTurnsMS;
	}

	public boolean isSystemActive(Vector3i npcSystem) {
		assert (getState() instanceof ServerStateInterface);
		NPCSystem system = structure.getSystem(npcSystem);

		return system != null && system.isActive();
	}

	public void onUncachedFleet(Fleet f) {
		if(isOnServer()) {
			NPCSystem system = structure.getSystem(f.getNpcSystem());
			if(system != null) {
				system.onUnachedFleet(f);
			}
		}
	}


	public void diplomacyAction(DiplActionType type, long otherDbId) {
		diplomacy.diplomacyAction(type, otherDbId);
	}

	//	public int getCloseTerritoryFactions(int fid) {
//		return closeTerritoryFactions.get(fid);
//	}
//
//	
//	
//	public void modCloseTerritoryFactions(int fid, int mod) {
//		closeTerritoryFactions.add(fid, mod);
//	}
//	public void fromCloseTerritoryTag(Tag tag){
//		Tag[] t = tag.getStruct();
//		for(int i = 0; i < t.length-1; i++){
//			Tag[] struct = t[i].getStruct();
//			closeTerritoryFactions.put(struct[0].getInt(), struct[1].getInt());
//		}
//	}
	public Tag getCloseTerritoryTag() {
		Tag[] t = new Tag[closeTerritoryFactions.size() + 1];
		t[t.length - 1] = FinishTag.INST;
		int i = 0;
		for(Entry<Integer, Integer> e : closeTerritoryFactions.entrySet()) {
			t[i] = new Tag(Type.STRUCT, null, new Tag[] {new Tag(Type.INT, null, e.getKey().intValue()), new Tag(Type.INT, null, e.getValue().intValue()), FinishTag.INST});
			i++;
		}
		return new Tag(Type.STRUCT, null, t);
	}

	public NPCDiplomacy getDiplomacy() {
		return diplomacy;
	}

	public void setDiplomacy(NPCDiplomacy diplomacy) {
		this.diplomacy = diplomacy;
	}

	@Override
	public void serializeExtra(DataOutputStream buffer) throws IOException {
		for(Turn t : turns) {
			buffer.writeBoolean(t.active);
		}

		buffer.writeShort(config.getDiplomacyReactions().size());
		for(DiplomacyReaction r : config.getDiplomacyReactions()) {
			buffer.writeShort((short) r.index);
			buffer.writeUTF(r.name);
		}
	}

	@Override
	public void deserializeExtra(DataInputStream stream) throws IOException {
		for(Turn t : turns) {
			t.active = stream.readBoolean();
		}

		short size = stream.readShort();
		for(int i = 0; i < size; i++) {
			int index = stream.readShort();
			String name = stream.readUTF();
			clientReactions.put(index, name);
		}
	}


	public void sendCommand(NPCFactionControlCommandType turnMod, Object... args) {

		NPCFactionControlCommand com = new NPCFactionControlCommand(getIdFaction(), turnMod, args);
		sendCommand(com);
	}

	private void sendCommand(NPCFactionControlCommand com) {
		((GameStateInterface) getState()).getGameState().getNetworkObject().simpleCommandQueue.add(new RemoteSimpelCommand(com, isOnServer()));
	}

	public void executeFactionCommand(NPCFactionControlCommand com) {
		NPCFactionControlCommandType c = NPCFactionControlCommandType.values()[com.getCommand()];

		switch(c) {
			case REQUEST_ALL:
				if(!sentFullDebugUpdate.contains(com.getUpdateSenderStateId())) {
					sentFullDebugUpdate.add(com.getUpdateSenderStateId());
					checkNPCDiplomacyNT(structure.getState().getGameState(), true);
				} else {
					checkNPCDiplomacyNT(structure.getState().getGameState(), false);
				}
				break;
			case REQUEST_STATUS: {
				Vector3i system = new Vector3i((Integer) com.getArgs()[0], (Integer) com.getArgs()[1], (Integer) com.getArgs()[2]);
				NPCSystem sys = structure.getSystem(system);
				if(sys != null) {
					sendCommand(NPCFactionControlCommandType.AWNSER_STATUS, system.x, system.y, system.z, (float) sys.status);
				}
				break;
			}
			case DIPLOMACY_ACTION: {
				DiplActionType t = DiplActionType.values()[(Integer) com.getArgs()[0]];
				long ent = (Long) com.getArgs()[1];

				diplomacy.diplomacyAction(t, ent);

				log("Manually triggered action " + t.name(), LogLevel.NORMAL);
				diplomacy.ntChanged(ent);
				break;
			}
			case MOD_POINTS: {
				int pnts = (Integer) com.getArgs()[0];
				long entId = (Long) com.getArgs()[1];
				NPCDiplomacyEntity dipl = diplomacy.entities.get(entId);
				if(dipl != null) {
					dipl.modPoints(pnts);
				} else {
					log("no entity found in diplomacy (maybe not created yet): " + entId, LogLevel.ERROR);
				}
				break;
			}
			case DIPLOMACY_REACTION: {
				int index = (Integer) com.getArgs()[0];
				long entId = (Long) com.getArgs()[1];

				for(DiplomacyReaction r : config.getDiplomacyReactions()) {
					if(r.index == index) {
						NPCDiplomacyEntity dipl = diplomacy.entities.get(entId);
						if(dipl != null) {
							dipl.triggerReactionManually(r);
							diplomacy.ntChanged(entId);
						} else {
							log("no entity found in diplomacy (maybe not created yet): " + entId, LogLevel.ERROR);
						}
						break;
					}
				}

				break;
			}
			case AWNSER_STATUS: {
				Vector3i system = new Vector3i((Integer) com.getArgs()[0], (Integer) com.getArgs()[1], (Integer) com.getArgs()[2]);
				float status = (Float) com.getArgs()[3];

				((GameStateInterface) getState()).getGameState().putClientNPCSystemStatus(system, status);

				break;
			}
			case DIPLOMACY_TRIGGER:
				diplomacy.trigger(NPCDipleExecType.values()[(Integer) com.getArgs()[0]]);
				break;
			case TURN_MOD:
				TurnType t = TurnType.values()[(Integer) com.getArgs()[0]];
				getTurn(t).active = (Boolean) com.getArgs()[1];
				if(isOnServer()) {
					sendCommand(com);
				}
				break;
			case TURN_TRIGGER:

				((FactionState) getState()).getFactionManager().scheduleTurn(this);
				break;

			case LOG_CREDIT_STATUS:
				String creds = "CREDITS: " + getCredits();
				log(creds, LogLevel.NORMAL);
				break;
			default:
				break;

		}
	}

	public NPCFleetManager getFleetManager() {
		assert (isOnServer());
		return fleetManager;
	}

	public void onAttackedFaction(Fleet fleet, EditableSendableSegmentController seg, Damager from) {
		NPCSystem system = structure.getSystem(fleet.getNpcSystem());
		if(system != null) {
			system.onAttackedFaction(fleet, seg, from);
		}
	}

	public void removeCompletely() {

		log("REMOVING FACTION COMPLETELY", LogLevel.NORMAL);

		long entityDBId = getTradeNode().getEntityDBId();
		GameServerState state = structure.getState();

		for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if(s instanceof SimpleTransformableSendableObject<?>) {
				SimpleTransformableSendableObject<?> m = (SimpleTransformableSendableObject<?>) s;
				if(m.getFactionId() == this.getIdFaction()) {
					m.markForPermanentDelete(true);
				}
			}
		}

		state.getDatabaseIndex().getTableManager().getTradeNodeTable().removeTradeNode(entityDBId);
		state.getUniverse().getGalaxyManager().sendDirectTradeUpdateOnServer(entityDBId);


		state.getDatabaseIndex().getTableManager().getEntityTable().removeFactionCompletely(this);


		log("FACTION HAS BEEN REMOVED COMPLETELY", LogLevel.NORMAL);
	}

	public boolean canAttackShips() {
		if(isOnServer()) {
			return config.canAttackShips;
		} else {
			return true;
		}
	}

	public boolean canAttackStations() {
		if(isOnServer()) {
			return config.canAttackStations;
		} else {
			return true;
		}
	}

	public Int2ObjectOpenHashMap<String> getClientReactions() {
		return clientReactions;
	}

	public void setClientReactions(Int2ObjectOpenHashMap<String> clientReactions) {
		this.clientReactions = clientReactions;
	}


	public enum NPCFactionControlCommandType {
		TURN_MOD(Integer.class, Boolean.class), TURN_TRIGGER(), DIPLOMACY_TRIGGER(Integer.class), LOG_CREDIT_STATUS(), REQUEST_STATUS(Integer.class, Integer.class, Integer.class), AWNSER_STATUS(Integer.class, Integer.class, Integer.class, Float.class), REQUEST_ALL(), DIPLOMACY_ACTION(Integer.class, Long.class), DIPLOMACY_REACTION(Integer.class, Long.class), MOD_POINTS(Integer.class, Long.class),
		;
		private Class<?>[] args;

		private NPCFactionControlCommandType(Class<?>... args) {
			this.args = args;
		}

		public void checkMatches(Object[] to) {
			if(args.length != to.length) {
				throw new IllegalArgumentException("Invalid argument count: Provided: " + Arrays.toString(to) + ", but needs: " + Arrays.toString(args));
			}
			for(int i = 0; i < args.length; i++) {
				if(!to[i].getClass().equals(args[i])) {
					System.err.println("Not Equal: " + to[i] + " and " + args[i]);
					throw new IllegalArgumentException("Invalid argument on index " + i + ": Provided: " + Arrays.toString(to) + "; cannot take " + to[i] + ":" + to[i].getClass() + ", it has to be type: " + args[i].getClass());
				}
			}
		}


	}


	public enum TurnType {
		MINING, PRODUCTION, RESUPPLY, CONSUME, TRADE, EXPAND, FLEETS, TRANSFER_TO_DB, RECALC_PRICES,
	}

	public class Turn {
		public boolean active;
		TurnType type;

		public void execute(long time) {
			if(active) {
				log("-----" + type.name() + " START", LogLevel.NORMAL);
				switch(type) {
					case CONSUME:
						structure.consume(time);
						break;
					case EXPAND:
						expandTurn();
						break;
					case MINING:
						structure.mine(time);
						break;
					case PRODUCTION:
						structure.produce();
						break;
					case RESUPPLY:
						structure.resupply(time);
						break;
					case TRADE:
						tradeController.tradeTurn();
						break;
					case FLEETS:
						fleetManager.fleetTurn();
						break;
					case TRANSFER_TO_DB:
						inventoryToDatabase();

						break;
					case RECALC_PRICES:

						try {
							tradeController.recalcPrices();
						} catch(SQLException e) {
							e.printStackTrace();
						} catch(IOException e) {
							e.printStackTrace();
						}
						break;
					default:
						throw new NullPointerException("UNKNOWN TURN");
				}

				log("-----" + type.name() + " END", LogLevel.NORMAL);
			} else {
				log("----------" + type.name() + " TURN INACTIVE", LogLevel.NORMAL);
			}
		}


	}


}
