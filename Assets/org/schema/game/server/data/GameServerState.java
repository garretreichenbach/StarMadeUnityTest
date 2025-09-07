package org.schema.game.server.data;

import api.StarLoaderHooks;
import api.listener.events.Event;
import api.listener.events.state.ServerShutdownCounterEvent;
import api.listener.events.world.MobSpawnRequestExecuteEvent;
import api.listener.events.world.ServerSendableAddEvent;
import api.listener.events.world.ServerSendableRemoveEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.LogUtil;
import org.schema.common.util.StringTools;
import org.schema.common.util.Version;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.controller.SendableAddedRemovedListener;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.SegmentManagerInterface;
import org.schema.game.common.Starter;
import org.schema.game.common.ThreadedSegmentWriter;
import org.schema.game.common.api.NewSessionCallback;
import org.schema.game.common.api.OldSessionCallback;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.activities.RaceManager;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.DatabaseIndex;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerElementManager;
import org.schema.game.common.controller.gamemodes.GameModes;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.ScanData;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.blockeffects.config.ConfigPoolProvider;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.chat.AllChannel;
import org.schema.game.common.data.chat.ChannelRouter;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.common.data.element.ControlElementMapOptimizer;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.explosion.ExplosionRunnable;
import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.common.data.fleet.FleetStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.playermessage.ServerPlayerMessager;
import org.schema.game.common.data.player.tech.Technology;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.migration.Chunk32Migration;
import org.schema.game.common.gui.PreparingFilesJFrame;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.ChatMessage;
import org.schema.game.network.objects.remote.RemoteScanData;
import org.schema.game.server.controller.*;
import org.schema.game.server.controller.gameConfig.GameConfig;
import org.schema.game.server.controller.pathfinding.AbstractPathFindingHandler;
import org.schema.game.server.controller.world.factory.regions.UsableRegion;
import org.schema.game.server.controller.world.factory.regions.hooks.RegionHook;
import org.schema.game.server.data.admin.AdminCommandQueueElement;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.game.server.data.admin.ScriptThread;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.simulation.SimulationManager;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFactionPresetManager;
import org.schema.game.server.data.simulation.resource.PassiveResourceManager;
import org.schema.game.server.data.simulation.resource.PassiveResourceProvider;
import org.schema.schine.auth.SessionCallback;
import org.schema.schine.common.DebugTimer;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.LoadingScreen;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.ClientIdNotFoundException;
import org.schema.schine.network.RegisteredClientInterface;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.client.HostPortLoginName;
import org.schema.schine.network.commands.GameRequestCommandPackage;
import org.schema.schine.network.commands.SynchronizeAllCommandPackage;
import org.schema.schine.network.commands.SynchronizePrivateCommandPackage;
import org.schema.schine.network.commands.SynchronizePublicCommandPackage;
import org.schema.schine.network.common.*;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.AdminLocalClient;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerProcessorInterface;
import org.schema.schine.network.server.ServerState;
import org.schema.schine.network.synchronization.SynchronizationReceiver;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.ResourceMap;
import org.schema.schine.resource.UniqueInterface;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.vecmath.Vector3f;
import java.io.*;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.*;

import static api.mod.StarLoader.fireEvent;
import static org.schema.game.server.data.simulation.resource.PassiveResourceProvider.DefaultResourceProviderTypes.*;

public class GameServerState extends ServerState implements MetaObjectState, FleetStateInterface, GameStateInterface, GravityStateInterface, SegmentManagerInterface, FactionState, CatalogState, RaceManagerState, ConfigPoolProvider {
	public static final ByteArrayOutputStream SEGMENT_BYTE_ARRAY_BUFFER = new ByteArrayOutputStream(100 * 1024);
	public static final int NT_SEGMENT_OCTREE_COMPRESSION_RATE = SegmentData.BLOCK_COUNT / 4;

	public static String SERVER_DATABASE = "." + File.separator + "server-database" + File.separator;
	public static String DATABASE_PATH = SERVER_DATABASE;
	public static String ENTITY_DATABASE_PATH;
	public static String ENTITY_BLUEPRINT_PATH;
	public static String ENTITY_TEMP_BLUEPRINT_PATH;
	public static String ENTITY_BLUEPRINT_PATH_DEFAULT;
	public static String SEGMENT_DATA_BLUEPRINT_PATH;
	public static String SEGMENT_DATA_TEMP_BLUEPRINT_PATH;
	public static String SEGMENT_DATA_BLUEPRINT_PATH_DEFAULT;
	public static String ENTITY_BLUEPRINT_PATH_STATIONS_NEUTRAL;
	public static String SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_NEUTRAL;
	public static String ENTITY_BLUEPRINT_PATH_STATIONS_PIRATE;
	public static String SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_PIRATE;
	public static String ENTITY_BLUEPRINT_PATH_STATIONS_TRADING_GUILD;
	public static String SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_TRADING_GUILD;
	public static String SEGMENT_DATA_DATABASE_PATH;

	public static int axisSweepsInMemory;

	public static Long dataReceived = 0L;

	//	public static Deflater deflater = new Deflater();
//	public static Inflater INFLATER = new Inflater();
	public static final int DATABASE_VERSION = 3;
	public static int allocatedSegmentData;
	public static int lastAllocatedSegmentData;
	public static int lastFreeSegmentData;
	public static boolean updateAllShopPricesFlag;
	public static int collectionUpdates;
	public static float dayTime;
	public static int itemIds;
	public static int debugObj = -1;
	public static GameServerState instance;
	public static int totalSectorCountTmp;
	public static int activeSectorCountTmp;
	public static int totalSectorCount;
	public static int activeSectorCount;
	public static long lastBCMessage;
	public static int segmentRequestQueue;
	public static int buffersInUse;
	public static int totalDockingChecks;
	private static int DB_VERSION_READ;
	public static final ObjectArrayList<ScriptThread> scriptThreads = new ObjectArrayList<>();

	//	private static byte[] byteArray = new byte[4 * 1024];
	public final IntOpenHashSet activeSectors = new IntOpenHashSet();
	public static final HashMap<String, Object> fileLocks = new HashMap<String, Object>();
	public final ArrayList<ShipSpawnWave> waves = new ArrayList<>();
	//	private final ObjectArrayFIFOQueue<SegmentPiece> segmentControllerBreaks = new ObjectArrayFIFOQueue<SegmentPiece>();
	public final ObjectArrayFIFOQueue<AbstractPathFindingHandler<?, ?>> pathFindingCallbacks = new ObjectArrayFIFOQueue<AbstractPathFindingHandler<?, ?>>();
	public final ObjectArrayFIFOQueue<CreatureSpawn> creatureSpawns = new ObjectArrayFIFOQueue<CreatureSpawn>();
	private final Short2ObjectOpenHashMap<Technology> techs = new Short2ObjectOpenHashMap<Technology>();
	private final MetaObjectManager metaObjectManager;
	private final ServerPlayerMessager serverPlayerMessager;
	private final AdminLocalClient adminLocalClient = new AdminLocalClient();
	private final ObjectArrayList<SimpleTransformableSendableObject> currentGravitySources = new ObjectArrayList<SimpleTransformableSendableObject>();
	private final Map<String, Admin> admins = new Object2ObjectOpenHashMap<String, Admin>();
	private final HashMap<String, ProtectedUplinkName> protectedNames = new HashMap<String, ProtectedUplinkName>();
	private final Universe universe;
	private final List<FileRequest> activeFileRequests = new ObjectArrayList<FileRequest>();
	private final List<FileRequest> fileRequests = new ObjectArrayList<FileRequest>();
//	private final List<String> chatLog = new ObjectArrayList<String>();
	private final List<SectorSwitch> sectorSwitches = new ObjectArrayList<SectorSwitch>();
	private final ObjectArrayFIFOQueue<ServerSegmentRequest> segmentRequests = new ObjectArrayFIFOQueue<ServerSegmentRequest>();
	private final ObjectArrayFIFOQueue<ServerSegmentRequest> segmentRequestsLoaded = new ObjectArrayFIFOQueue<ServerSegmentRequest>();
	private final GameConfig gameConfig;
	private final Long2ObjectOpenHashMap<String> playerDbIdToUID = new Long2ObjectOpenHashMap<>();
	private final SimulationManager simulationManager;
	private final ThreadedSegmentWriter threadedSegmentWriter = new ThreadedSegmentWriter("SERVER");
	private final ChannelRouter channelRouter;
	private final ObjectArrayFIFOQueue<ServerExecutionJob> serverExecutionJobs = new ObjectArrayFIFOQueue<ServerExecutionJob>();
	private final Map<String, SegmentController> segmentControllersByName = new Object2ObjectOpenHashMap<String, SegmentController>();
	private final Map<String, SegmentController> segmentControllersByNameLowerCase = new Object2ObjectOpenHashMap<String, SegmentController>();
	private final Map<String, PlayerState> playerStatesByName = new Object2ObjectOpenHashMap<String, PlayerState>();
	private final Map<String, PlayerState> playerStatesByNameLowerCase = new Object2ObjectOpenHashMap<String, PlayerState>();
	private final Map<Integer, PlayerState> playerStatesByClientId = new Object2ObjectOpenHashMap<Integer, PlayerState>();
	private final List<SegmentControllerOutline> bluePrintsToSpawn = new ObjectArrayList<SegmentControllerOutline>();
	private final PlayerAccountEntrySet blackListedIps = new PlayerAccountEntrySet();
	private final PlayerAccountEntrySet whiteListedIps = new PlayerAccountEntrySet();
	private final PlayerAccountEntrySet blackListedNames = new PlayerAccountEntrySet();
	private final PlayerAccountEntrySet blackListedAccounts = new PlayerAccountEntrySet();
	private final PlayerAccountEntrySet whiteListedNames = new PlayerAccountEntrySet();
	private final PlayerAccountEntrySet whiteListedAccounts = new PlayerAccountEntrySet();
	private final ObjectArrayFIFOQueue<ExplosionRunnable> explosionOrdersFinished = new ObjectArrayFIFOQueue<ExplosionRunnable>();
	private final ObjectArrayList<ExplosionRunnable> explosionOrdersQueued = new ObjectArrayList<ExplosionRunnable>();
	private final Set<PlayerState> spawnRequests = new ObjectOpenHashSet<PlayerState>();
	private final Set<PlayerState> spawnRequestsReady = new ObjectOpenHashSet<PlayerState>();
	private final List<Sendable> scheduledUpdates = new ObjectArrayList<Sendable>();
	private final DatabaseIndex databaseIndex;
	private final MobSpawnThread mobSpawnThread;
	private final GameMapProvider gameMapProvider;
	private final ObjectArrayList<RegionHook<?>> creatorHooks = new ObjectArrayList<RegionHook<?>>();
	private final ObjectArrayFIFOQueue<ByteBuffer> bufferPool = new ObjectArrayFIFOQueue<ByteBuffer>(5);
	public ObjectArrayFIFOQueue<Vector3i> toLoadSectorsQueue;
	public String[] logbookEntries;
	public long delayAutosave;
	public long udpateTime;
	byte[] buffer = new byte[1024 * 1024]; // 1mbBuffer
	//	public static final ObjectArrayFIFOQueue<Deflater> inflaterPool = new ObjectArrayFIFOQueue<Deflater>();
	//	static{
	//		for(int i = 0; i < 5; i++){
	//
	//		}
	//	}
	private Document blockBehaviorConfig;
	private SegmentDataManager segmentDataManager;
	private GameModes gameMode;
	private ArrayList<AdminCommandQueueElement> adminCommands = new ArrayList<AdminCommandQueueElement>();
	private HashSet<Sendable> flaggedAddedObjects = new HashSet<Sendable>();
	private HashSet<Sendable> flaggedRemovedObjects = new HashSet<Sendable>();
	private SendableGameState gameState;
	private final long serverStartTime;
	private long serverTimeMod;
	private long timedShutdownStart;
	private int timedShutdownSeconds;
	private long timedMessageStart;
	private int timedMessageSeconds;
	private String timedMessage;
	private boolean factionReinstitudeFlag;
	private String configCheckSum;
	private String factionConfigCheckSum;
	private String configPropertiesCheckSum;
	private byte[] blockConfigFile;
	private byte[] factionConfigFile;
	private byte[] blockPropertiesFile;
	private ResourceMap resourceMap;
	private byte[] blockBehaviorBytes;
	private String blockBehaviorChecksum;
	private String customTexturesChecksum;
	private byte[] customTextureFile;
	private boolean synched;
	private final ControlElementMapOptimizer controlOptimizer = new ControlElementMapOptimizer();

