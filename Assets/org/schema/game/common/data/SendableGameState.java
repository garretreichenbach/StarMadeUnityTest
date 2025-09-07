package org.schema.game.common.data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.ClientStatics;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.leaderboard.GUIScrollableLoaderboardList;
import org.schema.game.common.controller.FactionChange;
import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.activities.RaceManager;
import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.game.common.controller.rules.RulePropertyContainer;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.game.common.controller.trade.TradeManager;
import org.schema.game.common.controller.trade.manualtrade.ManualTradeManager;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.fleet.FleetStateInterface;
import org.schema.game.common.data.gamemode.AbstractGameMode;
import org.schema.game.common.data.gamemode.battle.KillerEntity;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.player.inventory.InventoryMultMod;
import org.schema.game.common.data.player.inventory.NPCFactionInventory;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.GalaxyRequestAndAwnser;
import org.schema.game.network.objects.LongBooleanPair;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.remote.RemoteGalaxyRequest;
import org.schema.game.network.objects.remote.RemoteInventory;
import org.schema.game.network.objects.remote.RemoteInventoryMultMod;
import org.schema.game.network.objects.remote.RemoteLongBoolean;
import org.schema.game.network.objects.remote.RemoteMetaObjectStateLess;
import org.schema.game.network.objects.remote.RemoteRuleSetManager;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFaction.NPCFactionControlCommandType;
import org.schema.game.server.data.simulation.npc.geo.NPCSystemStub;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class SendableGameState implements Sendable, InventoryHolder {

	public static final int TEXT_BLOCK_LIMIT = 240;
	public static final int TEXT_BLOCK_LINE_LIMIT = 10;
	private final StateInterface state;
	private final boolean onServer;
	private final ObjectArrayFIFOQueue<DebugServerObject> debugObjects = new ObjectArrayFIFOQueue<DebugServerObject>();
	private final FactionManager factionManager;
	private final CatalogManager catalogManager;
	private final ObjectArrayFIFOQueue<MetaObject> metaObjectsToAnnounce = new ObjectArrayFIFOQueue<MetaObject>();
	private final ObjectArrayList<AbstractGameMode> gameModes = new ObjectArrayList<AbstractGameMode>();
	private final IntOpenHashSet frozenSectors = new IntOpenHashSet();
	public boolean leaderBoardChanged;
	public GUIScrollableLoaderboardList leaderboardGUI;
	public float sunMinIntensityDamageRange = 1.42f;
	public double massLimitShip = -1;
	public double massLimitPlanet = -1;
	public double massLimitStation = -1;
	public int blockLimitShip = -1;
	public int blockLimitPlanet = -1;
	public int blockLimitStation = -1;
	private RulePropertyContainer ruleProperties;
	public final InventoryMap imap;
	
	String battlemodeDesc = "[What is this?]\n"
			+ "BattleMode is a first test for StarMade to\n"
			+ "handle all kinds of game modes in the future!\n"
			+ "\n"
			+ "To join battle, you have to build a ship and \n"
			+ "join a battle faction before the countdown ends!\n"
			+ "Your ship will copied before you enter battle,\n"
			+ "so you don't have to care about resources!\n"
			+ "After a winner is determined, the countdown resets.\n"
			+ "\n"
			+ "May the best player win!\n";
	float cachedYear;
	private int id;
	private boolean markedForDeleteVolatile;
	private boolean markedForDeleteVolatileSent;
	private NetworkGameState networkGameState;
	private float maxGalaxySpeed;
	private float linearDamping;
	private float turnSpeedDivisor;
	private float rotationalDamping;
	private Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>> clientLeaderboard = new Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>>();
	private Object2IntOpenHashMap<String> clientDeadCount = new Object2IntOpenHashMap<String>();
	private boolean additiveProjectiles;
	private boolean dynamicPrices;
	private String serverMessage;
	private final ManualTradeManager manualTradeManager;
	private String serverDescription = ServerConfig.SERVER_LIST_DESCRIPTION.getString().substring(0, Math.min(ServerConfig.SERVER_LIST_DESCRIPTION.getString().length(), 128));
	private String serverName = ServerConfig.SERVER_LIST_NAME.getString().substring(0, Math.min(ServerConfig.SERVER_LIST_NAME.getString().length(), 64));
	private Integer recipeBlockCost;
	private float relativeProjectiles;
	private float weaponRangeReference;
	private boolean writtenForUnload;
	private boolean serverDeployedBlockBehavior;
	private long updateTime = System.currentTimeMillis();
	private float sectorSize;
	private int maxBuildArea;
	private String currentGameModeOutput = "";
	private String lastClientShownGameModeMessage;
	private String clientBattlemodeSettings = "";
	private short lastUpdateRotationProg;
	private float planetSizeMean;
	private float planetSizeDeviation;
	private boolean lockFactionShips;
	private boolean buyBBWithCredits;
	private boolean weightedCenterOfMass;
	private float shopRebootCostPerSecond;
	private float shopArmorRepairPerSecond;
	private int sectorsToExploreForSystemScan;
	private int stationCostClient;
	private int factionKickInactiveTimeLimit;
	private final RaceManager raceManager;
	private boolean allowPersonalInvOverCap;
	private int maxChainDocking = 25;
	private boolean onlyAddFactionToFleet;
	private final TradeManager tradeManager;
	private ObjectArrayFIFOQueue<Inventory> ntInventoryAdds = new ObjectArrayFIFOQueue<Inventory>();
	private ObjectArrayFIFOQueue<Inventory>  ntInventoryRemoves = new ObjectArrayFIFOQueue<Inventory>();
	private final ObjectArrayFIFOQueue<InventoryMultMod> ntInventoryMultMods = new ObjectArrayFIFOQueue<InventoryMultMod>();
	private final Object2ObjectOpenHashMap<Vector3i, NPCSystemStub> clientNPCSystemMap = new Object2ObjectOpenHashMap<Vector3i, NPCSystemStub>();
	private final Object2FloatOpenHashMap<Vector3i> clientNPCStatusMap = new Object2FloatOpenHashMap<Vector3i>();
	private final ObjectArrayFIFOQueue<NPCSystemStub> clientNPCSystemToAdd = new ObjectArrayFIFOQueue<NPCSystemStub>();
	private boolean npcDebug;
	private float npcFleetSpeedLoaded;
	private boolean fow;
	private String npcDebugShopOwners = "";
	
	private final ConfigPool configPool;
	private final Long2BooleanOpenHashMap modulesEnabledByDefault = new Long2BooleanOpenHashMap();
	private int spawnProtection;
	private RuleSetManager ruleManager;
	private final ObjectArrayFIFOQueue<RuleSetManager> receivedRuleSetManagers = new ObjectArrayFIFOQueue<RuleSetManager>();
	private int aiWeaponSwitchDelay = 1000;
	private boolean allowFactoriesOnShips;
	private boolean shipyardIgnoreStructure;
	private boolean ignoreDockingArea;
	@Override
	public SendableType getSendableType() {
		return SendableTypes.SENDABLE_GAME_STATE;
	}
	public SendableGameState(StateInterface state) throws IOException {
		clientNPCStatusMap.defaultReturnValue(-2f);
		this.state = state;
		onServer = state instanceof ServerStateInterface;
		imap = new InventoryMap();
		if (onServer) {
			maxGalaxySpeed = ServerConfig.THRUST_SPEED_LIMIT.getInt();
			sectorSize = ((GameServerState) state).getSectorSizeWithoutMargin();
			
		}else{
		}
		
		factionManager = new FactionManager(this);
		if(onServer) {
			((GameServerState)state).getController().getSectorListeners().add(factionManager);
		}
		configPool = new ConfigPool();
//		if(isOnServer()){
			try {
				configPool.readConfigFromFile(configPool.getPath(true));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
//		}
		catalogManager = new CatalogManager(this);
		raceManager = new RaceManager(this);
		tradeManager = new TradeManager(this.state);
		manualTradeManager = new ManualTradeManager(this.state);
		
		ruleManager = new RuleSetManager((GameStateInterface) this.state);
		if(onServer) {
			ruleProperties = new RulePropertyContainer(ruleManager);
			this.ruleProperties.state = (GameStateInterface) this.state;
		}else {
			ruleProperties = null;
		}
		if (onServer) {
			try {
				readServerMessage();
			} catch (IOException e) {
				e.printStackTrace();
				serverMessage = "";
			}
			ruleManager.initializeOnServer();
			ruleProperties.initializeOnServer();
//			if(f instanceof NPCFaction){
//				((GameStateInterface)getState()).getGameState().getInventories().put(f.getIdFaction(), ((NPCFaction) f).getInventory());
//			}
			
			modulesEnabledByDefault.put(PlayerUsableInterface.USABLE_ID_SPACE_SCAN, (ServerConfig.SHORT_RANGE_SCAN_DRIVE_ENABLED_BY_DEFAULT.isOn()));
			modulesEnabledByDefault.put(PlayerUsableInterface.USABLE_ID_JUMP, (ServerConfig.JUMP_DRIVE_ENABLED_BY_DEFAULT.isOn()));
			
			
			linearDamping = ServerConfig.PHYSICS_LINEAR_DAMPING.getFloat();
			rotationalDamping = ServerConfig.PHYSICS_ROTATIONAL_DAMPING.getFloat();
			recipeBlockCost = ServerConfig.RECIPE_BLOCK_COST.getInt();
			maxChainDocking = ServerConfig.MAX_CHAIN_DOCKING.getInt();
			additiveProjectiles = ServerConfig.PROJECTILES_ADDITIVE_VELOCITY.isOn();
			dynamicPrices = ServerConfig.USE_DYNAMIC_RECIPE_PRICES.isOn();
			buyBBWithCredits = (ServerConfig.BUY_BLUEPRINTS_WITH_CREDITS.isOn());
			relativeProjectiles = ServerConfig.PROJECTILES_VELOCITY_MULTIPLIER.getFloat();
			weaponRangeReference = ServerConfig.WEAPON_RANGE_REFERENCE.getFloat();
			turnSpeedDivisor = ServerConfig.TURNING_DIMENSION_SCALE.getFloat();
			planetSizeMean = ServerConfig.PLANET_SIZE_MEAN_VALUE.getFloat();
			planetSizeDeviation = ServerConfig.PLANET_SIZE_DEVIATION_VALUE.getFloat();

			sectorsToExploreForSystemScan = ServerConfig.SECTORS_TO_EXPLORE_FOR_SYS.getInt();
			
			weightedCenterOfMass = ServerConfig.WEIGHTED_CENTER_OF_MASS.isOn();

			shopRebootCostPerSecond = ServerConfig.SHOP_REBOOT_COST_PER_SECOND.getFloat();
			shopArmorRepairPerSecond = ServerConfig.SHOP_ARMOR_REPAIR_COST_PER_HITPOINT.getFloat();

			lockFactionShips = ServerConfig.LOCK_FACTION_SHIPS.isOn();
			allowPersonalInvOverCap = ServerConfig.ALLOW_PERSONAL_INVENTORY_OVER_CAPACITY.isOn();
			onlyAddFactionToFleet = ServerConfig.ONLY_ALLOW_FACTION_SHIPS_ADDED_TO_FLEET.isOn();

			factionKickInactiveTimeLimit = ServerConfig.FACTION_FOUNDER_KICKABLE_AFTER_DAYS_INACTIVITY.getInt();
			
			aiWeaponSwitchDelay = ServerConfig.AI_WEAPON_SWITCH_DELAY.getInt();

			spawnProtection = ServerConfig.SPAWN_PROTECTION.getInt();
			

			sunMinIntensityDamageRange = ((GameServerState)state).getGameConfig().sunMinIntensityDamageRange;
			massLimitShip = ((GameServerState)state).getGameConfig().massLimitShip;
			massLimitPlanet = ((GameServerState)state).getGameConfig().massLimitPlanet;
			massLimitStation = ((GameServerState)state).getGameConfig().massLimitStation;
			blockLimitShip = ((GameServerState)state).getGameConfig().blockLimitShip;
			blockLimitPlanet = ((GameServerState)state).getGameConfig().blockLimitPlanet;
			blockLimitStation = ((GameServerState)state).getGameConfig().blockLimitStation;
			allowFactoriesOnShips = ServerConfig.ALLOW_FACTORY_ON_SHIPS.isOn();
			shipyardIgnoreStructure = ServerConfig.SHIPYARD_IGNORE_STRUCTURE.isOn();
			ignoreDockingArea = ServerConfig.IGNORE_DOCKING_AREA.isOn();
			
			
		}
	}

	public void announceMetaObject(MetaObject metaObject) {
		synchronized (metaObjectsToAnnounce) {
			metaObjectsToAnnounce.enqueue(metaObject);
		}
	}

	@Override
	public void cleanUpOnEntityDelete() {

	}

	@Override
	public void destroyPersistent() {
		//not persistant
	}

	@Override
	public NetworkGameState getNetworkObject() {
		return networkGameState;
	}

	@Override
	public StateInterface getState() {
		return state;
	}

	@Override
	public void initFromNetworkObject(NetworkObject o) {
		id = o.id.get();
		factionManager.initFromNetworkObject((NetworkGameState) o);
		catalogManager.initFromNetworkObject((NetworkGameState) o);
		raceManager.initFromNetworkObject((NetworkGameState) o);
		tradeManager.initFromNetworkObject((NetworkGameState) o);
		((FleetStateInterface)state).getFleetManager().updateFromNetworkObject((NetworkGameState) o);
		ruleManager.initFromNetworkObject((NetworkGameState) o);
		if (!onServer) {
			handleFromInventoryNT();
			readLeaderboard();
			maxGalaxySpeed = networkGameState.serverMaxSpeed.get();
			clientBattlemodeSettings = networkGameState.battlemodeInfo.get();
			linearDamping = networkGameState.linearDamping.get();
			spawnProtection = networkGameState.spawnProtection.get();
			rotationalDamping = networkGameState.rotationalDamping.get();
			turnSpeedDivisor = networkGameState.turnSpeedDivisor.get();
			recipeBlockCost = networkGameState.recipeBlockCost.get();
			maxChainDocking = networkGameState.maxChainDocking.get();
			maxBuildArea = networkGameState.maxBuildArea.get();

			currentGameModeOutput = networkGameState.gameModeMessage.get();

			dynamicPrices = networkGameState.dynamicPrices.get();
			buyBBWithCredits = (networkGameState.buyBBWIthCredits.get());
			additiveProjectiles = networkGameState.additiveProjectiles.get();
			relativeProjectiles = networkGameState.relativeProjectiles.get();
			weaponRangeReference = networkGameState.weaponRangeReference.get();
			setSectorSize(networkGameState.sectorSize.get());
			sectorsToExploreForSystemScan = networkGameState.sectorsToExploreForSystemScan.getInt();
			stationCostClient = networkGameState.stationCost.getInt();
			planetSizeMean = networkGameState.planetSizeMean.getFloat();
			planetSizeDeviation = networkGameState.planetSizeDeviation.getFloat();
			weightedCenterOfMass = networkGameState.weightedCenterOfMass.getBoolean();
			npcDebug = networkGameState.npcDebug.getBoolean();
			fow = networkGameState.fow.getBoolean();
			npcFleetSpeedLoaded = networkGameState.npcFleetSpeedLoaded.getFloat();
			npcDebugShopOwners = networkGameState.npcShopOwnersDebug.get();

			shopRebootCostPerSecond = networkGameState.shopRebootCostPerSecond.getFloat();
			shopArmorRepairPerSecond = networkGameState.shopArmorRepairPerSecond.getFloat();

			lockFactionShips = networkGameState.lockFactionShips.getBoolean();
			allowPersonalInvOverCap = networkGameState.allowPersonalInvOverCap.getBoolean();
			onlyAddFactionToFleet = networkGameState.onlyAddFactionToFleet.getBoolean();
			allowFactoriesOnShips = networkGameState.allowFactoriesOnShip.getBoolean();
			shipyardIgnoreStructure = networkGameState.shipyardIgnoreStructure.getBoolean();
			ignoreDockingArea = networkGameState.ignoreDockingArea.getBoolean();
			

			factionKickInactiveTimeLimit = networkGameState.factionKickInactiveTimeLimit.getInt();
			
			aiWeaponSwitchDelay = networkGameState.aiWeaponSwitchDelay.getInt();


			massLimitShip = networkGameState.massLimitShip.getFloat();
			massLimitPlanet = networkGameState.massLimitPlanet.getFloat();
			massLimitStation = networkGameState.massLimitStation.getFloat();
			blockLimitShip = networkGameState.blockLimitShip.getInt();
			blockLimitPlanet = networkGameState.blockLimitPlanet.getInt();
			blockLimitStation = networkGameState.blockLimitStation.getInt();
			
			for(int i = 0; i < networkGameState.ruleSetManagerBuffer.getReceiveBuffer().size(); i++) {
				RuleSetManager rs = networkGameState.ruleSetManagerBuffer.getReceiveBuffer().get(i).get();
				receivedRuleSetManagers.enqueue(rs);
				synchronized(state) {
					for(Sendable s: state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
						if(s instanceof RuleEntityContainer) {
							//globals might have changed so we need to flag for update
							((RuleEntityContainer)s).getRuleEntityManager().flagRuleChangePrepare();
						}
					}
				}
			}
			
			for(int i = 0; i < networkGameState.modulesEnabledByDefault.getReceiveBuffer().size(); i++){
				LongBooleanPair rb = networkGameState.modulesEnabledByDefault.getReceiveBuffer().get(i).get();
				modulesEnabledByDefault.put(rb.l, rb.b);
			}
			//			DynamicsWorld dynamicsWorld = ((GameClientState)getState()).getPhysics().getDynamicsWorld();
			//			for(int i = 0; i < dynamicsWorld.getNumCollisionObjects(); i++){
			//				CollisionObject collisionObject = dynamicsWorld.getCollisionObjectArray().get(i);
			//				if(collisionObject instanceof RigidBody && !((RigidBody)collisionObject).isStaticOrKinematicObject()){
			//					System.err.println("APPLYING DAMPING TO "+collisionObject);
			//					((RigidBody)collisionObject).setDamping(linearDamping, rotationalDamping);
			//				}
			//			}
			for(int i = 0; i < networkGameState.npcSystemBuffer.getReceiveBuffer().size(); i++){
				NPCSystemStub npcSystemStub = networkGameState.npcSystemBuffer.getReceiveBuffer().get(i).get();
				clientNPCSystemToAdd.enqueue(npcSystemStub);
			}
		}
	}

	@Override
	public void initialize() {

	}

	@Override
	public boolean isMarkedForDeleteVolatile() {
		return markedForDeleteVolatile;
	}

	@Override
	public void setMarkedForDeleteVolatile(boolean markedForDelete) {
		markedForDeleteVolatile = markedForDelete;

	}

	@Override
	public boolean isMarkedForDeleteVolatileSent() {
		return markedForDeleteVolatileSent;
	}

	@Override
	public void setMarkedForDeleteVolatileSent(boolean b) {
		markedForDeleteVolatileSent = b;

	}

	@Override
	public boolean isMarkedForPermanentDelete() {
		return false;
	}

	@Override
	public boolean isOkToAdd() {
		return true;
	}

	@Override
	public boolean isOnServer() {
		return onServer;
	}

	@Override
	public boolean isUpdatable() {
		return true;
	}

	@Override
	public void markForPermanentDelete(boolean mark) {
	}

	@Override
	public void newNetworkObject() {
		networkGameState = new NetworkGameState(this, state);
	}
	private void updateLocalInventory(){
		if(!ntInventoryAdds.isEmpty()){
			synchronized (ntInventoryAdds) {
				while(!ntInventoryAdds.isEmpty()){
					Inventory inventory = ntInventoryAdds.dequeue();
					
					assert(inventory.getParameterIndex() != 0);
					if(!onServer){
						for(Inventory i : imap.inventoriesList){
							assert(i != null);
						}
					}
					assert(inventory.getParameterIndex() != Long.MIN_VALUE);
					
					imap.put(inventory.getParameterIndex(), inventory);
					
					Faction faction = factionManager.getFaction((int)inventory.getParameterIndex());
					
					if(faction != null){
						((NPCFaction)faction).setInventory((NPCFactionInventory) inventory);
					}
					if (!onServer) {
						inventory.requestMissingMetaObjects();
					}
				}
			}
		}
		
		if(!ntInventoryRemoves.isEmpty()){
			synchronized (ntInventoryRemoves) {
				while(!ntInventoryRemoves.isEmpty()){
					Inventory inventory = ntInventoryRemoves.dequeue();
					//make sure to select actual removed inventory
					inventory = imap.remove(inventory.getParameterIndex());
					if (!onServer && inventory != null){
						volumeChanged(inventory.getVolume(), 0);
					 	if(inventory instanceof StashInventory && ((StashInventory) inventory).getCustomName() != null && ((StashInventory) inventory).getCustomName().length() > 0) {
//							getNamedInventoriesClient().remove(inventory.getParameterIndex());
//							namedInventoriesClientChanged = true;
					 	}
					}
				}
			}
		}
		if (!ntInventoryMultMods.isEmpty()) {
			synchronized (ntInventoryMultMods) {
				while (!ntInventoryMultMods.isEmpty()) {
					InventoryMultMod a = ntInventoryMultMods.dequeue();
					assert(a.parameter != Long.MIN_VALUE):a;
					
					Inventory inventory = getInventory(a.parameter);
					if (inventory != null) {
						assert(inventory.getInventoryHolder() != null):inventory;
						if (onServer) {
							assert(false):"Client should not send modifications to faction inventory";
						} else {
							inventory.handleReceived(a, networkGameState);
						}

					} else {
						
						assert (false):inventory;
					}
				}
			}
		}
	}
	private void handleFromInventoryNT(){
		{
			ObjectArrayList<RemoteInventory> changeBuffer = networkGameState.getInventoriesChangeBuffer().getReceiveBuffer();

			for (int i = 0; i < changeBuffer.size(); i++) {
				Inventory inventory = changeBuffer.get(i).get();
				if (changeBuffer.get(i).isAdd()) {
					synchronized (ntInventoryAdds) {
						ntInventoryAdds.enqueue(inventory);
					}
				} else {
					synchronized (ntInventoryRemoves) {
						ntInventoryRemoves.enqueue(inventory);
					}
				}
			}
			
		}
		{
			ObjectArrayList<RemoteInventoryMultMod> receiveBuffer =
					networkGameState.getInventoryMultModBuffer().getReceiveBuffer();

			for (int i = 0; i < receiveBuffer.size(); i++) {
				RemoteInventoryMultMod a = receiveBuffer.get(i);
				synchronized (ntInventoryMultMods) {
					ntInventoryMultMods.enqueue(a.get());
				}

			}

		}
	}
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		((FleetStateInterface)state).getFleetManager().updateFromNetworkObject((NetworkGameState) o);
		tradeManager.updateFromNetworkObject((NetworkGameState) o);
		
		for(int i = 0; i < networkGameState.ruleSetManagerBuffer.getReceiveBuffer().size(); i++) {
			RuleSetManager rs = networkGameState.ruleSetManagerBuffer.getReceiveBuffer().get(i).get();
			receivedRuleSetManagers.enqueue(rs);
			synchronized(state) {
				for(Sendable s: state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(s instanceof RuleEntityContainer) {
						//globals might have changed so we need to flag for update
						((RuleEntityContainer)s).getRuleEntityManager().flagRuleChangePrepare();
					}
				}
			}
		}
		
		if(ruleProperties != null) {
			ruleProperties.updateFromNetworkObject((NetworkGameState) o);
		}
		
		for(Faction f : factionManager.getFactionCollection()) {
			//broadcast received rule data (since factions are not network objects) 
			f.getRuleEntityManager().receive((NetworkGameState)o);
		}
		
		if (!onServer) {
			handleFromInventoryNT();
			readLeaderboard();
			clientBattlemodeSettings = networkGameState.battlemodeInfo.get();
			ServerConfig.deserialize((NetworkGameState) o);
			aiWeaponSwitchDelay = networkGameState.aiWeaponSwitchDelay.get();
			setSectorSize(networkGameState.sectorSize.get());
			npcDebug = networkGameState.npcDebug.getBoolean();
			fow = networkGameState.fow.getBoolean();
			npcFleetSpeedLoaded = networkGameState.npcFleetSpeedLoaded.getFloat();
			npcDebugShopOwners = networkGameState.npcShopOwnersDebug.get();
			for (int i = 0; i < networkGameState.frozenSectorRequests.getReceiveBuffer().size(); i++) {
				int fs = networkGameState.frozenSectorRequests.getReceiveBuffer().getInt(i);
				if (fs > 0) {
					frozenSectors.add(fs);
				} else {
					frozenSectors.remove(Math.abs(fs));
				}
			}
			for(int i = 0; i < networkGameState.npcSystemBuffer.getReceiveBuffer().size(); i++){
				NPCSystemStub npcSystemStub = networkGameState.npcSystemBuffer.getReceiveBuffer().get(i).get();
				if(npcSystemStub.abandoned){
					clientNPCSystemMap.remove(npcSystemStub.system);
				}else{
					clientNPCSystemMap.put(npcSystemStub.system, npcSystemStub);
				}
			}
			currentGameModeOutput = networkGameState.gameModeMessage.get();
			for (int i = 0; i < networkGameState.deployBlockBehaviorChecksum.getReceiveBuffer().size(); i++) {
				RemoteStringArray rm = networkGameState.deployBlockBehaviorChecksum.getReceiveBuffer().get(i);
				((GameClientState) state).setBlockBehaviorCheckSum(rm.get(i).get());
				byte[] b;
				try {
					b = rm.get(1).get().getBytes("UTF-8");

					String pathOfBlockBehavior = ClientStatics.ENTITY_DATABASE_PATH + ((GameClientState) state).getController().getConnection().getHost() + "-block-behavior.xml";
					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(pathOfBlockBehavior));
					fos.write(b);
					fos.flush();
					fos.close();

					System.err.println("[CLIENT] received deployed block behavior from server: " + pathOfBlockBehavior);
					((GameClientState) state).getController().parseBlockBehavior(pathOfBlockBehavior);
					((GameClientState) state).getController().reapplyBlockBehavior();
				} catch (Exception e) {
					e.printStackTrace();
					GLFrame.processErrorDialogExceptionWithoutReport(e, state);
				}
			}

			for (int i = 0; i < networkGameState.metaObjectStateLessBuffer.getReceiveBuffer().size(); i++) {
				RemoteMetaObjectStateLess mo = networkGameState.metaObjectStateLessBuffer.getReceiveBuffer().get(i);
				((MetaObjectState) state).getMetaObjectManager().receivedAnnoucedMetaObject(mo.get());
			}
			//			for(int i = 0; i < getNetworkObject().clearSegmentCacheCommands.getReceiveBuffer().size(); i++){
			//				RemoteString mo = getNetworkObject().clearSegmentCacheCommands.getReceiveBuffer().get(i);
			//				((GameClientState)getState())
			//			}
		} else {
			//onserver

		}
		if (!networkGameState.debugPhysical.getReceiveBuffer().isEmpty()) {
			for (int i = 0; i < networkGameState.debugPhysical.getReceiveBuffer().size(); i++) {
				debugObjects.enqueue(networkGameState.debugPhysical.getReceiveBuffer().get(i).get());
			}

		}
		factionManager.updateFromNetworkObject(networkGameState);
		catalogManager.updateFromNetworkObject(networkGameState);
		raceManager.updateFromNetworkObject(networkGameState);
	}

	@Override
	public void updateLocal(Timer timer) throws IOException {

		updateTime = System.currentTimeMillis();
		
		factionManager.updateLocal(timer);
		catalogManager.updateLocal();
		tradeManager.updateLocal(timer);
		if (onServer) {
			if (serverDeployedBlockBehavior) {

				RemoteStringArray a = new RemoteStringArray(2, onServer);
				a.set(0, ((GameServerState) state).getBlockBehaviorChecksum());
				a.set(1, new String(((GameServerState) state).getBlockBehaviorFile()));

				networkGameState.deployBlockBehaviorChecksum.add(a);

				((GameServerState) state).getController().broadcastMessage(Lng.astr("Server deploying new\nblock behavior config."), ServerMessage.MESSAGE_TYPE_INFO);

				serverDeployedBlockBehavior = false;
			}
		}
		updateLocalInventory();
		
		while(!receivedRuleSetManagers.isEmpty()) {
			System.err.println(state +" RECEIVED RULESET MANAGER. APPLYING");
			RuleSetManager ruleSetManager = receivedRuleSetManagers.dequeue();
			this.ruleManager = ruleSetManager;
			this.ruleManager.setState((GameStateInterface) this.state);
			
			
			if(this.ruleManager.receivedFullRuleChange != null) {
				this.ruleProperties = this.ruleManager.receivedFullRuleChange;
				this.ruleProperties.state = (GameStateInterface) state;
				this.ruleManager.receivedFullRuleChange = null;
				
				if(onServer) {
					System.err.println("[SERVER][RULES] Received ruleset and property change. Saving to disk.");
					ruleManager.writeToDisk();
					ruleProperties.saveToDisk(RulePropertyContainer.getPropertiesPath());
					System.err.println("[SERVER][RULES] Delegating new ruleset to clients.");
					ruleManager.includePropertiesInSendAndSaveOnServer = true;
					//if sent from client, mark as changed to propagate to clients
					networkGameState.ruleSetManagerBuffer.add(new RemoteRuleSetManager(ruleManager, networkGameState));
				}
				this.ruleManager.receivedInitialOnClient = true;
			}
			
			
			ruleManager.flagChanged(state);
		}
		if(ruleProperties != null) {
			ruleProperties.update();
		}else {
			System.err.println("No rule properties yet "+ onServer);
		}
		manualTradeManager.updateLocal(timer);
		if (leaderBoardChanged) {
			clientDeadCount.clear();
			for (Entry<String, ObjectArrayList<KillerEntity>> a : clientLeaderboard.entrySet()) {
				for (KillerEntity e : a.getValue()) {
					clientDeadCount.put(e.deadPlayerName, clientDeadCount.getInt(e.deadPlayerName) + 1);
				}
			}

			if (leaderboardGUI != null) {
				leaderboardGUI.updateEntries();
			}
			leaderBoardChanged = false;
		}
		while(!clientNPCSystemToAdd.isEmpty()){
			NPCSystemStub d = clientNPCSystemToAdd.dequeue();
			clientNPCSystemMap.put(d.system, d);
		}
		if (onServer && !metaObjectsToAnnounce.isEmpty()) {
			while (!metaObjectsToAnnounce.isEmpty()) {
				networkGameState.metaObjectStateLessBuffer.add(new RemoteMetaObjectStateLess(metaObjectsToAnnounce.dequeue(), networkGameState));
			}

		}

		if (!onServer && !debugObjects.isEmpty()) {
			while (!debugObjects.isEmpty()) {
				debugObjects.dequeue().draw((GameClientState) state);
			}
		}

		if (!onServer) {
			if (currentGameModeOutput.length() > 0) {
				this.lastClientShownGameModeMessage = currentGameModeOutput;
				((GameClientState) state).getController().showBigTitleMessage("gameMode", currentGameModeOutput, 0);
			} else if (this.lastClientShownGameModeMessage != null) {
				((GameClientState) state).getController().timeOutBigTitleMessage("gameMode");
			}

		}
		if(onServer){
			raceManager.updateServer(timer);
		}else{
			raceManager.updateClient(timer);
		}
	}

	@Override
	public void updateToFullNetworkObject() {
		networkGameState.id.set(id);
		assert (serverMessage != null);
		networkGameState.serverMessage.set(serverMessage);
		((FleetStateInterface)state).getFleetManager().updateToFullNetworkObject(networkGameState);
		tradeManager.updateToFullNetworkObject(networkGameState);
		
		//includes properties
		ruleManager.includePropertiesInSendAndSaveOnServer = true;
		ruleManager.updateToFullNetworkObject(networkGameState);
		
//		ruleProperties.updateToFullNetworkObject(getNetworkObject());
		if (onServer) {
			ServerConfig.serialize(networkGameState);

			for (AbstractGameMode m : gameModes) {
				m.updateToFullNT(networkGameState);
			}

			for (int froz : frozenSectors) {
				networkGameState.frozenSectorRequests.add(froz);
			}
			for(Inventory i : imap.inventoriesList){
//				System.err.println("SENDING INVINV "+i);
				i.sendAll();
			}
			if(ServerConfig.NPC_DEBUG_MODE.isOn()){
				factionManager.checkNPCFactionSendingDebug(true);
			}
			networkGameState.manCalcCancelOn.set(ServerConfig.MANAGER_CALC_CANCEL_ON.isOn());
			networkGameState.maxBuildArea.set(ServerConfig.PLAYER_MAX_BUILD_AREA.getInt());
			networkGameState.saveSlotsAllowed.set(ServerConfig.CATALOG_SLOTS_PER_PLAYER.getInt());
			networkGameState.buyBBWIthCredits.set(buyBBWithCredits);
			networkGameState.serverMaxSpeed.set(maxGalaxySpeed);
			networkGameState.linearDamping.set(linearDamping);
			networkGameState.spawnProtection.set(spawnProtection);
			networkGameState.gameModeMessage.set(currentGameModeOutput);
			networkGameState.recipeBlockCost.set(recipeBlockCost);
			networkGameState.maxChainDocking.set(maxChainDocking);
			networkGameState.rotationalDamping.set(rotationalDamping);
			networkGameState.turnSpeedDivisor.set(turnSpeedDivisor);
			networkGameState.sectorSize.set(sectorSize);
			networkGameState.additiveProjectiles.set(additiveProjectiles);
			networkGameState.dynamicPrices.set(dynamicPrices);
			networkGameState.relativeProjectiles.set(relativeProjectiles);
			networkGameState.weaponRangeReference.set(weaponRangeReference);
			networkGameState.seed.set(getUniverseSeed());
			networkGameState.serverStartTime.set(((GameServerState) state).getServerStartTime());
			networkGameState.universeDayDuration.set((long) ( ServerConfig.UNIVERSE_DAY_IN_MS.getInt()));
			networkGameState.stationCost.set(getStationCost());
			networkGameState.planetSizeMean.set(planetSizeMean);
			networkGameState.sectorsToExploreForSystemScan.set(sectorsToExploreForSystemScan);
			networkGameState.planetSizeDeviation.set(planetSizeDeviation);
			networkGameState.weightedCenterOfMass.set(weightedCenterOfMass);
			networkGameState.npcDebug.set(isNpcDebug());
			networkGameState.fow.set(isFow());
			networkGameState.npcFleetSpeedLoaded.set(getNPCFleetSpeedLoaded());
			networkGameState.npcShopOwnersDebug.set(getNPCShopOwnersDebug());

			networkGameState.shopRebootCostPerSecond.set(shopRebootCostPerSecond);
			networkGameState.shopArmorRepairPerSecond.set(shopArmorRepairPerSecond);
			networkGameState.lockFactionShips.set(lockFactionShips);
			networkGameState.allowPersonalInvOverCap.set(allowPersonalInvOverCap);
			networkGameState.onlyAddFactionToFleet.set(onlyAddFactionToFleet);
			networkGameState.allowFactoriesOnShip.set(allowFactoriesOnShips);
			networkGameState.shipyardIgnoreStructure.set(shipyardIgnoreStructure);
			networkGameState.ignoreDockingArea.set(ignoreDockingArea);
			networkGameState.factionKickInactiveTimeLimit.set(factionKickInactiveTimeLimit);
			networkGameState.aiWeaponSwitchDelay.set(aiWeaponSwitchDelay);

			networkGameState.massLimitShip.set((float)massLimitShip);
			networkGameState.massLimitPlanet.set((float)massLimitPlanet);
			networkGameState.massLimitStation.set((float)massLimitStation);
			networkGameState.blockLimitShip.set(blockLimitShip);
			networkGameState.blockLimitPlanet.set(blockLimitPlanet);
			networkGameState.blockLimitStation.set(blockLimitStation);

			for(it.unimi.dsi.fastutil.longs.Long2BooleanMap.Entry e : modulesEnabledByDefault.long2BooleanEntrySet()){
				networkGameState.modulesEnabledByDefault.add(new RemoteLongBoolean(new LongBooleanPair(e.getLongKey(), e.getBooleanValue()), networkGameState));
			}
			
			
		}
		factionManager.updateToFullNetworkObject(networkGameState);
		catalogManager.updateToFullNetworkObject(networkGameState);
		
		raceManager.updateToFullNetworkObject(networkGameState);
	}
	public void setWeaponRangeReference(float range) throws IOException {
		ServerConfig.WEAPON_RANGE_REFERENCE.setFloat(range);
		ServerConfig.write();
		weaponRangeReference = range;
		networkGameState.weaponRangeReference.set(weaponRangeReference);
	}
	@Override
	public void updateToNetworkObject() {
		networkGameState.id.set(id);
		tradeManager.updateToNetworkObject(networkGameState);
		ruleManager.updateToNetworkObject(networkGameState);
		if (onServer) {
			
//			getFactionManager().checkNPCFactionSending(false);
			
			networkGameState.serverModTime.set(((GameServerState) state).getServerTimeMod());
			networkGameState.sectorSize.set(sectorSize);
			networkGameState.npcDebug.set(isNpcDebug());
			networkGameState.fow.set(isFow());
			networkGameState.npcFleetSpeedLoaded.set(getNPCFleetSpeedLoaded());
			networkGameState.npcShopOwnersDebug.set(getNPCShopOwnersDebug());
			networkGameState.aiWeaponSwitchDelay.set(aiWeaponSwitchDelay);
		}
		factionManager.updateToNetworkObject(networkGameState);
		catalogManager.updateToNetworkObject(networkGameState);
		raceManager.updateToNetworkObject(networkGameState);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#isWrittenForUnload()
	 */
	@Override
	public boolean isWrittenForUnload() {
		return writtenForUnload;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#setWrittenForUnload(boolean)
	 */
	@Override
	public void setWrittenForUnload(boolean b) {
		writtenForUnload = b;
	}

	/**
	 * @return the catalogManager
	 */
	public CatalogManager getCatalogManager() {
		return catalogManager;
	}

	/**
	 * @return the factionManager
	 */
	public FactionManager getFactionManager() {
		return factionManager;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	public float getLinearDamping() {
		return linearDamping;
	}

	/**
	 * @return the maxGalaxySpeed
	 */
	public float getMaxGalaxySpeed() {
		return maxGalaxySpeed;
	}

	/**
	 * @param maxGalaxySpeed the maxGalaxySpeed to set
	 */
	public void setMaxGalaxySpeed(float maxGalaxySpeed) {
		this.maxGalaxySpeed = maxGalaxySpeed;
	}

	/**
	 * @return the recipeBlockCost
	 */
	public Integer getRecipeBlockCost() {
		return recipeBlockCost;
	}

	public float getRotationalDamping() {
		return rotationalDamping;
	}

	public String getServerDescription() {
		return serverDescription;
	}

	public String getServerName() {
		return serverName;
	}

	/**
	 * @return the turnSpeedDivisor
	 */
	public float getTurnSpeedDivisor() {
		return turnSpeedDivisor;
	}

	public float getPlanetSizeMean() {
		return planetSizeMean;
	}

	public float getPlanetSizeDeviation() {
		return planetSizeDeviation;
	}

	public boolean getLockFactionShips() {
		return lockFactionShips;
	}

	/**
	 * @return the additiveProjectiles
	 */
	public boolean isAdditiveProjectiles() {
		return additiveProjectiles;
	}


	/**
	 * @return the relativeProjectiles
	 */
	public float isRelativeProjectiles() {
		return relativeProjectiles;
	}

	public void readServerMessage() throws IOException {
		File l = new FileExt("./server-message.txt");
		if (!l.exists()) {
			l.createNewFile();
			serverMessage = "";
		} else {
			BufferedReader s = new BufferedReader(new FileReader(l));
			String line = null;
			StringBuffer b = new StringBuffer();
			while ((line = s.readLine()) != null) {
				b.append(line + "\n");
			}
			s.close();
			serverMessage = b.toString();
		}
	}

	@Override
	public String toString() {
		return "SendableGameState(" + id + ")";
	}

	private void readLeaderboard() {
		if (networkGameState.leaderBoardBuffer.getReceiveBuffer().size() > 0) {
			clientLeaderboard.clear();

			this.leaderBoardChanged = true;
		}
		for (int i = 0; i < networkGameState.leaderBoardBuffer.getReceiveBuffer().size(); i++) {
			clientLeaderboard.putAll(networkGameState.leaderBoardBuffer.getReceiveBuffer().get(i).get());
		}
	}

	public long getUniverseSeed() {
		if (onServer) {
			return ((GameServerState) state).getUniverse().getSeed();
		} else {
			return networkGameState.seed.getLong();
		}
	}

	/**
	 * @return the serverDeployedBlockBehavior
	 */
	public boolean isServerDeployedBlockBehavior() {
		return serverDeployedBlockBehavior;
	}

	/**
	 * @param serverDeployedBlockBehavior the serverDeployedBlockBehavior to set
	 */
	public void setServerDeployedBlockBehavior(boolean serverDeployedBlockBehavior) {
		this.serverDeployedBlockBehavior = serverDeployedBlockBehavior;
	}

	public float getRotationProgession() {
		if (state.getNumberOfUpdate() != lastUpdateRotationProg) {
			float year = 0;
			if (state.getController().getUniverseDayInMs() > 0) {
				//update time is refreshed every state update
				long diff = (updateTime - state.getController().calculateStartTime()) % state.getController().getUniverseDayInMs();
				year = (float) ((double) diff / (double) state.getController().getUniverseDayInMs());
			}

			lastUpdateRotationProg = state.getNumberOfUpdate();
			cachedYear = year;
		}
		return cachedYear;
	}

	/**
	 * @return the sectorSize
	 */
	public float getSectorSize() {
		return sectorSize;
	}
	public float getWeaponRangeReference() {
		return weaponRangeReference;
	}

	/**
	 * @param sectorSize the sectorSize to set
	 */
	public void setSectorSize(float sectorSize) {
		this.sectorSize = sectorSize;
		if (!onServer) {
			networkGameState.sectorSize.set(sectorSize);
		}
	}

	public int getMaxBuildArea() {
		return maxBuildArea;
	}

	/**
	 * @return the gameModes
	 */
	public ObjectArrayList<AbstractGameMode> getGameModes() {
		return gameModes;
	}

	/**
	 * @return the currentGameModeOutput
	 */
	public String getCurrentGameModeOutput() {
		return currentGameModeOutput;
	}

	/**
	 * @param currentGameModeOutput the currentGameModeOutput to set
	 */
	public void setCurrentGameModeOutput(String currentGameModeOutput) {
		this.currentGameModeOutput = currentGameModeOutput;
	}

	public IntOpenHashSet getFrozenSectors() {
		return frozenSectors;
	}

	public void serverRequestFrosenSector(int sectorId, boolean add) {
		synchronized (frozenSectors) {
			if (add) {
				if (frozenSectors.add(sectorId)) {
					networkGameState.frozenSectorRequests.add(sectorId);
				}
			} else {
				if (frozenSectors.remove(sectorId)) {
					networkGameState.frozenSectorRequests.add(-sectorId);
				}
			}
		}
	}

	public boolean allowedToSpawnBBShips(PlayerState playerState, Faction f) {

		for (int i = 0; i < gameModes.size(); i++) {
			if (!gameModes.get(i).allowedToSpawnBBShips(playerState, f)) {
				return false;
			}
		}

		return true;
	}

	public void announceKill(PlayerState playerState, int id) {
		for (int i = 0; i < gameModes.size(); i++) {
			gameModes.get(i).announceKill(playerState, id);
		}
	}

	public GUIElement getLoaderBoardGUI() {

		//		GUIElementList l = new GUIElementList((ClientState) getState());
		//
		//		for(Entry<String, ObjectArrayList<KillerEntity>> e : clientLeaderboard.entrySet()){
		//			GUIAncor a = new GUIAncor((ClientState) state, 400, 20);
		//			GUITextOverlay t1 = new GUITextOverlay(200, 30, (ClientState) state);
		//			t1.setTextSimple(e.getKey());
		//			GUITextOverlay t2 = new GUITextOverlay(200, 30, (ClientState) state);
		//			t2.setTextSimple("kills: "+e.getValue().size());
		//
		//			t2.setPos(200, 0, 0);
		//
		//			a.attach(t1);
		//			a.attach(t2);
		//
		//			l.add(new GUIListElement(a, a, (ClientState) state));
		//		}

		return new GUIScrollableLoaderboardList((ClientState) state, 820, 402, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {

			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
	}

	/**
	 * @return the clientLeaderboard
	 */
	public Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>> getClientLeaderboard() {
		return clientLeaderboard;
	}

	/**
	 * @param clientLeaderboard the clientLeaderboard to set
	 */
	public void setClientLeaderboard(Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>> clientLeaderboard) {
		this.clientLeaderboard = clientLeaderboard;
	}

	/**
	 * @return the clientBattlemodeSettings
	 */
	public String getClientBattlemodeSettings() {
		if (clientBattlemodeSettings.length() > 0) {
			return "BATTLE MODE\nCurrent Settings and Status: \n" + clientBattlemodeSettings + "\n\n\n" + battlemodeDesc;
		} else {
			return clientBattlemodeSettings;
		}
	}

	/**
	 * @return the clientDeadCount
	 */
	public Object2IntOpenHashMap<String> getClientDeadCount() {
		return clientDeadCount;
	}

	/**
	 * @param clientDeadCount the clientDeadCount to set
	 */
	public void setClientDeadCount(Object2IntOpenHashMap<String> clientDeadCount) {
		this.clientDeadCount = clientDeadCount;
	}

	/**
	 * @return the dynamicPrices
	 */
	public boolean isDynamicPrices() {
		return dynamicPrices;
	}

	public void onFactionChangedServer(PlayerState playerState, FactionChange factionChange) {
		for (AbstractGameMode m : gameModes) {
			m.onFactionChanged(playerState, factionChange);
		}
	}

	public void sendGalaxyModToClients(StellarSystem sys, Vector3i sec) {

		assert (onServer);

		((GameServerState) state).getUniverse().getGalaxyManager().markZoneDirty(sys);

		for (RegisteredClientOnServer c : ((GameServerState) state).getClients().values()) {

			GalaxyRequestAndAwnser a = new GalaxyRequestAndAwnser();
			a.factionUID = sys.getOwnerFaction();
			a.ownerUID = sys.getOwnerUID();
			a.secX = sec.x;
			a.secY = sec.y;
			a.secZ = sec.z;
			//the client's own client channel
			Sendable sendable = c.getLocalAndRemoteObjectContainer().getLocalObjects().get(0);
			if (sendable != null) {
				assert (sendable != null);
				a.networkObjectOnServer = ((ClientChannel) sendable).getNetworkObject();
				a.networkObjectOnServer.galaxyServerMods.add(new RemoteGalaxyRequest(a, true));
			} else {
				System.err.println("[SENDABLEGAMESTATE] WARNING: Cannot send galaxy mod to " + c + ": no client channel!");
			}

		}

	}
	public void sendGalaxyModToClients(int factionId, String ownerShip, Vector3i sec) {
		
		assert (onServer);
		
		Vector3i system = VoidSystem.getPosFromSector(sec, new Vector3i());
		
		((GameServerState) state).getUniverse().getGalaxyManager().markZoneDirty(system);
		
		for (RegisteredClientOnServer c : ((GameServerState) state).getClients().values()) {
			
			GalaxyRequestAndAwnser a = new GalaxyRequestAndAwnser();
			a.factionUID = factionId;
			a.ownerUID = ownerShip;
			a.secX = sec.x;
			a.secY = sec.y;
			a.secZ = sec.z;
			//the client's own client channel
			Sendable sendable = c.getLocalAndRemoteObjectContainer().getLocalObjects().get(0);
			if (sendable != null) {
				assert (sendable != null);
				a.networkObjectOnServer = ((ClientChannel) sendable).getNetworkObject();
				a.networkObjectOnServer.galaxyServerMods.add(new RemoteGalaxyRequest(a, true));
			} else {
				System.err.println("[SENDABLEGAMESTATE] WARNING: Cannot send galaxy mod to " + c + ": no client channel!");
			}
			
		}
		
	}

	/**
	 * @return the buyBBWithCredits
	 */
	public boolean isBuyBBWithCredits() {
		return buyBBWithCredits;
	}

	public int getStationCost() {
		if (onServer) {
			return ServerConfig.STATION_CREDIT_COST.getInt();
		} else {
			return stationCostClient;
		}
	}


	/**
	 * @return the weightedCenterOfMass
	 */
	public boolean isWeightedCenterOfMass() {
		return weightedCenterOfMass;
	}

	public float getShopRebootCostPerSecond() {
		return shopRebootCostPerSecond;
	}

	public float getShopArmorRepairPerSecond() {
		return shopArmorRepairPerSecond;
	}

	/**
	 * time at which a founder can kick another
	 */
	public int getFactionKickInactiveTimeLimitDays() {
		return factionKickInactiveTimeLimit;
	}

	public long getFactionKickInactiveTimeLimitMs() {
		return factionKickInactiveTimeLimit * 1000l * 60l * 60l * 24l;
	}

	@Override
	public void announceLag(long timeTaken) {
	}

	@Override
	public long getCurrentLag() {
		return 0;
	}

	public boolean isMassOk(SegmentController controller, double mass) {
		double limit = getMassLimit(controller);
		return limit <= 0 || mass <= limit;
	}
	public boolean isBlocksOk(SegmentController controller, int blocks) {
		int limit = getBlockLimit(controller);
		return limit <= 0 || blocks <= limit;
	}
	
	public double getMassLimit(SegmentController controller) {
		if (controller instanceof Ship) {
			return massLimitShip;
		} else if (controller instanceof Planet) {
			return massLimitPlanet;
		} else if (controller instanceof SpaceStation) {
			return massLimitStation;
		}
		return 0;
	}

	public int getBlockLimit(SegmentController controller) {
		if (controller instanceof Ship) {
			return blockLimitShip;
		} else if (controller instanceof Planet) {
			return blockLimitPlanet;
		} else if (controller instanceof SpaceStation) {
			return blockLimitStation;
		}
		return 0;
	}

	public RaceManager getRaceManager() {
		return raceManager;
	}

	public boolean isAllowPersonalInvOverCap() {
		return allowPersonalInvOverCap;
	}

	public int getMaxChainDocking() {
		return maxChainDocking;
	}

	public boolean isOnlyAddFactionToFleet() {
		return onlyAddFactionToFleet;
	}

	public TradeManager getTradeManager() {
		return tradeManager;
	}

	public void onStop() {
		tradeManager.onStop();
	}

	public ManualTradeManager getManualTradeManager() {
		return manualTradeManager;
	}

	public int getSectorsToExploreForSystemScan() {
		return sectorsToExploreForSystemScan;
	}

	@Override
	public InventoryMap getInventories() {
		return imap;
	}

	@Override
	public Inventory getInventory(long pos) {
		Faction faction = ((FactionState)state).getFactionManager().getFaction((int)pos);
		if(faction != null && faction instanceof NPCFaction){
			return ((NPCFaction)faction).getInventory();
		}
		return null;
	}

	@Override
	public NetworkInventoryInterface getInventoryNetworkObject() {
		return networkGameState;
	}

	@Override
	public String printInventories() {
		return "<SENDABLEGAMESTATEFACTIONINVENTORIES>";
	}

	@Override
	public void sendInventoryModification(
			IntCollection slots, long parameter) {
		Inventory inventory = getInventory(parameter);
		if (inventory != null) {
			InventoryMultMod m = new InventoryMultMod(slots, inventory, parameter);

			networkGameState.getInventoryMultModBuffer().add(new RemoteInventoryMultMod(m, networkGameState));
		} else {
			assert(false);
			try {
				throw new IllegalArgumentException("[INVENTORY] Exception: tried to send inventory " + parameter);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void sendInventoryModification(int slot, long parameter) {
		//implement if needed
	}

	@Override
	public void sendInventorySlotRemove(int slot, long parameter) {
		//implement if needed
	}

	@Override
	public double getCapacityFor(Inventory inventory) {
		return 100000000000000d;
	}

	@Override
	public void volumeChanged(double volumeBefore, double volumeNow) {
	}

	@Override
	public void sendInventoryErrorMessage(Object[] astr, Inventory inv) {
	}

	@Override
	public String getName() {
		return "SendableGameState";
	}

	public Object2ObjectOpenHashMap<Vector3i, NPCSystemStub> getClientNPCSystemMap() {
		return clientNPCSystemMap;
	}

	public void putClientNPCSystemStatus(Vector3i pos, float status) {
		clientNPCStatusMap.put(pos, status);
	}
	public float getClientNPCSystemStatus(int factionId, Vector3i pos) {
		float f = clientNPCStatusMap.getFloat(pos);
		if(f >= 0){
			return f;
		}else{
			//default return for this map is -2
			if(f < -1.1){
				//request and put in map as -1 so its not requested again
				
				Faction faction = ((FactionState) state).getFactionManager().getFaction(factionId);
				if(faction != null && faction.isNPC()){
					((NPCFaction)faction).sendCommand(NPCFactionControlCommandType.REQUEST_STATUS, pos.x, pos.y, pos.z);
				}
				
				clientNPCStatusMap.put(new Vector3i(pos), -1f);
			}
		}
		return -1;
	}

	public boolean isNpcDebug() {
		if(onServer){
			return ServerConfig.NPC_DEBUG_MODE.isOn();
		}
		return npcDebug;
	}
	public boolean isFow() {
		if(onServer){
			return ServerConfig.USE_FOW.isOn();
		}
		return fow;
	}

	public float getNPCFleetSpeedLoaded() {
		if(onServer){
			return ServerConfig.NPC_LOADED_SHIP_MAX_SPEED_MULT.getFloat();
		}
		return npcFleetSpeedLoaded;
	}
	public String getNPCShopOwnersDebug() {
		if(onServer){
			return ServerConfig.NPC_DEBUG_SHOP_OWNERS.getString();
		}
		return npcDebugShopOwners ;
	}

	public Set<String> getNPCShopOwnersDebugSet() {
		Set<String> own = new ObjectOpenHashSet<String>();
		if(getNPCShopOwnersDebug().trim().length() > 0){
			String[] split = getNPCShopOwnersDebug().split(",");
			for(String s : split){
				own.add(s.trim().toLowerCase(Locale.ENGLISH));
			}
		}
		return own;
	}

	@Override
	public TopLevelType getTopLevelType(){
		return TopLevelType.GENERAL;
	}
	@Override
	public boolean isPrivateNetworkObject(){
		return false;
	}

	public boolean isManCalcCancelOn() {
		if(onServer){
			return ServerConfig.MANAGER_CALC_CANCEL_ON.isOn();
		}else{
			return networkGameState.manCalcCancelOn.getBoolean();
		}
	}

	public ConfigPool getConfigPool() {
		return configPool;
	}

	public boolean isModuleEnabledByDefault(long usableId) {
		return modulesEnabledByDefault.get(usableId);
	}

	public int getSpawnProtectionSec() {
		return spawnProtection;
	}

	public RuleSetManager getRuleManager() {
		return ruleManager;
	}

	public RulePropertyContainer getRuleProperties() {
		return ruleProperties;
	}

	public void setAIWeaponSwitchDelayMS(int delay) {
		aiWeaponSwitchDelay = delay;
	}
	public long getAIWeaponSwitchDelayMS() {
		return aiWeaponSwitchDelay;
	}

	public boolean isAllowFactoryOnShips() {
		return allowFactoriesOnShips;
	}

	public boolean isShipyardIgnoreStructure() {
		return shipyardIgnoreStructure;
	}
	public boolean isIgnoreDockingArea() {
		return ignoreDockingArea;
	}

}
