package org.schema.game.client.controller;

import api.DebugFile;
import api.StarLoaderHooks;
import api.common.GameCommon;
import api.listener.events.block.BlockPublicPermissionEvent;
import api.listener.events.gui.BigMessagePopupEvent;
import api.listener.events.network.ClientSendableAddEvent;
import api.listener.events.network.ClientSendableRemoveEvent;
import api.mod.ModStarter;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import api.utils.StarRunnable;
import api.utils.game.chat.CommandInterface;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.InternalTickCallback;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.lang3.StringUtils;
import org.schema.common.TimeStatistics;
import org.schema.common.XMLTools;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.GlobalGameControlManager;
import org.schema.game.client.controller.manager.ingame.InGameControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.ship.ShipExternalFlightController;
import org.schema.game.client.controller.tutorial.DynamicTutorialStateMachine;
import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.controller.tutorial.newtut.TutorialController;
import org.schema.game.client.data.*;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.game.client.view.WorldDrawer;
import org.schema.game.client.view.gui.BigMessage;
import org.schema.game.client.view.gui.BigTitleMessage;
import org.schema.game.client.view.gui.GUIPopupInterface;
import org.schema.game.client.view.gui.lagStats.LagDataStatsEntry;
import org.schema.game.client.view.gui.lagStats.LagObject;
import org.schema.game.client.view.gui.shiphud.newhud.PopupMessageNew;
import org.schema.game.client.view.gui.transporter.TransporterDestinations;
import org.schema.game.client.view.shards.ShardDrawer;
import org.schema.game.common.Starter;
import org.schema.game.common.api.SessionNewStyle;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.mines.MineController;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.controller.gamemodes.GameModes;
import org.schema.game.common.controller.io.IOFileManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.missile.ClientMissileManager;
import org.schema.game.common.data.missile.MissileControllerInterface;
import org.schema.game.common.data.missile.MissileManagerInterface;
import org.schema.game.common.data.physics.GamePhysicsObject;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.faction.config.FactionConfig;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.util.Collisionable;
import org.schema.game.common.util.StarMadeCredentials;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.network.StarMadePlayerStats;
import org.schema.game.network.StarMadeServerStats;
import org.schema.game.network.commands.AdminCommandCommandPackage;
import org.schema.game.network.commands.gamerequests.*;
import org.schema.game.network.objects.ChatMessage;
import org.schema.game.server.controller.GameServerController;
import org.schema.game.server.data.admin.AdminCommandIllegalArgument;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.common.JoystickAxisMapping;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.core.settings.SettingStateParseError;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.ColoredTimedText;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIResizableGrabbableWindow;
import org.schema.schine.graphicsengine.util.WorldToScreenConverterFixedAspect;
import org.schema.schine.input.*;
import org.schema.schine.network.ServerInfo;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.SynchronizationContainerController;
import org.schema.schine.network.client.ClientController;
import org.schema.schine.network.client.HostPortLoginName;
import org.schema.schine.network.commands.GameRequestCommandPackage;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.common.commands.UnknownCommandException;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.physics.Physical;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public class GameClientController extends ClientController implements MineInterface, MissileControllerInterface, ElementHandlerInterface, CreatorThreadControlInterface, GameStateControllerInterface {

	public static final boolean IS_LOCALHOST = false;

	public static final int SHIP_SPAM_PROTECT_TIME_SEC = 5;

	public static final ObjectOpenHashSet<String> fileList = new ObjectOpenHashSet<String>();

	public static WorldToScreenConverterFixedAspect worldToScreenConverter = new WorldToScreenConverterFixedAspect();

	private static boolean started;

	public static boolean availableGUI;

	private final GameClientState state;

	private final SynchronizationContainerController synchController;

	private final SynchronizationContainerController privateChannelSynchController;

	private final HashSet<SectorChange> sectorChanges = new HashSet<SectorChange>();

	private final TextureSynchronizer textureSynchronizer;

	private final CreatorThreadController creatorThreadController;

	long lastLagList;

	private final ClientGameData clientGameData;

	private final ArrayList<SendableSegmentController> cleanUps = new ArrayList<SendableSegmentController>();

	private final ClientMissileManager clientMissileManager;

	public boolean flagWaypointUpdate;

	public Vector3i lastSector = new Vector3i();


	private TutorialMode tutorialMode;

	private boolean tutorialStarted;

	private long lastCleanUp;

	private boolean firstUpdate = true;

	private final MineController mineController;

	private ClientChannel clientChannel;

	private boolean flagRecalc;

	private ObjectArrayList<SimpleTransformableSendableObject> flagSectorCleanup = new ObjectArrayList<SimpleTransformableSendableObject>();

	private long lastMemorySample;

	private boolean resynched;

	public long lastShipSpawn;


	private ElementCollectionCalculationThreadManager elementCollectionCalculationThreadManager = new ElementCollectionCalculationThreadManager(false);

	private boolean flagReapplyBlockBehavior;

	private String lastClientStartVersion;

	private Vector3i lastSystem = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

	private SegmentController notSent;

	private Map<String, PlayerState> onlinePlayerMapHelper = new Object2ObjectOpenHashMap<String, PlayerState>();

	private ObjectArrayFIFOQueue<String> queuedDialogs = new ObjectArrayFIFOQueue<String>();

	private final BasicInputController inputController;

	private final TutorialController tutorialController;

	private final List<ShopInterface> shopInterfaces = new ObjectArrayList<ShopInterface>();

	public final SectorChangeObservable sectorEntitiesChangeObservable = new SectorChangeObservable();

	private List<TransporterDestinations> transporterDestinations = new ObjectArrayList<TransporterDestinations>();

	private List<SegmentController> possibleFleet = new ObjectArrayList<SegmentController>();

	private float zoom = 0;

	private final List<ClientSectorChangeListener> sectorChangeListeners = new ObjectArrayList<ClientSectorChangeListener>();

	private final List<ClientSystemChangeListener> systemChangeListeners = new ObjectArrayList<ClientSystemChangeListener>();

	private final List<EntitySelectionChangeChangeListener> entitySelectionListeners = new ObjectArrayList<EntitySelectionChangeChangeListener>();

	private final List<EntityTrackingChangedListener> entityTrackingListeners = new ObjectArrayList<EntityTrackingChangedListener>();

	private boolean trackingChanged;

	private final List<CollectionManagerChangeListener> collectionManagerChangeListeners = new ObjectArrayList<CollectionManagerChangeListener>();

	public GraphicsContext graphicsContext;

	public GameClientController(final GameClientState state, GraphicsContext context) throws NoSuchAlgorithmException, IOException {
		super(state);
		this.graphicsContext = context;
		started = true;
		this.state = state;
		updateFileList();
		inputController = new BasicInputController();
		tutorialController = new TutorialController(state);
		clientMissileManager = new ClientMissileManager(state);
		initializeState();
		creatorThreadController = new CreatorThreadController(state);
		if (!state.isPassive()) {
			creatorThreadController.start();
		}
		textureSynchronizer = new TextureSynchronizer(state);
		synchController = new SynchronizationContainerController(state.getLocalAndRemoteObjectContainer(), state, false);
		clientGameData = new ClientGameData(state);
		if (EngineSettings.PLAYER_SKIN.getString().trim().length() > 0) {
			textureSynchronizer.setModelPath(EngineSettings.PLAYER_SKIN.getString().trim());
		}
		if (!state.isPassive()) {
			elementCollectionCalculationThreadManager.start();
			inputController.initialize();
		}
		privateChannelSynchController = new SynchronizationContainerController(state.getPrivateLocalAndRemoteObjectContainer(), state, true);
		mineController = new MineController(state);
		EngineSettings.G_DRAW_GUI_ACTIVE.setOn(true);
		EngineSettings.G_DRAW_NO_OVERLAYS.setOn(false);
		File f = new FileExt("lastClientStartVerion.txt");
		this.lastClientStartVersion = "0.0.0";
		if (!f.exists()) {
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			w.append(String.valueOf(VersionContainer.VERSION));
			w.close();
		} else {
			try {
				BufferedReader r = new BufferedReader(new FileReader(f));
				this.lastClientStartVersion = r.readLine();
				r.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			w.append(String.valueOf(VersionContainer.VERSION));
			w.close();
		}
	}

	public static boolean exists(String fileName) {
		String fNam = new String(fileName);
		int lastIndexOf = fNam.lastIndexOf(File.separator);
		if (lastIndexOf >= 0) {
			fNam = fNam.substring(lastIndexOf + 1);
		}
		synchronized (fileList) {
			return fileList.contains(fNam);
		}
	}

	public static void updateFileList() {
		synchronized (fileList) {
			fileList.clear();
			File dir = new FileExt(ClientStatics.SEGMENT_DATA_DATABASE_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String[] list = dir.list();
			for (int i = 0; i < list.length; i++) {
				fileList.add(list[i]);
			}
		}
	}

	// public void requestControl(PlayerControllable controllable, Vector3i param){
	// state.getPlayer().requestControl(controllable, param);
	// }
	public static String autocompletePlayer(StateInterface state, String input) {
		ArrayList<PlayerState> playerStates = new ArrayList<PlayerState>();
		synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable se : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (se instanceof PlayerState) {
					playerStates.add((PlayerState) se);
				}
			}
		}
		String player = StringTools.autoComplete(input, playerStates, true, e -> e.getName().toLowerCase(Locale.ENGLISH));
		for (PlayerState p : playerStates) {
			if (p.getName().toLowerCase(Locale.ENGLISH).startsWith(player)) {
				player = p.getName().substring(0, player.length());
			}
		}
		return player;
	}

	public static String findCorrectedCommand(String command) {
		ArrayList<StringDistance> distanceArrayList = new ArrayList<StringDistance>();
		boolean exactMatch = false;
		// Find the levenshtein distance for each command to the player's command being entered
		for (AdminCommands e : AdminCommands.values()) {
			String name = e.name().toLowerCase(Locale.ENGLISH);
			int distance = StringUtils.getLevenshteinDistance(command, name, 4);
			System.err.println("" + distance + " " + command + " " + name);
			if (distance == 0) {
				exactMatch = true;
			}
			if (distance > 0) {
				distanceArrayList.add(new StringDistance(name, distance));
			}
		}

		///INSERTED CODE
		int modCmdsLength = StarLoader.getAllCommands().size();
		CommandInterface[] modCommands = StarLoader.getAllCommands().toArray(new CommandInterface[0]);
		int i;
		for(i = 0; i < modCmdsLength; ++i) {
			String commandLabel = modCommands[i].getCommand().toLowerCase(Locale.ROOT);
			int dist = StringUtils.getLevenshteinDistance(command, commandLabel, 4);
			System.err.println(dist + " " + command + " " + commandLabel);
			if(dist == 0) exactMatch = true;

			if(dist > 0) distanceArrayList.add(new GameClientController.StringDistance(commandLabel, dist));
		} //modded
		///

		// If an exact match was found, don't do suggestions
		if (!exactMatch) {
			if (distanceArrayList.isEmpty()) {
				return Lng.str("[ERROR] Unknown command: \"%s\"", command);
			} else {
				// Sort distance arraylist
				Collections.sort(distanceArrayList, (o1, o2) -> o1.distance - o2.distance);
				// Maximum seggestions is 5
				int valuesToOutput = Math.min(distanceArrayList.size(), 5);
				StringBuffer buffer = new StringBuffer();
				buffer.append(Lng.str("Did you mean:")).append(" ");
				int valueCount = 0;
				for (StringDistance strDist : distanceArrayList) {
					buffer.append(strDist.string);
					if (valueCount < valuesToOutput - 1) {
						buffer.append(", ");
					}
					if (++valueCount >= valuesToOutput) {
						break;
					}
				}
				return Lng.str("[ERROR]") + " " + buffer.toString();
			}
		}
		// If it's an exact match, do nothing
		return "";
	}

	@Override
	public void afterFullResynchronize() {
		synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (s instanceof SendableGameState) {
					state.setGameState((SendableGameState) s);
				}
			}
		}
	}

	@Override
	public void onShutDown() {
		//INSERTED CODE
		ModStarter.disableAllMods();
		PersistentObjectUtil.flushLogs(true);
		///
		if (!IS_LOCALHOST) {
			System.out.println("[CLIENT] CLIENT SHUTDOWN. Dumping client data!");
			try {
				writeSegmentDataToDatabase(false);
				System.out.println("[CLIENT] CLIENT SHUTDOWN. client data saved!");
				EngineSettings.write();
				GUIResizableGrabbableWindow.write();
				state.getConnectionThreadPool().shutdown();
				state.getConnectionThreadPool().awaitTermination(3, TimeUnit.SECONDS);
				System.out.println("[CLIENT] CLIENT SHUTDOWN. thread pool terminated!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onLogin() throws IOException, InterruptedException {
		String pathOfProperties = null;
		boolean useDefaultBlockProperties = false;
		try {
			System.out.println("[CLIENT] executing login hooks...");
			Thread.sleep(10000); //10s wait. FIXME The game would ignore its synch-all packets without this for some reason
			setGuiConnectionState(Lng.str("Checking server block config..."));
			System.out.println("[CLIENT] requesting game mode from server");
			state.setGameMode(requestGameMode());
			System.out.println("[CLIENT] recieved and set gamemode: " + state.getGameMode().name());

			
			
			boolean changedConfig = false;
			boolean changedFactionConfig = false;
			boolean changedProperties = false;
			boolean changedBlockBehavior = false;
			boolean changedCustomTextures = false;
			String pathOfBlockConfig = ClientStatics.ENTITY_DATABASE_PATH + getConnection().getHost() + ".xml";
			String pathOfFactionConfig = ClientStatics.ENTITY_DATABASE_PATH + getConnection().getHost() + "-faction.xml";
			String pathOfBlockBehavior = ClientStatics.ENTITY_DATABASE_PATH + getConnection().getHost() + "-block-behavior.xml";
			pathOfProperties = ClientStatics.ENTITY_DATABASE_PATH + getConnection().getHost() + ".properties";
			String pathOfCustomBlocks = ClientStatics.ENTITY_DATABASE_PATH + getConnection().getHost() + "-custom-textures" + File.separator;

			// set path for chunk caching for this server. each server has their own path to avoid name collision
			ClientStatics.SEGMENT_DATA_DATABASE_PATH = ClientStatics.ENTITY_DATABASE_PATH + getConnection().getHost() + File.separator + "DATA" + File.separator;
			File dataPath = new File(ClientStatics.SEGMENT_DATA_DATABASE_PATH);
			dataPath.mkdirs();

			System.out.println("[CLIENT] Begin block config verification");
			if(! state.receivedBlockConfigChecksum.equals(state.getBlockConfigCheckSum())) {
				if(graphicsContext != null) {
					//graphicsContext.setLoadMessage(Lng.str("Downloading block config"));
				}
				System.err.println("[Client] Config Checksum does not match: ");
				System.err.println("Remote: " + getState().receivedBlockConfigChecksum);
				System.err.println("Local:  " + getState().getBlockConfigCheckSum());
				File cFile = new FileExt(pathOfBlockConfig);
				String checkSum = null;
				boolean needsSynch = false;
				try {
					needsSynch = ! cFile.exists() || ! (checkSum = FileUtil.getSha1ChecksumZipped(pathOfBlockConfig)).equals(state.receivedBlockConfigChecksum);
				} catch(Exception e) {
					e.printStackTrace();
				}
				if (needsSynch) {
					System.err.println("[Client] Downloading and caching block config from remote (needs synch)");
					if (cFile.exists()) {
						System.err.println("Cached block config File existed. Checksums failed");
						System.err.println("Remote: " + state.receivedBlockConfigChecksum);
						System.err.println("Chached:  " + checkSum);
						System.err.println("Local-default:  " + state.getBlockConfigCheckSum());
						cFile.delete();
					}
					setGuiConnectionState(Lng.str("Downloading server block config..."));
					System.err.println("[CLIENT] Detected modified server config");
					byte[] requestBlockConfig = requestBlockConfig();
					System.err.println("[Client] Creating output stream at " + pathOfBlockConfig);
					GZIPOutputStream fos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(pathOfBlockConfig)));
					System.err.println("[Client] Writing new file to cache...");
					fos.write(requestBlockConfig);
					System.err.println("[Client] Flushing output stream...");
					fos.flush();
					System.err.println("[Client] Closing stream.");
					fos.close();
				} else {
					System.err.println("[Client] Found valid blockConfig cache file for this server in " + cFile.getAbsolutePath());
				}
				changedConfig = true;
			}

			System.out.println("[CLIENT] Block config verification DONE... Begin faction config verification");

			if(! state.receivedFactionConfigChecksum.equals(state.getFactionConfigCheckSum())) {
				if(graphicsContext != null) {
					//graphicsContext.setLoadMessage(Lng.str("Downloading faction config"));
				}
				System.err.println("[Client] Faction Config Checksum does not match: ");
				System.err.println("Remote: " + state.receivedFactionConfigChecksum);
				System.err.println("Local:  " + state.getFactionConfigCheckSum());

				File cFile = new FileExt(pathOfFactionConfig);
				String checkSum = null;
				boolean needsSynch = false;
				try {
					needsSynch = ! cFile.exists() || ! (checkSum = FileUtil.getSha1ChecksumZipped(pathOfFactionConfig)).equals(state.receivedFactionConfigChecksum);
				} catch(Exception e) {
					e.printStackTrace();
					System.err.println("[Client][FactionConfig] TRYING UNZIPPED CHECKSUM");
					try {
						needsSynch = ! cFile.exists() || ! (checkSum = FileUtil.getSha1Checksum(pathOfFactionConfig)).equals(state.receivedFactionConfigChecksum);
					} catch(Exception e1) {
						e1.printStackTrace();
						needsSynch = true;
					}
				}
				if(needsSynch) {
					if(cFile.exists()) {
						System.err.println("Cached faction config File existed. Checksums failed");
						System.err.println("Remote: " + state.receivedFactionConfigChecksum);
						System.err.println("Chached:  " + checkSum);
						System.err.println("Local-default:  " + state.getFactionConfigCheckSum());
						cFile.delete();
					}
					setGuiConnectionState(Lng.str("Downloading server faction config..."));
					System.err.println("[CLIENT] Detected modified server faction config");
					byte[] requestFactionConfig = requestFactionConfig();
					System.err.println("[Client] Creating output stream at " + pathOfFactionConfig);
					BufferedOutputStream fos = new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(pathOfFactionConfig)));
					System.err.println("[Client] Writing new file to cache...");
					fos.write(requestFactionConfig);
					System.err.println("[Client] Flushing output stream...");
					fos.flush();
					System.err.println("[Client] Closing stream.");
					fos.close();
				} else {
					System.err.println("[Client] Found valid blockConfig cache file for this server in " + cFile.getAbsolutePath());
				}
				changedFactionConfig = true;
			}

			System.out.println("[CLIENT] Faction config verification DONE... Begin block properties verification");

			if(! state.receivedBlockConfigPropertiesChecksum.equals(state.getConfigPropertiesCheckSum())) {
				if(graphicsContext != null) {
					//graphicsContext.setLoadMessage(Lng.str("Downloading block properties"));
				}
				System.err.println("[Client] Config Checksum check failed: ");
				System.err.println("Remote: " + state.receivedBlockConfigPropertiesChecksum);
				System.err.println("Local:  " + state.getConfigPropertiesCheckSum());
				File cFile = new FileExt(pathOfProperties);

				if(! cFile.exists() || ! FileUtil.getSha1Checksum(pathOfProperties).equals(state.receivedBlockConfigPropertiesChecksum)) {
					if(cFile.exists()) {
						System.err.println("Cached Block properties File existed. Checksums failed ");
						System.err.println("Remote: " + FileUtil.getSha1Checksum(pathOfProperties));
						System.err.println("Local:  " + state.getConfigPropertiesCheckSum());
						cFile.delete();
					}
					setGuiConnectionState(Lng.str("Downloading server block properties..."));
					System.err.println("[CLIENT] Detected modified server block properties");
					byte[] requestBlockProperties = requestBlockProperties();
					System.err.println("[Client] Creating output stream at " + pathOfProperties);
					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(pathOfProperties));
					System.err.println("[Client] Writing new file to cache...");
					fos.write(requestBlockProperties);
					System.err.println("[Client] Flushing output stream...");
					fos.flush();
					System.err.println("[Client] Closing stream.");
					fos.close();
				} else {
					System.err.println("[Client] Found valid blockProperties cache file for this server in " + cFile.getAbsolutePath());
				}
				changedProperties = true;
			}

			System.out.println("[CLIENT] Block properties verification DONE. Parsing block behavior");
			parseBlockBehavior(GameServerController.BLOCK_BEHAVIOR_DEFAULT_PATH);
			System.out.println("[CLIENT] Begin block behavior verification");
			if(! state.receivedBlockBehaviorChecksum.equals(state.getBlockBehaviorCheckSum())) {
				if(graphicsContext != null) {
					//graphicsContext.setLoadMessage(Lng.str("Downloading block behavior"));
				}
				System.err.println("[Client] Block behavior Checksum check failed: ");
				System.err.println("Remote: " + state.receivedBlockBehaviorChecksum);
				System.err.println("Local:  " + state.getBlockBehaviorCheckSum());
				File cFile = new FileExt(pathOfBlockBehavior);

				if(! cFile.exists() || ! FileUtil.getSha1Checksum(pathOfBlockBehavior).equals(state.receivedBlockBehaviorChecksum)) {
					if(cFile.exists()) {
						System.err.println("Cached Block behavior File existed. Checksums failed ");
						System.err.println("Remote: " + FileUtil.getSha1Checksum(pathOfBlockBehavior));
						System.err.println("Local:  " + state.getBlockBehaviorCheckSum());
						cFile.delete();
					}
					setGuiConnectionState(Lng.str("Downloading server block behavior config..."));
					System.err.println("[CLIENT] Detected modified server Block behavior ");
					byte[] requestBlockBehavior = requestBlockBehavior();
					System.err.println("[Client] Creating output stream at " + pathOfBlockBehavior);
					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(pathOfBlockBehavior));
					System.err.println("[Client] Writing new file to cache...");
					fos.write(requestBlockBehavior);
					System.err.println("[Client] Flushing output stream...");
					fos.flush();
					System.err.println("[Client] Closing stream.");
					fos.close();
				} else {
					System.err.println("[Client] Found valid blockProperties cache file for this server in " + cFile.getAbsolutePath());
				}
				changedBlockBehavior = true;
				if(graphicsContext != null) {
					//graphicsContext.setLoadMessage(Lng.str("Downloading block behavior DONE"));
				}
			}
			System.out.println("[CLIENT] Block behavior verification DONE. Begin custom textures synch");
			if(! state.receivedCustomTexturesChecksum.equals(state.getCustomTexturesCheckSum())) {
				//does not equal DEFAULT CUSTOM textures
				//check if we have the right one cached
				System.err.println("[Client] Custom textures Checksum check failed: ");
				System.err.println("Remote: " + state.receivedCustomTexturesChecksum);
				System.err.println("Local:  " + state.getCustomTexturesCheckSum());
				if(graphicsContext != null) {
					//graphicsContext.setLoadMessage(Lng.str("Downloading custom textures"));
				}

				String custom = FileUtil.createFilesHashRecursively(pathOfCustomBlocks, pathname -> {
					boolean accept = pathname.isDirectory() || pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".png");
					// System.err.println("ACCEPT: "+pathname+": "+accept);
					return accept;
				});
				File cFile = new FileExt(pathOfCustomBlocks + "pack.zip");
				if(! cFile.exists() || ! (custom).equals(state.receivedCustomTexturesChecksum)) {
					if(cFile.exists()) {
						System.err.println("Cached Custom textures File existed. Checksums failed ");
						System.err.println("Remote: " + state.receivedCustomTexturesChecksum);
						System.err.println("Local:  " + custom);
						cFile.delete();
					} else {
						File f = new FileExt(pathOfCustomBlocks);
						f.mkdirs();
					}
					setGuiConnectionState(Lng.str("Downloading server custom textures..."));
					Controller.setLoadMessage(Lng.str("Downloading server custom textures..."));
					System.err.println("[CLIENT] Detected modified server custom textures");
					byte[] requestCustomTextures = requestCustomTextures();
					System.err.println("[CLIENT] Custom Block Textures successfully received (bytes: " + requestCustomTextures.length + ")");
					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(pathOfCustomBlocks + "pack.zip"));
					System.err.println("[Client] Writing recieved zip file to cache...");
					fos.write(requestCustomTextures);
					System.err.println("[Client] Flushing output stream...");
					fos.flush();
					System.err.println("[Client] Closing stream.");
					fos.close();
				} else {
					System.err.println("[Client] Found valid custom textures cache file for this server in " + cFile.getAbsolutePath());
				}
				changedCustomTextures = true;
				if(graphicsContext != null) {
					Controller.setLoadMessage(Lng.str("Downloading custom textures DONE"));
				}
			}
			if(changedCustomTextures) {
				// extract
				File file = new FileExt(pathOfCustomBlocks + "pack.zip");
				FileUtil.extract(file, pathOfCustomBlocks);
				if(! IS_LOCALHOST) {
					EngineSettings.CLIENT_CUSTOM_TEXTURE_PATH.setString(pathOfCustomBlocks + GameResourceLoader.CUSTOM_TEXTURE_PATH);
					System.err.println("[CLIENT] set custom texture path to: " + EngineSettings.CLIENT_CUSTOM_TEXTURE_PATH.getString());
				}
			}
			if(getConnection().getHost().toLowerCase(Locale.ENGLISH).equals("localhost")) {
				System.err.println("[CLIENT] RESET TO LOCAL CUSTOM TEXTURES");
				EngineSettings.CLIENT_CUSTOM_TEXTURE_PATH.setString("./customBlockTextures");
				// reset to local if playing local
			}
			System.out.println("[CLIENT] Custom server textures processing DONE.");
			if(changedBlockBehavior) {
				state.getController().parseBlockBehavior(pathOfBlockBehavior);
			} else {
				// System.err.println("[CLIENT] Server is using default block behavior: CSUM: remote: " + getState().receivedBlockConfigPropertiesChecksum + " ::: local: " + getState().getConfigPropertiesCheckSum());
			}
			if(changedProperties) {
				ElementKeyMap.reparseProperties(pathOfProperties);
			} else {
				// System.err.println("[CLIENT] Server is using default block properties");
				useDefaultBlockProperties = true;
			}
			if(changedFactionConfig) {
				FactionConfig.load(state, pathOfFactionConfig);
			}
			if(changedConfig) {//Block config is reinitialized anyway
//			ElementKeyMap.reinitializeData(new FileExt(pathOfBlockConfig), true, changedProperties ? pathOfProperties : null, null);
			}
			File clientCacheVersion = new FileExt(ClientStatics.ENTITY_DATABASE_PATH + "version");
			boolean needsWrite = false;
			if(! clientCacheVersion.exists()) {
				System.out.println("[CLIENT] PURGE NEEDED: No Database Info");
				purgeCompleteDB();
				needsWrite = true;
			} else {
				BufferedReader r = new BufferedReader(new FileReader(clientCacheVersion));
				String host = r.readLine();
				r.close();
				if(host != null) {
					needsWrite = ! host.equals(getConnection().getHost());
					if(needsWrite) {
						System.out.println("[CLIENT] PURGE NEEDED: Playing on another host: was: " + host + "; now: " + getConnection().getHost());
						purgeCompleteDB();
					}
				}
			}
			File dir = new FileExt(ClientStatics.ENTITY_DATABASE_PATH);
			if(! dir.exists()) {
				dir.mkdir();
			}
			BufferedWriter s = new BufferedWriter(new FileWriter(clientCacheVersion));
			s.append(getConnection().getHost());
			s.flush();
			s.close();
			assert (state.getBlockBehaviorConfig() != null);

			if(! GameCommon.isOnSinglePlayer()) {
				DebugFile.log("[CLIENT] Reparsing block config on post-client login hooks ==========");
				// Do not use custom properties if we do not need them.
				if(useDefaultBlockProperties) pathOfProperties = null;
				ElementKeyMap.reinitializeData(new FileExt("./data/config/BlockConfig.xml"), false, pathOfProperties, GameResourceLoader.getConfigInputFile(), true);
			} else {
				DebugFile.log("[CLIENT] Not reparsing block config, singleplayer blocks are loaded by the server");
			}

			System.out.println("[CLIENT] synchronizing...");
			synchronizeAllGameObjectsBlocking();
			System.out.println("[CLIENT] finished executing client hooks!");

		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	private void synchronizeAllGameObjectsBlocking() throws IOException {
		System.err.println("[CLIENT] Synchronizing game objects... sending requests");
		requestSynchronizeAll();
		synchronized (state) {
			assert (!state.isNetworkSynchronized());
			while (!state.isNetworkSynchronized()) {
				// execute only package receiving
				state.getUpdateSynch().updateLock(state.getNetworkManager());
			}
		}
		System.err.println("[CLIENT] Synchronizing game objects... finished");
	}

	@Override
	protected void onResynchRequest() {
		resynched = true;
		System.err.println("[CLIENT] ################################# RESYNCHRONIZED");
	}

	@Override
	public void setGuiConnectionState(String state) {
	}

	@Override
	public void alertMessage(String message) {
		popupAlertTextMessage(message, 0);
	}

	@Override
	public void kick(String reason) {
		GLFrame.processErrorDialogException(new LogoutException(Lng.str("Server has logged you out. \n\nReason: %s", reason)), state);
		if (state.getGlFrame() != null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			state.getGlFrame().dispose();
		}
	}

	@Override
	public boolean isJoystickOk() {
		return JoystickMappingFile.ok();
	}

	public void flagTrackingChanged() {
		trackingChanged = true;
	}

	public void removeCollectionManagerChangeListener(CollectionManagerChangeListener c) {
		collectionManagerChangeListeners.remove(c);
	}

	public void addCollectionManagerChangeListener(CollectionManagerChangeListener c) {
		collectionManagerChangeListeners.add(c);
	}

	public void notifyCollectionManagerChanged(ElementCollectionManager<?, ?, ?> col) {
		for (int i = 0; i < collectionManagerChangeListeners.size(); i++) {
			collectionManagerChangeListeners.get(i).onChange(col);
		}
	}

	private final List<SendableAddedRemovedListener> sendableAddRemoveListener = new ObjectArrayList<SendableAddedRemovedListener>();

	public void removeSendableAddedRemovedListener(SendableAddedRemovedListener c) {
		sendableAddRemoveListener.remove(c);
	}

	public void addSendableAddedRemovedListener(SendableAddedRemovedListener c) {
		sendableAddRemoveListener.add(c);
	}

	public void removeEntityTrackingListener(EntityTrackingChangedListener c) {
		entityTrackingListeners.remove(c);
	}

	public void addEntityTrackingListener(EntityTrackingChangedListener c) {
		entityTrackingListeners.add(c);
	}

	public void addSectorChangeListener(ClientSectorChangeListener l) {
		sectorChangeListeners.add(l);
	}

	public void addSystemChangeListener(ClientSystemChangeListener l) {
		systemChangeListeners.add(l);
	}

	public void removeEntitySelectionChangeListener(EntitySelectionChangeChangeListener l) {
		entitySelectionListeners.remove(l);
	}

	public void addEntitySelectionChangeListener(EntitySelectionChangeChangeListener l) {
		entitySelectionListeners.add(l);
	}

	@Override
	public double getJoystickAxis(JoystickAxisMapping map) {
		return inputController.getJoystick().getAxis(map);
	}

	@Override
	public void update(Timer timer) throws IOException {
		state.getDebugTimer().start(state);
		state.getDebugTimer().start("STATE");
		TimeStatistics.reset("#state");
		synchronized (state) {
			try {
				state.setSynched();
				state.getNetworkManager().update();
				ShardDrawer.shardsAddedFromNTBlocks = Math.max(0, ShardDrawer.shardsAddedFromNTBlocks - 1);
				state.updateTime = timer.currentTime;
				ServerInfo.curtime = state.updateTime;
				GameClientState.requestQueue = 0;
				GameClientState.requestedSegments = 0;
				GameClientState.returnedRequests = 0;
				if (GameClientState.allocatedSegmentData > 0) {
					GameClientState.lastAllocatedSegmentData = GameClientState.allocatedSegmentData;
				}
				GameClientState.lastFreeSegmentData = state.getSegmentDataManager().sizeFree();
				GameClientState.allocatedSegmentData = 0;
				state.incUpdateNumber();
				GameClientState.collectionUpdates = 0;
				state.getParticleSystemManager().update(state.getPhysics(), timer);
				if (state.isFlagRequestServerTime()) {
					long current = System.currentTimeMillis();
					try {
						requestServerTime();
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					state.resetFlagRequestServerTime();
					state.setLastServerTimeRequest(System.currentTimeMillis());
					long diff = System.currentTimeMillis() - current;
					if(diff > 3000) System.err.println("[WARNING][CLIENT] Requesting server time took suspiciously long: " + diff + "ms");
				}
				if (state.getCharacter() != null && state.getController().getPlayerInputs().isEmpty()) {
					while (!queuedDialogs.isEmpty()) {
						String msg = queuedDialogs.dequeue();
						PlayerGameOkCancelInput p = new PlayerGameOkCancelInput("DASASS", state, 500, 300, "Server Message", msg) {

							@Override
							public void pressedOK() {
								deactivate();
							}

							@Override
							public void onDeactivate() {
							}
						};
						p.getInputPanel().setCancelButton(false);
						p.getInputPanel().setOkButton(true);
						p.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(34);
					}
				}
				if (state.isFlagPlayerReceived()) {
					state.setFlagPlayerReceived(false);
					ClientChannel sendableSegmentProvider = new ClientChannel(state);
					sendableSegmentProvider.initialize();
					// make it 0 so it doesnt collide with segmentRequesters
					sendableSegmentProvider.setId(0);
					sendableSegmentProvider.setPlayerId(state.getPlayer().getId());
					this.clientChannel = sendableSegmentProvider;
					privateChannelSynchController.addNewSynchronizedObjectQueued(sendableSegmentProvider);
				}
				if (clientChannel.isConnectionReady()) {
					textureSynchronizer.synchronize();
					clientChannel.updateLocal(timer);
					state.getChannelRouter().update(timer);
				}
				if (state.getGameState().getNetworkObject().serverShutdown.get() > 0) {
					float s = state.getGameState().getNetworkObject().serverShutdown.get();
					String countdown = StringTools.formatCountdown((int) s);
					showBigTitleMessage("shutdown", Lng.str("The server will shutdown in %s", countdown), 0);
				}
				if (state.getGameState().getNetworkObject().serverCountdownTime.get() > 0) {
					float s = state.getGameState().getNetworkObject().serverCountdownTime.get();
					String countdown = StringTools.formatCountdown((int) s);
					showBigTitleMessage("countdown", state.getGameState().getNetworkObject().serverCountdownMessage.get() + " " + countdown, 0);
				}
				if (resynched) {
					state.getWorldDrawer().clearAll();
				}
				AudioController.setListener(Controller.getCamera());
				if (!flagSectorCleanup.isEmpty()) {
					ObjectArrayList<SimpleTransformableSendableObject> copy = new ObjectArrayList<SimpleTransformableSendableObject>();
					copy.addAll(flagSectorCleanup);
					long t = System.currentTimeMillis();
					// synchronized(sectorChangeLock){
					scheduleWriteDataPush(copy);
					// }
					long took = System.currentTimeMillis() - t;
					if (took > 50) {
						System.err.println("[CLIENT] WARNING: Sector Clean ups schedule data push took " + took);
					}
					flagSectorCleanup.clear();
				}
				if (flagWaypointUpdate) {
					flagWaypointUpdate = false;
					clientGameData.updateNearest(state.getCurrentSectorId());
				}
				if (!cleanUps.isEmpty() && System.currentTimeMillis() - lastCleanUp > 100) {
					long t = System.currentTimeMillis();
					SendableSegmentController s;
					synchronized (cleanUps) {
						s = cleanUps.remove(cleanUps.size() - 1);
						if (!state.getCurrentSectorEntities().containsKey(s.getId())) {
							// only clear when really out of range (could be just shortly away, which is
							// significant in sectors with lots of objects
							int clear = s.getSegmentBuffer().clear(false);
							if (s instanceof ManagedSegmentController) {
								((ManagedSegmentController<?>) s).getManagerContainer().clear();
							}
							((ClientSegmentProvider) s.getSegmentProvider()).clearRequestedBuffers();
						}
					}
					// s.getControlElementMap().clear();
					// ((ClientSegmentProvider) s.getSegmentProvider()).resetCurrentCopntrolMap();
					lastCleanUp = System.currentTimeMillis();
					long took = System.currentTimeMillis() - t;
					if (took > 50) {
						System.err.println("[CLIENT] WARNING: Sector Clean ups took " + took);
					}
				}
				{
					long t = System.currentTimeMillis();
					DebugDrawer.clear();
					long took = System.currentTimeMillis() - t;
					if (took > 50) {
						System.err.println("[CLIENT] WARNING: DebugCleanup Took " + took);
					}
				}
				if (!state.isNetworkSynchronized()) {
					popupAlertTextMessage(Lng.str("RE-SYNCHRONIZING with server."), 0);
				}
				{
					long t = System.currentTimeMillis();
					synchController.handleQueuedSynchronizedObjects();
					privateChannelSynchController.handleQueuedSynchronizedObjects();
					long took = System.currentTimeMillis() - t;
					if (took > 50) {
						System.err.println("[CLIENT] queued synchronized objects " + took);
					}
				}
				if (System.currentTimeMillis() - lastMemorySample > 300) {
					GameClientState.totalMemory = Runtime.getRuntime().totalMemory();
					GameClientState.freeMemory = Runtime.getRuntime().freeMemory();
					GameClientState.takenMemory = GameClientState.totalMemory - GameClientState.freeMemory;
					lastMemorySample = System.currentTimeMillis();
				}
				updateAmbientSound(timer);
				if (firstUpdate) {
					firstUpdate = false;
				}
				if (!state.isPassive()) {
					state.getWorldDrawer().getBuildModeDrawer().update(timer);
				}
				tutorialController.update(timer);
				if (state.exportingShip != null && state.exportingShip.done) {
					File f = new FileExt(state.exportingShip.path + "/" + state.exportingShip.name);
					popupGameTextMessage(Lng.str("Export successfull.\nExported to: %s", f.getAbsolutePath()), 0);
					state.exportingShip = null;
				}
				if (trackingChanged) {
					for (EntityTrackingChangedListener t : entityTrackingListeners) {
						t.onTrackingChanged();
					}
					trackingChanged = false;
				}
				if (timer.currentTime - lastLagList > 1000) {
					state.laggyList.clear();
					ObjectArrayList<LagObject> l = new ObjectArrayList<LagObject>();
					for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
						if (s.getCurrentLag() > 0) {
							state.laggyList.add(s);
							l.add(new LagObject(s));
						}
					}
					state.lagStats.add(0, new LagDataStatsEntry(timer.currentTime, l));
					// 4 minutes
					state.lagStats.clearAllBefore(timer.currentTime - 4 * 60000);
					state.notifyLaggyListChanged();
					lastLagList = timer.currentTime;
				}
				updateActiveControllers();
				try {
					ColoredTimedText.blink.update(timer);
					synchronized (state.getVisibleChatLog()) {
						for (int i = 0; i < state.getVisibleChatLog().size(); i++) {
							((ColoredTimedText) state.getVisibleChatLog().get(i)).update(timer);
						}
					}
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				if (state.getCharacter() != null && !lastSystem.equals(state.getPlayer().getCurrentSystem()) && state.getCurrentClientSystem() != null) {
					onSystemChange(lastSystem, state.getPlayer().getCurrentSystem());
				}
				GameClientState.staticSector = state.getCurrentSectorId();
				if (!state.getToRequestMetaObjects().isEmpty() && clientChannel != null && clientChannel.isConnectionReady()) {
					synchronized (state.getToRequestMetaObjects()) {
						while (!state.getToRequestMetaObjects().isEmpty()) {
							int nextId = state.getToRequestMetaObjects().dequeueInt();
							assert (nextId >= 0) : nextId;
							clientChannel.requestMetaObject(nextId);
						}
					}
				}
				synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
					while (!state.unloadedInventoryUpdates.isEmpty()) {
						state.unloadedInventoryUpdates.dequeue().handleInventoryReceivedNT();
					}
					{
						applySectorChanges();
						if (state.isFlagSectorChange() >= 0) {
							onSectorChangeSelf(state.getCurrentSectorId(), state.isFlagSectorChange());
							state.setFlagSectorChange(-1);
						}
					}
					if (flagRecalc) {
						long t = System.currentTimeMillis();
						recalcCurrentEntities();
						long took = System.currentTimeMillis() - t;
						if (took > 50) {
							System.err.println("[CLIENT] WARNING: Sector Entity recalc took " + took);
						}
					}
					if (clientChannel.isConnectionReady()) {
						clientMissileManager.updateClient(timer, clientChannel);
					}
					{
						state.getParticleController().update(timer);
						state.getPulseController().update(timer);
						AbstractScene.infoList.add("# UParticles: " + state.getParticleController().getParticleCount());
					}
					if (state.isWaitingForPlayerActivate()) {
						for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
							if (s instanceof PlayerCharacter) {
								PlayerCharacter c = (PlayerCharacter) s;
								if (c.getClientOwnerId() == state.getId()) {
									if (c.checkClintSpawnSanity(state.getCurrentSectorEntities())) {
										state.setCharacter(c);
										state.getPlayer().setAssignedPlayerCharacter(c);
										state.setShip(null);
										requestControlChange(null, state.getCharacter(), null, new Vector3i(), false);
										state.getGlobalGameControlManager().getIngameControlManager().getFreeRoamController().setActive(false);
										state.getGlobalGameControlManager().getIngameControlManager().getAutoRoamController().setActive(false);
										state.setWaitingForPlayerActivate(false);
										if (!state.getPlayer().getNetworkObject().isAdminClient.get() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
											EngineSettings.P_PHYSICS_DEBUG_ACTIVE.setOn(false);
											System.err.println("physics debug: " + EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn());
											state.getController().popupAlertTextMessage(Lng.str("(Admin-Only) Debug Mode: %b", EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()), 0);
										}
										tutorialStarted = true;
										state.setPlayerSpawned(true);
										System.err.println("[CLIENT] client spawned character and player: " + c + "; " + state.getPlayer());

										(new Thread("Spawn Shopkeep runner") {
											@Override
											public void run() {
												try {
													sleep(5000);
													GameTransformable nearest = state.getPlayer().getAssingedPlayerCharacter().getNearestEntity(true);
													if(nearest instanceof ManagedShop && ((ManagedShop) nearest).isAdvancedShop()) {
														state.getPlayer().sendSimpleCommand(SimplePlayerCommands.SPAWN_SHOPKEEP, nearest.getId());
													}
												} catch(Exception exception) {
													exception.printStackTrace();
												}
											}
										}).start();
									} else {
										showBigMessage("WA", Lng.str("Waiting to spawn"), Lng.str("chunks around spawn position are being loaded: %d seconds to spawn in sector: %s", c.waitingForToSpawn.size(), state.getPlayer().getCurrentSector().toStringPure()), 0);
										c.waitingForToSpawn.clear();
									}
									break;
								}
							}
						}
					}
					state.spotlights.clear();
					mineController.updateLocal(timer);
					state.getMetaObjectManager().updateLocal(timer);
					for (SimpleTransformableSendableObject<?> s : state.getCurrentSectorEntities().values()) {
						state.getDebugTimer().start(s);
						if (s instanceof SegmentController) {
							((SegmentController) s).getSegmentBuffer().updateNumber();
						}
						if (s.getSectorId() == state.getCurrentSectorId()) {
							if (s instanceof PlayerCharacter && !s.isHidden()) {
								if (((PlayerCharacter) s).getFlashLightActive() != null) {
									state.spotlights.add((PlayerCharacter) s);
								}
							}
							if (s.getPhysicsDataContainer().getObject() != null && s.getPhysicsDataContainer().getObject() instanceof RigidBody) {
								if (s instanceof SegmentController) {
									((RigidBody) s.getPhysicsDataContainer().getObject()).setDamping(((SegmentController) s).getLinearDamping(), ((SegmentController) s).getRotationalDamping());
								} else {
									((RigidBody) s.getPhysicsDataContainer().getObject()).setDamping(state.getLinearDamping(), state.getRotationalDamping());
								}
							}
							assert (s.getPhysicsDataContainer().getObject() == null || ((Physical) s).getPhysicsDataContainer().getObject() instanceof GamePhysicsObject);
							if (s.getRemoteTransformable().isSnapped()) {
								System.err.println("[CLIENT] applied server-snap to " + s);
								s.getRemoteTransformable().setSnapped(false);
							}
						} else {
							if (s.getRemoteTransformable().isSnapped()) {
								System.err.println("[CLIENT] applying server-snap to " + s + ": NOT IN SECTOR; Out of reach of physics");
								if (s.getPhysicsDataContainer().isInitialized()) {
									s.getPhysicsDataContainer().updatePhysical(state.getUpdateTime());
								}
								s.getRemoteTransformable().setSnapped(false);
							}
						}
						state.getDebugTimer().start(s, "update");
						s.updateLocal(timer);
						state.getDebugTimer().end(s, "update");
						state.getDebugTimer().end(s);
					}
					state.getDebugTimer().start("OTHERS");
					updateOthers(timer);
					state.getDebugTimer().end("OTHERS");
					if (flagReapplyBlockBehavior) {
						reapplyBlockConfigInstantly();
						flagReapplyBlockBehavior = false;
					}
				}
				state.getDebugTimer().start("STATETONT");
				state.getGameState().updateToNetworkObject();
				state.getDebugTimer().end("STATETONT");
				for (SimpleTransformableSendableObject<?> s : state.getCurrentSectorEntities().values()) {
					s.getPhysicsDataContainer().lastTransform.set(s.getPhysicsDataContainer().thisTransform);
					s.getPhysicsDataContainer().thisTransform.set(s.getWorldTransform());
				}


			} finally {
				state.setUnsynched();
			}
		}
		state.getDebugTimer().end("STATE");
		state.getDebugTimer().start("PHYSICS");
		/**
		 * PHYSICS UPDATE
		 */
		if (!state.isPassive()) {
			TimeStatistics.reset("#Physics");
			state.getPhysics().update(timer, state.getHighestSubStep());
			TimeStatistics.set("#Physics");
			if (state.getCharacter() != null && !state.getCharacter().actionUpdate) {
				/*
				 * this is needed because not every frame is a physics update but
				 * every frame is a transformation smoother step. if the character
				 * is attached to a moving object, not having this update can desynch
				 * the positions
				 */
				state.getCharacter().inPlaceAttachedUpdate(state.getUpdateTime());
			}
		// for (SimpleTransformableSendableObject<?> s : getState().getCurrentSectorEntities().values()) {
		// //update matrix of obejct twice. once for eventual NT updates, once for physics
		// if(s.getType() == EntityType.SHIP){
		// s.getPhysicsDataContainer().updatePhysical();
		// }
		// }
		}
		state.getDebugTimer().end("PHYSICS");
		state.getDebugTimer().start("CAMERA");
		/**
		 * CAMERA UPDATES
		 */
		synchronized (state) {
			state.setSynched();
			boolean mapUpdate = !state.isPassive() && state.getWorldDrawer().getGameMapDrawer().updateCamera(timer);
			if (!mapUpdate && Controller.getCamera() != null && state.getScene() != null) {
				state.getScene().updateCurrentCamera(timer);
				Controller.getCamera().lookAt(false);
			}
			if (!state.isPassive()) {
				state.getGlobalGameControlManager().update(timer);
			}
			state.getDebugTimer().end("CAMERA");
			TimeStatistics.set("#state");
			if (TimeStatistics.get("#state") > 50) {
				System.err.println("[CLIENT-CONTROLLER][WARNING] state update took " + TimeStatistics.get("#state") + "ms");
			}
			// state.getPlanetTestSurface().update(timer);
			state.setHighestSubStep(0);
			if (!state.isPassive()) {
				if (state.getWorldDrawer() != null && TimeStatistics.get("#Physics") > EngineSettings.G_DEBRIS_THRESHOLD_SLOW_MS.getInt()) {
					state.getWorldDrawer().getShards().slow = Math.min(EngineSettings.G_DEBRIS_THRESHOLD_SLOW_MS.getInt(), (int) Math.ceil(TimeStatistics.get("#Physics") / 2d));
				} else {
					state.getWorldDrawer().getShards().slow = 0;
				}
			}
			if (TimeStatistics.get("#Physics") > 30) {
				System.err.println("[CLIENT] WARNING: Physics took " + TimeStatistics.get("#Physics"));
			}
			state.getDebugTimer().start("ADDANDREMOVE");
			state.handleFlaggedAddedOrRemovedObjects();
			state.getDebugTimer().end("ADDANDREMOVE");
			state.getDebugTimer().start("SYNCH");
			// synchronize
			try {
				super.updateSynchronization();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			state.getDebugTimer().end("SYNCH");
			state.setUnsynched();
		}
		if (System.currentTimeMillis() - state.getLastServerTimeRequest() > EngineSettings.N_SERVERTIME_UPDATE_FREQUENCY.getInt() && !state.isFlagRequestServerTime()) {
			state.flagRequestServerTime();
		}
		if (state.isDbPurgeRequested()) {
			purgeDB();
			state.setDbPurgeRequested(false);
		}
		if (state.isInWarp() && Math.abs(state.getWarpedUpdateNr() - state.getNumberOfUpdate()) > 2) {
			state.setWarped(false);
		}

		//INSERTED CODE
		// Tick StarRunnables, the server will tick them if on localhost
		if(!isLocalHost()) {
			StarRunnable.tickAll(false);
		}
		///

		state.getDebugTimer().end();
	}

	public void onSystemChange(Vector3i lastSystem, Vector3i currentSystem) {
		VoidSystem sys = state.getCurrentClientSystem();
		String f;
		if (sys.getOwnerFaction() == 0) {
			f = Lng.str("The system is neutral. Use a faction block to claim it.");
		} else if (state.getFactionManager().existsFaction(sys.getOwnerFaction())) {
			Faction faction = state.getFactionManager().getFaction(sys.getOwnerFaction());
			int ownId = state.getPlayer().getFactionId();
			RType relation = state.getFactionManager().getRelation(ownId, faction.getIdFaction());
			if (faction.getIdFaction() == ownId) {
				f = Lng.str("The system is owned by your own faction %s!", faction.getName());
			} else if (relation == RType.NEUTRAL) {
				f = Lng.str("The system is owned by %s, which is neutral to you!", faction.getName());
			} else if (relation == RType.FRIEND) {
				f = Lng.str("The system is owned by %s, which are your allies!", faction.getName());
			} else if (relation == RType.ENEMY) {
				f = Lng.str("The system is owned by %s! WARNING: this is enemy territory!", faction.getName());
			} else {
				f = Lng.str("No system data");
			}
		} else {
			f = Lng.str("Unknown system owner!");
		}
		if (!state.getPlayer().isInTutorial() && !state.getPlayer().isInTestSector() && !state.getPlayer().isInPersonalSector()) {
			showBigMessage("SystemChanged" + sys.getPos(), Lng.str("Entered System %s", sys.getPos().toStringPure()), f, 0);
		}
		for (ClientSystemChangeListener l : systemChangeListeners) {
			l.onSystemChanged(lastSystem, currentSystem);
		}
		this.lastSystem.set(state.getPlayer().getCurrentSystem());
		this.lastSector.set(state.getPlayer().getCurrentSector());
	}

	public void popupDialogMessage(String msg) {
		this.queuedDialogs.enqueue(msg);
	}

	public void flagCurrentEntitiesRecalc() {
		flagRecalc = true;
	}

	@Override
	public void updateStateInput(Timer timer) {
		synchronized (state) {
			state.setSynched();
			{
				inputController.updateInput(this, timer);
			}
			if (GameClientController.hasGraphics(state) && Controller.getCamera() != null) {
				state.getWorldDrawer().getGameMapDrawer().updateMouse();
			}
			state.setUnsynched();
		}
	}

	public boolean allowedToActivate(SegmentPiece p) {
		SegmentController c = p.getSegmentController();
		boolean publicException = false;
		boolean personalException = false;
		Vector3i pos = new Vector3i();
		p.getAbsolutePos(pos);

		if(c.isOwnerSpecific(state.getPlayer())){
			personalException = true;
		} else {
			Vector3i posDir = new Vector3i();
		for (int i = 0; i < 6; i++) {
			posDir.add(pos, Element.DIRECTIONSi[i]);
			SegmentPiece pointUnsave;
			pointUnsave = c.getSegmentBuffer().getPointUnsave(posDir);
			if (pointUnsave != null && (pointUnsave.getType() == ElementKeyMap.FACTION_PUBLIC_EXCEPTION_ID || (pointUnsave.getType() == ElementKeyMap.FACTION_FACTION_EXCEPTION_ID && pointUnsave.getSegmentController().getFactionId() == state.getPlayer().getFactionId()))) {
				publicException = true;
				break;
			}}
		}

		BlockPublicPermissionEvent ev = new BlockPublicPermissionEvent(c,pos, state.getPlayer().getFactionId(),publicException);
		StarLoader.fireEvent(ev,c.isOnServer());
		publicException = ev.getPermission();

		if(personalException && ev.isAllowingPersonalException()) return true;
		else{
			if(ev.isSetByEvent() && !ev.getPermission()) {
				//denied by event
				state.getController().popupAlertTextMessage(
						Lng.str("Activation access denied") + (ev.hasMessage() ? ": " + ev.getActivationMessage() : "."), 0);
				return false;
			}
			else if (!publicException && state.getGameState().getLockFactionShips() && // c.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) > 0 && //T895
			c.getFactionId() != 0 && c.getFactionId() != state.getPlayer().getFactionId() && state.getFactionManager().getFaction(c.getFactionId()) != null) {
				//blocked by other established faction
				state.getController().popupAlertTextMessage(Lng.str("Activation access denied:\nthis structure belongs\nto another faction!"), 0);
				return false;
			}
			else if (!publicException && state.getGameState().getLockFactionShips() && c.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) > 0 && c.getFactionId() == state.getPlayer().getFactionId() && !p.getSegmentController().isSufficientFactionRights(state.getPlayer())) {
				//blocked by rank permissions
				state.getController().popupAlertTextMessage(Lng.str("Activation access denied:\nyou do not have sufficient rank in your faction!"), 0);
				return false;
			}
		}
		return true;
	}

	public boolean allowedToConnect(SegmentController controller) {
		if (controller.getFactionId() != 0 && controller.getFactionId() != state.getPlayer().getFactionId() && state.getFactionManager().getFaction(controller.getFactionId()) != null) {
			state.getController().popupAlertTextMessage(Lng.str("Edit access denied:\nthis structure belongs\nto another faction!"), "sbtaf", 0);
			return false;
		}
		if (!state.isAdmin() && !controller.allowedToEdit(state.getPlayer())) {
			if (controller.getFactionId() == state.getPlayer().getFactionId() && (state.getPlayer().getFactionRights() < controller.getFactionRights() || controller.getFactionRights() == -1)) {
				state.getController().popupAlertTextMessage(Lng.str("Access denied by faction rank!\nYou must at least be rank\n'%s'\nto edit this!", state.getPlayer().getFactionRankName(controller.getFactionRights())), "alrmovSSr0", 0);
			} else {
				state.getController().popupAlertTextMessage(Lng.str("Not allowed to build!\nAccess denied!"), "alrmovSSr1", 0);
			}
			return false;
		} else if (state.isAdmin() && !controller.allowedToEdit(state.getPlayer())) {
			if (Segment.ALLOW_ADMIN_OVERRIDE) {
				if (notSent != controller) {
					state.getController().popupAlertTextMessage(Lng.str("Access denied\n(overwritten by admin rights)!"), "alrmovr2", 0);
					notSent = controller;
				}
			} else {
				if (controller.getFactionId() == state.getPlayer().getFactionId() && (state.getPlayer().getFactionRights() < controller.getFactionRights() || controller.getFactionRights() == -1)) {
					state.getController().popupAlertTextMessage(Lng.str("Access denied by faction rank!\nYou must at least be rank\n'%s'\nto edit this!", state.getPlayer().getFactionRankName(controller.getFactionRights())), "alrmovSSr0", 0);
				} else {
					state.getController().popupAlertTextMessage(Lng.str("Not allowed to build!\nAccess denied!"), "alrmovSSr1", 0);
				}
				return false;
			}
		}
		return true;
	}

	public boolean allowedToEdit(SegmentController controller) {
		if (controller.isOwnerSpecific(state.getPlayer())) {
			return true;
		}
		if (!state.isAdmin() && !controller.allowedToEdit(state.getPlayer())) {
			if (controller.getFactionId() == state.getPlayer().getFactionId() && (state.getPlayer().getFactionRights() < controller.getFactionRights() || controller.getFactionRights() == -1)) {
				state.getController().popupAlertTextMessage(Lng.str("Access denied by faction rank!\nYou must at least be rank\n'%s'\nto edit this!", state.getPlayer().getFactionRankName(controller.getFactionRights())), "alrmovSSr0", 0);
			} else {
				state.getController().popupAlertTextMessage(Lng.str("Not allowed to build!\nAccess denied!"), "alrmovSSr1", 0);
			}
			return false;
		} else if (state.isAdmin() && !controller.allowedToEdit(state.getPlayer())) {
			if (Segment.ALLOW_ADMIN_OVERRIDE) {
				if (notSent != controller) {
					state.getController().popupAlertTextMessage(Lng.str("Access denied\n(overwritten by admin rights)!"), "alrmovr2", 0);
					notSent = controller;
				}
			} else {
				if (controller.getFactionId() == state.getPlayer().getFactionId() && (state.getPlayer().getFactionRights() < controller.getFactionRights() || controller.getFactionRights() == -1)) {
					state.getController().popupAlertTextMessage(Lng.str("Access denied by faction rank!\nYou must at least be rank\n'%s'\nto edit this!", state.getPlayer().getFactionRankName(controller.getFactionRights())), "alrmovSSr0", 0);
				} else {
					state.getController().popupAlertTextMessage(Lng.str("Not allowed to build!\nAccess denied!"), "alrmovSSr1", 0);
				}
				return false;
			}
		}
		if (controller.getFactionId() != 0 && controller.getFactionId() != state.getPlayer().getFactionId() && state.getFactionManager().getFaction(controller.getFactionId()) != null) {
			state.getController().popupAlertTextMessage(Lng.str("Edit access denied:\nthis structure belongs\nto another faction!"), "ceatt", 0);
			return false;
		}
		if (controller.railController.isDockedAndExecuted() && controller.railController.isShipyardDockedRecursive()) {
			if (controller.railController.getRoot() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) controller.railController.getRoot()).getManagerContainer() instanceof ShipyardManagerContainerInterface) {
				ShipyardManagerContainerInterface m = (ShipyardManagerContainerInterface) ((ManagedSegmentController<?>) controller.railController.getRoot()).getManagerContainer();
				for (ShipyardCollectionManager a : m.getShipyard().getCollectionManagers()) {
					SegmentController currentDocked = a.getCurrentDocked();
					if (currentDocked != null && controller.railController.isAnyChildOf(currentDocked)) {
						boolean c = a.isDockedInEditableState();
						if (!c) {
							state.getController().popupAlertTextMessage(Lng.str("State: Cannot edit ship in shipyard at this state:\n%s", a.getStateDescription()), 0);
						}
						return c;
					}
				}
				state.getController().popupAlertTextMessage("" + Lng.str("Cannot edit ship in shipyard at this state:\nShipyard invalid!"), 0);
				return false;
			} else {
				state.getController().popupAlertTextMessage("" + Lng.str("Cannot edit ship in shipyard at this state:\nShipyard invalid!"), 0);
				return false;
			}
		}
		if (controller instanceof ShopSpaceStation) {
			state.getController().popupAlertTextMessage(Lng.str("You are not allowed\nto modify a shop"), 0);
			return false;
		}
		if (controller instanceof Ship) {
			if (((Ship) controller).getAttachedPlayers().size() > 0) {
				if (!((Ship) controller).getAttachedPlayers().contains(state.getPlayer())) {
					state.getController().popupInfoTextMessage(Lng.str("You are not allowed to edit this!\nSomeone else is in this ship."), 0);
					return false;
				}
			}
		}
		return true;
	}

	public boolean isNeighborToClientSector(int sec) {
		RemoteSector objSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sec);
		return objSector != null && state.getCurrentRemoteSector() != null && Sector.isNeighbor(objSector.clientPos(), state.getCurrentRemoteSector().clientPos());
	}

	public void applySectorChanges() {
		if (!sectorChanges.isEmpty()) {
			synchronized (sectorChanges) {
				for (SectorChange change : sectorChanges) {
					boolean wasPart = change.what.isInClientRange();
					change.what.setSectorId(change.to);
					if (!change.what.isHidden()) {
						change.what.setWarpToken(true);
					}
					updateSector(change.what, change.from);
					RemoteSector objSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(change.to);
					if (objSector != null && state.getCurrentRemoteSector() != null) {
						if (Sector.isNeighbor(objSector.clientPos(), state.getCurrentRemoteSector().clientPos())) {
							flagRecalc = true;
						} else if (wasPart) {
							flagRecalc = true;
						} else {
						// System.err.println("[APPLYSECTORCHANGES] NO RECALC NECESSARY FOR "+change.what);
						}
					} else {
						if (change.from >= 0) {
							RemoteSector last = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(change.from);
							if (state.getCurrentRemoteSector() == null || (last != null && Sector.isNeighbor(last.clientPos(), state.getCurrentRemoteSector().clientPos()))) {
								flagRecalc = true;
							} else {
							// System.err.println("[APPLYSECTORCHANGES] Exception while checking neighbor: " + change.what+" :: "+objSector + "; " + getState().getCurrentRemoteSector() + ";    " + change.from + "; " + change.to);
							}
						}
					}
				}
				sectorChanges.clear();
			}
		}
	}

	private void bigMessage(String id, String message, String subtitle, float timeDelayInSecs, Color color) {
		synchronized (state.getWorldDrawer().getGuiDrawer().bigMessages) {
			//INSERTED CODE @???
			BigMessagePopupEvent event = new BigMessagePopupEvent(id, message, subtitle, timeDelayInSecs, color, FontLibrary.FontSize.BIG_30.getFont(), FontLibrary.FontSize.MEDIUM_15.getFont());
			StarLoader.fireEvent(event, false);
			id = event.getUid();
			message = event.getTitle();
			subtitle = event.getSubtitle();
			timeDelayInSecs = event.getPopupDelay();
			color = event.getColor();
			//sound = event.getAudioString();
			if(event.isCanceled()){
				return;
			}
			///
			for (BigMessage p : state.getWorldDrawer().getGuiDrawer().bigMessages) {
				if (p.getId().equals(id)) {
					// the message is already on screen, so restart message
					p.setMessage(message);
					p.restartPopupMessage();
					return;
				}
			}
			BigMessage bigMessage = new BigMessage(id, state, message, subtitle, color);
			state.getWorldDrawer().getGuiDrawer().bigMessages.addFirst(bigMessage);
			bigMessage.startPopupMessage(timeDelayInSecs);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.ControllerInterface#getServerRunningTime()
	 */
	@Override
	public long getServerRunningTime() {
		long sStart = state.getGameState().getNetworkObject().serverStartTime.get();
		long sDiff = state.getServerTimeDifference();
		return System.currentTimeMillis() - (sStart - sDiff);
	}

	/**
	 * start time in the client's time
	 */
	@Override
	public long calculateStartTime() {
		if (state.getGameState().getNetworkObject().universeDayDuration.get() == -1) {
			return -1;
		} else {
			long sDuration = state.getGameState().getNetworkObject().universeDayDuration.get();
			long sMod = state.getGameState().getNetworkObject().serverModTime.get();
			long sStart = state.getGameState().getNetworkObject().serverStartTime.get();
			long sDiff = state.getServerTimeDifference();
			long t = (((sStart - sDiff) + sMod) - sDuration);
			// System.err.println("MOD TIME "+getState().getGameState().getNetworkObject().serverModTime.get());
			// System.err.println("[CLIENT] SST: "+state.getPlayer().getNetworkObject().serverStartTime.get()+"; "+state.getPlayer().getNetworkObject().serverModTime.get()+"; "+state.getServerTimeDifference()+" ---> "+t);
			return t;
		}
	}

	@Override
	public long getUniverseDayInMs() {
		// System.err.println("UNIVERSE DAY: "+state.getGameState().getNetworkObject().universeDay.get());
		return state.getGameState().getNetworkObject().universeDayDuration.get();
	}

	@Override
	public void onRemoveEntity(Sendable remove) {
		if (remove instanceof SimpleTransformableSendableObject) {
			SimpleTransformableSendableObject t = (SimpleTransformableSendableObject) remove;
			if (t.getSectorId() == state.getCurrentSectorId() || t.isNeighbor(t.getSectorId(), state.getCurrentSectorId())) {
				flagRecalc = true;
			}
		}
	}

	public GUIPopupInterface changePopupMessage(String before, String message) {
		for (GUIPopupInterface p : state.getWorldDrawer().getGuiDrawer().popupMessages) {
			if (p.getMessage().equals(before)) {
				p.setMessage(message);
				p.restartPopupMessage();
				return p;
			}
		}
		return null;
	}

	public void characterCommitSuicide() throws IOException, UnknownCommandException {
		if (state.getCharacter() != null) {
			sendUnblockedRequest(new KillCharacterRequest());
		} else {
			System.err.println("No character to kill");
		}
	}

	public void clearAllSegmentBuffers() {
		System.err.println("[CLIENT] clearing all buffered data");
		// has to be synched here, else there is deadlock danger
		synchronized (state) {
			synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if (s instanceof SendableSegmentController) {
						((SendableSegmentController) s).onClear();
						synchronized (((SendableSegmentController) s).getSegmentBuffer()) {
							((SendableSegmentController) s).getSegmentBuffer().clear(false);
						}
						if (s instanceof ManagedSegmentController) {
							((ManagedSegmentController<?>) s).getManagerContainer().clear();
						}
						((ClientSegmentProvider) ((SendableSegmentController) s).getSegmentProvider()).clearRequestedBuffers();
						if (((SendableSegmentController) s).getPhysicsDataContainer().getObject() != null && ((SendableSegmentController) s).getPhysicsDataContainer().getObject() instanceof RigidBody) {
							((SendableSegmentController) s).getPhysicsDataContainer().getObject().setWorldTransform(((SendableSegmentController) s).getWorldTransformOnClient());
							((RigidBody) ((SendableSegmentController) s).getPhysicsDataContainer().getObject()).setInterpolationWorldTransform(((SendableSegmentController) s).getWorldTransformOnClient());
						}
					}
				}
			}
		}
	}

	public void connect(HostPortLoginName to) throws Exception {
		System.out.println("[CLIENT] trying to connect to " + to.host + ":" + to.port);
		String loginName = to.loginName;
		if (loginName == null) {
			try {
				File l = new FileExt("./debugPlayerLock.lock");
				DataInputStream s = new DataInputStream(new FileInputStream(l));
				int loggedOn = s.readInt();
				s.close();
				// for testing purposes
				loginName = "player" + (loggedOn + 1);
			} catch (FileNotFoundException e) {
				loginName = "player1";
			}
		}
		if ((Starter.currentSession == null || !Starter.currentSession.isValid()) && (!EngineSettings.N_IGNORE_SAVED_UPLINK_CREDENTIALS_IN_SINGLEPLAYER.isOn() || !to.host.equals("localhost")) && StarMadeCredentials.exists()) {
			System.out.println("Found Credentials");
			setGuiConnectionState(Lng.str("Found credentials."));
			StarMadeCredentials cred = StarMadeCredentials.read();
			System.out.println(Lng.str("Decrypted StarMade credentials ... logging in to star-made.org"));
			setGuiConnectionState(Lng.str("Decrypted StarMade credentials ... logging in to star-made.org"));
			SessionNewStyle session = new SessionNewStyle(to.host + ":" + to.port);
			session.login(cred.getUser(), cred.getPasswd());
			setGuiConnectionState(Lng.str("Logging in to star-made.org successfull."));
			System.out.println(Lng.str("Logging in to star-made.org successfull."));
		} else {
		}
		connect(to.host, to.port, to.userAgent, loginName, Starter.currentSession);
		System.out.println("[CLIENT] connected to server " + to.host + ":" + to.port);
		if (Starter.currentSession != null) {
			System.err.println("UPDATING AFTER LOGIN");
			Starter.currentSession.afterLogin();
		}
	}

	public GUIPopupInterface endPopupMessage(String message) {
		for (GUIPopupInterface p : state.getWorldDrawer().getGuiDrawer().popupMessages) {
			if (p.getMessage().equals(message)) {
				p.timeOut();
				return p;
			}
		}
		return null;
	}

	public <E extends ElementCollection<E, EC, EM>, EC extends ElementCollectionManager<E, EC, EM>, EM extends UsableElementManager<E, EC, EM>> void enqueueElementCollectionUpdate(ElementCollectionManager<E, EC, EM> man) {
		elementCollectionCalculationThreadManager.enqueue(new ElementCollectionCalculationThreadExecution((EC) man));
	}

	/**
	 * @return the clientChannel
	 */
	public ClientChannel getClientChannel() {
		return clientChannel;
	}

	/**
	 * @param clientChannel the clientChannel to set
	 */
	public void setClientChannel(ClientChannel clientChannel) {
		this.clientChannel = clientChannel;
	}

	/**
	 * @return the clientGameData
	 */
	public ClientGameData getClientGameData() {
		return clientGameData;
	}

	/**
	 * @return the clientMissileManager
	 */
	public ClientMissileManager getClientMissileManager() {
		return clientMissileManager;
	}

	@Override
	public CreatorThreadController getCreatorThreadController() {
		return creatorThreadController;
	}

	/**
	 * @return the privateChannelSynchController
	 */
	public SynchronizationContainerController getPrivateChannelSynchController() {
		return privateChannelSynchController;
	}

	/**
	 * @return the synchController
	 */
	public SynchronizationContainerController getSynchController() {
		return synchController;
	}

	/**
	 * @return the textureSynchronizer
	 */
	public TextureSynchronizer getTextureSynchronizer() {
		return textureSynchronizer;
	}

	/**
	 * @return the tutorialMode
	 */
	public TutorialMode getTutorialMode() {
		return tutorialMode;
	}

	/**
	 * @param tutorialMode the tutorialMode to set
	 */
	public void setTutorialMode(TutorialMode tutorialMode) {
		this.tutorialMode = tutorialMode;
	}

	public void initialize() throws IOException, NoSuchAlgorithmException {
		System.err.println("[CLIENT] State initializing");
		synchronized (state) {
			state.setSynched();
			//setupControlManager();
			state.setUnsynched();
		}

		setGuiConnectionState(Lng.str("Loading block hevahior config..."));

		System.err.println("[CLIENT] InternalCallback initializing");

		state.getPhysics().getDynamicsWorld().setInternalTickCallback(new PhysicsCallback(), null);

		ClientSegmentProvider.dummySegment = new RemoteSegment(new FloatingRock(state));
		ClientSegmentProvider.dummySegment.setSegmentData(new SegmentData4Byte(false));

		System.out.println("[CLIENT] State initialized. GameMode: " + state.getGameMode());

		//INSERTED CODE
		//if(SteamAPIHandler.initialized && GameCommon.isClientConnectedToServer() && SteamConfigManager.getConfig().getBoolean("allow-friends-to-join-online")) Starter.apiHandler.createLobby();
		//
		setGuiConnectionState(Lng.str("Setting up global game control manager..."));
		setupGlobalGameControlManager();
		setGuiConnectionState(Lng.str("Loading block hevahior config..."));
		System.err.println("[CLIENT] InternalCallback initializing");
		state.getPhysics().getDynamicsWorld().setInternalTickCallback(new PhysicsCallback(), null);
		ClientSegmentProvider.dummySegment = new RemoteSegment(new FloatingRock(state));
		ClientSegmentProvider.dummySegment.setSegmentData(new SegmentData4Byte(false));
		System.out.println("[CLIENT] State initialized. GameMode: " + state.getGameMode());
		state.setUnsynched();
	}

	// public void connectDB() throws SQLException{
	// state.getSqlConnection().connect();
	// }
	// public void disconnectDB() throws SQLException{
	// state.getSqlConnection().close();
	// }
	// public void dropDB() throws ResourceException, IOException{
	// System.out.println("dropping schema");
	// state.getSqlConnection().executeSQLFile("schema-drop.sql");
	// }
	// public void createDB() throws ResourceException, IOException{
	// System.out.println("creating schema");
	// state.getSqlConnection().executeSQLFile("schema-creation.sql");
	// System.out.println("indicing schema");
	// state.getSqlConnection().executeSQLFile("schema-index.sql");
	// }
	// public static Planet getTestPlanet(GameClientState state){
	// PlanetInformations infos = new PlanetInformations();
	// infos.setDaytime(360);
	// infos.setEquatorTemperature(45);
	// infos.setPoleTemperature(-20);
	// infos.setRadius(10);
	// infos.setWaterInPercent(0.6f);
	// infos.setHeightFactor(0.2f);
	// infos.setSeed((int)System.currentTimeMillis());
	// infos.setHumidity(1.0f);
	// infos.setCloudHeight(0.0035f);
	// infos.setHasCloud(true);
	// infos.setAtmosphereDensity(1.0f);
	// Planet p = new Planet(state, infos, null);
	// return p;
	// }
	public void initializeState() throws NoSuchAlgorithmException, IOException {
		// parseBlockBehavior();
		state.setBlockBehaviorCheckSum((FileUtil.getSha1Checksum("./data/config/blockBehaviorConfig.xml")));
		state.setConfigCheckSum(FileUtil.getSha1Checksum("./data/config/BlockConfig.xml"));
		state.setFactionConfigCheckSum(FileUtil.getSha1Checksum("./data/config/FactionConfig.xml"));
		state.setConfigPropertiesCheckSum(FileUtil.getSha1Checksum("./data/config/BlockTypes.properties"));
		if (!ElementKeyMap.configHash.equals("none") && !state.getBlockConfigCheckSum().equals(ElementKeyMap.configHash)) {
			System.err.println("[CLIENT][CONFIG] DIFFERENT BLOCK CONFIG TO COMPARE TO. REFRESHING BLOCK CONFIG TO MATCH HASH OF ORIGINAL BLOCK CONFIG SO IT CAN BE COMPARED TO THE SERVER'S HASH AFTER LOGIN");
			ElementKeyMap.reinitializeData(new FileExt("./data/config/BlockConfig.xml"), false, null, null, false);
		}
		state.setCustomTexturesCheckSum(FileUtil.createFilesHashRecursively(GameResourceLoader.CUSTOM_TEXTURE_PATH, pathname -> {
			boolean accept = pathname.isDirectory() || pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".png");
			return accept;
		}));
		state.setController(this);
		try {
			// state.setSqlConnection(new SQLConnection());
			//
			// if(WITH_DB){
			// System.err.println("[CLIENT] starting DB");
			// connectDB();
			// Universe u = new Universe(119236986,state);
			// u.create();
			// u.addNewGalaxy("defGalaxy", 30);
			// state.setUniverse(u);
			// }else{
			// System.err.println("[CLIENT] NOT starting DB");
			// }
			state.setParticleController(new ProjectileController(state, -1));
			state.setPulseController(new PulseController(state, -1));
			state.setPhysics(new PhysicsExt(state));
			state.setGUIController(new GUIController(state));
			if (!state.isPassive()) {
				state.setWorldDrawer(new WorldDrawer(state));
			}
			/* do connection establishment here */
			state.setReady(true);
		// -----------------------------------
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the tutorialStarted
	 */
	public boolean isTutorialStarted() {
		return tutorialStarted;
	}

	public String onAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
		System.err.println("AUTOCOMPLETE: " + s + "; PREFIX: " + prefix);
		if (prefix.equals(state.getCommandPrefixes()[0])) {
			String compl = EngineSettings.autoCompleteString(s);
			if (s.equals(compl)) {
				ArrayList<EngineSettings> list = EngineSettings.list(compl);
				for (EngineSettings e : list) {
					callback.onTextEnter(e.toString(), false, true);
				}
			}
			return compl;
		} else if (prefix.equals(state.getCommandPrefixes()[1])) {
			String compl = AdminCommands.autoCompleteString(s);
			// Only do suggestions if something has been typed
			if (!s.isEmpty()) {
				boolean partialMatch = false;
				if (s.equals(compl) && !s.isEmpty()) {
					ArrayList<AdminCommands> list = AdminCommands.list(compl);
					if (list.size() > 1) {
						partialMatch = true;
						StringBuffer b = new StringBuffer();
						b.append(Lng.str("Possible Commands:"));
						for (int i = 0; i < list.size(); i++) {
							AdminCommands e = list.get(i);
							String name = e.name().toLowerCase(Locale.ENGLISH);
							b.append(name);
							if (i < list.size() - 1) {
								b.append(", ");
							}
						}
						callback.onTextEnter(b.toString(), false, true);
					}
				}
				if (s.equals(compl) && !partialMatch) {
					String corrected = findCorrectedCommand(s);
					if (!corrected.isEmpty()) {
						callback.onTextEnter(corrected, false, true);
					}
				}
			}
			ArrayList<String> commands = new ArrayList<String>();
			commands.add("pm");
			commands.add("f");
			for (int i = 0; i < AdminCommands.values().length; i++) {
				AdminCommands e = AdminCommands.values()[i];
				commands.add(e.name());
			}
			if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && state.getPlayer() != null && !state.getPlayer().getNetworkObject().isAdminClient.get()) {
				EngineSettings.P_PHYSICS_DEBUG_ACTIVE.setOn(false);
			}
			for (String com : commands) {
				// System.err.println("CHECKING "+s+" on "+e.name()+" -> "+s.startsWith(e.name().toLowerCase(Locale.ENGLISH)+" "));
				if (s.startsWith(com.toLowerCase(Locale.ENGLISH) + " ")) {
					System.err.println("ADMIN COMMAND SET: " + com);
					// admin command written
					String[] split = s.split(" ");
					if (split.length > 1) {
						compl = "";
						for (int j = 0; j < split.length - 1; j++) {
							compl += split[j] + " ";
						}
						compl += autocompletePlayer(state, split[split.length - 1]);
					}
					break;
				}
			}
			return compl;
		} else if (prefix.equals("#")) {
			String[] split = s.split(" ");
			if (split.length > 0) {
				String compl = "";
				for (int j = 0; j < split.length - 1; j++) {
					compl += split[j] + " ";
				}
				compl += autocompletePlayer(state, split[split.length - 1]);
				return compl;
			}
		}
		throw new PrefixNotFoundException(prefix);
	}

	public void onEndFrame() {
	}

	public void onHurt(PlayerState playerState, Sendable vessel) {
		if (playerState.isClientOwnPlayer()) {
			// System.err.println("ON HURT "+playerState+" CLIENT PLAYER: "+getState().getPlayerName()+"; "+vessel);
			state.getWorldDrawer().getGuiDrawer().startHurtAnimation(vessel);
		}
	}

	public void onPrivateSendableAdded(Sendable s) {
	}

	public void onPrivateSendableRemoved(Sendable s) {
	}

	/**
	 * withing main loop
	 *
	 * @param currentSectorId
	 * @param oldSectorId
	 * @throws IOException
	 */
	public void onSectorChangeSelf(int currentSectorId, int oldSectorId) {
		long time = System.currentTimeMillis();
		if (state.getShip() != null) {
			if (state.getShip().getSectorId() != currentSectorId) {
				// System.err.println("Transferring own Ship to new Sector");
				state.getShip().setSectorId(currentSectorId);
				updateSector(state.getShip(), oldSectorId);
			}
		}
		for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if (s instanceof SimpleTransformableSendableObject) {
				SimpleTransformableSendableObject t = (SimpleTransformableSendableObject) s;
				if (!t.isClientOwnObject()) {
					if (t instanceof SegmentController && (((SegmentController) t).getDockingController().isDocked() || ((SegmentController) t).railController.isDockedOrDirty())) {
					// no physics change for docked objects
					// their physics are bound to parent (same body)
					} else {
						t.onPhysicsRemove();
						if (!t.isHidden()) {
							t.onPhysicsAdd();
						}
					}
				}
			}
		}
		flagRecalc = true;
		for (ClientSectorChangeListener s : sectorChangeListeners) {
			s.onSectorChangeSelf(currentSectorId, oldSectorId);
		}
		clientGameData.updateNearest(currentSectorId);
		long took = System.currentTimeMillis() - time;
		if (took > 50) {
			System.err.println("[CLIENT] WARNING: Sector change took " + took + " ms");
		}
	}

	public void onSendableAdded(Sendable s) {
		System.err.println("[CLIENT] Added Sendable: " + s);
		if (s instanceof SegmentController) {
			Starter.modManager.onSegmentControllerSpawn((SegmentController) s);
		}

		//INSERTED CODE
		ClientSendableAddEvent event = new ClientSendableAddEvent(this, s, ClientSendableAddEvent.Condition.PRE);
		StarLoader.fireEvent(event, false);
		StarLoaderHooks.onClientSendableAddEvent(event);
		///
		if (s instanceof ShopInterface) {
			shopInterfaces.add((ShopInterface) s);
		}
		if (s instanceof SimpleTransformableSendableObject) {
			int sectorId = ((SimpleTransformableSendableObject) s).getSectorId();
			if (s instanceof PlayerCharacter) {
				System.err.println("ADDED PLAYER CHARACTER ''''''''''''''''''''''''''''''''''''''''''''''''''' CID: " + ((PlayerCharacter) s).getClientOwnerId());
				synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
					for (Sendable ps : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
						if (ps instanceof PlayerState && ((PlayerState) ps).getClientId() == ((PlayerCharacter) s).getClientOwnerId()) {
							((PlayerState) ps).setAssignedPlayerCharacter(((PlayerCharacter) s));
						}
					}
				}
			}
			RemoteSector objSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);
			RemoteSector currentSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(state.getCurrentSectorId());
			if (objSector != null && currentSector != null) {
				if (Sector.isNeighbor(objSector.clientPos(), currentSector.clientPos())) {
					flagRecalc = true;
				}
			}
		} else {
			state.getOtherSendables().add(s);
			if (s instanceof RemoteSector) {
				state.getLoadedSectors().put(((RemoteSector) s).clientPos(), (RemoteSector) s);
			}
		}
		for (SendableAddedRemovedListener e : sendableAddRemoveListener) {
			e.onAddedSendable(s);
		}
		flagTrackingChanged();
		if (s instanceof PlayerState) {
			Starter.modManager.onPlayerCreated((PlayerState) s);
		}
		//INSERTED CODE
		ClientSendableAddEvent event2 = new ClientSendableAddEvent(this, s, ClientSendableAddEvent.Condition.POST);
		StarLoader.fireEvent(event2, false);
		///

	}

	// public void popupInviewTextMessage(String message, float timeDelayInSecs){
	//
	// if(getState().getWorldDrawer().getGuiDrawer().inViewPopup != null &&
	// getState().getWorldDrawer().getGuiDrawer().inViewPopup.getMessage().equals(message)){
	// getState().getWorldDrawer().getGuiDrawer().inViewPopup.restartPopupMessage();
	// // the message is already on screen
	// return;
	// }else{
	//
	// //			System.err.println("NEW INVIEW POPUP");
	// PopupMessageBlinkingIntro popupMessage = new PopupMessageBlinkingIntro(getState(), message, ClientMessageLog.infoColor);
	// getState().getWorldDrawer().getGuiDrawer().inViewPopup = popupMessage;
	// getState().getWorldDrawer().getGuiDrawer().inViewPopup.startPopupMessage(timeDelayInSecs);
	// }
	// }
	// public void popupSelectedTextMessage(String message, float timeDelayInSecs){
	//
	// if(getState().getWorldDrawer().getGuiDrawer().selectedPopup != null &&
	// getState().getWorldDrawer().getGuiDrawer().selectedPopup.getMessage().equals(message)){
	// // the message is already on screen, so restart message
	// getState().getWorldDrawer().getGuiDrawer().selectedPopup.restartPopupMessage();
	// return;
	// }else{
	// PopupMessageBlinkingIntro popupMessage = new PopupMessageBlinkingIntro(getState(), message, ClientMessageLog.infoColor);
	// getState().getWorldDrawer().getGuiDrawer().selectedPopup = popupMessage;
	// getState().getWorldDrawer().getGuiDrawer().selectedPopup.startPopupMessage(timeDelayInSecs);
	// }
	// }
	public void onSendableRemoved(Sendable remove) {
		//INSERTED CODE
		ClientSendableRemoveEvent event = new ClientSendableRemoveEvent(this, remove, ClientSendableRemoveEvent.Condition.PRE);
		StarLoader.fireEvent(event, false);
		///
		if (remove instanceof SegmentController) {
			creatorThreadController.removeCreatorThread((SegmentController) remove);
		}
		// System.err.println("[CLIENT] Deleted Sendable: "+remove);
		if (remove instanceof SimpleTransformableSendableObject) {
			if (remove instanceof PlayerCharacter) {
				synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
					for (Sendable ps : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
						if (ps instanceof PlayerState && ((PlayerState) ps).getAssingedPlayerCharacter() == remove) {
							((PlayerState) ps).setAssignedPlayerCharacter(null);
						}
					}
				}
			}
			int sectorId = ((SimpleTransformableSendableObject) remove).getSectorId();
			RemoteSector objSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);
			RemoteSector currentSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(state.getCurrentSectorId());
			if (objSector != null && currentSector != null) {
				if (Sector.isNeighbor(objSector.clientPos(), currentSector.clientPos())) {
					flagRecalc = true;
				}
			}
		}
		if (remove instanceof ShopInterface) {
			shopInterfaces.remove((ShopInterface) remove);
		}
		state.getOtherSendables().remove(remove);
		if (remove instanceof RemoteSector) {
			state.getLoadedSectors().remove(((RemoteSector) remove).clientPos());
		}
		PlayerInteractionControlManager ppc = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		if (ppc.getSelectedEntity() == remove) {
			ppc.setSelectedEntity(null);
		}
		if (ppc.getSelectedAITarget() == remove) {
			ppc.setSelectedAITarget(null);
		}
		if (remove == state.getCurrentPlayerObject()) {
			System.err.println("[CLIENT][ONENTITYREMOVE] reset player object");
			state.setCurrentPlayerObject(null);
		}
		if (remove == state.getShip()) {
			if (state.getShip() != null) {
				System.err.println("[CLIENT][ONENTITYREMOVE] Removing own ship");
				state.setShip(null);
			}
		}
		if (remove == state.getCharacter()) {
			System.err.println("[CLIENT][ONENTITYREMOVE] Removing own character");
			state.setCharacter(null);
		}
		for (SendableAddedRemovedListener e : sendableAddRemoveListener) {
			e.onRemovedSendable(remove);
		}
		flagTrackingChanged();
		if (remove instanceof PlayerState) {
			Starter.modManager.onPlayerRemoved((PlayerState) remove);
		}
	// System.err.println("[CLIENT] TO REMOVE HAS IS "+remove+"; Current "+state.getCurrentPlayerObject()+"; Char "+state.getCharacter()+"; Ship "+state.getShip());
		//INSERTED CODE
		ClientSendableRemoveEvent event2 = new ClientSendableRemoveEvent(this, remove, ClientSendableRemoveEvent.Condition.POST);
		StarLoader.fireEvent(event2, false);
		///
	}

	public void onStartFrame() {
	// state.getController().getGuiCallbackController().deactivateInsideGUIs();
	}

	public void onStringCommand(String command, TextCallback callback, String prefix) {
		System.err.println("HANDLING COMMAND " + command + "::: " + state.getCommandPrefixes()[0].substring(1) + "; " + state.getCommandPrefixes()[1].substring(1));
		if (command.length() <= prefix.length()) {
			return;
		}
		command = command.substring(prefix.length() - 1);
		String[] parts = command.split("\\s+");
		try {
			if (prefix.equals(state.getCommandPrefixes()[0])) {
				if (parts.length <= 1 || parts.length > 2) {
					throw new IndexOutOfBoundsException("[ERROR] Invalid argument count " + parts.length + ": " + Arrays.toString(parts));
				}
				EngineSettings valueOf = Enum.valueOf(EngineSettings.class, parts[0].toUpperCase(Locale.ENGLISH));
				String arg = parts[1];
				valueOf.setFromString(arg);
				callback.onTextEnter("[COMMAND] \"" + command + "\" successful: " + valueOf.name() + " = " + valueOf.getAsString(), false, false);
			} else if (prefix.equals(state.getCommandPrefixes()[1])) {
				AdminCommands valueOf = Enum.valueOf(AdminCommands.class, parts[0].toUpperCase(Locale.ENGLISH));
				String param = command.substring(command.indexOf(parts[0]) + parts[0].length()).trim();
				if (param.length() > 0) {
					String[] parameterArray = StringTools.splitParameters(param);
					Object[] packParameters = AdminCommands.packParameters(valueOf, parameterArray);
					sendAdminCommand(valueOf, packParameters);
				} else {
					if (valueOf.getTotalParameterCount() > 0) {
						String needed = "need ";
						if (valueOf.getRequiredParameterCount() != valueOf.getTotalParameterCount()) {
							needed += "minimum of " + valueOf.getRequiredParameterCount();
						} else {
							needed += valueOf.getTotalParameterCount();
						}
						throw new AdminCommandIllegalArgument(valueOf, null, "No parameters provided: " + needed);
					}
					// no parameters needed
					sendAdminCommand(valueOf);
				}
			} else {
				throw new IllegalArgumentException("[ERROR] PREFIX NOT KNOWN: \"" + prefix + "\"; use one of " + Arrays.toString(state.getCommandPrefixes()));
			}
		} catch (IllegalArgumentException e) {
			if (!e.getMessage().startsWith("[ERROR]")) {
				callback.onTextEnter("[ERROR] UNKNOWN COMMAND: " + parts[0], false, false);
			} else {
				callback.onTextEnter(e.getMessage(), false, false);
			}
		} catch (IndexOutOfBoundsException e1) {
			callback.onTextEnter(e1.getMessage(), false, false);
		} catch (AdminCommandIllegalArgument e2) {
			if (e2.getMsg() != null) {
				callback.onTextEnter("[ERROR] " + e2.getCommand() + ": " + e2.getMsg(), false, false);
				callback.onTextEnter("[ERROR] usage: " + e2.getCommand().getDescription(), false, false);
			} else {
				callback.onTextEnter(e2.getMessage(), false, false);
			}
		} catch (SettingStateParseError es) {
			callback.onTextEnter("[ERROR] SETTING: " + es.getMessage(), false, false);
		}
	}

	public GUIPopupInterface popupAlertTextMessage(String message, String id, float timeDelayInSecs) {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.ALERT)*/