	public interface PlayerAttachedInterface{
		public void onPlayerAttached(PlayerState playerState, Sendable detachedFrom, Vector3i fromWhere, Vector3i parameter);
		public void onPlayerDetached(PlayerState playerState, boolean hide, Vector3i parameter);
	}

	public final List<PlayerAttachedInterface> playerAttachedListeners = new ObjectArrayList<GameServerState.PlayerAttachedInterface>();

	private final FleetManager fleetManager;
	private final GameServerNetworkSettings settings;
	private final GameNetworkManager networmanager;
	private final UpdateSynch updateSynch;

	
	/**
	 * The Constructor
	 *
	 * @throws SQLException
	 */
	public GameServerState() throws SQLException {

		this.settings = new GameServerNetworkSettings();
		this.networmanager = new GameNetworkManager(this);
		this.updateSynch = new UpdateSynch();

		File f = new FileExt(DATABASE_PATH + "version");
		if (!f.exists()) {
		} else {
			try {
				BufferedReader fi = new BufferedReader(new FileReader(f));
				DB_VERSION_READ = Integer.parseInt(fi.readLine());
				fi.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < 10; i++) {
			ByteBuffer byteBuffer = MemoryUtil.memAlloc(1024 * 1024); // 1mbBuffer
			bufferPool.enqueue(byteBuffer);
		}
		String[] l;
		if(DB_VERSION_READ == 1 || isOldDb()) {
			try {
				doChunk32Migration();
			} catch(IOException e) {
				e.printStackTrace();
				throw new SQLException(e);
			}

		}
		if(DB_VERSION_READ > 0 && DB_VERSION_READ < DATABASE_VERSION) {
			BufferedWriter fw;
			try {
				f.delete();
				fw = new BufferedWriter(new FileWriter(f));
				fw.append(String.valueOf(GameServerState.DATABASE_VERSION));
				fw.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		serverStartTime = System.currentTimeMillis();

		LogUtil.log().fine("STARMADE SERVER VERSION: " + VersionContainer.VERSION + "; Build(" + VersionContainer.build + ")");
		LogUtil.log().fine("STARMADE SERVER STARTED: " + (new Date(System.currentTimeMillis())));

		ServerConfig.read();
		try {
			ServerConfig.write();
		} catch(IOException e) {
			e.printStackTrace();
		}
		gameConfig = new GameConfig(this);
		try {
			gameConfig.parse();
		} catch(Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
		debugController = new DebugServerController(this);
		mobSpawnThread = new MobSpawnThread();
		mobSpawnThread.setPriority(3);
		mobSpawnThread.start();

		gameMode = GameModes.SANDBOX;

		segmentDataManager = new SegmentDataManager(this);

		gameMapProvider = new GameMapProvider(this);

		universe = new Universe(this);
		boolean exists = DatabaseIndex.existsDB();
		if(ENTITY_DATABASE_PATH != null){
			try {
				databaseIndex = new DatabaseIndex();
				if(!exists) {
					databaseIndex.createDatabase();
				} else {
					databaseIndex.getTableManager().migrate();
					try {
						databaseIndex.migrateFleets(null);
						databaseIndex.migrateAddFields();
						databaseIndex.migrateSectorsAndSystems(null);
						databaseIndex.getTableManager().getFTLTable().migrateFTL(null);
						databaseIndex.migrateMessageSystem(null);
						databaseIndex.migrateTrade(null);
						databaseIndex.migrateEffects(null);
						databaseIndex.migrateVisibilityAndPlayers(null);
						databaseIndex.migrateNPCFactionStats();

						//				databaseIndex.migrateMetaItemTable(null);
					} catch(IOException e) {
						e.printStackTrace();
					}

					databaseIndex.migrateAddSectorAsteroidsTouched();
					databaseIndex.migrateUIDFieldSize();
					databaseIndex.getTableManager().getSystemTable().migrateSystemsResourcesField();
				}
			} catch(Exception exception) {
				exception.printStackTrace();
				throw new RuntimeException(exception);
			}
			NPCFactionPresetManager.importPresets(ENTITY_DATABASE_PATH);

			universe.getGalaxyManager().initializeOnServer();
		} else {
			System.err.println("NO DATABASE CONNECTION");
			databaseIndex = null;
		}
		simulationManager = new SimulationManager(this);
		metaObjectManager = new MetaObjectManager(this);
		serverPlayerMessager = new ServerPlayerMessager(this);
		channelRouter = new ChannelRouter(this);
		fleetManager = new FleetManager(this);
		instance = this;
	}

	private boolean isOldDb() {
		if(SEGMENT_DATA_DATABASE_PATH == null) {
			return false;
		}
		if(DB_VERSION_READ == 0) {
			File f = new FileExt(SEGMENT_DATA_DATABASE_PATH);
			if(f.exists()) {
				String[] l = f.list();

				if(l.length > 0 && l[0].endsWith(".smd2")) {
					System.err.println("[SERVER] Old chunk16 database detected. MIGRATION NEEDED");
					return true;
				}
				if(l.length > 1 && l[1].endsWith(".smd2")) {
					System.err.println("[SERVER] Old chunk16 database detected. MIGRATION NEEDED");
					return true;
				}
				if(l.length > 2 && l[2].endsWith(".smd2")) {
					System.err.println("[SERVER] Old chunk16 database detected. MIGRATION NEEDED");
					return true;
				}
				if(l.length > 0 && l[l.length - 1].endsWith(".smd2")) {
					System.err.println("[SERVER] Old chunk16 database detected. MIGRATION NEEDED");
					return true;
				}
			}
		}
		return false;
	}

	private int bkmaxFile, bkfile;
	private final Long2ObjectOpenHashMap<PlayerState> playerStatesByDbId = new Long2ObjectOpenHashMap<PlayerState>();
	public final DebugServerController debugController;
	private Exception currentE;

	public final List<SendableAddedRemovedListener> sendableAddedRemovedListeners = new ObjectArrayList<SendableAddedRemovedListener>();
	private void doChunk32Migration() throws IOException {
		System.err.println("[SERVER] DATABASE VERSION < 2. SERVER DATABASE DATA IS NOW MIGRATING TO CHUNK32");
		if(ServerConfig.BACKUP_WORLD_ON_MIGRATION.isOn()) {
			bkmaxFile = FileUtil.countFilesRecusrively(DATABASE_PATH);
			FolderZipper.ZipCallback zipCallback = f -> {
				LoadingScreen.serverMessage = Lng.str("Migration to new Chunk32 System initiated. Backup up data just in case. Files backed up: %s/%s", bkfile, bkmaxFile);
				bkfile++;
			};
			File f = new FileExt(DATABASE_PATH);
			FolderZipper.zipFolder(f.getAbsolutePath(), "CHUNK16BACKUP_" + f.getName() + ".zip", "backup-StarMade-", zipCallback, "", null, true);
		}
		bkmaxFile = FileUtil.countFilesRecusrively(SEGMENT_DATA_DATABASE_PATH);
		bkfile = 0;
		FolderZipper.ZipCallback zipCallback = f -> {
			LoadingScreen.serverMessage = Lng.str("Migration to new Chunk32 System running... Files Converted: %s/%s", bkfile, bkmaxFile);
			bkfile++;
		};

		Chunk32Migration.processFolder(SEGMENT_DATA_DATABASE_PATH, true, zipCallback, false);
	}

	public static void initPaths(boolean gui, int iteration) {
		iteration++;
		ENTITY_DATABASE_PATH = DATABASE_PATH;
		ENTITY_BLUEPRINT_PATH = "." + File.separator + "blueprints" + File.separator;
		ENTITY_TEMP_BLUEPRINT_PATH = "." + File.separator + "tmpBB" + File.separator;
		ENTITY_BLUEPRINT_PATH_DEFAULT = "." + File.separator + "blueprints-default" + File.separator;

		SEGMENT_DATA_BLUEPRINT_PATH = "." + File.separator + "blueprints" + File.separator + "DATA" + File.separator;
		SEGMENT_DATA_TEMP_BLUEPRINT_PATH = "." + File.separator + "tmpBB" + File.separator + "DATA" + File.separator;
		SEGMENT_DATA_BLUEPRINT_PATH_DEFAULT = "." + File.separator + "blueprints-default" + File.separator + "DATA" + File.separator;

		ENTITY_BLUEPRINT_PATH_STATIONS_NEUTRAL = "." + File.separator + "blueprints-stations" + File.separator + "neutral" + File.separator;
		SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_NEUTRAL = "." + File.separator + "blueprints-stations" + File.separator + "neutral" + File.separator + "DATA" + File.separator;

		ENTITY_BLUEPRINT_PATH_STATIONS_PIRATE = "." + File.separator + "blueprints-stations" + File.separator + "pirate" + File.separator;
		SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_PIRATE = "." + File.separator + "blueprints-stations" + File.separator + "pirate" + File.separator + "DATA" + File.separator;

		ENTITY_BLUEPRINT_PATH_STATIONS_TRADING_GUILD = "." + File.separator + "blueprints-stations" + File.separator + "trading-guild" + File.separator;
		SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_TRADING_GUILD = "." + File.separator + "blueprints-stations" + File.separator + "trading-guild" + File.separator + "DATA" + File.separator;

		SEGMENT_DATA_DATABASE_PATH = DATABASE_PATH + File.separator + "DATA" + File.separator;

		boolean mkdirs = (new FileExt(SEGMENT_DATA_DATABASE_PATH)).mkdirs();

		System.err.println("[INIT] Segment Database Path: " + (new FileExt(SEGMENT_DATA_DATABASE_PATH)).getAbsolutePath() + "; exists? " + (new FileExt(SEGMENT_DATA_DATABASE_PATH)).exists() + "; is Dir? " + (new FileExt(SEGMENT_DATA_DATABASE_PATH)).isDirectory() + "; creating dir successful (gives false if exists)? " + mkdirs);

		if(!(new FileExt(SEGMENT_DATA_DATABASE_PATH)).exists()) {

			if(iteration < 4) {
				System.err.println("[ERROR] Exception creating directories. retrying in one sec");
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				initPaths(gui, iteration);

			} else {
				GuiErrorHandler.processErrorDialogException(new FileNotFoundException("Cannot create database directories in game installation dir.\nPlease make sure you have the right to do so.\nPlease go to help.star-made.org for additional help."));
			}
		}

		(new FileExt(ENTITY_BLUEPRINT_PATH_STATIONS_NEUTRAL)).mkdirs();
		(new FileExt(SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_NEUTRAL)).mkdirs();

		(new FileExt(ENTITY_BLUEPRINT_PATH_STATIONS_PIRATE)).mkdirs();
		(new FileExt(SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_PIRATE)).mkdirs();

		(new FileExt(ENTITY_BLUEPRINT_PATH_STATIONS_TRADING_GUILD)).mkdirs();
		(new FileExt(SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_TRADING_GUILD)).mkdirs();

		if((new FileExt(ENTITY_BLUEPRINT_PATH_STATIONS_NEUTRAL)).list().length == 1 && (new FileExt(ENTITY_BLUEPRINT_PATH_STATIONS_PIRATE)).list().length == 1 && (new FileExt(ENTITY_BLUEPRINT_PATH_STATIONS_TRADING_GUILD)).list().length == 1) {
			JFrame f = null;
			if(gui) {
				try {
					f = new PreparingFilesJFrame();
					f.setVisible(true);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			//import
			try {
				LoadingScreen.serverMessage = Lng.str("Importing default stations (one time operation)");
				FileUtil.extract(new FileExt("." + File.separator + "data" + File.separator + "prefabBlueprints" + File.separator + "defaultStations.zip"), "." + File.separator + "blueprints-stations");
			} catch(IOException e) {
				e.printStackTrace();
			}

			if(f != null) {
				f.dispose();
			}
		}


	}

	public static synchronized int getItemId() {
		int i = itemIds;
		itemIds++;
		return i;
	}


	public static void readDatabasePosition(boolean hasGUI) throws IOException {
		ServerConfig.read();

		if (ServerConfig.WORLD.getString().toString().toLowerCase(Locale.ENGLISH).equals("unset")) {
			ServerConfig.WORLD.setString("world0");
		}
		String db = "." + File.separator + "server-database" + File.separator + ServerConfig.WORLD.getString().toLowerCase(Locale.ENGLISH) + File.separator;
		DATABASE_PATH = db;
		System.out.println("[SERVER] using world: " + ServerConfig.WORLD.getString() + "; " + db);
		initPaths(hasGUI, 0);

		ServerConfig.write();
	}

	public void addCountdownMessage(int seconds, String message) {
		if (seconds >= 0) {
			this.timedMessageStart = System.currentTimeMillis();
			this.timedMessageSeconds = seconds;
			this.timedMessage = message;
		} else {
			this.timedShutdownStart = -1;
			gameState.getNetworkObject().serverCountdownTime.set(-1f);
			gameState.getNetworkObject().serverCountdownMessage.set("");
		}
	}

	public void addServerFileRequest(ClientChannel clientChannel, String req) {
		synchronized (fileRequests) {
			fileRequests.add(new FileRequest(clientChannel, req));
		}
	}

	public void addTimedShutdown(int seconds) {
		ServerShutdownCounterEvent event = new ServerShutdownCounterEvent(seconds);
		fireEvent(event, true);
		if(event.isCanceled()) return;
		seconds = event.getCountDown();

		if(seconds >= 0) {
			timedShutdownStart = System.currentTimeMillis();
			timedShutdownSeconds = seconds;
		} else {
			this.timedShutdownStart = -1;
			gameState.getNetworkObject().serverShutdown.set(-1f);
		}
	}

	public void announceModifiedBlueprintUsage(BlueprintInterface entry, String blueprintSpawnedBy) {
		PlayerState s;
		try {
			s = getPlayerFromName(blueprintSpawnedBy);

			if (s != null) {
				
				long timoutTimeInMin = ServerConfig.AUTO_BAN_TIME_IN_MINUTES.getInt();
				
				long bannedUntil = timoutTimeInMin > 0 ? System.currentTimeMillis() + timoutTimeInMin * 60000 : -1;
				if(ServerConfig.AUTO_BAN_ID_MODIFIED_BLUEPRINT_USE.isOn()) {
					System.err.println("[SERVER] banning name for modified blueprint use " + s.getName());
					LogUtil.log().fine("[SERVER] banning name for modified blueprint use " + s.getName());


					getController().addBannedName("[SYSTEM]", s.getName(), bannedUntil);
				}

				if(ServerConfig.AUTO_BAN_IP_MODIFIED_BLUEPRINT_USE.isOn()) {

					if(s.getIp() != null) {
						System.err.println("[SERVER] banning IP for modified blueprint use " + s.getIp());
						try {
							LogUtil.log().fine("[SERVER] banning IP for modified blueprint use " + s.getIp() + " (name: " + s.getName() + ")");
							getController().addBannedIp("[SYSTEM]", s.getIp().startsWith("/") ? s.getIp().substring(1) : s.getIp(), bannedUntil);
						} catch(NoIPException e) {
							e.printStackTrace();
						}
					}
				}

				if(ServerConfig.AUTO_KICK_MODIFIED_BLUEPRINT_USE.isOn()) {
					try {
						System.err.println("[SERVER] kicking for modified blueprint use " + s.getName());
						getController().sendLogout(s.getClientId(), "You have been kick for using a modified blueprint");
					} catch(IOException e) {
						e.printStackTrace();
					}
				}

				if(ServerConfig.REMOVE_MODIFIED_BLUEPRINTS.isOn()) {
					System.err.println("[SERVER] removing modified blueprint " + entry.getName());
					getController().broadcastMessageAdmin(Lng.astr("Removing corrupted blueprint.\nA backup will be created at\n%s", "blueprints/exported/" + entry.getName() + ".sment"), ServerMessage.MESSAGE_TYPE_ERROR);
					s.sendServerMessage(new ServerMessage(Lng.astr("Removing corrupted blueprint.\nA backup will be created at\n%s", "blueprints/exported/" + entry.getName() + ".sment"), ServerMessage.MESSAGE_TYPE_ERROR, ServerMessage.MESSAGE_TYPE_ERROR));
					getCatalogManager().serverDeletEntry(entry.getName());
				}
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void chat(String from, String message, String prefix, boolean displayName) {
//		chatLog.add(chatSystem.getOwnerStateId() + ": " + message);
	}


	@Override
	public String[] getCommandPrefixes() {
		return null;
	}

	@Override
	public byte[] getDataBuffer() {
		return buffer;
	}

	@Override
	public ByteBuffer getDataByteBuffer() {
		synchronized(bufferPool) {
			buffersInUse++;
			while(bufferPool.isEmpty()) {
				try {
					bufferPool.wait(20000);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			buffersInUse--;
			return bufferPool.dequeue();
		}
	}

	@Override
	public Version getVersion() {
		return VersionContainer.VERSION;
	}


	@Override
	public void notifyOfAddedObject(Sendable sendable) {
		if(sendable instanceof SegmentController) {
			SegmentController c = ((SegmentController) sendable);
			if(c.getCreatorThread() == null) {
				c.startCreatorThread();
			}
		}

		synchronized(flaggedAddedObjects) {
			flaggedAddedObjects.add(sendable);
		}
	}

	@Override
	public void notifyOfRemovedObject(Sendable sendable) {

		synchronized(flaggedRemovedObjects) {
			flaggedRemovedObjects.add(sendable);
		}
	}

	@Override
	public String onAutoComplete(String s, TextCallback callback, String prefix) {
		System.err.println("NO AUTOCOMPLETE ON SERVER");
		return s;
	}


	@Override
	public void onStringCommand(String subSequence, TextCallback callback, String prefix) {
		throw new IllegalArgumentException();
	}

	@Override
	public void releaseDataByteBuffer(ByteBuffer buffer) {
		synchronized(bufferPool) {
			bufferPool.enqueue(buffer);
			bufferPool.notify();
		}
	}

	/**
	 * @return the resourceMap
	 */
	@Override
	public ResourceMap getResourceMap() {
		return resourceMap;
	}

	/**
	 * @param resourceMap the resourceMap to set
	 */
	public void setResourceMap(ResourceMap resourceMap) {
		this.resourceMap = resourceMap;
	}

	@Override
	public long getUpdateTime() {
		return udpateTime;
	}

	@Override
	public void setSynched() {
		assert (!synched) : printLast();
		assert (setLastSynch());
		synched = true;
	}

	private String printLast() {
		if(currentE != null) {
			System.err.println("CURRENTLY SYNCHED BY:");
			currentE.printStackTrace();
		}
		return currentE.getMessage();
	}

	private boolean setLastSynch() {
		try {
			throw new Exception("Trace");
		} catch(Exception e) {
			currentE = e;
		}

		return true;
	}

	@Override
	public void setUnsynched() {
		assert (synched);
		synched = false;
	}

	@Override
	public boolean isSynched() {
		return synched;
	}

	public static int uploadBlockSize = 256;

	@Override
	public long getUploadBlockSize() {
		return uploadBlockSize;//(ServerConfig.SKIN_SERVER_UPLOAD_BLOCK_SIZE.getInt()).longValue();
	}


	@Override
	public void executeAdminCommand(String serverPassword, String command, RegisteredClientInterface c) {

		if(!(c instanceof AdminLocalClient) && !ServerConfig.SUPER_ADMIN_PASSWORD_USE.isOn()) {
			try {
				c.serverMessage("END; ERROR: super admin not enabled on this server");
			} catch(IOException e) {
				e.printStackTrace();
			}
			c.executedAdminCommand();
			return;
		}

		if (serverPassword != null && serverPassword.equals(ServerConfig.SUPER_ADMIN_PASSWORD.getString())) {
			try {
				if(command.startsWith("/chatchannel ")) {

					String[] split = StringTools.splitParameters(command.substring("/chatchannel".length()));
					if(split.length == 2) {
						ChatChannel channel = channelRouter.getChannel(split[0]);
						if(channel != null) {
							ChatMessage msg = new ChatMessage();
							msg.sender = "[SERVER]";
							msg.receiver = channel.getUniqueChannelName();
							msg.receiverType = ChatMessage.ChatMessageType.CHANNEL;
							msg.text = split[1];
							channel.send(msg);
//							getController().broadcastMessage(split[1],  ServerMessage.MESSAGE_TYPE_SIMPLE);
							c.serverMessage("END; broadcasted as server message: " + split[1]);
							//not an admin command
							c.executedAdminCommand();
						} else {
							c.serverMessage("END; error: chat channel not found: " + split[0] + "; note: General channel is \"all\" faction channels are \"Faction<fid>\" (e.g. Faction1001) (all channels are case sensitive)");
						}
					} else {
						c.serverMessage("END; error: invalid format: use /chatchannel \"channel name\" \"message\"");
					}
				} else if(command.startsWith("/chat ")) {
					AllChannel channel = channelRouter.getAllChannel();
					ChatMessage msg = new ChatMessage();
					msg.sender = "[SERVER]";
					msg.receiver = channel.getUniqueChannelName();
					msg.receiverType = ChatMessage.ChatMessageType.CHANNEL;
					msg.text = command.substring(6);
					channel.send(msg);
//					getController().broadcastMessage(command.substring(6), ServerMessage.MESSAGE_TYPE_SIMPLE);
					c.serverMessage("END; broadcasted as server message: " + command.substring(6));
					//not an admin command
					c.executedAdminCommand();
				} else if(command.startsWith("/pm ")) {
					String[] split = command.split(" ", 3);
					if(split.length == 3) {
						String plyer = split[1].trim();
						try {
							PlayerState playerFromName = getPlayerFromName(plyer);
							playerFromName.sendServerMessage(new ServerMessage(new Object[]{"[SERVER-PM] " + split[2]}, ServerMessage.MESSAGE_TYPE_SIMPLE, playerFromName.getId()));
							c.serverMessage("END; send to " + plyer + " as server message: " + split[2]);
						} catch(PlayerNotFountException e) {
							e.printStackTrace();
							c.serverMessage("END; player not found: '" + plyer + "'");
						}

					} else {
						c.serverMessage("END; not enough parameters for PM (/pm playername text) ");
					}

					//not an admin command
					c.executedAdminCommand();
				} else if(command.startsWith("/")) {
					try {
						command = command.substring(1);

						String[] split = command.split(" ");
						AdminCommands com = AdminCommands.valueOf(split[0].trim().toUpperCase(Locale.ENGLISH));
						command = command.substring(split[0].length());
						String[] parameterArray = StringTools.splitParameters(command);
						Object[] packParameters = AdminCommands.packParameters(com, parameterArray);

						//
						//
						//						String[] p = new String[split.length -1];
						//						for(int i = 0; i < split.length -1; i++){
						//							p[i] = split[i+1];
						//						}
						//						Object[] packParameters;
						//
						//						packParameters = AdminCommands.packParameters(com, p);

						getController().enqueueAdminCommand(c, com, packParameters);
						//no c.executedAdminCommand() needed, it will be triggered after execution
					} catch(Exception e) {
						e.printStackTrace();
						c.serverMessage("Admin command failed: Error packing parameters");
						c.executedAdminCommand();
					}
				} else {
					try {
						c.serverMessage("END; ERROR: command not regognize (use /chat to broadcast)");
						c.executedAdminCommand();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
				try {
					c.serverMessage("END; Error: " + e.getClass() + ": " + e.getMessage());
					c.executedAdminCommand();
				} catch(IOException e1) {
					e1.printStackTrace();
				}
			}

		} else {
			try {
				c.serverMessage("END; ERROR: wrong super password");
			} catch(IOException e) {
				e.printStackTrace();
			}
			c.executedAdminCommand();
		}

	}

	@Override
	public boolean filterJoinMessages() {
		return ServerConfig.FILTER_CONNECTION_MESSAGES.isOn();
	}

	@Override
	public boolean flushPingImmediately() {
		return ServerConfig.PING_FLUSH.isOn();
	}

	@Override
	public String getAcceptingIP() {
		return ServerConfig.SERVER_LISTEN_IP.getString();
	}

	@Override
	public int getMaxClients() {
		return ServerConfig.MAX_CLIENTS.getInt();
	}

	@Override
	public NetworkProcessor getProcessor(int client) {
		return getClients().get(client).getProcessor();
	}

	@Override
	public String getServerDesc() {
		return gameState.getServerDescription();
	}

	@Override
	public String getServerName() {
		return gameState.getServerName();
	}

	@Override
	public int getSocketBufferSize() {
		return ServerConfig.SOCKET_BUFFER_SIZE.getInt();
	}

	@Override
	public long getStartTime() {
		return serverStartTime;
	}

	@Override
	public boolean tcpNoDelay() {
		return ServerConfig.TCP_NODELAY.isOn();
	}

	@Override
	public boolean useUDP() {
		return ServerConfig.USE_UDP.isOn();
	}

	@Override
	public SessionCallback getSessionCallBack(String sessionIdOrServerName, String login_code) {
		return Starter.getAuthStyle() == 0 ? new OldSessionCallback(sessionIdOrServerName, login_code) : new NewSessionCallback(sessionIdOrServerName, login_code);
	}

	@Override
	public void addNTReceivedStatistics(RegisteredClientOnServer client, int size, int lastCheck, int lastHeader, int lastCommand, Object[] lastParameters, ObjectArrayList<NetworkObject> lastReceived) {

	}

	@Override
	public int getNTSpamProtectTimeMs() {
		return ServerConfig.NT_SPAM_PROTECT_TIME_MS.getInt();
	}

	@Override
	public int getNTSpamProtectMaxAttempty() {
		return ServerConfig.NT_SPAM_PROTECT_MAX_ATTEMPTS.getInt();
	}

	@Override
	public String getNTSpamProtectException() {
		return ServerConfig.NT_SPAM_PROTECT_EXCEPTIONS.getString();
	}

	@Override
	public boolean isNTSpamCheckActive() {
		return ServerConfig.NT_SPAM_PROTECT_ACTIVE.isOn();
	}

	@Override
	public boolean announceServer() {
		return ServerConfig.ANNOUNCE_SERVER_TO_SERVERLIST.isOn();
	}

	@Override
	public String announceHost() {
		return ServerConfig.HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST.getString();
	}

	@Override
	public boolean checkUserAgent(byte userAgent, String playerName) {
		System.err.println("[LOGIN] checking user agent: " + userAgent + ": " + playerName);
		if(userAgent == HostPortLoginName.STAR_MOTE) {
			System.err.println("[LOGIN] checking StarMote access for: " + playerName + ": " + isAdmin(playerName));
			return isAdmin(playerName);
		}

		return true;
	}

	/**
	 * @return the activeFileRequests
	 */
	public List<FileRequest> getActiveFileRequests() {
		return activeFileRequests;
	}

	/**
	 * @return the adminCommands
	 */
	public List<AdminCommandQueueElement> getAdminCommands() {
		return adminCommands;
	}

	/**
	 * @param adminCommands the adminCommands to set
	 */
	public void setAdminCommands(ArrayList<AdminCommandQueueElement> adminCommands) {
		this.adminCommands = adminCommands;
	}

	/**
	 * @return the adminLocalClient
	 */
	public AdminLocalClient getAdminLocalClient() {
		return adminLocalClient;
	}

	/**
	 * @return the admins
	 */
	public Map<String, Admin> getAdmins() {
		return admins;
	}

	public PlayerAccountEntrySet getBlackListedIps() {
		return blackListedIps;
	}

	public PlayerAccountEntrySet getBlackListedNames() {
		return blackListedNames;
	}

	public PlayerAccountEntrySet getBlackListedAccounts() {
		return blackListedAccounts;
	}

	public byte[] getBlockConfigFile() {
		return blockConfigFile;
	}

	/**
	 * @param blockConfigFile the blockConfigFile to set
	 */
	public void setBlockConfigFile(byte[] blockConfigFile) {
		this.blockConfigFile = blockConfigFile;
	}

	public byte[] getBlockPropertiesFile() {
		return blockPropertiesFile;
	}

	/**
	 * @param blockPropertiesFile the blockPropertiesFile to set
	 */
	public void setBlockPropertiesFile(byte[] blockPropertiesFile) {
		this.blockPropertiesFile = blockPropertiesFile;
	}

	public byte[] getBlockBehaviorFile() {
		return blockBehaviorBytes;
	}

	/**
	 * @return the bluePrintsToSpawn
	 */
	public List<SegmentControllerOutline> getBluePrintsToSpawn() {
		return bluePrintsToSpawn;
	}

	@Override
	public CatalogManager getCatalogManager() {
		return gameState.getCatalogManager();
	}

	/**
	 * @return the configCheckSum
	 */
	public String getConfigCheckSum() {
		return configCheckSum;
	}

	public void setConfigCheckSum(String s) {
		configCheckSum = s;
	}

	/**
	 * @return the configPropertiesCheckSum
	 */
	public String getConfigPropertiesCheckSum() {
		return configPropertiesCheckSum;
	}

	public void setConfigPropertiesCheckSum(String s) {
		configPropertiesCheckSum = s;
	}

	public String getCustomTexturesChecksum() {
		return customTexturesChecksum;
	}

	public void setCustomTexturesChecksum(String cs) {
		customTexturesChecksum = cs;
	}

	public ObjectArrayList<RegionHook<? extends UsableRegion>> getCreatorHooks() {
		return creatorHooks;
	}

	/**
	 * @return the currentGravitySources
	 */
	@Override
	public ObjectArrayList<SimpleTransformableSendableObject> getCurrentGravitySources() {
		return currentGravitySources;
	}

	/**
	 * @return the databaseIndex
	 */
	public DatabaseIndex getDatabaseIndex() {
		return databaseIndex;
	}


	@Override
	public FactionManager getFactionManager() {
		if(gameState != null) {
			return gameState.getFactionManager();
		} else {
			return null;
		}
	}

	/**
	 * @return the fileRequests
	 */
	public List<FileRequest> getFileRequests() {
		return fileRequests;
	}

	/**
	 * @return the gameMapProvider
	 */
	public GameMapProvider getGameMapProvider() {
		return gameMapProvider;
	}

	/**
	 * @return the gameMode
	 */
	public GameModes getGameMode() {
		return gameMode;
	}

	/**
	 * @param gameMode the gameMode to set
	 */
	public void setGameMode(GameModes gameMode) {
		this.gameMode = gameMode;
	}

	/**
	 * @return the gameState
	 */
	@Override
	public SendableGameState getGameState() {
		return gameState;
	}

	/**
	 * @param gameState the gameState to set
	 */
	public void setGameState(SendableGameState gameState) {
		this.gameState = gameState;
	}

	@Override
	public boolean isPhysicalAsteroids() {
		return ServerConfig.ASTEROIDS_ENABLE_DYNAMIC_PHYSICS.isOn();
	}

	/**
	 * @return the blockBehaviorConfig
	 */
	@Override
	public Document getBlockBehaviorConfig() {
		return blockBehaviorConfig;
	}

	/**
	 * @param blockBehaviorConfig the blockBehaviorConfig to set
	 */
	public void setBlockBehaviorConfig(Document blockBehaviorConfig) {
		this.blockBehaviorConfig = blockBehaviorConfig;
	}

	@Override
	public float getSectorSize() {
		return (getSectorSizeWithoutMargin() + Universe.SECTOR_MARGIN);
	}

	@Override
	public Short2ObjectOpenHashMap<Technology> getAllTechs() {
		return techs;
	}

	@Override
	public boolean getMaterialPrice() {
		return ServerConfig.USE_DYNAMIC_RECIPE_PRICES.isOn();
	}

	@Override
	public int getSegmentPieceQueueSize() {
		if (!Starter.DEDICATED_SERVER_ARGUMENT) {
			return EngineSettings.SEGMENT_PIECE_QUEUE_SINGLEPLAYER.getInt();
		}
		return ServerConfig.NT_BLOCK_QUEUE_SIZE.getInt();

	}

	@Override
	public ControlElementMapOptimizer getControlOptimizer() {
		return controlOptimizer;
	}

	/**
	 * @return the channelRouter
	 */
	@Override
	public ChannelRouter getChannelRouter() {
		return channelRouter;
	}


	/**
	 * @return the metaObjectManager
	 */
	@Override
	public MetaObjectManager getMetaObjectManager() {
		return metaObjectManager;
	}

	@Override
	public void requestMetaObject(int id) {
		throw new IllegalArgumentException("This may not be called for server");
	}

	public PlayerState getPlayerFromName(String name) throws PlayerNotFountException {
		PlayerState playerState = playerStatesByName.get(name);
		if(playerState != null) {
			return playerState;
		}
		throw new PlayerNotFountException(name);
	}

	public PlayerState getPlayerFromNameIgnoreCase(String name) throws PlayerNotFountException {
		PlayerState playerState = getPlayerFromNameIgnoreCaseWOException(name);
		if(playerState != null) {
			return playerState;
		}
		throw new PlayerNotFountException(name);
	}

	public PlayerState getPlayerFromNameIgnoreCaseWOException(String name) {
		return playerStatesByNameLowerCase.get(name.toLowerCase(Locale.ENGLISH));
	}

	public PlayerState getPlayerFromStateId(int id) throws PlayerNotFountException {
		PlayerState playerState = playerStatesByClientId.get(id);
		if(playerState != null) {
			return playerState;
		}
		throw new PlayerNotFountException("CLIENT-ID(" + id + ") ");

	}

	/**
	 * @return the playerStatesByName
	 */
	public Map<String, PlayerState> getPlayerStatesByName() {
		return playerStatesByName;
	}

	/**
	 * @return the admins
	 */
	public HashMap<String, ProtectedUplinkName> getProtectedUsers() {
		return protectedNames;
	}

	/**
	 * @return the scheduledUpdates
	 */
	public List<Sendable> getScheduledUpdates() {
		return scheduledUpdates;
	}

	/**
	 * @return the sectorSwitches
	 */
	public List<SectorSwitch> getSectorSwitches() {
		return sectorSwitches;
	}

	/**
	 * @return the segmentControllersByName
	 */
	public Map<String, SegmentController> getSegmentControllersByName() {
		return segmentControllersByName;
	}

	@Override
	public SegmentDataManager getSegmentDataManager() {
				return this.segmentDataManager;
	}

	/**
	 * @return the segmentRequests
	 */
	public ObjectArrayFIFOQueue<ServerSegmentRequest> getSegmentRequests() {
		return segmentRequests;
	}

	/**
	 * @return the sectorImportRequests
	 */
	public ObjectArrayFIFOQueue<ServerExecutionJob> getServerExecutionJobs() {
		return serverExecutionJobs;
	}

	public long getServerStartTime() {
		return serverStartTime;
	}

	/**
	 * @return the serverTimeMod
	 */
	public long getServerTimeMod() {
		return serverTimeMod;
	}

	/**
	 * @param serverTimeMod the serverTimeMod to set
	 */
	public void setServerTimeMod(long serverTimeMod) {
		this.serverTimeMod = serverTimeMod;
	}

	/**
	 * @return the simulationManager
	 */
	public SimulationManager getSimulationManager() {
		return simulationManager;
	}

	/**
	 * @return the spawnRequests
	 */
	public Set<PlayerState> getSpawnRequests() {
		return spawnRequests;
	}

	/**
	 * @return the threadedSegmentWriter
	 */
	public ThreadedSegmentWriter getThreadedSegmentWriter() {
		return threadedSegmentWriter;
	}

	/**
	 * @return the timedMessage
	 */
	public String getTimedMessage() {
		return timedMessage;
	}

	/**
	 * @param timedMessage the timedMessage to set
	 */
	public void setTimedMessage(String timedMessage) {
		this.timedMessage = timedMessage;
	}

	/**
	 * @return the timedMessageSeconds
	 */
	public int getTimedMessageSeconds() {
		return timedMessageSeconds;
	}

	/**
	 * @param timedMessageSeconds the timedMessageSeconds to set
	 */
	public void setTimedMessageSeconds(int timedMessageSeconds) {
		this.timedMessageSeconds = timedMessageSeconds;
	}

	/**
	 * @return the timedMessageStart
	 */
	public long getTimedMessageStart() {
		return timedMessageStart;
	}

	/**
	 * @param timedMessageStart the timedMessageStart to set
	 */
	public void setTimedMessageStart(long timedMessageStart) {
		this.timedMessageStart = timedMessageStart;
	}

	/**
	 * @return the timedShutdownSeconds
	 */
	public int getTimedShutdownSeconds() {
		return timedShutdownSeconds;
	}

	/**
	 * @param timedShutdownSeconds the timedShutdownSeconds to set
	 */
	public void setTimedShutdownSeconds(int timedShutdownSeconds) {
		this.timedShutdownSeconds = timedShutdownSeconds;
	}

	/**
	 * @return the timedShutdownStart
	 */
	public long getTimedShutdownStart() {
		return timedShutdownStart;
	}

	/**
	 * @param timedShutdownStart the timedShutdownStart to set
	 */
	public void setTimedShutdownStart(long timedShutdownStart) {
		this.timedShutdownStart = timedShutdownStart;
	}

	/**
	 * @return the universe
	 */
	public Universe getUniverse() {
		return universe;
	}

	/**
	 * @return the whiteListedIps
	 */
	public PlayerAccountEntrySet getWhiteListedIps() {
		return whiteListedIps;
	}

	/**
	 * @return the whiteListedNames
	 */
	public PlayerAccountEntrySet getWhiteListedNames() {
		return whiteListedNames;
	}

	/**
	 * @return the whiteListedNames
	 */
	public PlayerAccountEntrySet getWhiteListedAccounts() {
		return whiteListedAccounts;
	}

	public void handleAddedAndRemovedObjects() throws IOException, SQLException {
		long t0 = System.currentTimeMillis();
		if(!flaggedAddedObjects.isEmpty()) {
			HashSet<Sendable> tmpFlaggedAddedObjects = new HashSet<Sendable>(flaggedAddedObjects.size());
			synchronized(flaggedAddedObjects) {
				tmpFlaggedAddedObjects.addAll(flaggedAddedObjects);
			}
			for(Sendable s : tmpFlaggedAddedObjects) {

				onSendableAdded(s);

				if(s instanceof SendableSegmentProvider) {
					SendableSegmentController segmentController = ((SendableSegmentProvider) s).getSegmentController();
					if(segmentController != null) {
						segmentController.setServerSendableSegmentController(((SendableSegmentProvider) s));
						((SendableSegmentProvider) s).setConnectionReady();
					}

				}
				for(SendableAddedRemovedListener l : sendableAddedRemovedListeners) {
					l.onAddedSendable(s);
				}

				if(s instanceof SegmentController){
					fleetManager.onAddedEntity(((SegmentController)s));
				}
				if(s instanceof PlayerState){
					fleetManager.onJoinedPlayer((PlayerState)s);
				}
			}
			flaggedAddedObjects.clear();
		}
		long took = System.currentTimeMillis() - t0;
		if(took > 10) {
			System.err.println("[SERVER][UPDATE] WARNING: handleAddedAndRemovedObjects update took " + took);
		}

		if(!flaggedRemovedObjects.isEmpty()) {
			HashSet<Sendable> tmpFlaggedRemovedObjects = new HashSet<Sendable>(flaggedRemovedObjects.size());
			synchronized(flaggedRemovedObjects) {
				tmpFlaggedRemovedObjects.addAll(flaggedRemovedObjects);
			}
			for(Sendable s : tmpFlaggedRemovedObjects) {
				if(s instanceof DiskWritable) {
					//						System.err.println("[SERVER] WRITING REMOVED ENTITY "+s);
					if(!s.isWrittenForUnload()) {
						s.setWrittenForUnload(true);
						getController().writeEntity((DiskWritable) s, true);

					}
				} else {
				}
				onSendableRemoved(s);

				for(SendableAddedRemovedListener l : sendableAddedRemovedListeners) {
					l.onRemovedSendable(s);
				}

				
				if(s instanceof PlayerState){
					fleetManager.onLeftPlayer((PlayerState)s);
				}
			}
			flaggedRemovedObjects.clear();
		}
	}


	public boolean isAdmin(String playerName) {
		return admins.isEmpty() || admins.containsKey(playerName.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * @return the factionReinstitudeFlag
	 */
	public boolean isFactionReinstitudeFlag() {
		return factionReinstitudeFlag;
	}

	/**
	 * @param factionReinstitudeFlag the factionReinstitudeFlag to set
	 */
	public void setFactionReinstitudeFlag(boolean factionReinstitudeFlag) {
		this.factionReinstitudeFlag = factionReinstitudeFlag;
	}

	@Override
	public boolean isReady() {
		return super.isReady() && !isShutdown();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.ServerState#getController()
	 */
	@Override
	public GameServerController getController() {
		return (GameServerController) super.getController();
	}

	@Override
	public int getClientIdByName(String name) throws ClientIdNotFoundException {
		try {
			return getPlayerFromName(name).getClientId();
		} catch(PlayerNotFountException e) {
			throw new ClientIdNotFoundException(name);
		}
	}

	@Override
	public String getBuild() {
		return VersionContainer.build;
	}

	/**
	 * this method is thread save. its called on ServerController.update()
	 *
	 * @param s
	 */
	private void onSendableAdded(Sendable s) {
		if(s instanceof PlayerState) {
			playerStatesByName.put(((PlayerState) s).getName(), (PlayerState) s);
			playerStatesByNameLowerCase.put(((PlayerState) s).getName().toLowerCase(Locale.ENGLISH), (PlayerState) s);
			playerStatesByClientId.put(((PlayerState) s).getClientId(), (PlayerState) s);
			playerStatesByDbId.put(((PlayerState) s).getDbId(), (PlayerState) s);
			simulationManager.getPlanner().playerAdded(((PlayerState) s));
		}
		if (s instanceof SegmentController) {
			segmentControllersByName.put(((SegmentController) s).getUniqueIdentifier(), (SegmentController) s);
			segmentControllersByNameLowerCase.put(((SegmentController) s).getUniqueIdentifier().toLowerCase(Locale.ENGLISH), (SegmentController) s);
		}
		if (s instanceof SimpleTransformableSendableObject) {
			if (((SimpleTransformableSendableObject<?>) s).isGravitySource()) {
				currentGravitySources.add((SimpleTransformableSendableObject<?>) s);
			}
		}
		
		if(s instanceof RemoteSector){
			universe.onAddedSectorSynched(((RemoteSector) s).getServerSector());
		}
		//INSERTED CODE
		ServerSendableAddEvent event = new ServerSendableAddEvent(this, s, Event.Condition.POST);
		fireEvent(event, true);
		StarLoaderHooks.onServerSendableAddEvent(event);
		///
	}

	/**
	 * this method is thread save. its called on ServerController.update()
	 *
	 * @param s
	 */
	private void onSendableRemoved(Sendable s) {
		if(s instanceof PlayerState) {
			playerStatesByName.remove(((PlayerState) s).getName());
			playerStatesByNameLowerCase.remove(((PlayerState) s).getName().toLowerCase(Locale.ENGLISH));
			playerStatesByClientId.remove(((PlayerState) s).getClientId());
			playerStatesByDbId.remove(((PlayerState) s).getDbId());
			simulationManager.getPlanner().playerRemoved(((PlayerState) s));
		}
		if (s instanceof SegmentController) {
			segmentControllersByName.remove(((SegmentController) s).getUniqueIdentifier());
			segmentControllersByNameLowerCase.remove(((SegmentController) s).getUniqueIdentifier().toLowerCase(Locale.ENGLISH));
			fleetManager.onRemovedEntity(((SegmentController)s));
		}
		if (s instanceof SimpleTransformableSendableObject) {
			if (((SimpleTransformableSendableObject) s).isGravitySource()) {
				currentGravitySources.remove(s);
			}
		}
		if (s.isMarkedForPermanentDelete()) {
			if (s instanceof UniqueInterface) {
				simulationManager.removeMemberFromGroups(((UniqueInterface) s).getUniqueIdentifier());
			}
			s.destroyPersistent();
		}
		if(s instanceof RemoteSector) {
			universe.onRemovedSectorSynched(((RemoteSector) s).getServerSector());
		}
		//INSERTED CODE
		ServerSendableRemoveEvent event = new ServerSendableRemoveEvent(this, s, Event.Condition.POST);
		fireEvent(event, true);
		///
	}

	public void scheduleExecutionJob(ServerExecutionJob x) {
		synchronized (serverExecutionJobs) {
			serverExecutionJobs.enqueue(x);
		}
	}

	public void scheduleSectorBulkExport(RegisteredClientInterface client, String name) {
		SectorBulkRequest r = new SectorBulkRequest(client, name, true);
		scheduleExecutionJob(r);

	}

	public void scheduleSectorBulkImport(RegisteredClientInterface client, String name) {
		SectorBulkRequest r = new SectorBulkRequest(client, name, false);
		scheduleExecutionJob(r);

	}

	public void scheduleSectorExport(Vector3i sec, RegisteredClientInterface client, String name) {
		SectorExportRequest r = new SectorExportRequest(sec, client, name);
		scheduleExecutionJob(r);
	}

	public void scheduleSectorImport(Vector3i sec, RegisteredClientInterface client, String name) {
		SectorImportRequest r = new SectorImportRequest(sec, client, name);
		scheduleExecutionJob(r);

	}

	public void scheduleUpdate(Sendable sendableSegmentProvider) {
		synchronized (scheduledUpdates) {
			scheduledUpdates.add(sendableSegmentProvider);
		}
	}

	public void spawnMobs(int count, String catalogname, Vector3i sector, Transform transform, int factionId, BluePrintController bbc) throws EntityNotFountException, IOException, EntityAlreadyExistsException {

		mobSpawnThread.spawnMobs(count, catalogname, sector, transform, factionId, bbc);
	}

	/**
	 * @return the segmentRequestsLoaded
	 */
	public ObjectArrayFIFOQueue<ServerSegmentRequest> getSegmentRequestsLoaded() {
		return segmentRequestsLoaded;
	}

	public void setBlockBehaviorBytes(byte[] bc) {
		blockBehaviorBytes = bc;
	}

	/**
	 * @return the blockBehaviorChecksum
	 */
	public String getBlockBehaviorChecksum() {
		return blockBehaviorChecksum;
	}

	/**
	 * @param blockBehaviorChecksum the blockBehaviorChecksum to set
	 */
	public void setBlockBehaviorChecksum(String blockBehaviorChecksum) {
		this.blockBehaviorChecksum = blockBehaviorChecksum;
	}

	public float getSectorSizeWithoutMargin() {
		return ServerConfig.SECTOR_SIZE.getInt();
	}

	/**
	 * @return the serverPlayerMessager
	 */
	public ServerPlayerMessager getServerPlayerMessager() {
		return serverPlayerMessager;
	}

	/**
	 * @return the segmentControllersByNameLowerCase
	 */
	public Map<String, SegmentController> getSegmentControllersByNameLowerCase() {
		return segmentControllersByNameLowerCase;
	}

	public byte[] getCustomTexturesFile() {
		return customTextureFile;
	}

	public void setCustomTexturesFile(byte[] bcx) {
		customTextureFile = bcx;
	}

	/**
	 * @return the playerStatesByNameLowerCase
	 */
	public Map<String, PlayerState> getPlayerStatesByNameLowerCase() {
		return playerStatesByNameLowerCase;
	}

	public void scanOnServer(PlayerState fromPlayer, float distance) {
		System.err.println("[SERVER][SCAN] processing scan ....");
		try {
			StellarSystem stellar = universe.getStellarSystemFromStellarPos(fromPlayer.getCurrentSystem());
			assert (stellar.getPos().equals(fromPlayer.getCurrentSystem()));

			float range = LongRangeScannerElementManager.DEFAULT_SCAN_DISTANCE;
			Universe.SystemOwnershipType systemOwnerShipType = universe.getSystemOwnerShipType(stellar, fromPlayer.getFactionId());

			System.err.println("[SERVER] performing scan for " + fromPlayer + " in system " + stellar + ": " + systemOwnerShipType.name() + ": " + stellar.getOwnerUID() + "; " + stellar.getOwnerFaction());

			boolean scanHoleSystem = false;

			switch(systemOwnerShipType) {
				case BY_ALLY:
					range *= LongRangeScannerElementManager.ALLY_SYSTEM_DISTANCE_MULT;
					break;
				case BY_ENEMY:
					range *= LongRangeScannerElementManager.ENEMY_SYSTEM_DISTANCE_MULT;
					break;
				case BY_NEUTRAL:
					break;
				case BY_SELF:
					range *= LongRangeScannerElementManager.ALLY_SYSTEM_DISTANCE_MULT;
					scanHoleSystem = true;
					break;
				case NONE:
					break;
				default:
					break;

			}

			range *= distance;



			ScanData data = new ScanData();
			data.origin = new Vector3i(fromPlayer.getCurrentSector());
			data.time = System.currentTimeMillis();
			data.systemOwnerShipType = systemOwnerShipType;
			data.range = range;
			for (PlayerState player : playerStatesByName.values()) {
				if (player != fromPlayer && !player.isInvisibilityMode()) {
					if (Vector3fTools.length(player.getCurrentSector(), fromPlayer.getCurrentSector()) <= range || (scanHoleSystem && (player.getCurrentSystem().equals(fromPlayer.getCurrentSystem())))) {
						SimpleTransformableSendableObject<?> t = player.getFirstControlledTransformableWOExc();
						data.addEntity(player, t);
					}
				}
			}


			SimpleTransformableSendableObject<?> transformable = fromPlayer.getFirstControlledTransformableWOExc();
			if(transformable instanceof ManagedUsableSegmentController<?> entity) {
				ConfigEntityManager ce = entity.getConfigManager();

				//get system resources if nonzero and add to list
				for(int i=0;i<VoidSystem.RESOURCES;i++) {
					short id = ElementKeyMap.resources[i];
					float density = stellar.getTrueResourceDensityFromId(id);
					if(density > 0) {
						data.addSystemResourceEntry(id, density);
					}
				}

				if(ce.apply(StatusEffectType.RESOURCE_SCANNER_PROSPECTOR, false)) {
					data.extractorScan = true;
					//get passive resource providers and add to list
					ArrayList<PassiveResourceProvider> providers = PassiveResourceManager.getProvidersWithinRange(fromPlayer.getCurrentSector(), range);
					for (PassiveResourceProvider provider : providers) {
						if (provider.getSourceTypeInfo().canScanFromEntity(entity)) {
							boolean canSee =
									(
											provider.getSourceTypeInfo() == ATMOSPHERE && ce.apply(StatusEffectType.RESOURCE_SCANNER_ATMOSPHERE, false) ||
											provider.getSourceTypeInfo() == PLANET_INSIDE && ce.apply(StatusEffectType.RESOURCE_SCANNER_CORE, false) ||
											provider.getSourceTypeInfo() == NEBULA && ce.apply(StatusEffectType.RESOURCE_SCANNER_NEBULA, false)
									);
							data.addExtractorResourceEntry(provider, canSee);
							//TODO only populate data if chamber capability is there
						}
					}
				}
			}

			if(fromPlayer.getClientChannel() != null) {
				fromPlayer.getClientChannel().getNetworkObject().scanDataUpdates.add(new RemoteScanData(data, fromPlayer.isOnServer()));
			} else {
				System.err.println("[SERVER] Error: Exception: cannot send scan result to " + fromPlayer + "; client channel is null");
			}
			fromPlayer.addScanHistory(data);
		} catch(IOException e) {
			e.printStackTrace();
		}
		System.err.println("[SERVER][SCAN] " + this + " Scannig " + fromPlayer.getCurrentSystem());
		fromPlayer.getFogOfWar().scan(fromPlayer.getCurrentSystem());

	}

	/**
	 * @return the factionConfigFile
	 */
	public byte[] getFactionConfigFile() {
		return factionConfigFile;
	}

	/**
	 * @param factionConfigFile the factionConfigFile to set
	 */
	public void setFactionConfigFile(byte[] factionConfigFile) {
		this.factionConfigFile = factionConfigFile;
	}

	/**
	 * @return the factionConfigCheckSum
	 */
	public String getFactionConfigCheckSum() {
		return factionConfigCheckSum;
	}

	/**
	 * @param factionConfigCheckSum the factionConfigCheckSum to set
	 */
	public void setFactionConfigCheckSum(String factionConfigCheckSum) {
		this.factionConfigCheckSum = factionConfigCheckSum;
	}

	/**
	 * @return the gameConfig
	 */
	public GameConfig getGameConfig() {
		return gameConfig;
	}

	public ObjectArrayFIFOQueue<ExplosionRunnable> getExplosionOrdersFinished() {
		return explosionOrdersFinished;
	}

	public void enqueueExplosion(ExplosionRunnable n) {
		explosionOrdersQueued.add(n);
	}

	public ObjectArrayList<ExplosionRunnable> getExplosionOrdersQueued() {
		return explosionOrdersQueued;
	}

	public Set<PlayerState> getSpawnRequestsReady() {
		return spawnRequestsReady;
	}

	public class FileRequest {
		public ClientChannel channel;
		public String req;

		public FileRequest(ClientChannel channel, String req) {
			this.channel = channel;
			this.req = req;
		}

	}

	public class MobSpawnThread extends Thread {

		private final ArrayList<MobSpawnRequest> requests = new ArrayList<MobSpawnRequest>();
		private boolean shutdown;

		public MobSpawnThread() {
			super("MobSpawnThread");
			setDaemon(true);

		}

		@Override
		public void run() {

			ByteBuffer buffer = ByteBuffer.allocate(1000 * 1024);
			List<BoundingBox> extraBB = new ObjectArrayList<BoundingBox>();
			while(!shutdown) {
				MobSpawnRequest r;
				synchronized(requests) {
					while(requests.isEmpty()) {
						try {
							requests.wait();
							if(shutdown) {
								return;
							}
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					r = requests.remove(0);
				}
				Transform t = new Transform();

				MobSpawnRequestExecuteEvent ev = new MobSpawnRequestExecuteEvent(r.sector,r.factionId,r.catalogname,r.bbc,r.count,r.transform);
				fireEvent(ev,true);
				if(ev.isCanceled()) continue;
				else{
					r.factionId = ev.getFactionId();
					r.catalogname = ev.getBlueprintName();
					r.count = ev.getSpawnCount();
				}

				List<BlueprintEntry> bluePrints = r.bbc.readBluePrints();

				for(int i = 0; i < r.count; i++) {
					t.set(r.transform);
					//							System.err.println("[SERVER] SPAWING MOB: fId: "+factionId+": "+i);
					t.origin.set(t.origin.x + (float) (Math.random() - 0.5f) * 256.0f, t.origin.y + (float) (Math.random() - 0.5f) * 256.0f, t.origin.z + (float) (Math.random() - 0.5f) * 256.0f);

					String name = "MOB_" + r.catalogname + "_" + System.currentTimeMillis() + "_" + i;
					int len = r.catalogname.length() - 1;
					while(name.length() > 64) {
						System.err.println("[SERVER] WARNING: MOB NAME LENGTH TOO LONG: " + name + " -> " + name.length() + "/64");
						name = "MOB_" + r.catalogname.substring(0, len) + "_" + System.currentTimeMillis() + "_" + i;
						len--;
					}
					if(EntityRequest.isShipNameValid(name)) {
						SegmentControllerOutline<?> loadBluePrint;
						try {
							SegmentPiece toDockOn = null; //for manual player spawn (for docking turrets)
							loadBluePrint = r.bbc.loadBluePrint(GameServerState.this, r.catalogname, name, t, -1, r.factionId, bluePrints, r.sector, extraBB, "<system>", buffer, true, toDockOn, new ChildStats(false));
							loadBluePrint.spawnSectorId = new Vector3i(r.sector);

							Vector3f minOut = new Vector3f();
							Vector3f maxOut = new Vector3f();
							Vector3f localMin = new Vector3f(loadBluePrint.min.x * SegmentData.SEG, loadBluePrint.min.y * SegmentData.SEG, loadBluePrint.min.z * SegmentData.SEG);
							Vector3f localMax = new Vector3f(loadBluePrint.max.x * SegmentData.SEG, loadBluePrint.max.y * SegmentData.SEG, loadBluePrint.max.z * SegmentData.SEG);

							AabbUtil2.transformAabb(localMin, localMax, 40, t, minOut, maxOut);

							extraBB.add(new BoundingBox(minOut, maxOut));
							synchronized(getBluePrintsToSpawn()) {
								getBluePrintsToSpawn().add(loadBluePrint);
							}

						} catch(EntityNotFountException e) {
							e.printStackTrace();
						} catch(IOException e) {
							e.printStackTrace();
						} catch(EntityAlreadyExistsException e) {
							e.printStackTrace();
						}
					} else {
						System.err.println("[ADMIN] ERROR: Not a valid name: " + name);
					}
				}

			}
		}

		public void spawnMobs(int count, String catalogname, Vector3i sectorId, Transform transform, int factionId, BluePrintController bbc) {
			MobSpawnRequest m = new MobSpawnRequest(count, catalogname, sectorId, transform, factionId, bbc);
			synchronized(requests) {
				requests.add(m);
				requests.notify();
			}
		}

		private class MobSpawnRequest {
			int count;
			String catalogname;
			final Vector3i sector;
			final Transform transform;
			int factionId;
			final BluePrintController bbc;

			public MobSpawnRequest(int count, String catalogname, Vector3i sector, Transform transform, int factionId, BluePrintController bbc) {
				this.count = count;
				this.catalogname = catalogname;
				this.sector = sector;
				this.transform = transform;
				this.factionId = factionId;
				this.bbc = bbc;
			}

		}

		public void shutdown() {
			shutdown = true;
			synchronized(requests) {
				requests.notifyAll();
			}
		}
	}

	@Override
	public RaceManager getRaceManager() {
		return gameState.getRaceManager();
	}

	public boolean existsEntity(SimpleTransformableSendableObject.EntityType type, String name) {
		if(getLocalAndRemoteObjectContainer().getUidObjectMap().containsKey(type.dbPrefix + name)) {
			return true;
		}
		for(String uid : getLocalAndRemoteObjectContainer().getUidObjectMap().keySet()) {
			if(uid.toLowerCase(Locale.ENGLISH).equals((type.dbPrefix + name).toLowerCase(Locale.ENGLISH))) {
				return true;
			}

		}
		try {
			return databaseIndex.getTableManager().getEntityTable().getByUIDExact(name, 1).size() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public FleetManager getFleetManager() {
		return fleetManager;
	}

	public MobSpawnThread getMobSpawnThread() {
		return mobSpawnThread;
	}

	@Override
	public void doDatabaseInsert(Sendable sendable) {
		if(sendable instanceof SegmentController) {
			SegmentController c = (SegmentController) sendable;
			if(c.dbId < 0 && !c.isVirtualBlueprint()){
				Sector sector = universe.getSector(c.getSectorId());
				
				boolean transientSec;
				if(sector == null) {
					transientSec = c.transientSector;
				} else {
					transientSec = sector.isTransientSector();
				}
				long id = -1;
				if(c instanceof TransientSegmentController && !((TransientSegmentController) c).needsTagSave() && transientSec) {
					//no need to write this as it's all default values derived from the sector
				} else {
					System.err.println("[SERVER] Object " + sendable + " didn't have a db entry yet. Creating entry!");
					try {
						c.dbId = databaseIndex.getTableManager().getEntityTable().updateOrInsertSegmentController(c);
						
						if(c.npcSystem != null){
							assert(c.npcSpec != null);
							c.npcSystem.getContingent().spawn(c.npcSpec, c.dbId);
							c.npcSystem = null;
							c.npcSpec = null;
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	public void destroyEntity(long entityId) {
		Sendable sendable = getLocalAndRemoteObjectContainer().getDbObjects().get(entityId);
		if(sendable != null && sendable instanceof SimpleTransformableSendableObject<?>) {
			SimpleTransformableSendableObject<?> s = (SimpleTransformableSendableObject<?>) sendable;
			s.destroy();
		} else {
			try {
				DatabaseEntry byId = databaseIndex.getTableManager().getEntityTable().getById(entityId);
				if(byId != null){
					Faction faction = gameState.getFactionManager().getFaction(byId.faction);
					if(faction != null && faction instanceof NPCFaction){
						((NPCFaction)faction).onLostEntity(entityId, null, true);
					}

					if(byId != null) {
						File ent = new File(byId.getEntityFilePath());
						ent.delete();
					}
					databaseIndex.getTableManager().getEntityTable().removeSegmentController(entityId);

					fleetManager.onRemovedEntity(entityId);
				}else{
					try {
						throw new Exception("Entity not found: " + entityId + ". may already be removed");
					} catch(Exception e) {
						e.printStackTrace();
					}
				}

			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public String getPlayerNameFromDbIdLowerCase(long dbId) {

		String name = playerDbIdToUID.get(dbId);
		if(name == null) {
			try {
				name = databaseIndex.getTableManager().getPlayerTable().getPlayerName(dbId);
				if(name != null){
					name = name.toLowerCase(Locale.ENGLISH);
					playerDbIdToUID.put(dbId, name);
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return name;
	}

	public Long2ObjectOpenHashMap<PlayerState> getPlayerStatesByDbId() {
		return playerStatesByDbId;
	}

	@Override
	public ConfigPool getConfigPool() {
		return gameState.getConfigPool();
	}

	private final DebugTimer debugTimer = new DebugTimer();
	private ConnectionStats connectionStats = new ConnectionStats();
	@Override
	public DebugTimer getDebugTimer() {
		return debugTimer;
	}
	@Override
	public void onClientRegistered(RegisteredClientOnServer c) {
		System.err.println("[SERVER] client registered "+c);
	}
	@Override
	public void handleGameRequestAnswer(NetworkProcessor p, GameRequestCommandPackage pack) throws IOException {
		pack.request.handleAnswer(p	, this);
	}
	@Override
	public ConnectionStats getConnectionStats() {
		return connectionStats;
	}
	@Override
	public NetworkSettings getSettings() {
		return settings;
	}

	@Override
	public NetworkManager getNetworkManager() {
		return networmanager;
	}
	@Override
	public void notifyUpdateNeeded() {
		updateSynch.notfifyUpdateNeeded();
	}
	@Override
	public void onClientDisconnected(RegisteredClientInterface client) {
		System.err.println("[SERVER] client diconnected "+client);
	}
	@Override
	public void receivedSynchronization(NetworkProcessor from, SynchronizePublicCommandPackage pack)
			throws IOException {
		ServerProcessorInterface serverProcessor = (ServerProcessorInterface)from;
		RegisteredClientOnServer client = (RegisteredClientOnServer) serverProcessor.getClient();

		SynchronizationReceiver.update(getLocalAndRemoteObjectContainer(), serverProcessor.getClient().getId(), pack.in,
				this, true, false, (short)0, serverProcessor.getLastReceived());
	}
	@Override
	public void receivedPrivateSynchronization(NetworkProcessor from, SynchronizePrivateCommandPackage pack)
			throws IOException {
		if (isReady()) {
			ServerProcessorInterface serverProcessor = (ServerProcessorInterface)from;
			RegisteredClientOnServer client = (RegisteredClientOnServer) serverProcessor.getClient();

			SynchronizationReceiver.update(client.getLocalAndRemoteObjectContainer(), serverProcessor.getClient().getId(), pack.in,
					this, true, false, (short)0, serverProcessor.getLastReceived());
		}
	}

	@Override
	public void receivedAllSynchronization(NetworkProcessor recipient, SynchronizeAllCommandPackage pack) throws IOException {
		assert(false):"server cant received a synch all";
	}
}
