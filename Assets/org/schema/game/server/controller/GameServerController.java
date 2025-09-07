package org.schema.game.server.controller;

import api.common.GameCommon;
import api.listener.events.Event;
import api.listener.events.entity.SegmentControllerSpawnEvent;
import api.listener.events.player.PlayerJoinWorldEvent;
import api.listener.events.player.PlayerLeaveWorldEvent;
import api.listener.events.player.PlayerSpawnEvent;
import api.listener.events.world.WorldSaveEvent;
import api.mod.ModStarter;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import api.utils.StarRunnable;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.io.FileUtils;
import org.schema.common.LogUtil;
import org.schema.common.ParseException;
import org.schema.common.XMLTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.MineInterface;
import org.schema.game.client.data.GameStateControllerInterface;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.DatabaseIndex;
import org.schema.game.common.controller.database.tables.EntityTable;
import org.schema.game.common.controller.elements.ElementCollectionCalculationThreadExecution;
import org.schema.game.common.controller.elements.ElementCollectionCalculationThreadManager;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.elements.mines.MineController;
import org.schema.game.common.controller.elements.missile.MissileController;
import org.schema.game.common.controller.generator.PlanetIcoCreatorThread;
import org.schema.game.common.controller.io.IOFileManager;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.crashreporter.CrashReporter;
import org.schema.game.common.data.*;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.Logbook;
import org.schema.game.common.data.explosion.ExplosionRunnable;
import org.schema.game.common.data.gamemode.AbstractGameMode;
import org.schema.game.common.data.gamemode.GameModeException;
import org.schema.game.common.data.gamemode.battle.BattleMode;
import org.schema.game.common.data.missile.MissileControllerInterface;
import org.schema.game.common.data.missile.MissileManagerInterface;
import org.schema.game.common.data.physics.GamePhysicsObject;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.SkinManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.common.data.world.*;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.network.objects.BitsetResponse;
import org.schema.game.network.objects.NetworkSegmentProvider;
import org.schema.game.network.objects.remote.RemoteBitset;
import org.schema.game.network.objects.remote.RemoteSegmentRemoteObj;
import org.schema.game.server.controller.pathfinding.*;
import org.schema.game.server.data.*;
import org.schema.game.server.data.GameServerState.FileRequest;
import org.schema.game.server.data.admin.AdminCommandNotFoundException;
import org.schema.game.server.data.admin.AdminCommandQueueElement;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.auth.SessionCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.util.WorldToScreenConverterFixedAspect;
import org.schema.schine.network.*;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.commands.LoginRequest.LoginCode;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.AuthenticationRequiredException;
import org.schema.schine.network.server.ServerController;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerState;
import org.schema.schine.physics.Physical;
import org.schema.schine.resource.*;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.sound.controller.AudioController;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class GameServerController extends ServerController implements MineInterface, MissileControllerInterface, ElementHandlerInterface, CreatorThreadControlInterface, GameStateControllerInterface {

	public static final String BLOCK_BEHAVIOR_DEFAULT_PATH = "./data/config/blockBehaviorConfig.xml";
	public static WorldToScreenConverterFixedAspect worldToScreenConverter = new WorldToScreenConverterFixedAspect();
	public static Matrix4f projectionMatrix = new Matrix4f();

	public final ObjectArrayFIFOQueue<RegisteredClientOnServer> initialGameModeRequests = new ObjectArrayFIFOQueue<RegisteredClientOnServer>();
	private final SynchronizationContainerController synchController;
	private final MissileController missileController;
	private final CreatorThreadController creatorThreadController;
	private final ObjectOpenHashSet<RemoteSector> sectorsToUpdate = new ObjectOpenHashSet<RemoteSector>();
	private final SegmentBreaker segmentBreaker;
	private final SegmentPathFindingHandler segmentPathFinder;
	private final SegmentPathGroundFindingHandler segmentPathGroundFinder;
	long lastSynchronize;
	private GameServerState state;
	private long endRoundMode;
	private long lastSave;
	private ServerSegmentRequestThread serverSegmentRequestThread;
	private ElementCollectionCalculationThreadManager elementCollectionCalculationThreadManager = new ElementCollectionCalculationThreadManager(true);
	private byte[] blockBehaviorChanged = null;
	private String lastMessage = "";
	private long lastExceptionTime;
	private ObjectOpenHashSet<Class<Exception>> exceptionSet = new ObjectOpenHashSet<Class<Exception>>();
	private final Int2LongOpenHashMap sectorLag = new Int2LongOpenHashMap();
	private long lastDelayAutoSaveMsgSent;
	private Object reqLock = new Object();
	private boolean flaggedShutdown;
	private final MineController mineController;
	private final List<SectorListener> sectorListeners = new ObjectArrayList<SectorListener>();
	public GameServerController(final GameServerState state) {
		super(state);

		state.logbookEntries = readLogbookEntries();

		missileController = new MissileController(state);
		sectorListeners.add(mineController = new MineController(state));
		this.state = state;
		getTimer().initialize(true);
		lastSave = System.currentTimeMillis();
		synchController = new SynchronizationContainerController(state.getLocalAndRemoteObjectContainer(), state, false);

		creatorThreadController = new CreatorThreadController(state);
		creatorThreadController.start();

		this.serverSegmentRequestThread = new ServerSegmentRequestThread(state);
		serverSegmentRequestThread.start();
		segmentBreaker = new SegmentBreaker(this.state);
		segmentPathFinder = new SegmentPathFindingHandler(this.state);
		segmentPathGroundFinder = new SegmentPathGroundFindingHandler(this.state);
		ExplosionRunnable.initialize();
		File serverTmpDir = new FileExt(GameServerState.ENTITY_DATABASE_PATH+"tmp/");
		serverTmpDir.mkdirs();
		File lostAndFound = new FileExt(GameServerState.ENTITY_DATABASE_PATH+"backupCorrupted");
		lostAndFound.mkdir();
		if (serverTmpDir.exists()) {
			File[] files = serverTmpDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().endsWith(".tmp")) {
					File newFile = new FileExt(GameServerState.ENTITY_DATABASE_PATH+"backupCorrupted/" + files[i].getName());

					try {
						FileUtil.copyFile(files[i], newFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					files[i].delete();
				}
			}
		}

		elementCollectionCalculationThreadManager.start();
		
		//		parseBlockBehavior();
	}

	public static void checkIP(String ip) throws NoIPException {
		try {
			String[] split = ip.split("\\.");
			for (int i = 0; i < 4; i++) {
				int s = Integer.parseInt(split[i]);
				if (s < 0 || s > 255) {
					throw new NoIPException(ip);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new NoIPException(ip);
		}
	}

	public static void main(String[] args) {

	}

	public static String[] readLogbookEntries() {
		File f = new FileExt("./data/config/logbookEntriesGen.txt");
		BufferedReader r = null;
		if (f.exists()) {
			try {
				r = new BufferedReader(new FileReader(f));
				ArrayList<String> list = new ArrayList<String>();

				StringBuffer entry = new StringBuffer();
				String line;
				while ((line = r.readLine()) != null) {
					if (line.trim().equals("-")) {
						if (entry.length() > 0) {
							//delete last newline
							entry.deleteCharAt(entry.length() - 1);
						}
						if (entry.length() >= Logbook.MAX_LENGTH) {
							entry.substring(0, Logbook.MAX_LENGTH - 1);
						}
						list.add(entry.toString());
						entry = new StringBuffer();
					} else {
						entry.append(line + "\n");
					}
				}
				if (entry.length() > 0) {
					list.add(entry.toString());
				}

				if (list.size() > 0) {
					String[] en = new String[list.size()];
					for (int i = 0; i < en.length; i++) {
						en[i] = list.get(i);
					}
					return en;
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return new String[]{"Er! Ror ha, pe need! No en! Tries!"};

	}

	public static void handleSegmentRequest(NetworkSegmentProvider ntSegmentProvider, RemoteSegment segmentFromCache, long index, long localTimestamp, int sizeOnClient) {
		if(ntSegmentProvider == null) {
			//for prio requests on server
			return;
		}
		synchronized (ntSegmentProvider) {
			if (segmentFromCache.getLastChanged() > localTimestamp || segmentFromCache.getSize() != sizeOnClient) {
				if (segmentFromCache.isEmpty()) {
					//signal client that segment is empty
					ntSegmentProvider.signatureEmptyBuffer.add(index);
				} else {

					//send segment for real
					SegmentSignature segmentSignature = new SegmentSignature(new Vector3i(segmentFromCache.pos), segmentFromCache.getSegmentController().getId(), segmentFromCache.isEmpty(), (short) (segmentFromCache.isEmpty() ? -1 : segmentFromCache.getSize()));

					segmentSignature.context = segmentFromCache.getSegmentController();

					ntSegmentProvider.segmentBuffer.add(new RemoteSegmentRemoteObj(segmentFromCache, segmentSignature, ntSegmentProvider));

				}
			} else {
				//signal client that his version is OK
				ntSegmentProvider.signatureOkBuffer.add(index);
			}
		}

	}

	public void addAdminDeniedCommand(String from, String playerName, AdminCommands command) {
		boolean add = false;
		synchronized (state.getAdmins()) {
			String name = playerName.trim().toLowerCase(Locale.ENGLISH);
			Admin admin = state.getAdmins().get(name);
			if (admin != null) {
				add = admin.deniedCommands.add(command);
			}
		}
		if (add) {
			LogUtil.log().fine("[ADMIN] '" + from + "' added a denied command to admin: '" + playerName + "': " + command.name());
			try {
				writeAdminsToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void removeAdminDeniedCommand(String from, String playerName, AdminCommands command) {
		boolean remove = false;
		synchronized (state.getAdmins()) {
			String name = playerName.trim().toLowerCase(Locale.ENGLISH);
			Admin admin = state.getAdmins().get(name);
			if (admin != null) {
				remove = admin.deniedCommands.remove(command);
			}
		}
		if (remove) {
			LogUtil.log().fine("[ADMIN] '" + from + "' removed a denied command from admin: '" + playerName + "': " + command.name());
			try {
				writeAdminsToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void addAdmin(String from, String playerName) {
		Admin add;
		synchronized (state.getAdmins()) {
			String name = playerName.trim().toLowerCase(Locale.ENGLISH);
			add = new Admin(name);
			state.getAdmins().put(name, add);
		}
		LogUtil.log().fine("[ADMIN] '" + from + "' added to admins: '" + playerName + "'");
		try {
			writeAdminsToDisk();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addAdminsFromDisk() throws IOException {
		File l = new FileExt("./admins.txt");
		BufferedReader s = new BufferedReader(new FileReader(l));
		String line = null;
		while ((line = s.readLine()) != null) {
			synchronized (state.getAdmins()) {
				if (line.contains("#")) {
					String[] split = line.trim().split("#", 2);
					Admin admin = new Admin(split[0].trim().toLowerCase(Locale.ENGLISH));
					state.getAdmins().put(admin.name, admin);
					String[] splitCom = split[1].trim().split(",");
					for (String c : splitCom) {
						try {
							AdminCommands com = AdminCommands.valueOf(c.toUpperCase(Locale.ENGLISH).trim());
							admin.deniedCommands.add(com);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					state.getAdmins().put(line.trim().toLowerCase(Locale.ENGLISH), new Admin(line.trim().toLowerCase(Locale.ENGLISH)));
				}

			}
		}
		s.close();
	}

	public void addBannedIp(String from, String playerIp, long validUntil) throws NoIPException {
		checkIP(playerIp);
		boolean add = false;
		synchronized (state.getBlackListedIps()) {
			add = state.getBlackListedIps().add(new PlayerAccountEntry(validUntil, playerIp.trim()));
		}
		if (add) {
			LogUtil.log().fine("[ADMIN] '" + from + "' banned ip: '" + playerIp + "'");
			try {
				writeBlackListToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void addBannedName(String from, String playerName, long validUntil) {
		boolean add = false;
		synchronized (state.getBlackListedNames()) {
			add = state.getBlackListedNames().add(new PlayerAccountEntry(validUntil, playerName.trim().toLowerCase(Locale.ENGLISH)));
		}
		if (add) {
			LogUtil.log().fine("[ADMIN] '" + from + "' banned playerName: '" + playerName + "'");
			try {
				writeBlackListToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void addBannedAccount(String from, String playerName, long validUntil) {
		boolean add = false;
		synchronized (state.getBlackListedAccounts()) {
			add = state.getBlackListedAccounts().add(new PlayerAccountEntry(validUntil, playerName.trim()));
		}
		if (add) {
			LogUtil.log().fine("[ADMIN] '" + from + "' banned StarMade Account: '" + playerName + "'");
			try {
				writeBlackListToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void addBlacklistFromDisk() throws IOException {
		File l = new FileExt("./blacklist.txt");
		BufferedReader s = new BufferedReader(new FileReader(l));
		String line = null;
		while ((line = s.readLine()) != null) {
			if (line.startsWith("nm:")) {
				synchronized (state.getBlackListedNames()) {
					state.getBlackListedNames().add(new PlayerAccountEntry(line.substring(3).trim().toLowerCase(Locale.ENGLISH)));
				}
			} else if (line.startsWith("ip:")) {
				synchronized (state.getBlackListedIps()) {
					String trim = line.substring(3).trim();
					try {
						checkIP(trim);
						state.getBlackListedIps().add(new PlayerAccountEntry(trim));
					} catch (NoIPException e) {
						e.printStackTrace();
					}
				}
			} else if (line.startsWith("ac:")) {
				synchronized (state.getBlackListedAccounts()) {
					state.getBlackListedAccounts().add(new PlayerAccountEntry(line.substring(3).trim()));
				}
			}else{
				if (line.startsWith("nmt:")) {
					String[] split = line.split(":", 3);

					long validUntil = Long.parseLong(split[1]);
					synchronized (state.getBlackListedNames()) {

						PlayerAccountEntry playerAccountEntry = new PlayerAccountEntry(validUntil, split[2].trim().toLowerCase(Locale.ENGLISH));
						if(playerAccountEntry.isValid(System.currentTimeMillis())){

							state.getBlackListedNames().add(playerAccountEntry);
						}
					}
				} else if (line.startsWith("ipt:")) {
					String[] split = line.split(":", 3);
					long validUntil = Long.parseLong(split[1]);
					synchronized (state.getBlackListedIps()) {
						String trim = split[2].trim();
						try {
							checkIP(trim);

							PlayerAccountEntry playerAccountEntry = new PlayerAccountEntry(validUntil, trim);
							if(playerAccountEntry.isValid(System.currentTimeMillis())){
								state.getBlackListedIps().add(playerAccountEntry);
							}
						} catch (NoIPException e) {
							e.printStackTrace();
						}
					}
				} else if (line.startsWith("act:")) {
					String[] split = line.split(":", 3);
					long validUntil = Long.parseLong(split[1]);
					synchronized (state.getBlackListedAccounts()) {
						PlayerAccountEntry playerAccountEntry = new PlayerAccountEntry(validUntil, split[2].trim());
						if(playerAccountEntry.isValid(System.currentTimeMillis())){
							state.getBlackListedAccounts().add(playerAccountEntry);
						}
					}
				}
			}

		}
		s.close();

	}

	public ProtectedUplinkName removeProtectedUser(String playerName) {
		ProtectedUplinkName remove = null;
		synchronized (state.getProtectedUsers()) {
			remove = state.getProtectedUsers().remove(playerName.trim());
		}
		try {
			writeProtectedUsersToDisk();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return remove;
	}

	public void addProtectedUser(String uplinkName, String playerName) {
		ProtectedUplinkName add = null;
		System.err.println("[AUTH] PROTECTING USER " + playerName + " under uplink id " + uplinkName.trim());
		LogUtil.log().fine("[AUTH] PROTECTING USER " + playerName + " under uplink id " + uplinkName.trim());
		synchronized (state.getProtectedUsers()) {
			int max = Math.max(0, ServerConfig.PROTECTED_NAMES_BY_ACCOUNT.getInt());
			ArrayList<ProtectedUplinkName> alreadyProtected = new ArrayList<ProtectedUplinkName>();

			// make a list of all player names protected by this player
			for (Entry<String, ProtectedUplinkName> s : state.getProtectedUsers().entrySet()) {
				if (s.getValue().equals(uplinkName) && !isAdmin(s.getValue().playername)) {
					alreadyProtected.add(s.getValue());
				}
			}
			// remove oldest protection until size is right
			if (alreadyProtected.size() > max) {
				Collections.sort(alreadyProtected);
				while (alreadyProtected.size() > max) {
					ProtectedUplinkName oldest = alreadyProtected.remove(0);
					System.err.println("[AUTH] removing protection oldest used username of account " + uplinkName + ": " + oldest.playername);
					LogUtil.log().fine("[AUTH] removing protection oldest used username of account " + uplinkName + ": " + oldest.playername);
					state.getProtectedUsers().remove(oldest.playername);
				}
			}

			ProtectedUplinkName protectedUplinkName = new ProtectedUplinkName(uplinkName, playerName.trim(), System.currentTimeMillis());
			add = state.getProtectedUsers().put(playerName.trim(), protectedUplinkName);
		}
		try {
			writeProtectedUsersToDisk();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addProtectedUsersFromDisk() throws IOException {
		File l = new FileExt("./protected.txt");
		BufferedReader s = new BufferedReader(new FileReader(l));
		String line = null;
		while ((line = s.readLine()) != null) {
			synchronized (state.getProtectedUsers()) {
				try {
					String[] sp = line.split(";");
					long t = System.currentTimeMillis();
					if (sp.length > 2) {
						try {
							t = Long.parseLong(sp[2]);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
					ProtectedUplinkName p = new ProtectedUplinkName(sp[1].trim(), sp[0], t);
					state.getProtectedUsers().put(sp[0].toLowerCase(Locale.ENGLISH), p);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("EXCEPTION: Could not protect " + line + " invalid format");
				}
			}
		}
		s.close();
	}

	private void addWhitelistFromDisk() throws IOException {
		File l = new FileExt("./whitelist.txt");
		BufferedReader s = new BufferedReader(new FileReader(l));
		String line = null;
		while ((line = s.readLine()) != null) {
			if (line.startsWith("nm:")) {
				synchronized (state.getWhiteListedNames()) {
					state.getWhiteListedNames().add(new PlayerAccountEntry(line.substring(3).trim().toLowerCase(Locale.ENGLISH)));
				}
			} else if (line.startsWith("ip:")) {
				synchronized (state.getWhiteListedIps()) {
					String trim = line.substring(3).trim();
					try {
						checkIP(trim);
						state.getWhiteListedIps().add(new PlayerAccountEntry(trim));
					} catch (NoIPException e) {
						e.printStackTrace();
					}
				}
			} else if (line.startsWith("ac:")) {
				synchronized (state.getWhiteListedAccounts()) {
					state.getWhiteListedAccounts().add(new PlayerAccountEntry(line.substring(3).trim()));
				}
			}else {
				//new version of banning/whitelisting
				if (line.startsWith("nmt:")) {
					try{
						String[] split = line.split(":", 3);

						long validUntil = Long.parseLong(split[1]);
						synchronized (state.getWhiteListedNames()) {
							PlayerAccountEntry playerAccountEntry = new PlayerAccountEntry(validUntil, split[2].trim().toLowerCase(Locale.ENGLISH));
							if(playerAccountEntry.isValid(System.currentTimeMillis())){
								state.getWhiteListedNames().add(playerAccountEntry);
							}
						}
					}catch(NumberFormatException e){
						e.printStackTrace();
						System.err.println("ERROR IN WHITELIST ENTRY. Valid until must be a date in ms. Line: "+line);
					}catch(Exception e){
						e.printStackTrace();
						System.err.println("ERROR IN WHITELIST ENTRY. Wrong format. Line: "+line);
					}
				} else if (line.startsWith("ipt:")) {
					try{
						String[] split = line.split(":", 3);
						long validUntil = Long.parseLong(split[1]);
						synchronized (state.getWhiteListedIps()) {
							String trim = split[2].trim();
							try {
								checkIP(trim);
								PlayerAccountEntry playerAccountEntry = new PlayerAccountEntry(validUntil, trim);

								if(playerAccountEntry.isValid(System.currentTimeMillis())){
									state.getWhiteListedIps().add(playerAccountEntry);
								}
							} catch (NoIPException e) {
								e.printStackTrace();
							}
						}
					}catch(NumberFormatException e){
						e.printStackTrace();
						System.err.println("ERROR IN WHITELIST ENTRY. Valid until must be a date in ms. Line: "+line);
					}catch(Exception e){
						e.printStackTrace();
						System.err.println("ERROR IN WHITELIST ENTRY. Wrong format. Line: "+line);
					}
				} else if (line.startsWith("act:")) {
					try{
						String[] split = line.split(":", 3);
						long validUntil = Long.parseLong(split[1]);
						synchronized (state.getWhiteListedAccounts()) {
							PlayerAccountEntry playerAccountEntry = new PlayerAccountEntry(validUntil, split[2].trim());

							if(playerAccountEntry.isValid(System.currentTimeMillis())){
								state.getWhiteListedAccounts().add(playerAccountEntry);
							}
						}
					}catch(NumberFormatException e){
						e.printStackTrace();
						System.err.println("ERROR IN WHITELIST ENTRY. Valid until must be a date in ms. Line: "+line);
					}catch(Exception e){
						e.printStackTrace();
						System.err.println("ERROR IN WHITELIST ENTRY. Wrong format. Line: "+line);
					}
				}

			}

		}
		s.close();

	}

	public void addWitelistedIp(String playerIp, long validUntil) throws NoIPException {
		checkIP(playerIp);
		boolean add = false;
		synchronized (state.getWhiteListedIps()) {
			add = state.getWhiteListedIps().add(new PlayerAccountEntry(validUntil, playerIp.trim()));
		}
		if (add) {
			try {
				writeWhiteListToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void addWitelistedName(String playerName, long validUntil) {
		boolean add = false;
		synchronized (state.getWhiteListedNames()) {
			add = state.getWhiteListedNames().add(new PlayerAccountEntry(validUntil, playerName.trim().toLowerCase(Locale.ENGLISH)));
		}
		if (add) {
			try {
				writeWhiteListToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void addWitelistedAccount(String accName, long validUntil) {
		boolean add = false;
		synchronized (state.getWhiteListedAccounts()) {
			add = state.getWhiteListedAccounts().add(new PlayerAccountEntry(validUntil, accName.trim()));
		}
		if (add) {
			try {
				writeWhiteListToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public boolean authenticate(String playerName, SessionCallback sessionCallback) throws AuthenticationRequiredException {
		System.err.println("[AUTH] authenticating " + playerName + "; useAuth: " + ServerConfig.USE_STARMADE_AUTHENTICATION.isOn() + "; requireAuth: " + ServerConfig.REQUIRE_STARMADE_AUTHENTICATION.isOn());
		LogUtil.log().fine("[AUTH] authenticating " + playerName + "; useAuth: " + ServerConfig.USE_STARMADE_AUTHENTICATION.isOn() + "; requireAuth: " + ServerConfig.REQUIRE_STARMADE_AUTHENTICATION.isOn());
		if (!authSession(playerName, sessionCallback, ServerConfig.REQUIRE_STARMADE_AUTHENTICATION.isOn(), ServerConfig.USE_STARMADE_AUTHENTICATION.isOn(), isUserProtected(playerName))) {
			//			System.err.println("[AUTH] "+playerName+"; "+sessionId+"; "+sessionName+" failed");
			throw new IllegalArgumentException("AUTH FAILED");
		}

		return true;
	}

	@Override
	public void protectUserName(String playerName, SessionCallback sessionCallback) {

		if (sessionCallback.getStarMadeUserName() != null && sessionCallback.getStarMadeUserName().length() > 0) {
			String uplinkUser;
			addProtectedUser(sessionCallback.getStarMadeUserName(), playerName.toLowerCase(Locale.ENGLISH));
		}
	}
	private void afterShutdownPhase() {

	}
	@Override
	public void update(Timer timer) throws IOException, AdminCommandNotFoundException, SQLException {
		state.udpateTime = System.currentTimeMillis();
		ServerInfo.curtime = state.udpateTime;
		GameServerState.totalDockingChecks = SegmentController.dockingChecks;

		SegmentController.dockingChecks = 0;


		if (ServerState.isFlagShutdown()) {
			ServerState.setFlagShutdown(false);

			this.flaggedShutdown = true;
			LogUtil.log().fine("[SHUTDOWN] Shutting down server");

			System.out.println("[SERVER] now executing regular shutdown");
			ServerState.setShutdown(true);
			onShutDown(false);

			return;
		}

		synchronized (state) {
			state.setSynched();
			state.incUpdateNumber();

			state.getNetworkManager().update();

			assert(!flaggedShutdown);

			if (state.getTimedShutdownStart() > 0) {
				long elapsed = System.currentTimeMillis() - state.getTimedShutdownStart();
				float leftInSeconds = (state.getTimedShutdownSeconds() * 1000 - elapsed) / 1000f;

				if (leftInSeconds > 0) {
					synchronized(state){
						//synch for network field set
						state.setSynched();
						try{
							if (leftInSeconds > 60 &&
									(int) ((float) state.getGameState().getNetworkObject().serverShutdown.get()) != ((int) leftInSeconds)) {
								state.getGameState().getNetworkObject().serverShutdown.set(leftInSeconds);
							} else {
								state.getGameState().getNetworkObject().serverShutdown.set(leftInSeconds);
							}
						}finally{
							state.setUnsynched();
						}
					}
				} else {
					System.err.println("[SERVER] SCHEDULED SHUTDOWN NOW EXECUTING. EXITING SERVER!");
					ServerState.setFlagShutdown(true);
					return;
				}
			}

			if (state.getTimedMessageStart() > 0) {
				synchronized (state) {
					state.setSynched();
					try {
						long elapsed = System.currentTimeMillis() - state.getTimedMessageStart();
						float leftInSeconds = (state.getTimedMessageSeconds() * 1000 - elapsed) / 1000f;

						if (leftInSeconds > 0) {

							state.getGameState().getNetworkObject().serverCountdownMessage.set(state.getTimedMessage());
							if (leftInSeconds > 60 &&
									(int) ((float) state.getGameState().getNetworkObject().serverCountdownTime.get()) != ((int) leftInSeconds)) {
								state.getGameState().getNetworkObject().serverCountdownTime.set(leftInSeconds);
							} else {
								state.getGameState().getNetworkObject().serverCountdownTime.set(leftInSeconds);
							}
						} else {
							state.setTimedMessageStart(-1);
							state.getGameState().getNetworkObject().serverCountdownTime.set(-1f);
							state.getGameState().getNetworkObject().serverCountdownMessage.set("");

							return;
						}
					} finally {
						state.setUnsynched();
					}
				}
			}

			if (GameServerState.allocatedSegmentData > 0) {
				GameServerState.lastAllocatedSegmentData = GameServerState.allocatedSegmentData;
			}
			GameServerState.lastFreeSegmentData = getServerState().getSegmentDataManager().sizeFree();

			GameServerState.allocatedSegmentData = 0;

			GameServerState.collectionUpdates = 0;

			GameServerState.segmentRequestQueue = state.getSegmentRequests().size();
			GameServerState.dayTime = ((GameStateInterface) state).getGameState().getRotationProgession();

			//		Octree.clearCache(state, true);
			if (endRoundMode > 0) {
				handleEndRound();
			}
			GlUtil.gluPerspective(projectionMatrix, 75f,
					1.3333333f, 0.1f, 1500f, false);
			int autosaveMS = (ServerConfig.SECTOR_AUTOSAVE_SEC.getInt()) * 1000;
			if (System.currentTimeMillis() > state.delayAutosave && autosaveMS > 0 && (System.currentTimeMillis() - lastSave) > autosaveMS) {

				System.out.println("[SERVER] SERVER AUTOSAVE START. Dumping server State!");

				broadcastMessage(Lng.astr("SERVER AUTO-SAVING"), ServerMessage.MESSAGE_TYPE_WARNING);

				writeEntitiesToDatabaseAtAutosaveOrShutdown(false, false, state.getUniverse());
				System.out.println("[SERVER] SERVER AUTOSAVE END. ServerState saved!");
				lastSave = System.currentTimeMillis();
			} else if (System.currentTimeMillis() < state.delayAutosave) {
				if (System.currentTimeMillis() - lastDelayAutoSaveMsgSent > 60000) {
					broadcastMessage(Lng.astr("Server not saving:\ndelayed by admin\n(left %ssec)",  (state.delayAutosave - System.currentTimeMillis()) / 1000), ServerMessage.MESSAGE_TYPE_WARNING);
					lastDelayAutoSaveMsgSent = System.currentTimeMillis();
				}
			}

			if (!state.getFileRequests().isEmpty()) {
				synchronized (state) {
					state.setSynched();
					synchronized (state.getFileRequests()) {
						if (!state.getFileRequests().isEmpty()) {
							FileRequest r = state.getFileRequests().remove(0);
							if (state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(r.channel.getPlayerId())) {
								try {

									String path = SkinManager.serverDB + r.req;

									System.err.println("[SERVER] transferring file to client " + r.channel.getId() + ": " + path);
									r.channel.getClientFileDownloadController().upload(path);
									state.getActiveFileRequests().add(r);
								} catch (UploadInProgressException e) {
									System.err.println("[SERVER] Cannot upload " + r.req + ": an upload already is in progress for channel " + r.channel.getId());
									state.getFileRequests().add(r);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
					state.setUnsynched();
				}
			}
			if (!state.getActiveFileRequests().isEmpty()) {
				synchronized (state) {
					state.setSynched();
					synchronized (state.getActiveFileRequests()) {
						for (int i = 0; i < state.getActiveFileRequests().size(); i++) {
							FileRequest fileRequest = state.getActiveFileRequests().get(i);
							if (state.getPlayerStatesByName().values().contains(fileRequest.channel.getPlayer()) && fileRequest.channel.getClientFileDownloadController().isNeedsUpdate()) {
								fileRequest.channel.updateLocal(timer);
							} else {
								state.getActiveFileRequests().remove(i);
								i--;
							}
						}
					}
					state.setUnsynched();
				}
			}

			if (!state.getServerExecutionJobs().isEmpty()) {
				synchronized (state.getServerExecutionJobs()) {
					while (!state.getServerExecutionJobs().isEmpty()) {
						state.getServerExecutionJobs().dequeue().execute(state);
					}
				}
			}

			if (!state.pathFindingCallbacks.isEmpty()) {
				synchronized (state.pathFindingCallbacks) {
					while (!state.pathFindingCallbacks.isEmpty()) {
						AbstractPathFindingHandler<?, ?> dequeue = state.pathFindingCallbacks.dequeue();
						dequeue.handleReturn();
						//notify the thread to be able to continue
					}
					state.pathFindingCallbacks.notifyAll();
				}

			}
			if (!state.creatureSpawns.isEmpty()) {
				synchronized (state.creatureSpawns) {
					while (!state.creatureSpawns.isEmpty()) {
						state.creatureSpawns.dequeue().execute(state);
					}
				}
			}


			if (!getSystemInQueue().isEmpty()) {
				synchronized (getSystemInQueue()) {
					while (!getSystemInQueue().isEmpty()) {
						handleSystemIn(getSystemInQueue().dequeue());
					}
				}
			}

			if (!state.getAdminCommands().isEmpty()) {
				ArrayList<AdminCommandQueueElement> copy = new ArrayList<AdminCommandQueueElement>(state.getAdminCommands().size());
				synchronized (state.getAdminCommands()) {
					copy.addAll(state.getAdminCommands());
					state.getAdminCommands().clear();
				}

				while (!copy.isEmpty()) {
					AdminCommandQueueElement q = copy.remove(0);
					q.execute(state);

				}

			}

			if (state.toLoadSectorsQueue != null && !state.toLoadSectorsQueue.isEmpty()) {

				for (int i = 0; i < 8 && !state.toLoadSectorsQueue.isEmpty(); i++) {
					Sector sector = state.getUniverse().getSector(state.toLoadSectorsQueue.dequeue());
				}
			}



			if (!toRemoveClients.isEmpty()) {
				long t0 = System.currentTimeMillis();
				synchronized (toRemoveClients) {
					for (int id : toRemoveClients) {
						System.err.println("[SERVER] logging out client " + id);
						RegisteredClientOnServer remove = state.getClients().remove(id);
						if (remove == null) {
							System.err.println("[SERVER][WARNING] Exception cound NOT remove client with ID " + id + ": " + state.getClients());
						} else {
							System.err.println("[SERVER] successfully removed client with ID " + id);
							remove.getProcessor().disconnect();
							onLoggedout(remove);
						}

					}
					toRemoveClients.clear();
				}
				long took = System.currentTimeMillis() - t0;
				if (took > 30) {
					System.err.println("[SERVER][UPDATE] WARNING: handleRemoveClients update took " + took);
				}
				for(ServerClientChangeListener l : clientChangeListeners) {
					l.onClientsChanged();
				}
			}

			for (RegisteredClientOnServer r : state.getClients().values()) {

				r.getSynchController().handleQueuedSynchronizedObjects();
				if (!r.checkConnection()) {
					System.err.println("[SERVER][WARNING] #### client not connected anymore: removing " + r.getClientName() + "; ID: " + r.getId() + "; IP: " + r.getIp());
					toRemoveClients.add(r.getId());
				}
			}

			sendMessages(state.getClients().values());



			state.getChannelRouter().update(timer);

			state.handleAddedAndRemovedObjects();

			checkExplosionOrders();

			mineController.updateLocal(timer);
			state.getSimulationManager().update(timer);

			state.getFleetManager().update(timer);

			if (!state.getBluePrintsToSpawn().isEmpty()) {
				long t0 = System.currentTimeMillis();
				synchronized (state.getBluePrintsToSpawn()) {
					while (!state.getBluePrintsToSpawn().isEmpty()) {
						final SegmentControllerOutline<?> outline = state.getBluePrintsToSpawn().remove(0);
						try {
							outline.checkOkName();
							String log = "[BLUEPRINT][LOAD] " + outline.playerUID + " loaded " + outline.en.getName() + " as \"" + outline.realName + "\"" + " in " + outline.spawnSectorId + " as faction " + outline.getFactionId();
							LogUtil.log().fine(log);
							System.err.println(log);
							outline.spawn(outline.spawnSectorId, outline.checkProspectedBlockCount, new ChildStats(false), new SegmentControllerSpawnCallbackDirect(state, outline.spawnSectorId) {
								@Override
								public void onNoDocker() {
									PlayerState pl = state.getPlayerFromNameIgnoreCaseWOException(outline.playerUID);
									if(pl != null) {
										pl.sendServerMessagePlayerError(Lng.astr("No docker blocks on blueprint!"));
										System.err.println("[BLUEPRINT] no docker blocks on blueprint "+outline);
									}else {
										System.err.println("[BLUEPRINT] no docker blocks on blueprint (not a player spawner "+outline.playerUID+") "+outline);
									}
								}
							});
						} catch (EntityAlreadyExistsException e) {
							e.printStackTrace();
							try {
								PlayerState playerFromName = state.getPlayerFromName(outline.playerUID);
								playerFromName.sendServerMessage(new ServerMessage(Lng.astr("Name already exists!"), ServerMessage.MESSAGE_TYPE_ERROR, playerFromName.getId()));
							} catch (PlayerNotFountException e1) {
								e1.printStackTrace();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				long took = System.currentTimeMillis() - t0;
				if (took > 30) {
					System.err.println("[SERVER][UPDATE] WARNING: blueprintstoSpawn update took " + took);
				}
			}

			if (!state.getSegmentRequestsLoaded().isEmpty()) {
				int maxHandlingPerUpdate = 1000;
				int i = 0;
				while (!state.getSegmentRequestsLoaded().isEmpty() && i < maxHandlingPerUpdate) {
					synchronized (state.getSegmentRequestsLoaded()) {
						ServerSegmentRequest dequeue = state.getSegmentRequestsLoaded().dequeue();
						try{
							handleLoadedRequest(dequeue);
						}catch(ArrayIndexOutOfBoundsException e){
							e.printStackTrace();
							state.getSegmentRequestsLoaded().enqueue(dequeue);
						}
					}
					i++;
				}

			}

			synchController.handleQueuedSynchronizedObjects();


			state.getGameMapProvider().updateServer();

			if (!state.getScheduledUpdates().isEmpty()) {
				synchronized (state.getScheduledUpdates()) {
					while (!state.getScheduledUpdates().isEmpty()) {
						Sendable remove = state.getScheduledUpdates().remove(0);
						remove.updateLocal(timer);
					}
				}
			}
			boolean anyEntitySectorSwitched = false;
			{
				long t0 = System.currentTimeMillis();

				for (int i = 0; i < getServerState().getSectorSwitches().size(); i++) {
					SectorSwitch sw = getServerState().getSectorSwitches().get(i);
					if (sw.delay <= 0 || state.getUpdateTime() > sw.delay) {
						sw.execute(getServerState());
						getServerState().getSectorSwitches().remove(i);
						i--;
						anyEntitySectorSwitched = true;
					}
				}

				long took = System.currentTimeMillis() - t0;
				if (took > 30) {
					System.err.println("[SERVER][UPDATE] WARNING: sectorSwitchExecutions update took " + took);
				}
			}

			if (!state.getSpawnRequestsReady().isEmpty()) {
				synchronized (state.getSpawnRequestsReady()) {
					for (PlayerState playerState : state.getSpawnRequestsReady()) {
						spawnPlayerCharacter(playerState);
					}
					state.getSpawnRequestsReady().clear();
				}
			}
			if (!state.getSpawnRequests().isEmpty()) {
				synchronized (state.getSpawnRequests()) {
					for (PlayerState playerState : state.getSpawnRequests()) {
						spawnPlayerCharacterPrepare(playerState);
					}
					state.getSpawnRequests().clear();
				}
			}
			{

				long tU = System.currentTimeMillis();
				getServerState().getUniverse().update(timer, sectorLag);
				sectorLag.clear();
				long tookUniverse = System.currentTimeMillis() - tU;
				if (tookUniverse > 30) {
					System.err.println("[SERVER][UPDATE] WARNING: UNIVERSE update took " + tookUniverse);
				}
			}

			synchronized (getServerState().getLocalAndRemoteObjectContainer().getLocalObjects()) {


				state.getMetaObjectManager().updateLocal(timer);

				executeWaves();

				missileController.updateServer(timer);

				if (getServerState().isFactionReinstitudeFlag()) {

					for (Faction f : getServerState().getFactionManager().getFactionCollection()) {
						for (FactionPermission p : f.getMembersUID().values()) {
							PlayerState playerFromName;
							try {
								playerFromName = getServerState().getPlayerFromName(p.playerUID);

								if (playerFromName != null) {
									playerFromName.getFactionController().setFactionId(f.getIdFaction());
								}
							} catch (PlayerNotFountException e) {
								e.printStackTrace();
							}
						}
					}

					getServerState().setFactionReinstitudeFlag(false);
				}
				{
//					sectorsToUpdate.clear();
//					RemoteSector.getUpdateSet(this.getState(), sectorsToUpdate);
//
//					for (RemoteSector rs : sectorsToUpdate) {
//						rs.update(timer);
//					}

					long t0 = System.currentTimeMillis();

					ObjectCollection<Sendable> values;
					//					if(System.currentTimeMillis() - lastFullUpdate < 5000){
					values = getServerState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values();
					//					}else{
					//						values = getServerState().getLocalAndRemoteObjectContainer().getLocalObjects().values();
					//						lastFullUpdate = System.currentTimeMillis();
					//					}
					state.activeSectors.clear();
					for (Sendable s : values) {
						assert (s != null) : s;
						if(s == null){
							throw new NullPointerException();
						}



						try {

							if (s instanceof SegmentController) {
								SegmentController seg = (SegmentController)s;
								seg.getSegmentBuffer().updateNumber();
								if(anyEntitySectorSwitched) {
									seg.getRuleEntityManager().triggerAnyEntitySectorSwitched();
									seg.getRuleEntityManager().triggerSectorEntitiesChanged();
								}
							}
							if (s instanceof SimpleTransformableSendableObject && !state.getUniverse().getIsSectorActive(((SimpleTransformableSendableObject) s).getSectorId())) {
								continue;
							}

							assert (!(s instanceof Physical) || (((Physical) s).getPhysicsDataContainer().getObject() == null || ((Physical) s).getPhysicsDataContainer().getObject() instanceof GamePhysicsObject)) : ((Physical) s).getPhysicsDataContainer().getObject();
							long t1 = System.currentTimeMillis();

							s.updateLocal(timer);


							long took = System.currentTimeMillis() - t1;
							if(s instanceof SimpleTransformableSendableObject){
								sectorLag.addTo(((SimpleTransformableSendableObject) s).getSectorId(), took);
							}
							if (took > 50) {
								System.err.println("[SERVER][UPDATE] WARNING: object local update of " + s + " took " + took);
								s.announceLag(took);
							}

						} catch (Exception e) {
							e.printStackTrace();
							if (exceptionSet.contains(e.getClass())) {
								int x = 0;
								String es = "./logs/exceptionlog" + x + ".txt";
								while ((new FileExt(es)).exists()) {
									x++;
									es = "./logs/exceptionlog" + x + ".txt";
									System.err.println("Finding exception file " + es);
								}

								FileUtil.copyFile(new FileExt("./logs/log.txt.0"), new FileExt(es));
							}
							System.err.println("[SERVER] Exception catched ");
							if (System.currentTimeMillis() - lastExceptionTime > 5000 || !lastMessage.equals(e.getClass().getSimpleName())) {
								broadcastMessage(Lng.astr("Error occured on Server!\nPlease send in server error report.\n%s",  e.getClass().getSimpleName()), ServerMessage.MESSAGE_TYPE_ERROR);
								lastExceptionTime = System.currentTimeMillis();
								lastMessage = e.getClass().getSimpleName();
							}
						}
					}

					IntArrayList removed = new IntArrayList();
					for (int sectorId : state.activeSectors) {
						Sendable s = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);
						if (s != null && s instanceof RemoteSector) {
							s.updateLocal(timer);
						} else {
							System.err.println("[SERVER] Exception: active sector " + sectorId + " could not be updated. not loaded: " + s + "; Removing sector id from active sector set...");
							removed.add(sectorId);
						}
					}
					long took = System.currentTimeMillis() - t0;
					if (took > 50) {
						System.err.println("[SERVER][UPDATE] WARNING: object local update of " + values.size() + " objects took " + took);
					}
					state.activeSectors.removeAll(removed);

//					for(Sendable s : values){
//						if(s instanceof SimpleTransformableSendableObject){
//							((SimpleTransformableSendableObject)s).getPhysicsDataContainer().lastTransform.set(((SimpleTransformableSendableObject)s).getWorldTransform());
//						}
//					}

					for (Sendable s : values) {
						if (s instanceof SimpleTransformableSendableObject) {
							((SimpleTransformableSendableObject) s).getPhysicsDataContainer().lastTransform.set(((SimpleTransformableSendableObject) s).getPhysicsDataContainer().thisTransform);
							((SimpleTransformableSendableObject) s).getPhysicsDataContainer().thisTransform.set(((SimpleTransformableSendableObject) s).getWorldTransform());
						}
					}
				}

				if (getBlockBehaviorChanged() != null) {

					File f = new FileExt(BLOCK_BEHAVIOR_DEFAULT_PATH);
					if (f.exists()) {
						FileUtil.backupFile(f);
					}
					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(BLOCK_BEHAVIOR_DEFAULT_PATH));
					fos.write(getBlockBehaviorChanged());
					fos.flush();
					fos.close();

					parseBlockBehavior(BLOCK_BEHAVIOR_DEFAULT_PATH);

					System.err.println("[SERVER] a new block behavior has been received. Applying to all entities, and deligating to all clients");
					for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
						if (s instanceof ManagedSegmentController<?>) {
							((ManagedSegmentController<?>) s).getManagerContainer().reparseBlockBehavior(true);
						}
					}
					getServerState().getGameState().setServerDeployedBlockBehavior(true);

					blockBehaviorChanged = null;
				}

			}
			if (state.getGameState().getGameModes().size() > 0) {
				String output = "";
				for (AbstractGameMode mode : state.getGameState().getGameModes()) {

					try {
						mode.update(timer);
						output += mode.getCurrentOutput() + "\n";
					} catch (GameModeException e) {
						throw new RuntimeException(e);
					}
				}
				state.getGameState().getNetworkObject().gameModeMessage.set(output);
			} else {
				state.getGameState().getNetworkObject().gameModeMessage.set("");
			}

			state.getGameState().updateToNetworkObject();


			/*
			 * the only change to objects could happen from received packages this
			 * method will lock the processors to make sure it doesn't happen, since
			 * all encode methods will lock local and remote containers.
			 * The receiving methods will do the same
			 */
			synchronize(state.getClients().values());

			state.setUnsynched();
		}
		if (!state.getCreatorHooks().isEmpty()) {
			synchronized (state) {
				state.setSynched();
				synchronized (state.getCreatorHooks()) {
					for (int i = 0; i < state.getCreatorHooks().size(); i++) {
						state.getCreatorHooks().get(i).execute();
					}
					state.getCreatorHooks().clear();
				}
				state.setUnsynched();
			}
		}


		BlockBulkSerialization.freeUsedServerPool();
		lastSynchronize = System.currentTimeMillis();

		GameServerState.updateAllShopPricesFlag = false;

		//INSERTED CODE
		StarRunnable.tickAll(false);
		///


		if(Starter.CONFIG_ONLY) {
			System.out.println("[SERVER] THIS IS A CONFIG ONLY RUN. SHUTTING DOWN SERVER AFTER ONE UPDATE!");
			ServerState.setFlagShutdown(true);
		}

	}

	private boolean authSession(String playerName, SessionCallback sessionCallback, boolean requireAuth, boolean useAuthProtect, boolean isUserProtected) throws AuthenticationRequiredException {

		return sessionCallback.authorize(playerName, this, requireAuth, useAuthProtect, isUserProtected);

	}

	@Override
	public long getServerRunningTime() {
		long sStart = state.getServerStartTime();
		return System.currentTimeMillis() - sStart;
	}

	@Override
	public long calculateStartTime() {
		if (ServerConfig.UNIVERSE_DAY_IN_MS.getInt() == -1) {
			return -1;
		} else {
			long t = (state.getServerStartTime() - ServerConfig.UNIVERSE_DAY_IN_MS.getInt()
					+ (state.getServerTimeMod())) - getServerState().getServerTimeDifference();
			//		System.err.println("[SERVER] SST: "+state.getServerStartTime()+"; "+state.getServerTimeMod()+"; "+state.getServerTimeDifference()+" ---> "+t);

			return t;
		}
	}

	@Override
	public long getUniverseDayInMs() {
		return ServerConfig.UNIVERSE_DAY_IN_MS.getInt();
	}

	@Override
	public void onRemoveEntity(Sendable remove) {

	}

	private boolean clearWorld() {
		System.err.println("SERVER CLEARING WORLD");
		boolean worldCleared = true;
		purgeDB();
		synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (s instanceof PlayerState) {

					((PlayerState) s).handleServerHealthAndCheckAliveOnServer(0, (PlayerState) s);
				}
				if (s instanceof SegmentController) {
					worldCleared = false;
					s.setMarkedForDeleteVolatile(true);
				}
			}
		}
		return worldCleared;

	}

	private void createAdminFile() throws IOException {
		File l = new FileExt("./admins.txt");
		if (!l.exists()) {
			l.createNewFile();
		}
	}

	private void createBlacklistFile() throws IOException {
		File l = new FileExt("./blacklist.txt");
		if (!l.exists()) {
			l.createNewFile();
		}
	}
	public void despawn(String name) throws SQLException{
		System.err.println("[DESPAWN] despawning: "+name);
		for (Sendable s : state.getLocalAndRemoteObjectContainer()
				.getLocalObjects().values()) {
			if (s instanceof SegmentController) {
				if (DatabaseEntry.removePrefixWOException(((SegmentController) s).getUniqueIdentifier()).equals(name)) {
					s.markForPermanentDelete(true);
					s.setMarkedForDeleteVolatile(true);
					System.err.println("[DESPAWN] despawning loaded: "+name);
					return;
				}
			}
		}
		String escSer = DatabaseIndex.escape(name)+"%";

		int count = state.getDatabaseIndex().getTableManager().getEntityTable().despawn(
				escSer, EntityTable.Despawn.ALL, null, null);
		if(count > 0){
			System.err.println("[DESPAWN] despawned "+count+" from DB using escaped matching string '" + escSer + "'");
		}
	}
	private void createProtectedUserFile() throws IOException {
		File l = new FileExt("./protected.txt");
		if (!l.exists()) {
			l.createNewFile();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.server.ServerController#createThreadDump()
	 */
	@Override
	public void createThreadDump() {
		try {
			CrashReporter.createThreadDump("threadDumpFrozen");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void displayError(Exception e) {
		if (Starter.sGUI != null) {
			GuiErrorHandler.processErrorDialogException(e);
		}
	}

	/**
	 * @return the serverState
	 */
	@Override
	public GameServerState getServerState() {
		return (GameServerState) super.getServerState();
	}

	@Override
	public void initializeServerState() throws IOException, SQLException, NoSuchAlgorithmException, ResourceException, ParseException, SAXException, ParserConfigurationException {

		createVersionFile();

		parseBlockBehavior(BLOCK_BEHAVIOR_DEFAULT_PATH);

		state.setConfigCheckSum(FileUtil.getSha1Checksum("./data/config/BlockConfig.xml"));
		state.setFactionConfigCheckSum(FileUtil.getSha1Checksum("./data/config/FactionConfig.xml"));
		state.setConfigPropertiesCheckSum(FileUtil.getSha1Checksum("./data/config/BlockTypes.properties"));

		state.setCustomTexturesChecksum(FileUtil.createFilesHashRecursively(GameResourceLoader.CUSTOM_TEXTURE_PATH, pathname -> {
			boolean accept = pathname.isDirectory() || pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".png");
//				System.err.println("ACCEPT: "+pathname+": "+accept);
			return accept;
		}));
		RandomAccessFile fa = new RandomAccessFile(GameResourceLoader.CUSTOM_TEXTURE_PATH + "pack.zip", "r");
		byte[] bcx = new byte[(int) fa.length()];
		fa.read(bcx);
		fa.close();
		state.setCustomTexturesFile(bcx);

		ObjectArrayList<ResourceLoadEntry> l = new ObjectArrayList<ResourceLoadEntry>();
		ResourceLoader.loadModelConfig(l);

		state.setResourceMap(new ResourceMap());
		state.getResourceMap().initForServer(l);

		RandomAccessFile ff = new RandomAccessFile("./data/config/FactionConfig.xml", "r");
		byte[] bcF = new byte[(int) ff.length()];
		ff.read(bcF);
		ff.close();

		state.setFactionConfigFile(bcF);

		RandomAccessFile f = new RandomAccessFile("./data/config/BlockConfig.xml", "r");
		byte[] bc = new byte[(int) f.length()];
		f.read(bc);
		f.close();

		state.setBlockConfigFile(bc);

		RandomAccessFile fp = new RandomAccessFile("./data/config/BlockTypes.properties", "r");
		byte[] bpc = new byte[(int) fp.length()];
		fp.read(bpc);
		fp.close();
		state.setBlockPropertiesFile(bpc);

		try {
			addAdminsFromDisk();
			addBlacklistFromDisk();
			addWhitelistFromDisk();
			addProtectedUsersFromDisk();
		} catch (IOException e) {
			createAdminFile();
			createBlacklistFile();
			createWhitelistFile();
			createProtectedUserFile();
		}
		;

//		ChatSystem chatSystem = new ChatSystem(state);
//		chatSystem.setId(state.getNextFreeObjectId());
//		chatSystem.initialize();
//		state.setChat(chatSystem);
//		getSynchController().addNewSynchronizedObjectQueued(chatSystem);

		state.setGameState(new SendableGameState(state));
		state.getGameState().setId(state.getNextFreeObjectId());
		state.getGameState().initialize();
		state.getSimulationManager().initialize();
		synchController.addNewSynchronizedObjectQueued(state.getGameState());
		loadUniverse();
		if(GameCommon.isDedicatedServer()) AudioController.initialize();
		if (ServerConfig.BATTLE_MODE.isOn()) {
			state.getGameState().getGameModes().add(new BattleMode(state));
		}
	}

	@Override
	public boolean isAdmin(RegisteredClientOnServer client) {
		// no admins file -> everybody is admin

		return client != null && client.getPlayerObject() != null && (state.getAdmins().isEmpty() || isAdmin(client.getClientName().trim().toLowerCase(Locale.ENGLISH))
		);

	}

	@Override
	public boolean isBanned(RegisteredClientOnServer client, StringBuffer failReason) {
		System.err.println("[SERVER] checking ip ban: " + client.getIp());
		return state.getBlackListedNames().containsAndIsValid(client.getClientName().toLowerCase(Locale.ENGLISH), failReason)
				||
				state.getBlackListedIps().containsAndIsValid(
						client.getIp(), failReason) ||
				(client.getStarmadeName() != null &&
						state.getBlackListedAccounts().containsAndIsValid(
								client.getStarmadeName(), failReason));
	}

	@Override
	public boolean isWhiteListed(RegisteredClientOnServer client) {
		if (ServerConfig.USE_WHITELIST.isOn()) {
			System.err.println("[SERVER] checking whitelist: Name: " + client.getClientName() + "; IP: " + client.getIp() + "; SM-Account: " + client.getStarmadeName());
			return state.getWhiteListedNames().containsAndIsValid(client.getClientName().toLowerCase(Locale.ENGLISH)) ||
					state.getWhiteListedIps().containsAndIsValid(client.getIp()) ||
					(client.getStarmadeName() != null && state.getWhiteListedAccounts().containsAndIsValid(client.getStarmadeName()));
		} else {
			return true;
		}
	}

	@Override
	public LoginCode onLoggedIn(RegisteredClientOnServer client) throws Exception {

		PlayerState player = new PlayerState(state);
		player.setName(client.getClientName());
		player.setStarmadeName(client.getStarmadeName());
		player.setUpgradedAccount((client.isUpgradedAccount()));


		player.setId(state.getNextFreeObjectId());
		player.setClientId(client.getId());




		Sector defaultSec = ((GameServerState) player.getState()).getUniverse().getSector(
				new Vector3i(
						ServerConfig.DEFAULT_SPAWN_SECTOR_X.getInt(),
						ServerConfig.DEFAULT_SPAWN_SECTOR_Y.getInt(),
						ServerConfig.DEFAULT_SPAWN_SECTOR_Z.getInt()));
		player.setCurrentSectorId(defaultSec.getId());

		int uniquePlayerId = 0;

		player.readDatabase();

		try {
			Tag tag = readEntity(player.getUniqueIdentifier());
			player.fromTagStructure(tag);

			player.setPersonalSectors();

			player.spawnData.setFromLoadedSpawnPoint();

			System.err.println("[SERVER] player LOADED FROM DISK " + client.getClientName() + ", credits: " + player.getCredits()+"; Sector: "+player.getCurrentSector()+"; Invetory slots filled: "+player.getInventory().getCountFilledSlots()+"; last login: "+player.getLastLogin()+"; last logout: "+player.getLastLogout()+"; ");

		} catch (EntityNotFountException e) {
			player.setPersonalSectors();
			//set initial spawn point
			player.spawnData.setForNewPlayerOnLogin();

			//fill inventory with default stuff
			player.getInventoryController().fillInventory();


			;

		} catch (Exception e) {
			e.printStackTrace();
			try {
				GuiErrorHandler.processErrorDialogException(new Exception("Sorry :(, the system failed to load player " + client + ". Possible data corruption detected.\n" +
						"You may need to remove your player state from the StarMade/server-database directory, or reset the universe", e));
			} catch (Exception e1) {
			}

			throw e;
		}

		player.updateDatabase();


		player.initialize();



		System.err.println("[SERVER] adding player to synch queue (ID: " + client.getId() + ") (SECTOR: " + player.getCurrentSector() + " [" + player.getCurrentSectorId() + "])");
		synchController.addNewSynchronizedObjectQueued(player);
		client.setPlayerObject(player);


		for(Faction f : state.getFactionManager().getFactionCollection()){
			if(f.isNPC()){
				((NPCFaction)f).getDiplomacy().onPlayerJoined(player);
			}
		}

		Starter.modManager.onPlayerCreated(player);

		assert (player.getCurrentSectorId() != 0);
		//INSERTED CODE
		try {
			StarLoader.fireEvent(new PlayerJoinWorldEvent(client, player), true);
		} catch (NullPointerException exception) {
			exception.printStackTrace();
		}
		//
		return LoginCode.SUCCESS_LOGGED_IN;
	}

	@Override
	public void onLoggedout(RegisteredClientOnServer client) {
		if(!debugLogoutOnShutdown && this.flaggedShutdown){
			return;
		}
		System.err.println("[SERVER] onLoggedOut starting for " + client);

		LogUtil.log().fine("[LOGOUT] logging out client ID " + client);
		if (client != null) {
			synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {

				for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if (s instanceof PlayerState && ((PlayerState) s).getClientId() == client.getId()) {
						System.err.println("[SERVER][ClientLogout] saving and removing Player " + s);

						((PlayerState) s).spawnData.onLoggedOut();

						s.setMarkedForDeleteVolatile(true);

						Starter.modManager.onPlayerRemoved((PlayerState) s);

						state.getChannelRouter().onLogoff(((PlayerState) s));

						try {

							writeEntity(((PlayerState) s), true);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (SQLException e) {
							e.printStackTrace();
						}

					} else if (s instanceof PlayerCharacter && ((PlayerCharacter) s).getClientOwnerId() == client.getId()) {
						System.err.println("[SERVER][ClientLogout] deleting PLAYERCHARACTER " + s + "");
						s.setMarkedForDeleteVolatile(true);
					}
				}
			}
		} else {
			System.err.println("Skipping logout procedure for null client");
		}
		System.err.println("[SERVER] onLoggedOut DONE for " + client);

	}

	@Override
	protected void onShutDown(boolean emergency) throws IOException {
		//INSERTED CODE
		ModStarter.disableAllMods();
		PersistentObjectUtil.flushLogs(true);
		///
		//catch all exception to make sure we get to saving the state to disk
		synchronized(state){
			try{
				LogUtil.log().fine("[SHUTDOWN] shutting down element collection thread");
				System.out.println("[SERVER][SHUTDOWN] shutting down element collection thread");
				elementCollectionCalculationThreadManager.onStop();
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				LogUtil.log().fine("[SHUTDOWN] shutting down pathfinding threads");
				System.out.println("[SERVER][SHUTDOWN] shutting down pathfinding threads");
				segmentBreaker.shutdown();
				segmentPathFinder.shutdown();
				segmentPathGroundFinder.shutdown();
			}catch(Exception e){
				e.printStackTrace();
			}


			try{
				LogUtil.log().fine("[SHUTDOWN] shutting down universe");
				System.out.println("[SERVER][SHUTDOWN] shutting down universe");
				getServerState().getUniverse().onShutdown();
			}catch(Exception e){
				e.printStackTrace();
			}

			try{
				LogUtil.log().fine("[SHUTDOWN] shutting down segment request thread");
				System.out.println("[SERVER][SHUTDOWN] shutting segment request thread");
				serverSegmentRequestThread.shutdown();
			}catch(Exception e){
				e.printStackTrace();
			}

			try{
				LogUtil.log().fine("[SHUTDOWN] shutting down simulation");
				System.out.println("[SERVER][SHUTDOWN] shutting down simulation");
				state.getSimulationManager().shutdown();
			}catch(Exception e){
				e.printStackTrace();
			}

			try{
				LogUtil.log().fine("[SHUTDOWN] shutting down active checker");
				System.out.println("[SERVER][SHUTDOWN] shutting down active checker");
				serverActiveChecker.shutdown();
			}catch(Exception e){
				e.printStackTrace();
			}

			try{
				LogUtil.log().fine("[SHUTDOWN] shutting sysin listener");
				System.out.println("[SERVER][SHUTDOWN] shutting down sysin listener");
				systemInListener.setState(null);
			}catch(Exception e){
				e.printStackTrace();
			}

			try{
				LogUtil.log().fine("[SHUTDOWN] shutting down mob thread");
				System.out.println("[SERVER][SHUTDOWN] shutting down mob thread");
				state.getMobSpawnThread().shutdown();
			}catch(Exception e){
				e.printStackTrace();
			}

			try{
				LogUtil.log().fine("[SHUTDOWN] shutting down game map provider");
				System.out.println("[SERVER][SHUTDOWN] shutting down game map provider");
				state.getGameMapProvider().shutdown();
			}catch(Exception e){
				e.printStackTrace();
			}

			LogUtil.log().fine("[SHUTDOWN] server stop listening");
			System.out.println("[SERVER][SHUTDOWN] Stopping to listen!");

			try{
				stopListening();
			}catch(Exception e){
				e.printStackTrace();
			}
			LogUtil.log().fine("[SHUTDOWN] disconnecting all clients");
			System.out.println("[SERVER][SHUTDOWN] disconnecting all clients!");

			try{
				for(RegisteredClientOnServer r : getServerState().getClients().values()){
					try{
						r.getProcessor().disconnect();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}


			LogUtil.log().fine("[SHUTDOWN] writing current universe STARTED");
			System.out.println("[SERVER][SHUTDOWN] Dumping server State!");
			try {
				writeEntitiesToDatabaseAtAutosaveOrShutdown(true, false, getServerState().getUniverse());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		state.getThreadQueue().shutdown();
		System.out.println("[SERVER] WAITING FOR WRITING QUEUE TO FINISH; Active: " + state.getThreadQueue().getActiveCount());
		while (state.getThreadQueue().getActiveCount() > 0) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[SERVER] SERVER THREAD QUEUE TERMINATED");

		System.out.println("[SERVER][SHUTDOWN] Player Entities Written, Database updated!");
		try {
			int i = 0;
			while (state.getThreadQueue().getActiveCount() > 0) {
				System.out.println("[SERVER][SHUTDOWN] waiting for " + state.getThreadQueue().getActiveCount() + " queued writing jobs to finish");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				i++;

				if((i*10) > 300){
					System.err.println("Exception: WAITED LONGER THAN 5 minutes for final write. Something went wrong!");
					break;
				}
			}
			System.out.println("[SERVER][SHUTDOWN] all queued writing jobs finished. closing database");
			state.getDatabaseIndex().commit();
			if (emergency) {
				System.out.println("[SERVER][SHUTDOWN] Emergency shudown: cannot close database is an ordered mannor (threads might be interrupted unexpected)");
				System.out.println("[SERVER][SHUTDOWN] Emergency shudown: leaving database to recover on next server start");
			} else {
				state.getDatabaseIndex().destroy();
			}
			LogUtil.log().fine("[SHUTDOWN] database closed successfully");
			System.out.println("[SERVER][SHUTDOWN] database closed successfully");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		LogUtil.log().fine("[SHUTDOWN] writing current universe FINISHED");
		System.out.println("[SERVER][SHUTDOWN] ServerState saved!");



		if(state.getThreadedSegmentWriter() != null){
			state.getThreadedSegmentWriter().shutdown();
		}
		state.getGameState().onStop();
		state.getThreadQueue().shutdown();


		IOFileManager.cleanUp(true);

		ServerState.setCreated(false);

		System.out.println("[SERVER][SHUTDOWN] ServerState.created set to "+ServerState.isCreated());

		boolean setFinished = ClientState.setFinishedFrameAfterLocalServerShutdown;
		if(setFinished){
			//if(!GraphicsContext.isFinished()){
			//	LogUtil.closeAll();
			//}
		}
		ServerState.serverIsOkToShutdown = true;
		if (!GameClientController.isStarted()) {
			try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
		} else {
			if(setFinished){
				GLFrame.setFinished(true);
			}
		}

	}

	@Override
	public boolean isUserProtectionAuthenticated(String username, String starmadeUserName) {
		ProtectedUplinkName p = state.getProtectedUsers().get(username.trim().toLowerCase(Locale.ENGLISH));
		System.err.println("[AUTH] Protection status of " + username + " is " + p + " -> protected = " + (p != null && p.equals(starmadeUserName)));
		LogUtil.log().fine("[AUTH] Protection status of " + username + " is " + p + " -> protected = " + (p != null && p.equals(starmadeUserName)));
		return p != null && p.equals(starmadeUserName);
	}

	private void createVersionFile() {
		File f = new FileExt("lpversion");
		if (f.exists()) {
			f.delete();
		}
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f));
			bufferedWriter.append(VersionContainer.VERSION + ";" + VersionContainer.build);
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createWhitelistFile() throws IOException {
		File l = new FileExt("./whitelist.txt");
		if (!l.exists()) {
			l.createNewFile();
		}
	}

	public void endRound(int winner, int loser, Damager lastKill) {
		for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if (s instanceof PlayerState) {
				((PlayerState) s).notifyRoundEnded(winner, loser, lastKill);
			}
		}

		endRoundMode = System.currentTimeMillis();

	}

	public void enqueueAdminCommand(RegisteredClientInterface c,
	                                AdminCommands command, Object[] commandParams) {
		synchronized (state.getAdminCommands()) {
			state.getAdminCommands().add(new AdminCommandQueueElement(c, command, commandParams));
		}

	}

	public <E extends ElementCollection<E, EC, EM>, EC extends ElementCollectionManager<E, EC, EM>, EM extends UsableElementManager<E, EC, EM>> void enqueueElementCollectionUpdate(ElementCollectionManager<E, EC, EM> man) {
		if (state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(man.getSegmentController().getId())) {
			elementCollectionCalculationThreadManager.enqueue(new ElementCollectionCalculationThreadExecution((EC) man));
		} else {
			System.err.println("[SERVER] Exception tried to schedule update for nonexisting controller: " + man);
		}
	}

	public void executeWave(ShipSpawnWave wave) throws Exception {
		//		System.err.println("[SERVER] spawing wave LEVEL "+wave.getLevel()+": "+wave.getPrintsToSpawn());

		if (wave.getPrintsToSpawn() == null || wave.getPrintsToSpawn().isEmpty()) {
			System.err.println("[SERVER][WAVE] not spawning empty list: " + wave.getPrintsToSpawn());
			return;
		}
		Transform t = new Transform();
		t.setIdentity();

		Vector3f dir = new Vector3f((float) Math.random() - 0.5f, (float) Math.random() - 0.5f, (float) Math.random() - 0.5f);
		if (dir.length() == 0) {
			dir.x = 1;
		}
		dir.normalize();
		dir.scale((float) (100 + Math.random() * 50));

		t.origin.add(dir);
		for (CatalogPermission e : wave.getPrintsToSpawn()) {
			state.spawnMobs(1, e.getUid(), wave.sectorId, t, wave.getWaveTeam(), wave.bluePrintController);
		}
	}

	private void executeWaves() {
		if (!state.waves.isEmpty()) {
			long lowestTime = Long.MAX_VALUE;
			ShipSpawnWave lowestTimeWave = null;

			for (int i = 0; i < state.waves.size(); i++) {
				ShipSpawnWave wave = state.waves.get(i);
				if ((System.currentTimeMillis() - wave
						.getTimeInitiatedInMS()) > wave.getTimeInSecs() * 1000) {
					try {
						if (state.getUniverse().isSectorActive(wave.sectorId)) {
							executeWave(wave);
						} else {
							System.err.println("[SERVER][WAVE] WARNING: Could not load wave. sector not loaded " + wave.sectorId);
						}
					} catch (EntityNotFountException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (EntityAlreadyExistsException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					state.waves.remove(i);
					i--;
				} else {
					long timeRemaining = wave.getTimeInSecs()
							* 1000
							- (System.currentTimeMillis() - wave
							.getTimeInitiatedInMS());
					if (timeRemaining < lowestTime) {
						lowestTime = timeRemaining;
						lowestTimeWave = wave;
					}
				}
			}

		}
	}


	@Override
	public CreatorThreadController getCreatorThreadController() {
		return creatorThreadController;
	}

	/**
	 * @return the missileController
	 */
	public MissileController getMissileController() {
		return missileController;
	}
	/**
	 * @return the missileController
	 */
	public MineController getMineController() {
		return mineController;
	}
	public GameServerState getState() {
		return state;
	}

	/**
	 * @return the synchController
	 */
	public SynchronizationContainerController getSynchController() {
		return synchController;
	}

	private void handleEndRound() {

		boolean worldCleared = clearWorld();

		if (worldCleared) {

			state.getUniverse().resetAllSectors();

			endRoundMode = 0;
		}

	}

	public void handleEmpty(NetworkSegmentProvider ntSegmentProvider, SegmentController controller, Vector3i pos, long index, long localTimestamp) {
		if (controller.getSegmentBuffer().getLastChanged(pos) > localTimestamp) {
			//signal client that segment is empty
			ntSegmentProvider.signatureEmptyBuffer.add(index);
		}
	}

	public void handleLoadedRequest(ServerSegmentRequest r) {
		assert (r.segment != null);
		RemoteSegment segmentFromCache = r.segment;

		((ServerSegmentProvider) r.getSegmentController().getSegmentProvider()).addToBufferIfNecessary(segmentFromCache, r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);

		if (!r.sigatureOfSegmentBuffer) {
			handleSegmentRequest(r.getNetworkSegmentProvider(), segmentFromCache, segmentFromCache.getIndex(), r.getLocalTimestamp(), r.getSizeOnClient());
		} else {
			long segmentBufferIndex = SegmentBufferManager.getBufferIndexFromAbsolute(r.getSegmentPos());

			if (r.bitMap != null) {
				r.bitMap = r.getSegmentController().getSegmentBuffer().applyBitMap(segmentBufferIndex, r.bitMap);
			}
			if (r.bitMap != null) {
				r.getSegmentController().getSegmentBuffer().insertFromBitset(r.getSegmentPos(), segmentBufferIndex, r.bitMap, new SegmentBufferIteratorEmptyInterface() {
					@Override
					public boolean handle(Segment s, long lastChanged) {
						return false;
					}

					@Override
					public boolean handleEmpty(int posX, int posY, int posZ, long lastChanged) {
						return false;
					}
				});
			}
			assert (!(r.getSegmentController() instanceof ShopSpaceStation) || r.bitMap == null);

			synchronized (r.getNetworkSegmentProvider()) {
				r.getNetworkSegmentProvider().segmentBufferAwnserBuffer.add(new RemoteBitset(new BitsetResponse(segmentBufferIndex, r.bitMap, new Vector3i(r.getSegmentPos())), r.getNetworkSegmentProvider()));
			}
		}
	}

	/**
	 * Do all the IO here
	 * this is done THREADED (from pool)
	 *
	 * @param r
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SegmentOutOfBoundsException
	 */
	public void handleSegmentRequest(ServerSegmentRequest r) throws IOException, InterruptedException, SegmentOutOfBoundsException {
		RemoteSegment segmentFromCache = null;


		if (r.getSegmentController() instanceof Planet && !(((Planet)r.getSegmentController()).isTouched())) {


			/*
			 * SPECIAL STUFF FOR OLD PLANETS
			 */

			ObjectArrayList<RemoteSegment> column = new ObjectArrayList<RemoteSegment>(96/SegmentData.SEG);
			if(r.getSegmentPos().y < 96){
				synchronized(r.getSegmentController()){

					synchronized(state){
						SegmentRetrieveCallback segmentRetrieveCallback = new SegmentRetrieveCallback();
						r.getSegmentController().getSegmentBuffer().get(r.getSegmentPos(), segmentRetrieveCallback);
						if(segmentRetrieveCallback.state == SegmentBufferOctree.EMPTY){
							segmentFromCache = new RemoteSegment(r.getSegmentController());
							segmentFromCache.setPos(r.getSegmentPos());
							segmentFromCache.setSize(0);
							segmentFromCache.setLastChanged(System.currentTimeMillis());
						}else if(segmentRetrieveCallback.state != SegmentBufferOctree.NOTHING){
							segmentFromCache = (RemoteSegment) segmentRetrieveCallback.segment;
						}
					}
					if(segmentFromCache == null){
						RequestData allocateTerrainData = r.getSegmentController().getCreatorThread().allocateRequestData(r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);
						for (int y = 0; y < 96; y += SegmentData.SEG) {

							long index = ElementCollection.getIndex(r.getSegmentPos().x, y, r.getSegmentPos().z);

							RemoteSegment s = ((ServerSegmentProvider) r.getSegmentController().getSegmentProvider()).doRequest(index, allocateTerrainData);
							SegmentProvider.buildRevalidationIndex(s, r.getSegmentController().isStatic(), true); //planets prevalidated
							if(s.getSegmentData() != null) {
								s.getSegmentData().setNeedsRevalidate(true);
							}
							if (s.pos.equals(r.getSegmentPos())) {
								segmentFromCache = s;
							}else{
								column.add(s);
							}

						}
						r.getSegmentController().getCreatorThread().freeRequestData(allocateTerrainData, r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);

						for(RemoteSegment seg : column){
							SegmentProvider.buildRevalidationIndex(seg, r.getSegmentController().isStatic(), true); //planets prevalidated
							if(seg.getSegmentData() != null) {
								seg.getSegmentData().setNeedsRevalidate(true);
							}
							synchronized(state){
								//add the rest of the chunks now
								((ServerSegmentProvider) r.getSegmentController().getSegmentProvider()).addToBufferIfNecessary(
										seg, seg.pos.x, seg.pos.y, seg.pos.z);
							}
						}
					}
				}
			}else{
				segmentFromCache = new RemoteSegment(r.getSegmentController());
				segmentFromCache.setPos(r.getSegmentPos());
				segmentFromCache.setSize(0);
				segmentFromCache.setLastChanged(System.currentTimeMillis());
			}
		}
		if (r.getSegmentController() instanceof PlanetIco){
//			if(!((PlanetIco)r.getSegmentController()).isPlanetCore()){
//				System.err.println("[SERVER] creating chunk: "+r.getSegmentController());
				PlanetIcoCreatorThread creatorThread = (PlanetIcoCreatorThread) ((PlanetIco)r.getSegmentController()).getCreatorThread();
				ServerSegmentProvider provider = ((ServerSegmentProvider) r.getSegmentController().getSegmentProvider());


				RequestData allocatedGenerationData = r.getSegmentController().getCreatorThread().allocateRequestData(r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);

				long index = ElementCollection.getIndex(r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);

				segmentFromCache = ((ServerSegmentProvider) r.getSegmentController().getSegmentProvider())
						.doRequestStaged(index, (RequestDataIcoPlanet) allocatedGenerationData);

				(r.getSegmentController().getCreatorThread()).freeRequestData(allocatedGenerationData, r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);


				assert(segmentFromCache != null);
//			}else{
//				System.err.println("[SERVER] not creating chunk for core: "+r.getSegmentController());

//			}
		}
		boolean prevalidatedTypes = true;
		if (segmentFromCache == null) {

			/*
			 * FOR ALL REGULAR GENERATION/LOADING
			 */

			RequestData allocatedGenerationData = r.getSegmentController().getCreatorThread().allocateRequestData(r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);

			long index = ElementCollection.getIndex(r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);
			segmentFromCache = ((ServerSegmentProvider) r.getSegmentController().getSegmentProvider()).doRequest(index, allocatedGenerationData);
			(r.getSegmentController().getCreatorThread()).freeRequestData(allocatedGenerationData, r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);
			prevalidatedTypes = false; //possibly loaded from disk and therefore types not validated
		}
		r.segment = segmentFromCache;


		if (r.sigatureOfSegmentBuffer) {
			try {
				r.bitMap = ((ServerSegmentProvider) r.getSegmentController().getSegmentProvider()).getSegmentDataIO().requestSignature(r.getSegmentPos().x, r.getSegmentPos().y, r.getSegmentPos().z);
			} catch (DeserializationException e) {
				e.printStackTrace();
			}
		}
		SegmentProvider.buildRevalidationIndex(r.segment, r.getSegmentController().isStatic(), prevalidatedTypes);
		if(r.segment.getSegmentData() != null) {
			r.segment.getSegmentData().setNeedsRevalidate(true);
		}
		synchronized (state.getSegmentRequestsLoaded()) {
			assert(r.segment.getSegmentData() == null || r.segment.getSegmentData().needsRevalidate());
			state.getSegmentRequestsLoaded().enqueue(r);
		}
	}

	private void handleSystemIn(String in) {
		state.executeAdminCommand((String) ServerConfig.SUPER_ADMIN_PASSWORD.getString(), in, state.getAdminLocalClient());
	}

	public void initiateWave(int shipCountToSpawn, int waveFaction, int level, int time,
	                         BluePrintController bbc, Vector3i sectorId) throws EntityNotFountException, IOException, EntityAlreadyExistsException {
		//		System.err.println("INITIATING NEW WAVE IN "+time+" SECONDS");
		ShipSpawnWave wave = new ShipSpawnWave( waveFaction, level, bbc, time, sectorId);
		wave.createWave(state, shipCountToSpawn);
		if (wave.getPrintsToSpawn() != null && !wave.getPrintsToSpawn().isEmpty()) {
			state.waves.add(wave);
		}

	}

	//	public void scheduleSegmentSignatureRequest(SegmentController c, Vector3i v,
	//			NetworkSegmentProvider sc) {
	//		ServerSegmentRequest r = new ServerSegmentRequest(c, v, sc, RemoteSegmentSignature.class);
	//		synchronized(state.getSegmentRequests()){
	//			state.getSegmentRequests().enqueue(r);
	//			state.getSegmentRequests().notify();
	//		}
	//	}

	public boolean isAdmin(String username) {
		return state.getAdmins().isEmpty() || state.getAdmins().containsKey(username.trim().toLowerCase(Locale.ENGLISH));
	}

	public boolean isUserProtected(String username) {
		return state.getProtectedUsers().containsKey(username.trim().toLowerCase(Locale.ENGLISH));
	}

	private void loadUniverse() throws IOException, SQLException {
		try {
			state.getMetaObjectManager().load();
		} catch (FileNotFoundException e) {
		}
//		loadDefaultSector();

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

	public SectorSwitch queueSectorSwitch(SimpleTransformableSendableObject o,
	                                      Vector3i belogingVector, int jump, boolean copy) {

		return queueSectorSwitch(o, belogingVector, jump, copy, false, false);
	}

	public SectorSwitch queueSectorSwitch(SimpleTransformableSendableObject o,
	                                      Vector3i belogingVector, int jump, boolean copy, boolean force, boolean eliminateGravity) {
		if (force || System.currentTimeMillis() - o.lastSectorSwitch > 4000) {

			SectorSwitch sectorSwitch = new SectorSwitch(o, new Vector3i(belogingVector), jump);
			sectorSwitch.makeCopy = copy;
			sectorSwitch.eliminateGravity = eliminateGravity;
			if (!state.getSectorSwitches().contains(sectorSwitch)) {
				o.lastSectorSwitch = System.currentTimeMillis();
				state.getSectorSwitches().add(sectorSwitch);
				return sectorSwitch;
			}
		}
		return null;
	}

	public Tag readEntity(String identifier) throws IOException, EntityNotFountException {
		return readEntity(identifier, ".ent");
	}

	public Tag readEntity(String identifier, String fileExtension) throws IOException, EntityNotFountException {
		long t = System.currentTimeMillis();

		if (fileExtension.length() > 0 && !fileExtension.startsWith(".")) {
			fileExtension = "." + fileExtension;
		}

		String fileName = GameServerState.ENTITY_DATABASE_PATH + identifier + fileExtension;
		while (fileName.endsWith(".")) {
			System.err.println("Replacing point at end!");
			fileName = fileExtension.substring(0, fileName.length() - 2);
		}

		Object lock = null;
		synchronized (GameServerState.fileLocks) {
			lock = GameServerState.fileLocks.get(fileName);
			if (lock == null) {
				lock = new Object();
				GameServerState.fileLocks.put(fileName, lock);
			}
		}
		synchronized (lock) {
			Tag readTag = readTag(fileName, identifier);
			return readTag;
		}

	}

	@Override
	public void readjustControllers(Collection<Element> elems,
	                                SegmentController sc, Segment segment) {

	}

	private Tag readTag(String filePath, String identifier) throws IOException, EntityNotFountException {
		File f = new FileExt(filePath);
		if (f.exists()) {
			try {
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(f), 4096);
				Tag readFrom = Tag.readFrom(is, true, false);
				is.close();
				return readFrom;
			} catch (EOFException e) {
				e.printStackTrace();
				broadcastMessage(Lng.astr("WARNING: File corrupt!\n%s\ncreating backup,\nand regenerating new sector\nplease report this bug",  f.getName()), ServerMessage.MESSAGE_TYPE_ERROR);
				System.err.println("Exception: File corrupt! creating backup, and regenerating new sector please report this bug: "+f.getName());
				try {
					FileUtils.copyFile(f, new FileExt(filePath + ".corrupt"));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				throw new EntityNotFountException(identifier + "; file: " + f.getAbsolutePath() + "; pathString: " + filePath);
			}

		} else {
			System.err.println("[FILE NOT FOUND] File for " + identifier + " does not exist: " + f.getAbsolutePath());
			throw new EntityNotFountException(identifier + "; file: " + f.getAbsolutePath() + "; pathString: " + filePath);
		}
	}

	public boolean removeAdmin(String from, String playerName) {
		Admin remove = null;
		synchronized (state.getAdmins()) {
			remove = state.getAdmins().remove(playerName.trim().toLowerCase(Locale.ENGLISH));
		}
		if (remove != null) {
			LogUtil.log().fine("[ADMIN] '" + from + "' removed from admins: '" + playerName + "'");
			try {
				writeAdminsToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return remove != null;

	}

	@SuppressWarnings("unlikely-arg-type")
	public boolean removeBannedIp(String from, String playerIp) {
		boolean remove = false;
		synchronized (state.getBlackListedIps()) {
			remove = state.getBlackListedIps().remove(playerIp.trim());
		}
		if (remove) {
			LogUtil.log().fine("[ADMIN] '" + from + "' unbanned ip: '" + playerIp + "'");
			try {
				writeBlackListToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return remove;

	}

	@SuppressWarnings("unlikely-arg-type")
	public boolean removeBannedName(String from, String playerName) {
		boolean remove = false;
		synchronized (state.getBlackListedNames()) {
			remove = state.getBlackListedNames().remove(playerName.trim().toLowerCase(Locale.ENGLISH));
		}
		if (remove) {
			LogUtil.log().fine("[ADMIN] '" + from + "' unbanned playerName: '" + playerName + "'");
			try {
				writeBlackListToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return remove;

	}

	@SuppressWarnings("unlikely-arg-type")
	public boolean removeBannedAccount(String from, String playerName) {
		boolean remove;
		synchronized (state.getBlackListedAccounts()) {
			remove = state.getBlackListedAccounts().remove(playerName.trim());
		}
		if (remove) {
			LogUtil.log().fine("[ADMIN] '" + from + "' unbanned StarMade account: '" + playerName + "'");
			try {
				writeBlackListToDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return remove;

	}

	public void scheduleSegmentRequest(SegmentController c, Vector3i v, NetworkSegmentProvider sc, long localTimestamp, short sizeOnClient, boolean sigatureOfSegmentBuffer, boolean highPrio) {
		ServerSegmentRequest r = new ServerSegmentRequest(c, v, sc, localTimestamp, sizeOnClient);
		r.highPrio = highPrio;
		r.sigatureOfSegmentBuffer = sigatureOfSegmentBuffer;
		synchronized (state.getSegmentRequests()) {
			state.getSegmentRequests().enqueue(r);
			state.getSegmentRequests().notify();
		}
	}

	public void sendPlayerMessage(String to, Object[] message,
	                              int messageType) {
		try {
			System.err.println("[SERVER] sending message to " + to + ": Param: " + Arrays.toString(message));
			PlayerState playerFromName = state.getPlayerFromName(to);

			playerFromName.sendServerMessage(new ServerMessage(message, (byte)messageType, playerFromName.getId()));
		} catch (PlayerNotFountException e) {
			System.err.println("CANNOT SEND MESSAGE TO " + to + ": " + Arrays.toString(message));
			e.printStackTrace();

		}
	}

	//	public void setLogoutSpawnToPlayerPos(PlayerState pstate){
//		SimpleTransformableSendableObject s;
//		try {
//			s = pstate.getFirstControlledTransformable();
//			setLogoutSpawnToPos(pstate, s.getWorldTransform().origin);
//		} catch (PlayerControlledTransformableNotFound e) {
//			System.err.println(e.getClass().getSimpleName()+" [WARNING] no new logout pos set: "+e.getMessage()+"; Maybe not spawned yet");
//		}
//	}
//
//
//	public void setLogoutSpawnToPos(PlayerState pstate, Vector3f posO){
//		Vector3f pos = new Vector3f(posO);
//		System.err.println("[SERVER] SETTING LOGOUT SPAWNING POINT OF "+pstate+" TO "+pos);
//		pstate.getLogoutSpawnPoint().set(pos);
//		pstate.getLogoutSpawnSector().set(pstate.getCurrentSector());
//
//	}
//	public void setSpawnToPlayerPos(PlayerState pstate){
//		SimpleTransformableSendableObject s;
//		try {
//			s = pstate.getFirstControlledTransformable();
//			setSpawnToPos(pstate, s.getWorldTransform().origin, true);
//		} catch (PlayerControlledTransformableNotFound e) {
//			e.printStackTrace();
//		}
//	}
//	public void setSpawnToPos(PlayerState pstate, Vector3f posO, boolean checkSectorCollision){
//		try{
//			Vector3f pos = new Vector3f(posO);
//
//			if(checkSectorCollision){
//				SimpleTransformableSendableObject s = pstate.getFirstControlledTransformable();
//				Sector sector = state.getUniverse().getSector(pstate.getCurrentSector());
//				int c = 0;
//
//				while(sector.checkSectorCollision(s, pos) != null){
//					int b = c % 3;
//					if(b == 0){
//						pos.x+=8;
//					}else if (b == 1){
//						pos.y+=8;
//					}else{
//						pos.z+=8;
//					}
//
//
//					c++;
//				}
//			}
//			pstate.setSpawnPoint(pos);
//			pstate.getSpawnSector().set(pstate.getCurrentSector());
//
//		}catch (PlayerControlledTransformableNotFound ex){
//			ex.printStackTrace();
//			System.err.println("Exception catched: ServerExecution can continue");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//
//	}
	public void spawnKillBonus(SimpleTransformableSendableObject s) {
		float countMul = ServerConfig.AI_DESTRUCTION_LOOT_COUNT_MULTIPLIER.getFloat();
		float stackMul = ServerConfig.AI_DESTRUCTION_LOOT_STACK_MULTIPLIER.getFloat();
		if (countMul <= 0 || stackMul <= 0) {
			return;
		}
		if (s instanceof Ship) {

			Ship ship = (Ship) s;
			int amountOfSpawning = (int) (Universe.getRandom().nextInt((Math.min(5000, ship.getTotalElements()) / 10) + 1) * countMul);
			Sector sector = state.getUniverse().getSector(s.getSectorId());
			Vector3f sPos = new Vector3f(s.getWorldTransform().origin);

			System.err.println("[SERVER] SPAWNING BONUS IN sector: " + sector + "; amount: " + amountOfSpawning);
			if (sector != null) {
				for (int i = 0; i < amountOfSpawning; i++) {
					int count = Math.max(1, (int) Math.floor(Universe.getRandom().nextGaussian() * 500 * stackMul) + 10);
					short type = ElementKeyMap.typeList()[Universe.getRandom().nextInt(ElementKeyMap.typeList().length)];
					ElementInformation info = ElementKeyMap.getInfo(type);
					if (!info.isShoppable()) {
						continue;
					}

					Vector3f pos = new Vector3f(sPos);
					pos.x += 5 * (Math.random() - 0.5f);
					pos.y += 5 * (Math.random() - 0.5f);
					pos.z += 5 * (Math.random() - 0.5f);
					sector.getRemoteSector().addItem(pos, type, -1, count);

				}
			}
		}
	}

	private void spawnPlayerCharacterPrepare(PlayerState playerState) throws IOException {

		playerState.spawnData.onSpawnPreparePlayerCharacter(playerState.spawnedOnce);

	}

	private void spawnPlayerCharacter(PlayerState playerState) throws IOException {

		String uName = "ENTITY_PLAYERCHARACTER_" + playerState.getName();

		System.err.println("[SERVER][SPAWN] SPAWNING NEW CHARACTER FOR " + playerState);

		playerState.getControllerState().setLastTransform(null);

		PlayerCharacter c = new PlayerCharacter(state);

		c.initialize();

		playerState.spawnData.onSpawnPlayerCharacter(c, playerState.spawnedOnce);


		c.setFactionId(playerState.getFactionId());

		c.setId(state.getNextFreeObjectId());
		c.setUniqueIdentifier(uName);

		c.setClientOwnerId(playerState.getClientId());

		System.err.println("[SERVER][PlayerCharacter] " + state + " Set initial transform to " + c.getInitialTransform().origin+"; ClientID: "+playerState.getClientId()+"; PlayerState: "+playerState);

		synchController.addNewSynchronizedObjectQueued(c);

		playerState.setAssignedPlayerCharacter(c);
		playerState.spawnedOnce = true;
		playerState.setAlive(true);

		playerState.lastSpawnedThisSession(System.currentTimeMillis());
	}

	public void triggerForcedSave() {
		lastSave = 0;
	}

	private void checkExplosionOrders() {

		for (int i = 0; i < state.getExplosionOrdersQueued().size(); i++) {
			ExplosionRunnable explosionRunnable = state.getExplosionOrdersQueued().get(i);

			if (!explosionRunnable.canExecute()) {
				break;
			}

			boolean check = explosionRunnable.beforeExplosion();
			if(check) {
				state.getTheadPoolExplosions().execute(explosionRunnable);
			}else {
				System.err.println("[EXPLOSION][WARNING] NOT EXECUTING EXPLOSION ORDER");
			}
			state.getExplosionOrdersQueued().remove(i);
			i--;
		}

		if (!state.getExplosionOrdersFinished().isEmpty()) {
			synchronized (state.getExplosionOrdersFinished()) {
				while (!state.getExplosionOrdersFinished().isEmpty()) {
					ExplosionRunnable dequeue = state.getExplosionOrdersFinished().dequeue();
					dequeue.afterExplosion();
				}
			}
		}
	}

	private void writeAdminsToDisk() throws IOException {
		File l = new FileExt("./admins.txt");
		l.delete();
		createAdminFile();
		BufferedWriter s = new BufferedWriter(new FileWriter(l));
		synchronized (state.getAdmins()) {
			for (Admin admin : state.getAdmins().values()) {
				s.append(admin.name.trim().toLowerCase(Locale.ENGLISH));
				if (admin.deniedCommands.size() > 0) {
					s.append("#");
					Iterator<AdminCommands> iterator = admin.deniedCommands.iterator();
					while (iterator.hasNext()) {
						AdminCommands next = iterator.next();

						s.append(next.name());

						if (iterator.hasNext()) {
							s.append(",");
						}

					}
				}
				s.newLine();
			}
		}
		s.close();
	}

	private void writeBlackListToDisk() throws IOException {
		File l = new FileExt("./blacklist.txt");
		l.delete();
		createBlacklistFile();
		BufferedWriter s = new BufferedWriter(new FileWriter(l));
		synchronized (state.getBlackListedNames()) {
			for (PlayerAccountEntry line : state.getBlackListedNames()) {
				if(line.isValid(System.currentTimeMillis())){
					s.append( line.fileLineName());
					s.newLine();
				}
			}
		}
		synchronized (state.getBlackListedIps()) {
			for (PlayerAccountEntry line : state.getBlackListedIps()) {
				if(line.isValid(System.currentTimeMillis())){
					s.append(line.fileLineIP());
					s.newLine();
				}
			}
		}
		synchronized (state.getBlackListedAccounts()) {
			for (PlayerAccountEntry line : state.getBlackListedAccounts()) {
				if(line.isValid(System.currentTimeMillis())){
					s.append(line.fileLineAccount());
					s.newLine();
				}
			}
		}

		s.close();
	}

	/**
	 * THIS METHOD IS CALLED AT
	 * triggered on shudown or autosave
	 *
	 * @param terminate
	 * @param clear
	 * @param universe
	 * @throws IOException
	 * @throws SQLException
	 */
	public void writeEntitiesToDatabaseAtAutosaveOrShutdown(boolean terminate, boolean clear, Universe universe)
			throws IOException, SQLException {

		assert (getServerState() != null);
		assert (getServerState().getUniverse() != null);

		synchronized (state.getLocalAndRemoteObjectContainer()
				.getLocalObjects()) {
			if(terminate) {
				System.err.println("[SERVER][SHUTDOWN] writing players ...");
			}
			for (PlayerState player : state.getPlayerStatesByName().values()) {
				writeEntity(player, true);
			}
		}
		if(terminate) {
			System.err.println("[SERVER][SHUTDOWN] written players ");
		}
		//write universe after normal sendables,
		//so the assigned sectors don't get deleted
		universe.writeToDatabase(terminate, clear);
		if(terminate) {
			System.err.println("[SERVER][SHUTDOWN] written to database ");
		}
	}
	public void writeSingleEntityWithDock(Sendable s)
			throws IOException, SQLException {
		Sector.writeSingle(state, s);
		if(s instanceof SegmentController){
			for(RailRelation r : ((SegmentController)s).railController.next){
				writeSingleEntityWithDock(r.docked.getSegmentController());
			}
		}
	}
	public void writeEntity(DiskWritable ts, boolean dbUpdate) throws IOException, SQLException {
		if (ts.isVolatile()) {
			return;
		}
		if (ts instanceof TransientSegmentController && !((TransientSegmentController) ts).isTouched() && !((TransientSegmentController) ts).needsTagSave()) {
			state.getDatabaseIndex().getTableManager().getEntityTable().updateOrInsertSegmentController((SegmentController) ts);
			return;
		}

		long t = System.currentTimeMillis();

		long delTime = 0;
		long renameTime = 0;
		long tagWriteTime = 0;
		long tagCreateTime = 0;
		long dbTime = 0;
		long t0 = 0;
		boolean needsTagSave = true;

		if (ts instanceof TransientSegmentController && !((TransientSegmentController) ts).isTouched() && !((TransientSegmentController) ts).needsTagSave()) {
			needsTagSave = false;
		}
		if (ts instanceof PlayerState) {
			System.err.println("[SERVER] WRITING PLAYER TO DISK: " + ts + " DD: " + needsTagSave);
		}
		if (needsTagSave) {
			assert (ts.getUniqueIdentifier() != null) : "no ident for " + ts + " on SERVER";
			String fileName = ts.getUniqueIdentifier() + ".ent";
			String path = GameServerState.ENTITY_DATABASE_PATH;
			EntityFileTools.write(GameServerState.fileLocks, ts, path, fileName);
		} else {
			System.err.println("[SERVER] NOT WRITING ENTITY TAG: " + ts + " DOESNT NEED SAVE");
		}
		t0 = System.currentTimeMillis();
		if (dbUpdate && ts instanceof SegmentController) {
			if(!((SegmentController) ts).isVirtualBlueprint()){
				state.getDatabaseIndex().getTableManager().getEntityTable().updateOrInsertSegmentController((SegmentController) ts);
			}else{
				state.getDatabaseIndex().getTableManager().getEntityTable().removeSegmentController((SegmentController) ts);
				if(((SegmentController) ts).getDbId() > 0){
					state.getDatabaseIndex().getTableManager().getTradeNodeTable().removeTradeNode(((SegmentController) ts).getDbId());
					//designs can be -1
					state.getUniverse().tradeNodesDirty.enqueue(((SegmentController) ts).getDbId());
				}
			}
		}
		dbTime += System.currentTimeMillis() - t0;

		long took = (System.currentTimeMillis() - t);
		if (took > 20) {
			System.err.println("[SERVER] WARNING: WRITING ENTITY TAG: " + ts + " FINISHED: " + took + "ms -> tagcreate: " + tagCreateTime + "; tagwrite: " + tagWriteTime + "; rename: " + renameTime + "; delete: " + delTime + "; DB: " + dbTime);
		}
	}

	private void writeProtectedUsersToDisk() throws IOException {
		File l = new FileExt("./protected.txt");
		l.delete();
		createProtectedUserFile();
		BufferedWriter s = new BufferedWriter(new FileWriter(l));
		synchronized (state.getProtectedUsers()) {
			for (Entry<String, ProtectedUplinkName> admin : state.getProtectedUsers().entrySet()) {
				s.append(admin.getKey().toLowerCase(Locale.ENGLISH) + ";" + admin.getValue().uplinkname + ";" + admin.getValue().timeProtected);
				s.newLine();
			}
		}
		s.close();
	}

	private void writeWhiteListToDisk() throws IOException {
		File l = new FileExt("./whitelist.txt");
		l.delete();
		createWhitelistFile();
		BufferedWriter s = new BufferedWriter(new FileWriter(l));
		synchronized (state.getWhiteListedNames()) {
			for (PlayerAccountEntry line : state.getWhiteListedNames()) {
				s.append(line.fileLineName());
				s.newLine();
			}
		}
		synchronized (state.getWhiteListedIps()) {
			for (PlayerAccountEntry line : state.getWhiteListedIps()) {
				s.append(line.fileLineIP());
				s.newLine();
			}
		}
		synchronized (state.getWhiteListedAccounts()) {
			for (PlayerAccountEntry line : state.getWhiteListedAccounts()) {
				s.append(line.fileLineAccount());
				s.newLine();
			}
		}
		s.close();
	}

	public void queueSegmentControllerBreak(SegmentPiece cr) {
		segmentBreaker.enqueue(new BreakTestRequest(cr));
	}

	public void queueSegmentPath(SegmentPiece cr, SegmentPiece to, SegmentPathCallback callback) {
		segmentPathFinder.enqueue(new SegmentPathRequest(cr, to, callback));
	}

	public void queueSegmentGroundPath(SegmentPiece cr, SegmentPiece to, SegmentPathCallback callback) {
		segmentPathGroundFinder.enqueue(new SegmentPathRequest(cr, to, callback));
	}

	public void queueSegmentPath(Vector3i cr, Vector3i to, SegmentController c, SegmentPathCallback callback) {
		segmentPathFinder.enqueue(new SegmentPathRequest(cr, to, c, callback));
	}

	public void queueSegmentGroundPath(Vector3i cr, Vector3i to, SegmentController c, SegmentPathCallback callback) {
		segmentPathGroundFinder.enqueue(new SegmentPathRequest(cr, to, c, callback));
	}

	public void queueSegmentRandomPath(SegmentPiece cr, Vector3i origin, BoundingBox roaming, Vector3i prefferedDir, SegmentPathCallback callback) {
		segmentPathFinder.enqueue(new SegmentPathRequest(cr, origin, roaming, prefferedDir, callback));
	}

	public void queueSegmentRandomGroundPath(SegmentPiece cr, Vector3i origin, BoundingBox roaming, Vector3i prefferedDir, SegmentPathCallback callback) {
		segmentPathGroundFinder.enqueue(new SegmentPathRequest(cr, origin, roaming, prefferedDir, callback));
	}

	public void queueCreatureSpawn(CreatureSpawn cr) {
		synchronized (state.creatureSpawns) {
			state.creatureSpawns.enqueue(cr);
		}

	}

	/**
	 * @return the segmentPathFinder
	 */
	public SegmentPathFindingHandler getSegmentPathFinder() {
		return segmentPathFinder;
	}

	/**
	 * @return the segmentPathGroundFinder
	 */
	public SegmentPathGroundFindingHandler getSegmentPathGroundFinder() {
		return segmentPathGroundFinder;
	}

	@Override
	public void parseBlockBehavior(String path) throws IOException {
		File bbFile = new FileExt(path);
		Document orig = XMLTools.loadXML(bbFile);

		Document merge = null;
		File customVoidElementManager = new FileExt(GameResourceLoader.CUSTOM_BLOCK_BEHAVIOR_CONFIG_PATH + "customBlockBehaviorConfig.xml");
		if (customVoidElementManager.exists()) {
			merge = XMLTools.loadXML(customVoidElementManager);
			System.err.println("[SERVER] Custom block behavior config found");
			XMLTools.mergeDocument(orig, merge);
			//XMLTools.writeDocument(new FileExt("blockBehaviorConfigMergeResult.xml"), orig);
			bbFile = new FileExt("blockBehaviorConfigMergeResult.xml");
		}



		state.setBlockBehaviorConfig(orig);

		RandomAccessFile f = new RandomAccessFile(bbFile, "r");
		byte[] bc = new byte[(int) f.length()];
		f.read(bc);
		f.close();

		state.setBlockBehaviorBytes(bc);

		state.setBlockBehaviorChecksum(FileUtil.getSha1Checksum(bbFile));

	}

	/**
	 * @return the blockBehaviorChanged
	 */
	public byte[] getBlockBehaviorChanged() {
		return blockBehaviorChanged;
	}

	/**
	 * @param blockBehaviorChanged the blockBehaviorChanged to set
	 */
	public void setBlockBehaviorChanged(byte[] blockBehaviorChanged) {
		this.blockBehaviorChanged = blockBehaviorChanged;
	}

	public boolean allowedToExecuteAdminCommand(
			RegisteredClientOnServer client, AdminCommands command) {
		if (state.getAdmins().size() == 0) {
			return true;
		}
		if (state.getAdmins().containsKey(client.getClientName().toLowerCase(Locale.ENGLISH))) {
			Admin admin = state.getAdmins().get(client.getClientName().toLowerCase(Locale.ENGLISH));
			return !admin.deniedCommands.contains(command);
		}
		return false;
	}

	public void broadcastMessageSector(Object[] astr, byte messageType,
	                                   int sectorId) {

		for(Sendable a : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()){
			if(a instanceof PlayerState && ((PlayerState)a).getCurrentSectorId() == sectorId){
				((PlayerState)a).sendServerMessage(new ServerMessage(astr, messageType));
			}
		}

	}

	public void onEntityAddedToSector(Sector sector,
	                                  SimpleTransformableSendableObject s) {
		final int size = sectorListeners.size();
		for(int i = 0; i < size; i++) {
			sectorListeners.get(i).onSectorEntityAdded(s, sector);
		}
	}
	public void onEntityRemoveFromSector(Sector sector,
	                                     SimpleTransformableSendableObject s) {
		final int size = sectorListeners.size();
		for(int i = 0; i < size; i++) {
			sectorListeners.get(i).onSectorEntityRemoved(s, sector);
		}
	}

	public void onSectorAddedSynch(Sector sec) {
		final int size = sectorListeners.size();
		for(int i = 0; i < size; i++) {
			sectorListeners.get(i).onSectorAdded(sec);;
		}
	}
	public void onSectorRemovedSynch(Sector sec) {
		final int size = sectorListeners.size();
		for(int i = 0; i < size; i++) {
			sectorListeners.get(i).onSectorRemoved(sec);
		}
	}


	public List<SectorListener> getSectorListeners() {
		return sectorListeners;
	}

	@Override
	public MissileManagerInterface getMissileManager() {
		return missileController.getMissileManager();
	}
}
