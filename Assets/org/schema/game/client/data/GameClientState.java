package org.schema.game.client.data;

import api.listener.events.world.generation.GalaxyInstantiateEvent;
import api.mod.ModStarter;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.StringTools;
import org.schema.common.util.Version;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GUIController;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.PlayerLagStatsInput;
import org.schema.game.client.controller.manager.GlobalGameControlManager;
import org.schema.game.client.controller.manager.ingame.BlockSyleSubSlotController;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.PowerChangeListener.PowerChangeType;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.game.client.view.Segment2ObjWriter;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.WorldDrawer;
import org.schema.game.client.view.gui.lagStats.LagDataStatsList;
import org.schema.game.common.Starter;
import org.schema.game.common.ThreadedSegmentWriter;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.activities.RaceManager;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ParticleHandler;
import org.schema.game.common.controller.elements.PulseController;
import org.schema.game.common.controller.elements.PulseHandler;
import org.schema.game.common.controller.gamemodes.GameModes;
import org.schema.game.common.controller.generator.ClientCreatorThread;
import org.schema.game.common.crashreporter.CrashReporter;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.blockeffects.config.ConfigPoolProvider;
import org.schema.game.common.data.chat.ChannelRouter;
import org.schema.game.common.data.element.ControlElementMapOptimizer;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.event.EventFactory;
import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.common.data.fleet.FleetStateInterface;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.PlayerCatalogManager;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.tech.Technology;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.network.objects.ChatMessage;
import org.schema.game.network.objects.ChatMessage.ChatMessageType;
import org.schema.game.server.data.*;
import org.schema.game.server.data.GameServerState.PlayerAttachedInterface;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.common.DebugTimer;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.movie.subtitles.Subtitle;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.IdGen;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.client.HostPortLoginName;
import org.schema.schine.network.commands.GameRequestAnswerCommandPackage;
import org.schema.schine.network.commands.SynchronizeAllCommandPackage;
import org.schema.schine.network.commands.SynchronizePrivateCommandPackage;
import org.schema.schine.network.commands.SynchronizePublicCommandPackage;
import org.schema.schine.network.common.*;
import org.schema.schine.network.exception.SynchronizationException;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.synchronization.SynchronizationReceiver;
import org.schema.schine.physics.Physics;
import org.schema.schine.physics.PhysicsState;
import org.schema.schine.resource.ResourceMap;
import org.schema.schine.sound.controller.RemoteAudioEntry;
import org.w3c.dom.Document;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class GameClientState extends ClientState implements MetaObjectState, FleetStateInterface, GameStateInterface, GravityStateInterface, PhysicsState, SegmentManagerInterface, FactionState, CatalogState, ParticleHandler, PulseHandler, RaceManagerState, ConfigPoolProvider{

	
	public static final ByteArrayOutputStream SEGMENT_BYTE_ARRAY_BUFFER = new ByteArrayOutputStream(100 * 1024);
	private static final String prefixes[] = new String[]{"//", "/"};
	public static int SERVER_BLOCK_QUEUE_SIZE = -1;
	public static long totalMemory;
	public static long freeMemory;
	public static long takenMemory;
	public static int allocatedSegmentData;
	public static int lastAllocatedSegmentData;
	public static int lastFreeSegmentData;
	public static int requestQueue;
	public static int requestedSegments;
	public static int returnedRequests;
	public static int clientCreatorThreadIterations;
	public static Long dataReceived = 0L;
//	public static Deflater deflater = new Deflater();
//	public static Inflater INFLATER = new Inflater();
	private final List<RailDockingListener> dockingListeners = new ObjectArrayList<RailDockingListener>();
	private final List<PowerChangeListener> powerChangeListeners = new ObjectArrayList<PowerChangeListener>();
	private final EventFactory eventFactory;
	private final Long2ObjectOpenHashMap<PlayerState> playerStatesByDbId = new Long2ObjectOpenHashMap<PlayerState>();
	;
	
	public final ObjectArrayList<Sendable> laggyList = new ObjectArrayList<Sendable>();
	
	public static int singleplayerCreativeMode = -1;
	public static final int CREATIVE_MODE_ON = 1;
	public static final int CREATIVE_MODE_OFF = 2;
	
	public static int collectionUpdates;
	public static GameClientState instance;
	public static int drawnSegements;
	public static int realVBOSize;
	public static int prospectedVBOSize;
	public static int debugSelectedObject = -1;
	public static float avgBlockLightTime;
	public static float avgBlockLightLockTime;
	public static int staticSector;
	public static boolean instanced;
	public static boolean smoothDisableDebug;
//	private static FastByteArrayOutputStream byteArrayOutput = new FastByteArrayOutputStream(4 * 1024);
//	private static byte[] byteArray = new byte[4 * 1024];
	public Segment2ObjWriter exportingShip;

	
	private final MetaObjectManager metaObjectManager;
	private final Object2ObjectOpenHashMap<Vector3i, RemoteSector> loadedSectors = new Object2ObjectOpenHashMap<Vector3i, RemoteSector>();
	private final Map<String, PlayerState> onlinePlayersLowerCaseMap = new Object2ObjectOpenHashMap<String, PlayerState>();
	private final ClientMessageLog messageLog = new ClientMessageLog();
	private final IntArrayFIFOQueue toRequestMetaObjects = new IntArrayFIFOQueue();
	
	private final ThreadedSegmentWriter threadedSegmentWriter = new ThreadedSegmentWriter("CLIENT");
	private final HashSet<Sendable> flaggedAddedObjects = new HashSet<Sendable>();
	private final HashSet<Sendable> flaggedRemovedObjects = new HashSet<Sendable>();
	private final HashSet<Sendable> otherSendables = new HashSet<Sendable>();
	private final ChannelRouter channelRouter;
	private final AiEntityState tutorialAIState;
	private final ObjectArrayFIFOQueue<ByteBuffer> bufferPool = new ObjectArrayFIFOQueue<ByteBuffer>(5);
	private final Int2ObjectOpenHashMap<SimpleTransformableSendableObject<?>> currentSectorEntities = new Int2ObjectOpenHashMap<SimpleTransformableSendableObject<?>>();
	private final ObjectArrayList<SimpleTransformableSendableObject> currentGravitySources = new ObjectArrayList<SimpleTransformableSendableObject>();
	private final Short2ObjectOpenHashMap<Technology> techs = new Short2ObjectOpenHashMap<Technology>();
	private final Object2ObjectOpenHashMap<Vector3i, Galaxy> surrounding = new Object2ObjectOpenHashMap<Vector3i, Galaxy>();
	public String receivedBlockConfigPropertiesChecksum;
	public String receivedBlockConfigChecksum;
	public String receivedFactionConfigChecksum;
	public String receivedBlockBehaviorChecksum;
	public SegmentController currentEnterTry;
	public long currentEnterTryTime;
	public String receivedCustomTexturesChecksum;
	public long updateTime;
	public List<LightTransformable> spotlights = new ObjectArrayList<LightTransformable>();
	byte[] buffer = new byte[1024 * 1024]; // 1mbBuffer
	boolean wasInShopDistance = false;
	private Document blockBehaviorConfig;
	private float highestSubStep;
	private Physics physics;
	private WorldDrawer worldDrawer;
	private GameClientController controller;
	private GLFrame glFrame;
	private MainGameGraphics scene;
	private GUIController guiController;
	private GlobalGameControlManager globalGameControlManager;
	private Map<Short, RemoteSegment> requestingSegments;
	private SendableGameState gameState;
	private long lastServerTimeRequest;
	private Ship ship;
	private PlayerState player;
	private SimpleTransformableSendableObject currentPlayerObject;
	private PlayerCharacter character;
	private EditableSendableSegmentController planetTestSurface;
	private SegmentDataManager segmentDataManager;
	private String playerName;
	private boolean waitingForPlayerActivate;
	private GameModes gameMode = null;
	private boolean requestServerTimeFlag;
	private boolean dbPurgeRequested;
	private Integer initialSectorId = -2;
	private Vector3i initialSectorPos = new Vector3i(-1, -1, -1);
	private int flagSectorChange = -1;
	private ProjectileController particleController;
	private PulseController pulseController;
	private ShopInterface currentClosestShop;
	private boolean playerSpawned;
	private boolean flagPlayerReceived;
	private String configPropertiesCheckSum;
	private String configCheckSum;
	private String factionConfigCheckSum;
	private boolean warped;
	private short warpedUpdateNr;
	private boolean physicalAsteroids;
	private int hinderedInput;
	private long hinderedInputTime;
	private String blockBehaviorCheckSum;
	private Vector3i tmpStellar = new Vector3i();
	private Vector3i tmpGalaxy = new Vector3i();
	private Galaxy currentGalaxy;
	private String customTexturesCheckSum;
	private boolean synched;
	private ControlElementMapOptimizer controlOptimizer = new ControlElementMapOptimizer();
	public final LagDataStatsList lagStats = new LagDataStatsList(new Vector4f(1,0,0,1));
	private final FleetManager fleetManager;
	public final ObjectArrayFIFOQueue<ManagerContainer<?>> unloadedInventoryUpdates = new ObjectArrayFIFOQueue<ManagerContainer<?>>();
	private List<Subtitle> activeSubtitles;
	private final BlockSyleSubSlotController blockSyleSubSlotController = new BlockSyleSubSlotController();
	private Exception currentE;
	public final List<PlayerAttachedInterface> playerAttachedListeners = new ObjectArrayList<GameServerState.PlayerAttachedInterface>();
	private final GameClientNetworkSettings settings;
	private final GameNetworkManager networkManager;
	private final DebugTimer debugTimer = new DebugTimer();
	private final UpdateSynch updateSynch;
	public GameClientState() {
		this(false);
	}
	public GameClientState(boolean passive) {
		super(passive);
		for (int i = 0; i < 5; i++) {
			ByteBuffer byteBuffer = MemoryUtil.memAlloc(1024 * 1024); // 1mbBuffer
			bufferPool.enqueue(byteBuffer);
		}
		this.settings = new GameClientNetworkSettings();
		this.networkManager = new GameNetworkManager(this);
		this.updateSynch = new UpdateSynch();
		segmentDataManager = new SegmentDataManager(this);
		requestingSegments = new HashMap<Short, RemoteSegment>();
		metaObjectManager = new MetaObjectManager(this);
		tutorialAIState = new AiEntityState("tutorialAIState", this);

		channelRouter = new ChannelRouter(this);
		this.fleetManager = new FleetManager(this);
		
		ManagerContainer.onClientStartStatic();
		instance = this;
		
		blockSyleSubSlotController.load();
		
		eventFactory = new EventFactory(this);
	}


	public static boolean isDebugObject(SimpleGameObject s) {
		return s != null && debugSelectedObject > 0 && s.getAsTargetId() == debugSelectedObject;
	}

	public static boolean isCreated() {
		return instanced;
	}


	public void addRequestingSegment(short packetId, RemoteSegment segment) {
		synchronized (requestingSegments) {
			assert (!requestingSegments.containsKey(packetId)) : "double packetId";
			requestingSegments.put(packetId, segment);
		}
	}
	public SimpleTransformableSendableObject<?> getSelectedEntity(){
		return globalGameControlManager.getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
	}
	@Override
	public void chat(String from, String message, String prefix, boolean displayName) {
		Vector4f color = new Vector4f(1, 1, 1, 1);

		String name = from;

		if (prefix.startsWith("[PM to")) {
			try {
				String oName = prefix.replaceAll("\\[PM to", "").replaceAll("\\]", "").trim();
				System.err.println("PM SENDING FROM: " + name + " to " + oName);
				ClientMessageLogEntry clientMessageLogEntry = new ClientMessageLogEntry(playerName, oName,
						message, System.currentTimeMillis(), ClientMessageLogType.CHAT_PRIVATE_SEND);
				messageLog.log(clientMessageLogEntry);
				messageLog.logPrivate(oName, clientMessageLogEntry);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (prefix.startsWith("[PM]")) {
			String oName = message.replaceFirst("\\[", "").replaceFirst("\\]", " ").split(" ")[0].trim();
			ClientMessageLogEntry clientMessageLogEntry = new ClientMessageLogEntry(name, playerName,
					message, System.currentTimeMillis(), ClientMessageLogType.CHAT_PRIVATE);
			messageLog.log(clientMessageLogEntry);
			System.err.println("PM RECEIVED: " + prefix + "; " + oName + " " + name + "; " + playerName);
			messageLog.logPrivate(oName, clientMessageLogEntry);
			color.set(0.5f, 0.5f, 1f, 1f);
		} else if (prefix.startsWith("[FACTION]")) {
			ClientMessageLogEntry clientMessageLogEntry = new ClientMessageLogEntry(name, playerName,
					message, System.currentTimeMillis(), ClientMessageLogType.CHAT_FACTION);
			messageLog.log(clientMessageLogEntry);
			messageLog.logFaction(clientMessageLogEntry);
			color.set(0.5f, 1.0f, 0.5f, 1f);
		} else if (prefix.startsWith("[SERVER]") || prefix.startsWith("[MESSAGE]")) {
			ClientMessageLogEntry clientMessageLogEntry = new ClientMessageLogEntry(name, playerName,
					message, System.currentTimeMillis(), ClientMessageLogType.INFO);
			messageLog.log(clientMessageLogEntry);
			color.set(1.0f, 0.5f, 0.5f, 1f);
		} else {
			ClientMessageLogEntry clientMessageLogEntry = new ClientMessageLogEntry(name, playerName,
					message, System.currentTimeMillis(), ClientMessageLogType.CHAT_PUBLIC);
			messageLog.log(clientMessageLogEntry);
		}
		String chatString;

		StringBuilder b = new StringBuilder();
		if (prefix.length() > 0) {
			b.append(prefix).append(" ");
		}
		if (displayName) {
			b.append(name).append(": ");
		}
		b.append(message);
		chatString = b.toString();

		int len = 56;
		chatString = StringTools.wrap(chatString, len);

//		for (int i = 0; i < getChatListeners().size(); i++) {
//			getChatListeners().get(i).notifyOfChat(chatString, color);
//		}
		System.err.println("[CLIENT][CHAT] " + chatString);
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.receiverType = ChatMessageType.SYSTEM;
		chatMessage.receiver = this.playerName;
		if (displayName) {
			chatMessage.sender = name;
		} else {
			chatMessage.sender = "";
		}
		chatMessage.text = message;
		chatMessage.reset();
//		chatMessage.getStartColor().set(color);
		getGeneralChatLog().add(chatMessage);

		List<Object> visibleChatLog = getVisibleChatLog();
		visibleChatLog.add(0, new ChatMessage(chatMessage));
		while (visibleChatLog.size() > 8) {
			visibleChatLog.remove(visibleChatLog.size() - 1);
		}
	}


	@Override
	public String[] getCommandPrefixes() {
		return prefixes;
	}

	@Override
	public byte[] getDataBuffer() {
		return buffer;
	}

	@Override
	public ByteBuffer getDataByteBuffer() {
		synchronized (bufferPool) {
			while (bufferPool.isEmpty()) {
				try {
					bufferPool.wait(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return bufferPool.dequeue();
		}
	}

	@Override
	public Version getVersion() {
		return VersionContainer.VERSION;
	}


	@Override
	public void notifyOfAddedObject(Sendable sendable) {
		if (sendable instanceof SegmentController) {
			SegmentController c = ((SegmentController) sendable);
			if (c.getCreatorThread() == null) {
				c.setCreatorThread(new ClientCreatorThread(c));
			}
		}
		synchronized (flaggedAddedObjects) {
			flaggedAddedObjects.add(sendable);
		}
	}

	@Override
	public void notifyOfRemovedObject(Sendable sendable) {

		synchronized (flaggedRemovedObjects) {
			flaggedRemovedObjects.add(sendable);
		}
	}

	@Override
	public String onAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
		return controller.onAutoComplete(s, callback, prefix);
	}


	@Override
	public void onStringCommand(String subSequence, TextCallback callback, String prefix) {
		controller.onStringCommand(subSequence, callback, prefix);

	}

	@Override
	public void releaseDataByteBuffer(ByteBuffer buffer) {
		synchronized (bufferPool) {
			bufferPool.enqueue(buffer);
			bufferPool.notify();
		}
	}

	@Override
	public ResourceMap getResourceMap() {
		return Controller.getResLoader().getResourceMap();
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	public void setSynched() {
		assert (!synched):printLast();
//		assert(setLastSynch());
		synched = true;
	}

	private String printLast() {
		if(currentE != null){
			System.err.println("CURRENTLY SYNCHED BY:");
			currentE.printStackTrace();
		}
		return currentE != null ? currentE.getMessage() : "null";
	}
	@Override
	public void setUnsynched() {
		assert (synched);
//		try{
//		throw new NullPointerException();
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		synched = false;
	}

	@Override
	public boolean isSynched() {
		return synched;
	}

    public static int uploadBlockSize = 256;
	@Override
	public long getUploadBlockSize() {
		return uploadBlockSize;//getGameState().getClientUploadBlockSize();
	}



	public void flagRequestServerTime() {
		requestServerTimeFlag = true;
	}

	public void flagWarped() {
		this.warped = true;
		this.warpedUpdateNr = getNumberOfUpdate();
	}

	public RemoteSegment getAndRemoveRequestingSegment(short packetId) {
		synchronized (requestingSegments) {
			RemoteSegment remove = requestingSegments.remove(packetId);
			return remove;
		}
	}

	public PlayerCatalogManager getCatalog() {
		return player.getCatalog();
	}

	@Override
	public CatalogManager getCatalogManager() {
		return gameState.getCatalogManager();
	}

	public PlayerCharacter getCharacter() {
		return character;
	}

	public void setCharacter(PlayerCharacter character) {
		this.character = character;
	}

	/**
	 * @return the configCheckSum
	 */
	public String getBlockConfigCheckSum() {
		return configCheckSum;
	}

	/**
	 * @return the configPropertiesCheckSum
	 */
	public String getConfigPropertiesCheckSum() {
		return configPropertiesCheckSum;
	}

	public void setConfigPropertiesCheckSum(String s) {
		this.configPropertiesCheckSum = s;
	}

	/**
	 * @return the controller
	 */
	@Override
	public GameClientController getController() {
		return controller;
	}

	@Override
	public String getPlayerName() {
		return playerName;
	}

	@Override
	public void setPlayerName(String playerName) {
		this.playerName = playerName;

	}

	@Override
	public void message(ServerMessage m) {
		if (m.receiverPlayerId > 0 && m.receiverPlayerId != player.getId()) {
			return;
		}
		switch(m.type) {
			case (ServerMessage.MESSAGE_TYPE_SIMPLE) -> chat(playerName, StringTools.getFormatedMessage(m.getMessage()), "[MESSAGE]", false);
			case (ServerMessage.MESSAGE_TYPE_INFO) -> controller.popupInfoTextMessage(StringTools.getFormatedMessage(m.getMessage()), 0);
			case (ServerMessage.MESSAGE_TYPE_WARNING) -> controller.popupGameTextMessage(StringTools.getFormatedMessage(m.getMessage()), 0);
			case (ServerMessage.MESSAGE_TYPE_ERROR) -> controller.popupAlertTextMessage(StringTools.getFormatedMessage(m.getMessage()), 0);
			case (ServerMessage.MESSAGE_TYPE_DIALOG) -> controller.popupDialogMessage(StringTools.getFormatedMessage(m.getMessage()));
		}
	}


	@Override
	public Version getClientVersion() {
		return VersionContainer.VERSION;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(GameClientController controller) {
		this.controller = controller;
	}

	/**
	 * @return the currentClosestShop
	 */
	public ShopInterface getCurrentClosestShop() {
		return currentClosestShop;
	}

	/**
	 * @return the currentGravitySources
	 */
	@Override
	public ObjectArrayList<SimpleTransformableSendableObject> getCurrentGravitySources() {
		return currentGravitySources;
	}

	/**
	 * @return the currentPlayerObject
	 */
	public SimpleTransformableSendableObject getCurrentPlayerObject() {
		return currentPlayerObject;
	}

	/**
	 * @param currentPlayerObject the currentPlayerObject to set
	 */
	public void setCurrentPlayerObject(SimpleTransformableSendableObject currentPlayerObject) {
		//		try{
		//		throw new NullPointerException(currentPlayerObject+"");
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		this.currentPlayerObject = currentPlayerObject;

	}

	public RemoteSector getCurrentRemoteSector() {
		Sendable sendable = getLocalAndRemoteObjectContainer().getLocalObjects().get(player.getCurrentSectorId());
		if (sendable != null && sendable instanceof RemoteSector) {
			return (RemoteSector) sendable;
		}
		return null;
	}

	public Int2ObjectOpenHashMap<SimpleTransformableSendableObject<?>> getCurrentSectorEntities() {
		//		HashSet<SimpleTransformableSendableObject> hashSet = sectorCacheMap.get(getCurrentSectorId());
		//		//		assert(hashSet != null):": "+getCurrentSectorId();
		//		if(hashSet == null){
		//			hashSet = new HashSet<SimpleTransformableSendableObject>();
		//			sectorCacheMap.put(getCurrentSectorId(), hashSet);
		//		}
		//		return hashSet;
		return currentSectorEntities;
	}

	public final int getCurrentSectorId() {
		if (player == null) {
			assert (initialSectorId != Sector.SECTOR_INITIAL);
			return initialSectorId;
		}
		assert (player.getCurrentSectorId() != Sector.SECTOR_INITIAL);
		return player.getCurrentSectorId();
	}


	public Faction getFaction() {
		return getFactionManager().getFaction(player.getFactionId());
	}

	@Override
	public FactionManager getFactionManager() {
		if(gameState == null) {
			return null;
		}
		return gameState.getFactionManager();
	}

	public String getFactionString() {
		Faction f = getFaction();
		if (f == null) {
			return "neutral";
		}
		return f.getName();
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

	/**
	 * @return the physicalAsteroids
	 */
	@Override
	public boolean isPhysicalAsteroids() {
		return physicalAsteroids;
	}

	/**
	 * @param physicalAsteroids the physicalAsteroids to set
	 */
	public void setPhysicalAsteroids(boolean physicalAsteroids) {
		this.physicalAsteroids = physicalAsteroids;
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
		return gameState.getSectorSize() + Universe.SECTOR_MARGIN;
	}

	@Override
	public Short2ObjectOpenHashMap<Technology> getAllTechs() {
		return techs;
	}

	@Override
	public boolean getMaterialPrice() {
		return gameState.isDynamicPrices();
	}

	@Override
	public int getSegmentPieceQueueSize() {
		assert (SERVER_BLOCK_QUEUE_SIZE > 0) : SERVER_BLOCK_QUEUE_SIZE;
		return SERVER_BLOCK_QUEUE_SIZE;
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
	 * @return the glFrame
	 */
	public GLFrame getGlFrame() {
		return glFrame;
	}

	/**
	 * @param glFrame the glFrame to set
	 */
	public void setGlFrame(GLFrame glFrame) {
		this.glFrame = glFrame;
	}

	/**
	 * @return the globalGameControlManager
	 */
	public GlobalGameControlManager getGlobalGameControlManager() {
		return globalGameControlManager;
	}

	/**
	 * @param globalGameControlManager the globalGameControlManager to set
	 */
	public void setGlobalGameControlManager(GlobalGameControlManager globalGameControlManager) {
		this.globalGameControlManager = globalGameControlManager;
	}

	public GUIController getGUIController() {
		return guiController;
	}

	public void setGUIController(GUIController guiController) {
		this.guiController = guiController;
	}

	/**
	 * @return the highestSubStep
	 */
	public float getHighestSubStep() {
		return highestSubStep;
	}

	/**
	 * @param highestSubStep the highestSubStep to set
	 */
	public void setHighestSubStep(float highestSubStep) {
		this.highestSubStep = highestSubStep;
	}

	/**
	 * @return the initialSectorId
	 */
	public Integer getInitialSectorId() {
		return initialSectorId;
	}

	/**
	 * @param initialSectorId the initialSectorId to set
	 */
	public void setInitialSectorId(Integer initialSectorId) {
		System.err.println("SET INITIAL SECTOR ID TO " + initialSectorId);
		assert (initialSectorId != Sector.SECTOR_INITIAL);
		this.initialSectorId = initialSectorId;
	}

	/**
	 * @return the initialSectorPos
	 */
	public Vector3i getInitialSectorPos() {
		return initialSectorPos;
	}

	/**
	 * @param initialSectorPos the initialSectorPos to set
	 */
	public void setInitialSectorPos(Vector3i initialSectorPos) {
		this.initialSectorPos = initialSectorPos;
	}

	/**
	 * @return the lastServerTimeRequest
	 */
	public long getLastServerTimeRequest() {
		return lastServerTimeRequest;
	}

	/**
	 * @param lastServerTimeRequest the lastServerTimeRequest to set
	 */
	public void setLastServerTimeRequest(long lastServerTimeRequest) {
		this.lastServerTimeRequest = lastServerTimeRequest;
	}

	@Override
	public float getLinearDamping() {
		if (gameState != null) {
			return gameState.getLinearDamping();
		} else {
			return 0.09f;
		}
	}

	/**
	 * @return the physics
	 */
	@Override
	public Physics getPhysics() {
		return physics;
	}

	@Override
	public String getPhysicsSlowMsg() {
		return "[PHYSICS][CLIENT] WARNING: PHYSICS SYNC IN DANGER";
	}

	@Override
	public float getRotationalDamping() {
		if (gameState != null) {
			return gameState.getRotationalDamping();
		} else {
			return 0.09f;
		}
	}

	@Override
	public void handleNextPhysicsSubstep(float maxPhysicsSubsteps) {
		highestSubStep = Math.max(highestSubStep, maxPhysicsSubsteps);
	}

	@Override
	public String toStringDebug() {
		return "CLIENT: " + toString();
	}

	/**
	 * @param physics the physics to set
	 */
	public void setPhysics(Physics physics) {
		this.physics = physics;
	}

	@Override
	public MetaObjectManager getMetaObjectManager() {
		return metaObjectManager;
	}

	@Override
	public void requestMetaObject(int metaObjectId) {
		synchronized (toRequestMetaObjects) {
			assert (metaObjectId >= 0) : metaObjectId;
			toRequestMetaObjects.enqueue(metaObjectId);
		}
	}

	/**
	 * @return the otherSendables
	 */
	public HashSet<Sendable> getOtherSendables() {
		return otherSendables;
	}

	@Override
	public ProjectileController getParticleController() {
		return particleController;
	}

	public void setParticleController(ProjectileController particleController) {
		this.particleController = particleController;
	}

	public EditableSendableSegmentController getPlanetTestSurface() {
		return planetTestSurface;
	}

	public void setPlanetTestSurface(EditableSendableSegmentController planetTestSurface) {
		this.planetTestSurface = planetTestSurface;
	}

	/**
	 * @return the player
	 */
	public PlayerState getPlayer() {
		return player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(PlayerState player) {
		this.player = player;
		flagPlayerReceived = true;
	}

	public PlayerState getPlayerFromClientId(long clientId) throws PlayerNotFountException {
		synchronized (getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (s instanceof PlayerState) {
					PlayerState p = (PlayerState) s;
					if (p.getClientId() == clientId) {
						return p;
					}
				}
			}
		}
		throw new PlayerNotFountException("clientID: " + clientId);
	}

	@Override
	public List<DialogInterface> getPlayerInputs() {
		return controller.getInputController().getPlayerInputs();
	}

	@Override
	public void exit() {
		try {
			if (player != null) {
				player.getPlayerChannelManager().saveChannelsClient();
			}
		} catch (Exception e) {
			System.err.println("Exception: CHAT CHANNELS COULD NOT BE SAVED");
			e.printStackTrace();
		}
		try {
			CrashReporter.createThreadDump();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			GLFrame.setFinished(true);
		}
	}

	@Override
	public List<Object> getGeneralChatLog() {
		return (List<Object>) ((List<? extends Object>) channelRouter.getDefaultChatLog());
	}

	/**
	 * @return the visibleChatLog
	 */
	@Override
	public List<Object> getVisibleChatLog() {
		return (List<Object>) ((List<? extends Object>) channelRouter.getDefaultVisibleChatLog());
	}

	@Override
	public Transform getCurrentPosition() {
		return currentPlayerObject.getWorldTransform();
	}

	@Override
	public void onSwitchedSetting(EngineSettings engineSettings) {
		switch(engineSettings) {
			case LIGHT_RAY_COUNT -> SegmentDrawer.forceFullLightingUpdate = true;
			default -> {
			}
		}
	}

	/**
	 * @return the pulseController
	 */
	@Override
	public PulseController getPulseController() {
		return pulseController;
	}

	/**
	 * @param pulseController the pulseController to set
	 */
	public void setPulseController(PulseController pulseController) {
		this.pulseController = pulseController;
	}

	public Map<Short, RemoteSegment> getRequestingLocks() {
		return requestingSegments;
	}

	/**
	 * @return the scene
	 */
	public MainGameGraphics getScene() {
		return scene;
	}

	/**
	 * @param scene the scene to set
	 */
	public void setScene(MainGameGraphics scene) {
		this.scene = scene;
	}

	@Override
	public SegmentDataManager getSegmentDataManager() {
		return this.segmentDataManager;
	}


	/**
	 * @return the ship
	 */
	public Ship getShip() {
		return ship;
	}

	/**
	 * @param ship the ship to set
	 */
	public void setShip(Ship ship) {
		this.ship = ship;
	}

	/**
	 * @return the threadedSegmentWriter
	 */
	public ThreadedSegmentWriter getThreadedSegmentWriter() {
		return threadedSegmentWriter;
	}

	/**
	 * @return the toRequestMetaObjects
	 */
	public IntArrayFIFOQueue getToRequestMetaObjects() {
		return toRequestMetaObjects;
	}

	public AiEntityState getTutorialAIState() {
		return tutorialAIState;
	}

	/**
	 * @return the warpedUpdateNr
	 */
	public short getWarpedUpdateNr() {
		return warpedUpdateNr;
	}

	/**
	 * @return the worldDrawer
	 */
	public WorldDrawer getWorldDrawer() {
		return worldDrawer;
	}

	/**
	 * @param worldDrawer the worldDrawer to set
	 */
	public void setWorldDrawer(WorldDrawer worldDrawer) {
		this.worldDrawer = worldDrawer;
	}

	public void handleFlaggedAddedOrRemovedObjects() {
		if (!flaggedAddedObjects.isEmpty()) {
			HashSet<Sendable> tmpFlaggedAddedObjects = new HashSet<Sendable>(flaggedAddedObjects.size());
			synchronized (flaggedAddedObjects) {
				tmpFlaggedAddedObjects.addAll(flaggedAddedObjects);
			}

			for (Sendable s : tmpFlaggedAddedObjects) {

				if (s instanceof PlanetIcoCore) {
					System.err.println("[CLIENT] ADDED PLANET CORE OBJECT: " + s);
				}
				onSendableAdded(s);
				
				
			}
			flaggedAddedObjects.clear();
		}
		if (!flaggedRemovedObjects.isEmpty()) {
			HashSet<Sendable> tmpFlaggedAddedObjects = new HashSet<Sendable>(flaggedRemovedObjects.size());
			synchronized (flaggedRemovedObjects) {
				tmpFlaggedAddedObjects.addAll(flaggedRemovedObjects);
			}

			for (Sendable s : tmpFlaggedAddedObjects) {
				onSendableRemoved(s);
				
				
			}
			flaggedRemovedObjects.clear();
		}
	}

	private void onSendableRemoved(Sendable s) {
		if(s instanceof PlayerState) {
			playerStatesByDbId.put( ((PlayerState)s).getDbId(), ((PlayerState)s));
		}
		if (s.isPrivateNetworkObject()) {
			controller.onPrivateSendableAdded(s);
		} else {
			controller.onSendableRemoved(s);
		}
	}
	private void onSendableAdded(Sendable s) {
		if(s instanceof PlayerState) {
			playerStatesByDbId.remove( ((PlayerState)s).getDbId());
		}
		if (s.isPrivateNetworkObject()) {
			controller.onPrivateSendableAdded(s);
		} else {
			controller.onSendableAdded(s);
		}
	}

	public boolean isControllManagerInitialized() {
		return globalGameControlManager != null && globalGameControlManager.isInitialized();
	}

	/**
	 * @return the dbPurgeRequested
	 */
	public boolean isDbPurgeRequested() {
		return dbPurgeRequested;
	}

	/**
	 * @param dbPurgeRequested the dbPurgeRequested to set
	 */
	public void setDbPurgeRequested(boolean dbPurgeRequested) {
		this.dbPurgeRequested = dbPurgeRequested;
	}

	/**
	 * @return the flagPlayerReceived
	 */
	public boolean isFlagPlayerReceived() {
		return flagPlayerReceived;
	}

	/**
	 * @param flagPlayerReceived the flagPlayerReceived to set
	 */
	public void setFlagPlayerReceived(boolean flagPlayerReceived) {
		this.flagPlayerReceived = flagPlayerReceived;
	}

	public boolean isFlagRequestServerTime() {
		return requestServerTimeFlag;
	}

	/**
	 * @return the flagSectorChange
	 */
	public int isFlagSectorChange() {
		return flagSectorChange;
	}

	public boolean isInShopDistance() {
		//		if(currentPlayerObject != null &&
		//				currentPlayerObject instanceof ShopperInterface ){
		//			System.err.println("SHOPS: "+((ShopperInterface)currentPlayerObject).getShopsInDistance().size());
		//		}
		currentClosestShop = null;
		if (!playerSpawned) {
			return false;
		}
		try {

			boolean inShopDistance = currentPlayerObject != null &&
					currentPlayerObject instanceof ShopperInterface &&
					((ShopperInterface) currentPlayerObject).getShopsInDistance().size() > 0;

			if (inShopDistance != wasInShopDistance) {
				if (controller.getTutorialMode() != null) {
					controller.getTutorialMode().shopDistanceChanged(inShopDistance);
					wasInShopDistance = inShopDistance;
				}

			}
//			System.err.println("CC: "+currentPlayerObject.getSectorId()+": "+((ShopperInterface)currentPlayerObject).getShopsInDistance());
			if (inShopDistance) {
				Set<ShopInterface> shopsInDistance = ((ShopperInterface) currentPlayerObject).getShopsInDistance();
				float l = -1;
				for (ShopInterface shop : shopsInDistance) {
					if (shop.getSectorId() == currentPlayerObject.getSectorId()) {
						
						Vector3f pos = new Vector3f(shop.getWorldTransform().origin);
						pos.sub(currentPlayerObject.getWorldTransform().origin);
						if (l < 0 || pos.lengthSquared() < l) {
							currentClosestShop = shop;
							l = pos.lengthSquared();
						}
					}
				}
			}
			return inShopDistance;
		} catch (NullPointerException e) {
			System.err.println("EXCEPTION HAS BEEN CATCHED. CURRENT OBJECT PROBABLY BECAME NULL: " + currentPlayerObject);
			e.printStackTrace();
		}
		return false;
	}

	public boolean isInWarp() {
		return warped;
	}

	/**
	 * @return the playerSpawned
	 */
	public boolean isPlayerSpawned() {
		return playerSpawned;
	}

	/**
	 * @param playerSpawned the playerSpawned to set
	 */
	public void setPlayerSpawned(boolean playerSpawned) {
		if (this.playerSpawned && !playerSpawned) {
			if (controller.getTutorialMode() != null) {
				controller.getTutorialMode().repeat();
			}
		}
		this.playerSpawned = playerSpawned;
	}

	/**
	 * @return the waitingForPlayerActivate
	 */
	public boolean isWaitingForPlayerActivate() {
		return waitingForPlayerActivate;
	}

	/**
	 * @param waitingForPlayerActivate the waitingForPlayerActivate to set
	 */
	public void setWaitingForPlayerActivate(boolean waitingForPlayerActivate) {
		this.waitingForPlayerActivate = waitingForPlayerActivate;
	}

	public void notifyOfCatalogChange() {
		getCatalog().onCatalogChanged();
	}

	public void resetFlagRequestServerTime() {
		requestServerTimeFlag = false;
	}

	public void setConfigCheckSum(String s) {
		this.configCheckSum = s;
	}

	/**
	 * @param old the flagSectorChange to set
	 */
	public void setFlagSectorChange(int old) {
		this.flagSectorChange = old;
	}

	/**
	 * @param warped the warped to set
	 */
	public void setWarped(boolean warped) {
		this.warped = warped;
	}


	/**
	 * @return the messageLog
	 */
	public ClientMessageLog getMessageLog() {
		return messageLog;
	}

	/**
	 * @return the hinderedInput
	 */
	public int getHinderedInput() {
		return hinderedInput;
	}

	/**
	 * @param hinderedInput the hinderedInput to set
	 */
	public void setHinderedInput(int hinderedInput) {
		this.hinderedInput = hinderedInput;
		hinderedInputTime = System.currentTimeMillis();
	}

	/**
	 * @return the hinderedInputTime
	 */
	public long getHinderedInputTime() {
		return hinderedInputTime;
	}

	/**
	 * @return the blockBehaviorCheckSum
	 */
	public String getBlockBehaviorCheckSum() {
		return blockBehaviorCheckSum;
	}

	/**
	 * @param blockBehaviorCheckSum the blockBehaviorCheckSum to set
	 */
	public void setBlockBehaviorCheckSum(String blockBehaviorCheckSum) {
		this.blockBehaviorCheckSum = blockBehaviorCheckSum;
	}

	public String getCustomTexturesCheckSum() {
		return customTexturesCheckSum;
	}

	public void setCustomTexturesCheckSum(String blockBehaviorCheckSum) {
		this.customTexturesCheckSum = blockBehaviorCheckSum;
	}

	/**
	 * @return the loadedSectors
	 */
	public Object2ObjectOpenHashMap<Vector3i, RemoteSector> getLoadedSectors() {
		return loadedSectors;
	}

	public float getMaxBuildArea() {
		return gameState.getMaxBuildArea();
	}

	public Galaxy getCurrentGalaxyNeighbor(Vector3i galPosAdd) {
		Vector3i sysPos = VoidSystem.getContainingSystem(
				(player.isInTutorial() || player.isInPersonalSector() || player.isInTestSector())
				? new Vector3i(0, 0, 0) : player.getCurrentSector(), tmpStellar);
		Vector3i galaxyPos = Galaxy.getContainingGalaxyFromSystemPos(sysPos, tmpGalaxy);
		galaxyPos.add(galPosAdd);

		Galaxy galaxy = surrounding.get(galaxyPos);
		if (galaxy == null) {
			long t = System.currentTimeMillis();
			long seed = gameState.getUniverseSeed() + galaxyPos.hashCode();
			galaxy = new Galaxy(seed, new Vector3i(galaxyPos));
			///INSERTED CODE
			GalaxyInstantiateEvent e = new GalaxyInstantiateEvent(galaxy, sysPos);
			StarLoader.fireEvent(GalaxyInstantiateEvent.class, e, true);
			galaxy = e.getGalaxy();
			///
			galaxy.generate();
			System.err.println("[CLIENT] creating galaxy " + tmpGalaxy + "; Stars: " + galaxy.getNumberOfStars() + "; created in: " + (System.currentTimeMillis() - t) + "ms");
			surrounding.put(galaxy.galaxyPos, galaxy);
		}

		return galaxy;
	}

	public Galaxy getCurrentGalaxy() {
		Vector3i sysPos = VoidSystem.getContainingSystem(
				(player.isInTutorial() || player.isInPersonalSector() || player.isInTestSector())
				? new Vector3i(0, 0, 0) : player.getCurrentSector(), tmpStellar);
		Vector3i galaxyPos = Galaxy.getContainingGalaxyFromSystemPos(sysPos, tmpGalaxy);

		long seed = gameState.getUniverseSeed() + galaxyPos.hashCode();
		assert (seed != 0);
		if (currentGalaxy == null || currentGalaxy.getSeed() != seed) {
			surrounding.clear();
			long t = System.currentTimeMillis();
			currentGalaxy = new Galaxy(seed, new Vector3i(galaxyPos));
			///INSERTED CODE
			GalaxyInstantiateEvent e = new GalaxyInstantiateEvent(currentGalaxy, sysPos);
			StarLoader.fireEvent(GalaxyInstantiateEvent.class, e, true);
			currentGalaxy = e.getGalaxy();
			///
			currentGalaxy.generate();
			System.err.println("[CLIENT] creating galaxy " + tmpGalaxy + "; Stars: " + currentGalaxy.getNumberOfStars() + "; created in: " + (System.currentTimeMillis() - t) + "ms");
		}

		return currentGalaxy;
	}
	private Vector3i lastSector = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	private VoidSystem cached;
	
	public VoidSystem getCurrentClientSystem() {
		if(!lastSector.equals(player.getCurrentSector())){
			if (controller.getClientChannel() != null && controller.getClientChannel().isConnectionReady() && player != null) {
				cached = controller.getClientChannel().getGalaxyManagerClient().getSystemOnClient(player.getCurrentSector());
				lastSector.set(player.getCurrentSector());
			} else {
				cached = null;
			}
		}
		return cached;
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
	 * @return the onlinePlayers
	 */
	public Map<String, PlayerState> getOnlinePlayersLowerCaseMap() {
		return onlinePlayersLowerCaseMap;
	}

	public void notifyLaggyListChanged() {
		for(DialogInterface p : getPlayerInputs()){
			if(p instanceof PlayerLagStatsInput){
				((PlayerLagStatsInput)p).flagChanged();
			}
		}
	}

	@Override
	public boolean isAdmin() {
		return player != null && player.getNetworkObject() != null && player.getNetworkObject().isAdminClient.getBoolean();
	}
	
	@Override
	public RaceManager getRaceManager() {
		return gameState.getRaceManager();
	}
	
	public boolean isInAnyBuildMode() {
		PlayerInteractionControlManager p = globalGameControlManager.getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		return p.isInAnyBuildMode();
	}
	public boolean isInCharacterBuildMode() {
		PlayerInteractionControlManager p = globalGameControlManager.getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		return p.isInAnyCharacterBuildMode();
	}
	public boolean isInAnyStructureBuildMode() {
		PlayerInteractionControlManager p = globalGameControlManager.getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		return p.isInAnyStructureBuildMode();
	}
	@Override
	public boolean isDebugKeyDown() {
		return KeyboardMappings.PLAYER_LIST.isDown();
	}
	@Override
	public FleetManager getFleetManager() {
		return fleetManager;
	}
	@Override
	public void stopClient() {
		Starter.stopClient(controller.graphicsContext);
	}
	@Override
	public void startClient(HostPortLoginName hostPortLogin, boolean startConnectDialog) {
		Starter.startClient(hostPortLogin, startConnectDialog, controller.graphicsContext);
	}
	@Override
	public void startLocalServer() {
		ModStarter.justStartedSinglePlayer = true;
		Starter.startServer(false, false);
	}
	@Override
	public void handleExceptionGraphically(Exception e) {
		if(GameMainMenuController.currentMainMenu != null){
			GameMainMenuController.currentMainMenu.switchFrom(this, e);
		}
	}
	@Override
	public String getGUIPath() {
		return UIScale.getUIScale().getGuiPath()+"ingame/";
	}
	@Override
	public GraphicsContext getGraphicsContext() {
		return controller.graphicsContext;
	}
	@Override
	public void setActiveSubtitles(List<Subtitle> activeSubtitles) {
		this.activeSubtitles = activeSubtitles;
	}
	public List<Subtitle> getActiveSubtitles() {
		return activeSubtitles;
	}
	
	public BlockSyleSubSlotController getBlockSyleSubSlotController() {
		return blockSyleSubSlotController;
	}
	@Override
	public ConfigPool getConfigPool() {
		if(gameState == null){
			return null;
		}
		return gameState.getConfigPool();
	}
	public EventFactory getEventFactory() {
		return eventFactory;
	}
	public void onPowerChanged(SegmentController s, PowerChangeType t) {
		for(PowerChangeListener d : powerChangeListeners){
			d.powerChanged(s, t);
		}
	}
	public void onDockChanged(SegmentController s, boolean dockedOrUndocked) {
		for(RailDockingListener d : dockingListeners){
			d.dockingChanged(s, dockedOrUndocked);
		}
	}
	public List<RailDockingListener> getDockingListeners() {
		return dockingListeners;
	}
	public List<PowerChangeListener> getPowerChangeListeners() {
		return powerChangeListeners;
	}
	public Long2ObjectOpenHashMap<PlayerState> getPlayerStatesByDbId() {
		return playerStatesByDbId;
	}
	@Override
	public DebugTimer getDebugTimer() {
		return debugTimer;
	}
	public boolean isInFlightMode() {
		return globalGameControlManager.getIngameControlManager().getPlayerGameControlManager()
				.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().isTreeActiveInFlight();
	}


	@Override
	public void handleGameRequestAnswer(GameRequestAnswerCommandPackage pack) {
		networkManager.notifyPackageArrivedForWaiting(pack, updateSynch);
	}
	@Override
	public void onClientDisconnected() {
		System.err.println("[CLIENT] onClientDisconnected()");
		stopClient();
	}
	@Override
	public NetworkSettings getSettings() {
		return settings;
	}
	@Override
	public NetworkManager getNetworkManager() {
		return networkManager;
	}
	@Override
	public void notifyUpdateNeeded() {
		updateSynch.notfifyUpdateNeeded();
	}
	public UpdateSynch getUpdateSynch() {
		return updateSynch;
	}

	@Override
	public LoginStateEnum GetLoginState() {
		return loginState;
	}

	@Override
	public void receivedSynchronization(NetworkProcessor from, SynchronizePublicCommandPackage pack)
			throws IOException {
//		System.err.println("RECEIVED SYNCH");
		if (isNetworkSynchronized()) {
			try {
				SynchronizationReceiver.update(getLocalAndRemoteObjectContainer(), IdGen.SERVER_ID, pack.in,
						this, isNetworkSynchronized(), false, (short)0, from.getLastReceived());
				setSynchronized(true);

			} catch (SynchronizationException e) {
				e.printStackTrace();
				System.err.println("[CLIENT]SCHEDULING RESYNCH FOR " + this);
				setSynchronized(false);
			}
		} else {
			System.err.println("[CLIENT][SYNCHRONIZE] " + this
					+ " IS WAITING TO SYNCH WITH SERVER - SKIPPING PACKAGE (" + pack.getType().name() + ')');
		}
	}
	@Override
	public void receivedPrivateSynchronization(NetworkProcessor from, SynchronizePrivateCommandPackage pack)
			throws IOException {
		SynchronizationReceiver.update(getPrivateLocalAndRemoteObjectContainer(), IdGen.SERVER_ID, pack.in,
				this, isNetworkSynchronized(), false, (short)0, from.getLastReceived());
	}
	
	@Override
	public void receivedAllSynchronization(NetworkProcessor from, SynchronizeAllCommandPackage pack) throws IOException {
		boolean forced = true;
		SynchronizationReceiver.update(getLocalAndRemoteObjectContainer(), IdGen.SERVER_ID, pack.in,
				this, isNetworkSynchronized(), forced, (short)0, from.getLastReceived());
		setSynchronized(true);

		controller.afterFullResynchronize();

		System.out.println("[CLIENT] RE- synchronized client: " + getId());
		notifyUpdateNeeded();
	}
	@Override
	public boolean canPlayAudioEntry(RemoteAudioEntry remoteAudioEntry) {
		return currentSectorEntities.containsKey(remoteAudioEntry.targetId);
	}
}