//		AudioController.fireAudioEventID(35);
		return popupTextMessage(message, id, timeDelayInSecs, ClientMessageLogType.ERROR);
	}

	@Override
	public void popupAlertTextMessage(String message) {
		popupAlertTextMessage(message, 0);
	}

	public GUIPopupInterface popupAlertTextMessage(String message, float timeDelayInSecs) {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.ALERT)*/
//		AudioController.fireAudioEventID(36);
		return popupTextMessage(message, message, timeDelayInSecs, ClientMessageLogType.ERROR);
	}

	public GUIPopupInterface popupGameTextMessage(String message, String id, float timeDelayInSecs) {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.GAME)*/
//		AudioController.fireAudioEventID(37);
		return popupTextMessage(message, id, timeDelayInSecs, ClientMessageLogType.GAME);
	}

	// public void releaseControl(PlayerControllable controllable, Vector3i param, boolean hide){
	// state.getPlayer().requestControlRelease(controllable, param, hide);
	// }
	public GUIPopupInterface popupInfoTextMessage(String message, String id, float timeDelayInSecs) {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.INFO)*/
//		AudioController.fireAudioEventID(38);
		return popupTextMessage(message, id, timeDelayInSecs, ClientMessageLogType.INFO);
	}

	public GUIPopupInterface popupGameTextMessage(String message, float timeDelayInSecs) {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.GAME)*/
//		AudioController.fireAudioEventID(39);
		return popupTextMessage(message, message, timeDelayInSecs, ClientMessageLogType.GAME);
	}

	public GUIPopupInterface popupInfoTextMessage(String message, float timeDelayInSecs) {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.INFO)*/
//		AudioController.fireAudioEventID(40);
		return popupTextMessage(message, message, timeDelayInSecs, ClientMessageLogType.INFO);
	}

	private GUIPopupInterface popupTextMessage(String message, String id, float timeDelayInSecs, ClientMessageLogType type) {
		if (state.isPassive()) {
			for (ChatListener c : state.getChatListeners()) {
				ChatMessage chatMessage = new ChatMessage();
				chatMessage.sender = "[POPUP]";
				chatMessage.receiver = "[CLIENT]";
				chatMessage.text = message;
				c.notifyOfChat(chatMessage);
			}
			PopupMessageNew popupMessage = new PopupMessageNew(state, id, message, type.color);
			return popupMessage;
		} else {
			synchronized (state.getWorldDrawer().getGuiDrawer().popupMessages) {
				for (GUIPopupInterface p : state.getWorldDrawer().getGuiDrawer().popupMessages) {
					if (p.getId().equals(id)) {
						p.setMessage(message);
						// the message is already on screen, so restart message
						p.restartPopupMessage();
						return p;
					}
				}
				System.err.println("[CLIENT][POPUP] " + type.name() + ": " + message);
				state.getMessageLog().log(new ClientMessageLogEntry("SYSTEM", state.getPlayerName(), message, System.currentTimeMillis(), type));
				GUIPopupInterface popupMessage;
				popupMessage = new PopupMessageNew(state, id, message, type.color);
				popupMessage.setFlashing(type == ClientMessageLogType.FLASHING);
				state.getWorldDrawer().getGuiDrawer().popupMessages.addFirst(popupMessage);
				popupMessage.startPopupMessage(timeDelayInSecs);
				switch(type) {
					case CHAT_FACTION ->
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.CHAT_FACTION)*/
						AudioController.fireAudioEventID(41);
					case CHAT_PRIVATE ->
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.CHAT_PRIVATE)*/
						AudioController.fireAudioEventID(42);
					case CHAT_PRIVATE_SEND ->
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.CHAT_PRIVATE_SEND)*/
						AudioController.fireAudioEventID(43);
					case CHAT_PUBLIC ->
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.CHAT_PUBLIC)*/
						AudioController.fireAudioEventID(44);
					case ERROR ->
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.ERROR)*/
						AudioController.fireAudioEventID(45);
					case FLASHING ->
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.FLASHING)*/
						AudioController.fireAudioEventID(46);
					case GAME ->
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.GAME)*/
						AudioController.fireAudioEventID(47);
					case INFO ->
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.INFO)*/
						AudioController.fireAudioEventID(48);
					case TIP ->
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.POPUP, AudioTags.TIP)*/
						AudioController.fireAudioEventID(49);
					default -> {
					}
				}
				return popupMessage;
			}
		}
	}

	public GUIPopupInterface popupTipTextMessage(String message, float timeDelayInSecs) {
		return popupTextMessage(message, message, timeDelayInSecs, ClientMessageLogType.TIP);
	}

	public GUIPopupInterface popupFlashingTextMessage(String message, float timeDelayInSecs) {
		return popupTextMessage(message, message, timeDelayInSecs, ClientMessageLogType.FLASHING);
	}

	private void purgeCompleteDB() {
		System.out.println("[CLIENT] PURGING CLIENT DATABASE CACHE");
		File f = new FileExt(ClientStatics.SEGMENT_DATA_DATABASE_PATH);
		if (f.exists()) {
			FileUtil.deleteDir(f);
			f.mkdir();
		}
	}

	private void purgeDB() {
		synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (s instanceof SegmentController) {
					((SegmentController) s).getSegmentProvider().purgeDB();
				}
			}
		}
	}

	@Override
	public void readjustControllers(Collection<Element> elems, SegmentController sc, Segment segment) {
	}

	private void recalcCurrentEntities() {
		/*
		 * ! synched under state, localObjects !
		 */
		try {
			// System.err.println("---------------------START SECTOR ENTITY RECALC");
			for (SimpleTransformableSendableObject s : state.getCurrentSectorEntities().values()) {
				flagSectorCleanup.add(s);
			}
			state.getCurrentGravitySources().clear();
			state.getCurrentSectorEntities().clear();
			for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if (s instanceof SimpleTransformableSendableObject) {
					SimpleTransformableSendableObject ent = (SimpleTransformableSendableObject) s;
					ent.setInClientRange(true);
					if (ent.getSectorId() == state.getCurrentSectorId() || ent.isNeighbor(ent.getSectorId(), state.getCurrentSectorId())) {
						ent.setClientCleanedUp(false);
						if (ent.isGravitySource()) {
							state.getCurrentGravitySources().add(ent);
						}
						state.getCurrentSectorEntities().put(ent.getId(), ent);
					} else {
						ent.onSectorInactiveClient();
					// System.err.println("#!!ENTITY IS NOT A NEIGBOR "+ent.getSectorId()+"; "+ent);
					}
				}
			}
			// remove all entities that are still part of the set
			for (int i = 0; i < flagSectorCleanup.size(); i++) {
				SimpleTransformableSendableObject s = flagSectorCleanup.get(i);
				if (state.getCurrentSectorEntities().containsKey(s.getId())) {
					flagSectorCleanup.remove(i);
					i--;
				} else {
					s.setInClientRange(false);
				}
			}
			// System.err.println("[CLIENT] #!# SECTOR ENTITIES RECALC DONE: "+getState().getCurrentSectorEntities().size()+"; cleaning up "+flagSectorCleanup.size()+" objects");
			// System.err.println("######################END SECTOR ENTITY RECALC");
			flagRecalc = false;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception: Delayed recalc");
			flagRecalc = true;
		}
		if (!state.isPassive()) {
			state.getWorldDrawer().setFlagSegmentControllerUpdate(true);
			state.getWorldDrawer().setFlagCharacterUpdate(true);
			state.getWorldDrawer().setFlagManagedSegmentControllerUpdate(true);
			state.getWorldDrawer().setFlagPlanetCoreUpdate((true));
		}
		for(EntitiesChangedListener l : entitiesChangedListeners) {
			l.onEntitiesChanged(state.getCurrentSectorEntities());
		}

		sectorEntitiesChangeObservable.note();
	}
	public final List<EntitiesChangedListener> entitiesChangedListeners = new ObjectArrayList<GameClientController.EntitiesChangedListener>();
	public static interface EntitiesChangedListener{
		public void onEntitiesChanged(Int2ObjectOpenHashMap<SimpleTransformableSendableObject<?>> sectorEntities);
	}
	public static class SectorChangeObservable extends GUIObservable {

		public void note() {
			notifyObservers();
		}
	}

	public byte[] requestBlockConfig() throws IOException, UnknownCommandException {
		return sendBlockedDataRequest(new BlockConfigRequest());
	}

	public byte[] sendBlockedDataRequest(GameRequestInterface request) throws IOException, UnknownCommandException {
		return ((DataRequestAnswer) sendBlockedRequest(request)).data;
	}

	public byte[] requestFactionConfig() throws IOException, UnknownCommandException {
		return sendBlockedDataRequest(new FactionRequest());
	}

	public byte[] requestBlockProperties() throws IOException, UnknownCommandException {
		return sendBlockedDataRequest(new BlockPropertiesRequest());
	}

	public byte[] requestBlockBehavior() throws IOException, UnknownCommandException {
		return sendBlockedDataRequest(new BlockBehaviorRequest());
	}

	public byte[] requestCustomTextures() throws IOException, UnknownCommandException {
		return sendBlockedDataRequest(new CustomTextureRequest());
	}

	public void requestControlChange(PlayerControllable from, PlayerControllable to, Vector3i fromParam, Vector3i toParam, boolean hide) {
		state.getPlayer().getControllerState().requestControl(from, to, fromParam, toParam, hide);
	}

	public GameModes requestGameMode() throws IOException, UnknownCommandException {
		GameModeAnswer a = (GameModeAnswer) sendBlockedRequest(new GameModeRequest());
		state.setInitialSectorId(a.sectorId);
		state.getInitialSectorPos().set(a.sectorPos);
		state.receivedBlockConfigChecksum = a.blockConfigChecksum;
		state.receivedBlockConfigPropertiesChecksum = a.blockPropertiesChecksum;
		;
		state.setPhysicalAsteroids(a.asteroidPhysics);
		state.receivedBlockBehaviorChecksum = a.blockBehaviorChecksum;
		state.receivedCustomTexturesChecksum = a.customBlockTextureChecksum;
		state.receivedFactionConfigChecksum = a.factionConfigChecksum;
		GameClientState.SERVER_BLOCK_QUEUE_SIZE = a.segmentPieceQueueSize;
		System.err.println("[CLIENT] RECEIVED STARTING SECTOR: " + state.getInitialSectorPos());
		return a.gameMode;
	}

	public void requestInvetoriesUnblocked(int segProviderId) throws IOException {
		EntityInventoriesRequest c = new EntityInventoriesRequest();
		c.segmentControllerID = segProviderId;
		GameRequestCommandPackage pack = new GameRequestCommandPackage();
		pack.request = c;
		pack.send(state.getProcessor());
	}

	public void requestNewShip(Transform where, Vector3i min, Vector3i max, PlayerState player, String uniqueName, String realName) throws IOException {
		if (System.currentTimeMillis() - lastShipSpawn > SHIP_SPAM_PROTECT_TIME_SEC * 1000) {
			spawnEntity(EntityType.SHIP, where, min, max, player, uniqueName, realName);
			lastShipSpawn = System.currentTimeMillis();
		} else {
			popupAlertTextMessage(Lng.str("Cannot spawn!\nPlease wait %s sec...\n(spam protection)", (SHIP_SPAM_PROTECT_TIME_SEC - (int) Math.ceil((System.currentTimeMillis() - lastShipSpawn) / 1000f))), 0);
		}
	}

	public void spawnEntity(EntityType type, Transform where, Vector3i min, Vector3i max, PlayerState player, String uniqueName, String realName) throws IOException {
		EntityRequest c = new EntityRequest();
		GameRequestCommandPackage pack = new GameRequestCommandPackage();
		where.getOpenGLMatrix(c.mat);
		int pointer = 0;
		System.err.println("[CLIENT][RequestNewShip] Ship pos: " + Arrays.toString(c.mat));
		assert (min.x <= max.x);
		assert (min.y <= max.y);
		assert (min.z <= max.z);
		c.minX = min.x;
		c.minY = min.y;
		c.minZ = min.z;
		c.maxX = max.x;
		c.maxY = max.y;
		c.maxZ = max.z;
		c.ownerId = player.getId();
		c.uniqueIdentifier = uniqueName;
		c.realName = realName;
		c.type = type;
		pack.request = c;
		pack.send(state.getProcessor());
	}

	public void requestNewStation(Transform where, PlayerState player, String uniqueName, String realName) throws IOException, InterruptedException {
		spawnEntity(EntityType.SPACE_STATION, where, new Vector3i(-4, -4, -4), new Vector3i(4, 4, 4), player, uniqueName, realName);
	}

	public void requestNewVehicle(Transform where, Vector3i min, Vector3i max, PlayerState player, String uniqueName, String realName) throws IOException, InterruptedException {
		spawnEntity(EntityType.VEHICLE, where, min, max, player, uniqueName, realName);
	}

	public StarMadePlayerStats requestPlayerStats(int mode) throws IOException, UnknownCommandException {
		return ((PlayerStatsAnswer) sendBlockedRequest(new PlayerStatsRequest())).stats;
	}

	public StarMadeServerStats requestServerStats() throws IOException, UnknownCommandException {
		return ((ServerStatsAnswer) sendBlockedRequest(new ServerStatsRequest())).stats;
	}

	/**
	 * @return the sectorChanges
	 */
	public void scheduleSectorChange(SectorChange c) {
		if (!sectorChanges.contains(c)) {
			synchronized (sectorChanges) {
				sectorChanges.add(c);
			}
		}
	}

	private void scheduleWriteDataPush(final ObjectArrayList<SimpleTransformableSendableObject> toClean) {
		for (SimpleTransformableSendableObject<?> s : toClean) {
			s.setClientCleanedUp(true);
		}
		// HashSet<SimpleTransformableSendableObject> hashSet = state.getSectorCacheMap().get(sectorId);
		// if(hashSet == null ){
		// if(sectorId >= 0){
		// System.err.println("Exception: WARNING: "+sectorId+" has no result");
		// }
		// return;
		// }
		// final IntOpenHashSet currentSectorEntities = new IntOpenHashSet(27);
		//
		// synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()){
		// for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()){
		// if(s instanceof RemoteSector){
		// Vector3i pos = ((RemoteSector)s).clientPos();
		// if(
		// Math.abs(pos.x - state.getPlayer().getCurrentSector().x) < 2 ||
		// Math.abs(pos.y - state.getPlayer().getCurrentSector().y) < 2 ||
		// Math.abs(pos.z - state.getPlayer().getCurrentSector().z) < 2
		// ){
		// currentSectorEntities.add(((RemoteSector)s).getId());
		// }
		// }
		// }
		// }
		// only save stuff out of range
		final Object changeLog = new Object();
		Runnable r = () -> {
			synchronized (changeLog) {
				// System.err.println("CLIENT STARTING TO WRITE BUFFER of "+currentSectorEntities.size()+" segCo: free "+state.getSegmentDataManager().sizeFree());
				for (SimpleTransformableSendableObject s : toClean) {
					if (s instanceof SendableSegmentController) {
						try {
							((SendableSegmentController) s).writeAllBufferedSegmentsToDatabase(false, false, false);
						} catch (IOException e) {
							e.printStackTrace();
						}
						synchronized (cleanUps) {
							cleanUps.add(((SendableSegmentController) s));
						}
					}
				}
			// System.err.println("CLIENT FINISHED TO WRITE BUFFER free "+state.getSegmentDataManager().sizeFree());
			}
			updateFileList();
		};
		state.getConnectionThreadPool().execute(r);
	}

	public void sendAdminCommand(AdminCommands command, Object... parameters) {
		AdminCommandCommandPackage pack = new AdminCommandCommandPackage();
		pack.adminCommand = command;
		pack.commandParams = parameters;
		try {
			pack.send(getConnection().getClientProcessor());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setSpawnPoint(SegmentPiece piece) {
		// System.err.println("[CLIENT] setting spawn to "+origin);
		state.getPlayer().sendSimpleCommand(SimplePlayerCommands.SET_SPAWN, piece.getSegmentController().getId(), piece.getAbsoluteIndex());
	// getState().getPlayer().getNetworkObject().spawnPointSetBuffer.add(new RemoteVector3f(getState().getPlayer().getNetworkObject(), origin));
	}

	private void setupGlobalGameControlManager() {
		state.setGlobalGameControlManager(new GlobalGameControlManager(state));
		state.getGlobalGameControlManager().initialize();
		state.getGlobalGameControlManager().setActive(true);
	}

	public void showBigMessage(String id, String message, String subtitle, float timeDelayInSecs) {
		//Note by jakev: Changed ClientMessageLogType.GAME (dark blue) to Color.white, because big messages can be coloured now
		bigMessage(id, message, subtitle, timeDelayInSecs, Color.white);
		///
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.TITLE_POPUP)*/
		//AudioController.fireAudioEventID(50);
//		bigMessage(id, message, subtitle, timeDelayInSecs, ClientMessageLogType.GAME.color);
	}

	public void showServerMessage() {
		if (state.getGameState() != null && !state.isPassive()) {
			String msg = state.getGameState().getNetworkObject().serverMessage.get();
			if (msg.length() > 0) {
				state.getController().getPlayerInputs().add(new GUIMessageDialog(state, msg, Lng.str("Server Message"), false));
			}
		}
	}

	public void showBigTitleMessage(String id, String message, float timeDelayInSecs) {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.TITLE_POPUP)*/
		AudioController.fireAudioEventID(51);
		titleMessage(id, message, timeDelayInSecs, ClientMessageLogType.GAME.color);
	}

	public void showSmallBigTitleMessage(String id, String message, float timeDelayInSecs) {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.TITLE_POPUP)*/
		AudioController.fireAudioEventID(52);
		titleMessage(id, message, timeDelayInSecs, ClientMessageLogType.GAME.color);
	}

	public void spawnAndActivatePlayerCharacter() throws PlayerNotYetInitializedException {
		if (state.getPlayer() != null && state.getPlayer().getNetworkObject() != null) {
			if (!state.isWaitingForPlayerActivate()) {
				state.getPlayer().getNetworkObject().spawnRequest.add(new RemoteBoolean(state.getPlayer().getNetworkObject()));
				state.getPlayer().hasSpawnWait = true;
				state.setWaitingForPlayerActivate(true);
			}
		} else {
			throw new PlayerNotYetInitializedException();
		}
	}

	public boolean startGraphics(GraphicsContext gc) throws Exception {
		setGuiConnectionState(Lng.str("initializing OpenGL frame..."));
		MainGameGraphics g = new MainGameGraphics(state);
		state.setScene(g);
		if (currentGLFrame == null) {
			currentGLFrame = new GLFrame();
		}
		state.setGlFrame(currentGLFrame);
		setGuiConnectionState(Lng.str("Starting up graphics..."));
		setGuiConnectionState(Lng.str("DONE"));
		System.err.println("[CLIENT] GRAPHICS CONTEXT: " + gc);
		String title = "StarMade alpha v" + VersionContainer.VERSION + " (" + VersionContainer.build + ") [" + state.getGameMode().name() + "]; " + (VersionContainer.is64Bit() ? "64bit" : "32bit");
		assert (gc != null);

		System.err.println("[STARTUP] SETTING GRAPHICS FRAME, WHICH RUNS IN DIFFERENT THREAD");
		Controller.setLoadMessage(Lng.str("setting up graphics"));
		currentGLFrame.setState(state, g);
		if (gc != null) {
			gc.setFrame(currentGLFrame, null);
		}
		this.graphicsContext = gc;
		return false;
	// }
	}

	public boolean suicide() {
		if (state.getPlayer() != null) {
			state.getPlayer().suicideOnClient();
			return true;
		}
		return false;
	}

	public void timeOutBigMessage(String id) {
		synchronized (state.getWorldDrawer().getGuiDrawer().bigMessages) {
			for (BigMessage p : state.getWorldDrawer().getGuiDrawer().bigMessages) {
				if (p.getId().equals(id) || p.getId().equals(id.replaceAll(" ", ""))) {
					// the message is already on screen, so restart message
					p.timeOut();
					return;
				}
			}
		}
	}

	public void timeOutBigTitleMessage(String id) {
		synchronized (state.getWorldDrawer().getGuiDrawer().titleMessages) {
			for (BigTitleMessage p : state.getWorldDrawer().getGuiDrawer().titleMessages) {
				if (p.getId().equals(id) || p.getId().equals(id.replaceAll(" ", ""))) {
					// the message is already on screen, so restart message
					p.timeOut();
					return;
				}
			}
		}
	}

	private void titleMessage(String id, String message, float timeDelayInSecs, Color color) {
		synchronized (state.getWorldDrawer().getGuiDrawer().titleMessages) {
			for (BigTitleMessage p : state.getWorldDrawer().getGuiDrawer().titleMessages) {
				if (p.getId().equals(id) || p.getId().equals(id.replaceAll(" ", ""))) {
					// the message is already on screen, so restart message
					p.setMessage(message);
					p.restartPopupMessage();
					return;
				}
			}
			BigTitleMessage titleMessage = new BigTitleMessage(id, state, message, color);
			state.getWorldDrawer().getGuiDrawer().titleMessages.addFirst(titleMessage);
			titleMessage.startPopupMessage(timeDelayInSecs);
		}
	}

	public void reapplyBlockConfigInstantly() throws IOException {
		System.err.println("[CLIENT] a new block behavior has been received. Applying to all entities");
		for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if (s instanceof ManagedSegmentController<?>) {
				((ManagedSegmentController<?>) s).getManagerContainer().reparseBlockBehavior(true);
			}
		}
		popupInfoTextMessage(Lng.str("New block behavior config\nhas been applied."), 0);
	}

	public void updateActiveControllers() {
		InGameControlManager ingameControlManager = state.getGlobalGameControlManager().getIngameControlManager();
		PlayerGameControlManager playerGameControlManager = ingameControlManager.getPlayerGameControlManager();
		boolean menuWasActive = state.getUpdateTime() - state.getController().inputController.getLastDeactivatedMenu() < PlayerInteractionControlManager.MENU_DELAY_MS;
		boolean notHindered = (state.getUpdateTime() - state.getHinderedInputTime() > state.getHinderedInput());
		boolean noActive = state.getController().getPlayerInputs().isEmpty();
		state.getPlayer().getNetworkObject().activeControllerMask.forceClientUpdates();
		state.getPlayer().getNetworkObject().activeControllerMask.set(AbstractControlManager.CONTROLLER_PLAYER_EXTERN, !ingameControlManager.isAnyMenuOrChatActive() && !menuWasActive && playerGameControlManager.getPlayerIntercationManager().getPlayerCharacterManager().isTreeActiveAndNotSuspended() && noActive && notHindered);
		PlayerInteractionControlManager pi = playerGameControlManager.getPlayerIntercationManager();
		ShipExternalFlightController ext = pi.getInShipControlManager().getShipControlManager().getShipExternalFlightController();
		state.getPlayer().getNetworkObject().activeControllerMask.set(AbstractControlManager.CONTROLLER_SHIP_EXTERN, !ingameControlManager.isAnyMenuOrChatActive() && !menuWasActive && ((ext.isTreeActiveAndNotSuspended() && ext.isTreeActiveInFlight()) || pi.getSegmentControlManager().getSegmentExternalController().isTreeActiveAndNotSuspended()) && noActive && notHindered);
		state.getPlayer().getNetworkObject().activeControllerMask.set(AbstractControlManager.CONTROLLER_SHIP_BUILD, !menuWasActive && playerGameControlManager.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActiveAndNotSuspended() && noActive && notHindered);
	}

	private void updateAmbientSound(Timer timer) {
		Ship ship = state.getShip();
	// if(ship != null){
	// if(Controller.getCamera() instanceof InShipCamera && Controller.getCamera().getCameraOffset() < 1){
	// if(ship.getShipAudioContainer().coreSound != null){
	// ship.getShipAudioContainer().coreSound.pause();
	// }
	// if(ship.getShipAudioContainer().inShipSound != null){
	// ship.getShipAudioContainer().inShipSound.resume();
	// }else{
	// ship.getShipAudioContainer().inShipSound =
	// Controller.queueLoopedTransformableAudio(
	// "0022_ambience loop - interior cockpit (loop)", ship.getWorldTransform(), 16, ship);
	// }
	// }else{
	// if(ship.getShipAudioContainer().coreSound != null){
	// ship.getShipAudioContainer().coreSound.resume();
	// }
	// if(ship.getShipAudioContainer().inShipSound != null){
	// ship.getShipAudioContainer().inShipSound.pause();
	// }
	// }
	// }
	}

	public void updateCurrentControlledEntity(ControllerState o) {
		assert (o != null);
		assert (o.getOwner() != null);
		if (o.getOwner().getClientId() == state.getId()) {
			state.setCurrentPlayerObject(null);
			for (ControllerStateUnit u : o.getUnits()) {
				if (u.playerControllable instanceof SimpleTransformableSendableObject) {
					state.setCurrentPlayerObject((SimpleTransformableSendableObject) u.playerControllable);
					System.err.println("[CLIENT] CURRENT MAIN CONTROLLING IS NOW: " + state.getCurrentPlayerObject() + "; at: " + state.getCurrentPlayerObject().getWorldTransform().origin);
					return;
				}
			}
		// System.err.println("[CLIENT] CURRENT MAIN CONTROLLING IS NOW:  "+state.getCurrentPlayerObject());
		}
	}

	public void updateMouseGrabbed() {
		boolean chat = state.getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive();
		boolean freeRoam = state.getGlobalGameControlManager().getIngameControlManager().getFreeRoamController().isActive();
		boolean player = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().isActive();
	}

	private void updateOthers(Timer timer) throws IOException {
		onlinePlayerMapHelper.putAll(state.getOnlinePlayersLowerCaseMap());
		boolean chagedOnlinePlayers = false;
		for (ShopInterface s : shopInterfaces) {
			s.getShoppingAddOn().update(timer.currentTime);
		}
		for (Sendable s : state.getOtherSendables()) {
			long tS = System.currentTimeMillis();
			assert (!(s instanceof SimpleTransformableSendableObject)) : s;
			s.updateLocal(timer);
			if (s instanceof PlayerState) {
				PlayerState put = state.getOnlinePlayersLowerCaseMap().put((((PlayerState) s).getName().toLowerCase(Locale.ENGLISH)), (PlayerState) s);
				if (put == null) {
					chagedOnlinePlayers = true;
				}
				onlinePlayerMapHelper.remove((((PlayerState) s).getName().toLowerCase(Locale.ENGLISH)));
				if (((PlayerState) s).getBuildModePosition().isSpotLightOn()) {
					state.spotlights.add(((PlayerState) s).getBuildModePosition());
				}
			}
			long took = System.currentTimeMillis() - tS;
			if (took > 50) {
				System.err.println("[CLIENT] WARNING: UPDATE OF (OTHER) " + s + " took " + took);
			}
		}
		if (onlinePlayerMapHelper.size() > 0) {
			chagedOnlinePlayers = true;
			for (String key : onlinePlayerMapHelper.keySet()) {
				state.getOnlinePlayersLowerCaseMap().remove(key);
			}
		}
		onlinePlayerMapHelper.clear();
		if (chagedOnlinePlayers) {
			onOnlinePlayersChanged();
		}
	}

	private void onOnlinePlayersChanged() {
		if (state.getWorldDrawer() != null && state.getWorldDrawer().getGuiDrawer() != null && state.getWorldDrawer().getGuiDrawer().getPlayerStatisticsPanel() != null) {
			state.getWorldDrawer().getGuiDrawer().getPlayerStatisticsPanel().playerListUpdated();
		}
	}

	public void updateSector(SimpleTransformableSendableObject<?> a, int from) {
		if (a.getPhysicsDataContainer().getObject() != null && a.getPhysicsDataContainer().isInitialized()) {
			// System.err.println("[CLIENT] UPDATING SECTOR FOR " + a + " FROM " + from + " TO " + a.getSectorId());
			a.onPhysicsRemove();
			if (!a.isHidden()) {
				a.onPhysicsAdd();
			}
		}
	}

	public void updateTutorialMode(Timer timer) {
		if (Controller.getResLoader().isLoaded() && tutorialStarted) {
			if (this.tutorialMode == null) {
				state.getTutorialAIState().setCurrentProgram(this.tutorialMode = new TutorialMode(state.getTutorialAIState()));
			}
		}
		if (this.tutorialMode != null && (EngineSettings.TUTORIAL_NEW.isOn() || ((this.tutorialMode.getMachine() instanceof DynamicTutorialStateMachine) && !(this.tutorialMode.isInStartMachine())))) {
			tutorialMode.suspend(!state.getGlobalGameControlManager().getIngameControlManager().isActive());
			if (state.isPlayerSpawned()) {
				try {
					state.getTutorialAIState().updateOnActive(timer);
				} catch (FSMException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void writeSegmentDataToDatabase(boolean forceWriteUnchanged) throws Exception {
		final ArrayList<SendableSegmentController> toWrite = new ArrayList<SendableSegmentController>();
		synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (final Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if (s instanceof SendableSegmentController) {
					// System.err.println("[CLIENT] Writing to disk: "+s);
					((SendableSegmentController) s).writeAllBufferedSegmentsToDatabase(false, true, forceWriteUnchanged);
					toWrite.add(((SendableSegmentController) s));
				}
			}
		}
		state.getConnectionThreadPool().execute(() -> {
			for (SendableSegmentController s : toWrite) {
				IOFileManager.writeAllOpenFiles(s.getSegmentProvider().getSegmentDataIO().getManager());
			}
		});
		updateFileList();
	}

	/**
	 * @return the state
	 */
	@Override
	public GameClientState getState() {
		return state;
	}

	// public boolean isMouseButtonDown(int i) {
	// if (Mouse.isButtonDown(i)) {
	// return true;
	// } else if (isJoystickOk()) {
	// if (i == 0) {
	// return isJoystickMouseLeftButtonDown();
	// } else if (i == 1) {
	// return isJoystickMouseRigthButtonDown();
	// }
	// }
	// return false;
	// }
	@Override
	public void parseBlockBehavior(String path) throws IOException {
		state.setBlockBehaviorConfig(XMLTools.loadXML(new FileExt(path)));
		state.setBlockBehaviorCheckSum(FileUtil.getSha1Checksum(path));
	}

	public void reapplyBlockBehavior() {
		flagReapplyBlockBehavior = true;
	}

	private static class StringDistance {

		String string;

		int distance;

		StringDistance(String string, int distance) {
			this.string = string;
			this.distance = distance;
		}
	}

	private class PhysicsCallback extends InternalTickCallback {

		@Override
		public void internalTick(DynamicsWorld dyncapicsWorld, float timeStep) {
			int numManifolds = getState().getPhysics().getDynamicsWorld().getDispatcher().getNumManifolds();
			for (int i = 0; i < numManifolds; i++) {
				PersistentManifold contactManifold = getState().getPhysics().getDynamicsWorld().getDispatcher().getManifoldByIndexInternal(i);
				CollisionObject obA = (CollisionObject) (contactManifold.getBody0());
				CollisionObject obB = (CollisionObject) (contactManifold.getBody1());
				int numContacts = contactManifold.getNumContacts();
				if (obA != null && obB != null && obA.getUserPointer() instanceof Integer && obB.getUserPointer() instanceof Integer) {
					int aId = (Integer) obA.getUserPointer();
					int bId = (Integer) obB.getUserPointer();
					Sendable sendableA = null;
					Sendable sendableB = null;
					sendableA = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(aId);
					sendableB = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(bId);
					Vector3f ptAtmp = new Vector3f();
					Vector3f ptBtmp = new Vector3f();
					if (sendableA instanceof Collisionable && sendableB instanceof Collisionable) {
						Collisionable a = ((Collisionable) sendableA);
						Collisionable b = ((Collisionable) sendableB);
						boolean aCol = a.needsManifoldCollision();
						boolean bCol = b.needsManifoldCollision();
						if (aCol || bCol) {
							for (int j = 0; j < numContacts; j++) {
								ManifoldPoint pt = contactManifold.getContactPoint(j);
								if (pt.getDistance() < 0.f && obA.getUserPointer() != null && obB.getUserPointer() != null) {
									Vector3f ptA = pt.getPositionWorldOnA(ptAtmp);
									Vector3f ptB = pt.getPositionWorldOnB(ptBtmp);
									Vector3f normalOnB = pt.normalWorldOnB;
									if (aCol) {
										a.onCollision(pt, sendableB);
									}
									if (bCol) {
										b.onCollision(pt, sendableA);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public List<TransporterDestinations> getActiveTransporterDestinations(SegmentController from) {
		transporterDestinations.clear();
		for (Sendable s : state.getCurrentSectorEntities().values()) {
			if (s instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) s).getManagerContainer() instanceof TransporterModuleInterface) {
				TransporterModuleInterface c = (TransporterModuleInterface) ((ManagedSegmentController<?>) s).getManagerContainer();
				for (TransporterCollectionManager t : c.getTransporter().getCollectionManagers()) {
					if (t.isValid() && (t.isPublicAccess() || (t.isFactionAccess() && t.getFactionId() == from.getFactionId()) || t.getSegmentController().railController.isInAnyRailRelationWith(from))) {
						TransporterDestinations td = new TransporterDestinations();
						td.name = t.getTransporterName();
						td.pos = new Vector3i(t.getControllerPos());
						td.target = t.getSegmentController();
						transporterDestinations.add(td);
					}
				}
			}
		}
		return transporterDestinations;
	}

	public List<SegmentController> getPossibleFleetAdd() {
		possibleFleet.clear();
		for (Sendable s : state.getCurrentSectorEntities().values()) {
			if (s instanceof Ship) {
				Ship ship = (Ship) s;
				Fleet fleet = state.getFleetManager().getByEntity(ship);
				if (fleet == null && (!state.getGameState().isOnlyAddFactionToFleet() || (ship.getFactionId() != 0 && state.getPlayer().getFactionId() == ship.getFactionId())) && state.getController().isNeighborToClientSector(ship.getSectorId()) && ship.allowedToEdit(state.getPlayer())) {
					possibleFleet.add(ship);
				}
			}
		}
		return possibleFleet;
	}

	public void onStopClient() {
		creatorThreadController.onStopClient();
		elementCollectionCalculationThreadManager.onStop();
		if (clientChannel != null) {
			clientChannel.onStopClient();
		}
		if (state.getThreadedSegmentWriter() != null) {
			state.getThreadedSegmentWriter().shutdown();
		}
		if (state.getWorldDrawer() != null) {
			state.getWorldDrawer().onStopClient();
		}
		if (state.getGameState() != null) {
			state.getGameState().onStop();
		}
		state.setDoNotDisplayIOException(true);
		state.setExitApplicationOnDisconnect(false);
		// state.setDoNotDisplayIOException(true);
		if (getConnection() != null) {
			getConnection().disconnect();
		}
		IOFileManager.cleanUp(false);
	}

	@Override
	public List<DialogInterface> getPlayerInputs() {
		return inputController.getPlayerInputs();
	}

	@Override
	public boolean isChatActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e, Timer timer) {
		if (!GLFrame.activeForInput) {
			System.err.println("Not active for input");
			return;
		}
		if(e.isPressed()) {
			state.getGlobalGameControlManager().handleKeyEvent(e);
			tutorialController.handleKeyEvent(e);
		}
		// handle input for player
		if (state.getPlayer() != null) {
			List<KeyboardMappings> triggeredMappings = e.getTriggeredMappings();
			for (KeyboardMappings m : triggeredMappings) {
				state.getPlayer().tempSeed++;
				// handle input for GUI/Controllers
				state.getPlayer().getControllerState().handleKeyEvent(m, timer);
			}
		}
		if (zoom > 0 && e.isTriggered(KeyboardMappings.SHIP_ZOOM)) {
			boolean b = !state.getWorldDrawer().getGameMapDrawer().isMapActive();
			if (AbstractScene.getZoomFactorUnchecked() == zoom) {
				AbstractScene.setZoomFactorForRender(b, 1.0F);
			} else {
				AbstractScene.setZoomFactorForRender(b, zoom);
			}
		}
	}

	@Override
	public void handleCharEvent(KeyEventInterface e) {
	// nothing to do. char is already handled from callbacks
	}

	@Override
	public void handleLocalMouseInput() {
		state.getPlayer().handleRecoilAndSendMouseCameraInput();
	}

	@Override
	public boolean beforeInputUpdate() {
		if (!GameClientController.hasGraphics(state)) {
			return false;
		}
		boolean characterActive = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getPlayerCharacterManager().isTreeActive() && !state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getPlayerCharacterManager().isSuspended();
		zoom = -1;
		if (characterActive && state.getPlayer() != null) {
			int selectedBuildSlot = state.getPlayer().getSelectedBuildSlot();
			int metaId = state.getPlayer().getInventory().getMeta(selectedBuildSlot);
			if (metaId != -1) {
				MetaObject r = state.getMetaObjectManager().getObject(metaId);
				if (r != null) {
					zoom = r.hasZoomFunction();
				}
			}
		} else if (state.isInFlightMode() && state.getCurrentPlayerObject() instanceof ManagedSegmentController<?>) {
			// gonna be -1 for non zoomable weapons/selectable
			zoom = ((ManagedSegmentController<?>) state.getCurrentPlayerObject()).getManagerContainer().getSelectedWeaponZoom(state.getPlayer());
		}
		// reset the zoom factor if current selection doesn't support zoom
		// if it does support zoom, the zoomofactor is set directly in onMouseEvent and not changed here
		if (zoom <= 0) {
			// reset zoom
			boolean b = !state.getWorldDrawer().getGameMapDrawer().isMapActive();
			AbstractScene.setZoomFactorForRender(b, 1.0F);
		}
		assert (state.getPlayer() != null);
		state.getPlayer().updateNTJoystick();
		int roundEndIndex = state.getPlayerInputs().size() - 1;
		for (int i = 0; i < state.getPlayerInputs().size(); i++) {
			if (state.getPlayerInputs().get(i) instanceof RoundEndMenu) {
				roundEndIndex = i;
				break;
			}
		}
		int lastIndex = state.getPlayerInputs().size() - 1;
		if (!state.getPlayerInputs().isEmpty() && state.getPlayerInputs().get(lastIndex) instanceof MainMenu) {
			lastIndex--;
		}
		// switch round end to the top
		if (roundEndIndex < lastIndex) {
			DialogInterface p = state.getPlayerInputs().get(lastIndex);
			state.getPlayerInputs().set(lastIndex, state.getPlayerInputs().get(roundEndIndex));
			state.getPlayerInputs().set(roundEndIndex, p);
		}
		state.getGlobalGameControlManager().activateDelayed();
		PlayerGameControlManager playerGameControlManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
		// FIXME this can happen: reproducable by deleting (destroy_entity) own ship (when in it)
		assert (!(playerGameControlManager.getPlayerIntercationManager().getInShipControlManager().isActive() && playerGameControlManager.getPlayerIntercationManager().getPlayerCharacterManager().isActive()));
		return true;
	}

	@Override
	public BasicInputController getInputController() {
		return inputController;
	}

	public JoystickMappingFile getJoystick() {
		return inputController.getJoystick();
	}

	public TutorialController getTutorialController() {
		return tutorialController;
	}

	public static boolean isStarted() {
		return started;
	}

	public static void setStarted(boolean started) {
		GameClientController.started = started;
	}

	private boolean wasWindowActive = false;

	private short wasWindowActiveUNum = 0;

	public boolean isWindowActive() {
		if (state.getNumberOfUpdate() != wasWindowActiveUNum) {
			wasWindowActive = GameClientController.hasGraphics(state) && GraphicsContext.isCurrentFocused() && Keyboard.isCreated();
			wasWindowActiveUNum = state.getNumberOfUpdate();
		}
		return wasWindowActive;
	}

	// private boolean wasWindowActiveOutOfMenu = false;
	// private short wasWindowActiveOutOfMenuUNum = 0;
	// public boolean isWindowActiveOutOfMenu() {
	// if(getState().getNumberOfUpdate() != wasWindowActiveOutOfMenuUNum){
	// wasWindowActiveOutOfMenu = isWindowActive() && outOfMenu();
	// wasWindowActiveOutOfMenuUNum = getState().getNumberOfUpdate();
	// }
	// return wasWindowActiveOutOfMenu;
	// }
	// public boolean outOfMenu() {
	// PlayerInteractionControlManager playerIntercationManager = getState()
	// .getGlobalGameControlManager()
	// .getIngameControlManager()
	// .getPlayerGameControlManager()
	// .getPlayerIntercationManager();
	// return !playerIntercationManager.isSuspended()
	// && playerIntercationManager.isActive()); && System.currentTimeMillis() - playerIntercationManager.getSuspentionFreedTime() > 400;
	// }
	public MineController getMineController() {
		return mineController;
	}

	@Override
	public MissileManagerInterface getMissileManager() {
		return clientMissileManager;
	}

	public void onSelectedEntityChanged(SimpleTransformableSendableObject<?> old, SimpleTransformableSendableObject selectedEntity) {
		for (EntitySelectionChangeChangeListener e : entitySelectionListeners) {
			e.onEntityChanged(old, selectedEntity);
		}
	}

	@Override
	public void consumeIntputs() {
		inputController.consume();
	}
}
