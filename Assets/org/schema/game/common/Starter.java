package org.schema.game.common;

import api.DebugFile;
import api.StarLoaderHooks;
import api.common.GameServer;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.ModStarter;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.LogUtil;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.data.ResourceUtil;
import org.schema.common.util.security.OperatingSystem;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.manager.ingame.BlockBuffer;
import org.schema.game.client.data.ClientStatics;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.cubes.CubeMeshBufferContainerPool;
import org.schema.game.client.view.cubes.occlusion.Occlusion;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.mainmenu.MainMenuFrame;
import org.schema.game.common.api.SessionNewStyle;
import org.schema.game.common.controller.database.DatabaseIndex;
import org.schema.game.common.controller.database.tables.EntityTable;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.io.SegmentDataIO16;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.element.ControlElementMapperFactory;
import org.schema.game.common.data.element.ElementCountMapFactory;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.faction.config.FactionConfig;
import org.schema.game.common.data.world.NativeMemoryManager;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.migration.StationAndShipTransienceMigration;
import org.schema.game.common.gui.ServerGUI;
import org.schema.game.common.util.DebugUtil;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.mod.ModManager;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.GameServerController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNewsEventFactory;
import org.schema.game.server.data.simulation.resource.PassiveResourceManager;
import org.schema.game.server.data.structurepersistence.PersistentStructureDataManager;
import org.schema.schine.auth.Session;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIResizableGrabbableWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.client.HostPortLoginName;
import org.schema.schine.network.commands.LoginRequest;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.common.commands.Command;
import org.schema.schine.network.exception.ServerPortNotAvailableException;
import org.schema.schine.network.server.ServerController;
import org.schema.schine.network.server.ServerState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.MeshLoader;
import org.schema.schine.resource.ResourceLoader;
import org.schema.schine.resource.tag.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

import static org.schema.game.common.data.SendableTypes.initTypesMap;

public class Starter implements Observer {

	public static final ModManager modManager = new ModManager();
	protected static boolean cleanUpDBForStations;

	public static boolean DEDICATED_SERVER_ARGUMENT;

	public static Session currentSession;
	public static boolean hasGUI = true;
	public static boolean serverInitFinished;
	public static ServerGUI sGUI;
	public static boolean serverUp;
	public static final Object serverLock = new Object();
	public static boolean loggedIn;
	private static int authStyle = -1;
	private static boolean forceSimClean;
	private static String uniqueSessionId;
	private static ClientRunnable clientRunnable;
	private static boolean importedCustom;
	private static boolean registered;
	public static Exception startupException;
	public static boolean CONFIG_ONLY;
	private final ArrayList<HostPortLoginName> history = new ArrayList<HostPortLoginName>();

	public static synchronized String getUniqueSessionId() {
		if(uniqueSessionId == null) {
			uniqueSessionId = createUniqueSessionId();
		}
		return uniqueSessionId;
	}

	public static void writeDefaultConfigs() {
		EngineSettings.writeDefault();
		//		ServerConfig.writeDefault();
		KeyboardMappings.writeDefault();
	}

	public static String createUniqueSessionId() {
		return (System.currentTimeMillis() + "-" + (int) (Math.random() * 40000));
	}

	public static void checkDatabase(boolean nogui) {
		File server = (new FileExt(GameServerState.ENTITY_DATABASE_PATH));
		if(server.exists() && server.list().length > 1) {
			JFrame f = null;

			if(!DatabaseIndex.existsDB()) {
				//only call if physical database exists but no index (SQL)
				//this will attempt to create the index from the files
				try {
					System.err.println("DATABASE STARTING");
					System.err.println("INFO SET");
					DatabaseIndex i = new DatabaseIndex();
					System.err.println("DATABASE CREATING");
					i.createDatabase();
					System.err.println("DATABASE CREATED");
					i.fillCompleteDatabase(p -> {
					});
					System.err.println("DATABASE FILLED");
					i.getTableManager().getEntityTable().optimizeDatabase(p -> {
					});
					System.err.println("DATABASE OPTIMIZED");
					i.destroy();
					System.err.println("DATABASE CLOSED");
				} catch(SQLException e) {
					e.printStackTrace();
					int s = JOptionPane.showOptionDialog(f, Lng.str("An error occurred creating the index database!\nPlease sent an error report!"), "ERROR", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Ok"}, "Ok");
				} catch(IOException e) {
					e.printStackTrace();
					int s = JOptionPane.showOptionDialog(f, Lng.str("An error occurred creating the index database!\nPlease sent an error report!"), "ERROR", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Ok"}, "Ok");
				}

				//				if (f != null) {
				//					int n = JOptionPane.showOptionDialog(f,
				//							Lng.str("Index Creation Successfull!\nHave fun playing!")
				//							, Lng.str("Database Index Creation"),
				//							JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
				//							null, new String[]{"Ok"}, "Ok");
				//				}
				try {
					LogUtil.setUp(20, () -> {

					});
				} catch(SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch(IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private static void cleanSimulatedMobs() throws IOException, SQLException {
		DatabaseIndex db = new DatabaseIndex();
		db.getTableManager().getEntityTable().removeAll(DatabaseIndex.escape("MOB_SIM") + "%", EntityTable.Despawn.ALL, null, false);
		{
			File dbFiles = new FileExt(GameServerState.ENTITY_DATABASE_PATH);
			File[] listFilesEdb = dbFiles.listFiles((arg0, arg1) -> arg1.startsWith("ENTITY_SHIP_MOB_SIM"));
			for(File f : listFilesEdb) {
				f.delete();
			}
			File simFile = new FileExt(GameServerState.ENTITY_DATABASE_PATH + "/SIMULATION_STATE.sim");
			simFile.delete();
		}
		{
			File dbFiles = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
			File[] listFilesEdb = dbFiles.listFiles((arg0, arg1) -> arg1.startsWith("ENTITY_SHIP_MOB_SIM"));
			for(File f : listFilesEdb) {
				f.delete();
			}
		}
		db.destroy();
	}
	//	private static void joinOldCatalog() {
	//		File dir = new FileExt("./data/blueprints");
	//		if(dir.exists()){
	//			File[] listFiles = dir.listFiles();
	//			for(int i = 0; i < listFiles.length; i++){
	//				if(listFiles[i].getName().startsWith("Catalog-old")){
	//					integrateCatalogFile(listFiles[i]);
	//					listFiles[i].delete();
	//				}
	//			}
	//		}
	//
	//	}

	public static void cleanUniverseWithBackup() throws IOException {
		long x = System.currentTimeMillis();
		File backUp = new FileExt("./backup/");
		backUp.mkdir();
		File server = (new FileExt(GameServerState.ENTITY_DATABASE_PATH));
		File client = (new FileExt(ClientStatics.ENTITY_DATABASE_PATH));
		if(server.exists()) {
			FolderZipper.zipFolder(GameServerState.ENTITY_DATABASE_PATH, "./backup/server-database-backup-" + x + ".zip", null, null);
			FileUtil.deleteDir(server);
			server.delete();

		}
		if(client.exists()) {
			FolderZipper.zipFolder(ClientStatics.ENTITY_DATABASE_PATH, "./backup/client-database-backup-" + x + ".zip", null, null);
			FileUtil.deleteDir(client);
			client.delete();
		}
	}

	public static void cleanUniverseWithoutBackup() {
		System.out.println("RESETTING DB");
		FileUtil.deleteDir((new FileExt(GameServerState.ENTITY_DATABASE_PATH)));
		(new FileExt(GameServerState.ENTITY_DATABASE_PATH)).delete();
		FileUtil.deleteDir((new FileExt(ClientStatics.ENTITY_DATABASE_PATH)));
		(new FileExt(ClientStatics.ENTITY_DATABASE_PATH)).delete();
	}

	public static void cleanClientCacheWithoutBackup() {
		System.out.println("RESETTING CLIENT DB");
		FileUtil.deleteDir((new FileExt(ClientStatics.ENTITY_DATABASE_PATH)));
		(new FileExt(ClientStatics.ENTITY_DATABASE_PATH)).delete();
		FileUtil.deleteDir((new FileExt(ClientStatics.ENTITY_DATABASE_PATH)));
		(new FileExt(ClientStatics.ENTITY_DATABASE_PATH)).delete();
	}

	public static void doMigration(JDialog f, boolean onlyCatalog) {
		File dir = new FileExt(BluePrintController.active.entityBluePrintPath);
		if(!dir.exists()) {
			dir.mkdir();
			try {
				FileUtil.copyDirectory(new FileExt(BluePrintController.defaultBB.entityBluePrintPath), new FileExt(BluePrintController.active.entityBluePrintPath));
				BluePrintController.active.setImportedByDefault((true));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		try {
			BluePrintController.active.convert(BluePrintController.active.entityBluePrintPath, true);
		} catch(IOException e1) {
			e1.printStackTrace();
		}

		checkDatabase(f == null);

		File server = (new FileExt(GameServerState.ENTITY_DATABASE_PATH));
		if(server.exists() && server.list().length > 1) {
			File vFile = new FileExt("lpversion");
			boolean dbSimulationCleanNeeded = !vFile.exists();
			boolean dbShopAndTagMigrationNeeded = !vFile.exists();
			boolean blueprintMigrationNeeded = !vFile.exists();

			if(vFile.exists()) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(vFile, StandardCharsets.UTF_8));
					String[] p;
					String readLine = br.readLine();
					if(readLine.length() > 0 && !readLine.equals(VersionContainer.VERSION + ";" + VersionContainer.build)) {
						blueprintMigrationNeeded = true;
					}
					if(readLine.length() > 0) {
						p = readLine.split(";");

						if(p[0].split("\\.").length < 3) {
							float version = Float.parseFloat(p[0]);
							if(version < 0.0934f) {
								System.out.println("Old Version Found: " + version);
								dbSimulationCleanNeeded = true;
							}
							if(version < 0.09444f) {
								System.out.println("Database size Migration needed : " + version);
								dbShopAndTagMigrationNeeded = true;
							}

						}
					}
					br.close();
					//					if(version <= 0.09376f){
					//						System.err.println("UPDATE HOOK FOR "+0.09377+": default server config buffer entries");
					//						EngineSettings.CLIENT_BUFFER_SIZE.setCurrentState(64*1024);
					//						EngineSettings.write();
					//						ServerConfig.USE_UDP.setOn(false);
					//						ServerConfig.SOCKET_BUFFER_SIZE.setCurrentState(64*1024);
					//						ServerConfig.write();
					//					}
				} catch(IOException e) {
					e.printStackTrace();
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else {
			}
			if(blueprintMigrationNeeded) {
				List<BlueprintEntry> readBluePrints = BluePrintController.active.readBluePrints();
				if(readBluePrints.isEmpty()) {
					BluePrintController.active.setDirty(true);
				}
			}
			if(dbShopAndTagMigrationNeeded) {
				if(f != null) {
					//					int n = JOptionPane.showOptionDialog(f,
					//							"Update 0.09444f release hook:\n\n" +
					//									"A database migration will now be peformed\n"
					//									+ "to reduce size and improve performance.\n"
					//									+ "It could take a few minutes.\n"
					//									+ "Please do not kill the process\n"
					//									+ "and make sure you have a backup (created automatically on update)\n\n" +
					//									"- schema",
					//							"Database Cleanup needed",
					//							JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
					//							null, new String[]{"Ok", "Continue Without Migration"}, "Continue Without Migration");
					//					if (n == 1) {
					//						return;
					//					}

				}
				try {
					System.err.println("DOING TAG DATABASE MIGRATION (this can take a little) [DO NOT KILL THE PROCESS]");
					dbShopAndTagMigration();
				} catch(IOException e) {
					e.printStackTrace();
					if(f != null) {
						//						int s = JOptionPane.showOptionDialog(f,
						//								"An error occurred saving cleaning the database!", "ERROR",
						//								JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
						//								null, new String[]{"Ok"}, "Ok");
					}
				}
				if(f != null) {
					//					int s = JOptionPane.showOptionDialog(f,
					//							"Your Universe has been successfully cleaned!\n" +
					//									"Thank you for playing StarMade!", "Reset Successful",
					//							JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
					//							null, new String[]{"Ok"}, "Ok");
				}
			}

			if(dbSimulationCleanNeeded || forceSimClean) {
				if(f != null) {
					//					int n = JOptionPane.showOptionDialog(f,
					//							"Update 0.0934 release hook:\n\n" +
					//									"The last updates caused a bit of havok (mass spawn of NPCs)!\n" +
					//									"This is the option to clean up all simulated Mobs\n" +
					//									"Im sorry for the inconvenience!\n" +
					//									"- schema",
					//							"Database Cleanup needed",
					//							JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
					//							null, new String[]{"Ok", "Continue Without Reset"}, "Continue Without Reset");
					//					if (n == 1) {
					//						return;
					//					}
				}
				try {
					cleanSimulatedMobs();
				} catch(IOException e) {
					e.printStackTrace();
					if(f != null) {
						//						int s = JOptionPane.showOptionDialog(f,
						//								"An error occurred saving cleaning the database!", "ERROR",
						//								JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
						//								null, new String[]{"Ok"}, "Ok");
					}
				} catch(SQLException e) {
					e.printStackTrace();
					if(f != null) {
						//						int s = JOptionPane.showOptionDialog(f,
						//								"An error occurred saving cleaning the database!", "ERROR",
						//								JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
						//								null, new String[]{"Ok"}, "Ok");
					}
				}
				if(f != null) {
					//					int s = JOptionPane.showOptionDialog(f,
					//							"Your Universe has been successfully cleaned!\n" +
					//									"Thank you for playing StarMade!", "Reset Successful",
					//							JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
					//							null, new String[]{"Ok"}, "Ok");
				}
			}
		}
	}

	private static void dbShopAndTagMigration() throws IOException {
		{
			File shopRawFiles = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
			File[] listFiles = shopRawFiles.listFiles((arg0, arg1) -> {
				boolean ok = arg1.startsWith(SimpleTransformableSendableObject.EntityType.SHOP.dbPrefix) && arg1.endsWith(SegmentDataIO16.BLOCK_FILE_EXT);
				return ok;
			});

			System.err.println("[MIGRATION] REMOVING UNNECESSARY SHOP RAW BLOCK DATA. FILES FOUND: " + listFiles.length);
			for(int i = 0; i < listFiles.length; i++) {
				try {
					listFiles[i].delete();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			System.err.println("[MIGRATION] REMOVING UNNECESSARY SHOP RAW BLOCK DATA DONE");
		}
		{
			System.err.println("[MIGRATION] MIGRATING META-DATA STRUCTURE (can take very long)");
			File shopRawFiles = new FileExt(GameServerState.ENTITY_DATABASE_PATH);
			File[] listFiles = shopRawFiles.listFiles((arg0, arg1) -> arg1.startsWith("ENTITY_") && arg1.endsWith(".ent"));
			long size = 0;
			long savedBytes = 0;
			for(int i = 0; i < listFiles.length; i++) {
				try {
					long fSize = listFiles[i].length();
					size += fSize;
					FileInputStream f = new FileInputStream(listFiles[i]);
					Tag readFrom = Tag.readFrom(f, true, false);

					listFiles[i].delete();

					FileOutputStream fout = new FileOutputStream(listFiles[i]);
					readFrom.writeTo(fout, true);

					FileInputStream f1 = new FileInputStream(listFiles[i]);
					Tag readFrom1 = Tag.readFrom(f1, true, true);

					savedBytes = fSize - listFiles[i].length();
					//					System.err.println("SIZE SAVED ON "+listFiles[i].getName()+": "+(listFiles[i].length()-fSize));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			System.err.println("[MIGRATION] MIGRATING META-DATA STRUCTURE DONE; files migrated: " + listFiles.length + "; Bytes cleaned: " + savedBytes);

		}
	}

	public static void handlePortError(String error) {
		String s = (String) JOptionPane.showInputDialog(new JFrame(), error + "Some other program is blocking port " + ServerController.port + ". Please end that program or choose another port for starmade (Type a number from 1024 to 65535)", "Port Problem", JOptionPane.PLAIN_MESSAGE, null, null, ServerController.port);
		try {
			//If a string was returned, say so.
			if((s != null) && (s.length() > 0)) {
				int parseInt = Integer.parseInt(s);
				ServerController.port = parseInt;

			} else {
				handlePortError("invalid Port: '" + s + "'. ");
			}
		} catch(NumberFormatException e) {
			handlePortError("invalid Port: '" + s + "'. ");
		}
	}

	public static void initializeServer(boolean gui) throws SecurityException, IOException {
		//INSERTED CODE @665
		ModStarter.preServerStart();
		///
		DatabaseIndex.registerDriver();
		importCustom(gui);
	}

	public static void initialize(boolean gui) throws SecurityException, IOException {
		try {
			BlockShapeAlgorithm.initialize();
		} catch(Exception e) {
			e.printStackTrace();
			if(gui) {
				GuiErrorHandler.processErrorDialogException(e);
			} else {
				try {
					throw new Exception("System.exit() called");
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				System.exit(0);
			}
		}
		System.setProperty("hsqldb.reconfig_logging", "false");

		registerSerializableFactories();
		registerRemoteClasses();
		System.out.println("[INITIALIZE] REMOTE CLASSES REGISTERED");
		ResourceLoader.resourceUtil = new ResourceUtil();
		System.out.println("[INITIALIZE] RESOURCE MANAGER INITIALIZED");
		System.setSecurityManager(null);
		System.out.println("[INITIALIZE] SECURITY MANAGER SET");
		ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
		System.out.println("[INITIALIZE] BLOCK CONFIGURATION READ");
		Command.initializeCommands();
		GameRequestAnswerFactory.initAll();
		initTypesMap();
		System.out.println("[INITIALIZE] NET COMMANDS & UTILS INITIALIZED");

		//		joinOldCatalog();
	}

	public static void importCustom(boolean gui) throws IOException {
		if(!importedCustom) {
			File customTextures = new FileExt(GameResourceLoader.CUSTOM_TEXTURE_PATH);
			if(!customTextures.exists()) {
				GameResourceLoader.copyDefaultCustomTexturesTo(GameResourceLoader.CUSTOM_TEXTURE_PATH);
			}
			createCustomFactionConfig();
			createCustomBlockBehaviorConfig();

			createCustomEffectConfig();

			GameResourceLoader.createCustomTextureZip();

			File cusbtomConfigImport = new FileExt(GameResourceLoader.CUSTOM_CONFIG_IMPORT_PATH);

			if(!cusbtomConfigImport.exists()) {
				GameResourceLoader.copyCustomConfig(GameResourceLoader.CUSTOM_CONFIG_IMPORT_PATH);
			}

			importedCustom = true;
		}
	}

	public static boolean isServerStart(List<String> argsSet) {
		return argsSet.contains("-server") || argsSet.contains("-generateconfig");
	}

	public static void main(String[] args) throws IOException {
		//EWAHCompressedBitmap c = new EWAHCompressedBitmap();
		try {
			EngineSettings.read();
		} catch(Exception exception) { //Just have it write the defaults if it fails to read the config file. This also gets around the issue of parsing outdated or missing config files.
			exception.printStackTrace();
			System.err.println("[STARTER] Error reading config file, writing defaults...");
			writeDefaultConfigs();
			EngineSettings.read();
		}
		try {
			UIScale.load();
			System.setProperty("sun.java2d.uiScale", String.valueOf(UIScale.getScalef()));
		} catch(SAXException | ParserConfigurationException e1) {
			e1.printStackTrace();
		}

		OperatingSystem os = OperatingSystem.getOS();
		VersionContainer.loadVersion();
		System.out.println("#################### StarMade #######################");
		System.out.println("# version " + VersionContainer.VERSION + " - build " + VersionContainer.build + " #");
		System.out.println("#####################################################");
		System.out.println("[SERIAL] \"" + os.serial + "\"");
		System.out.println("[INSTALLATION_DIRECTORY] \"" + (new FileExt("./")).getAbsolutePath() + "\"");

		//		if(VersionContainer.build.equals("latest")) {
		//			writeDefaultConfigs();
		//		}

		try {
			RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
			List<String> arguments = runtimeMxBean.getInputArguments();
			System.out.println("[JVM-ARGUMENTS] " + arguments);
		} catch(Exception e) {
			e.printStackTrace();
		}

		//		for(int i = 0; i < 256; i++){
		//			System.err.println("CUSTUM_ID_"+StringTools.formatThreeZero(i)+ " = "+((2048-256)+i));
		//		}
		try {
			List<String> argsSet = new ObjectArrayList<String>();

			for(String arg : args) {
				if(arg.startsWith("-auth")) {
					try {
						String[] s = arg.split("\\s+");
						argsSet.add(s[0].trim());
						argsSet.add(s[1].trim());
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else {
					argsSet.add(arg);
				}
			}

			File f = new File("testcredentials.properties");
			if(f.exists() && !argsSet.contains("-auth")) {
				Properties p = new Properties();
				try {
					FileInputStream fis = new FileInputStream(f);
					p.load(fis);

					String username = p.getProperty("user");
					String password = p.getProperty("password");

					SessionNewStyle test = new SessionNewStyle("test");
					test.login(username, password);

					String authTokenCode = test.getToken();

					argsSet.add("-auth");
					argsSet.add(authTokenCode);

					System.err.println("[STARTER] Test token used from test credentials (" + username + ")");

					fis.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			int indexOf = argsSet.indexOf("-auth");
			boolean gui = argsSet.contains("-gui");
			DebugUtil.loggingEnabled = argsSet.contains("-debug_logging");
			if((argsSet.contains("-cleanuptransient"))) {
				System.out.println("WARNING: PLEASE CREATE A BACKUP BEFORE DOING THIS! DO YOU WANT TO PROCEED Y/N?");
				BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
				String s = bufferRead.readLine();
				if(s.equals("Y")) cleanUpDBForStations = true;
			}
			try {
				EngineSettings.read();
			} catch(Exception e) {
				e.printStackTrace();
				//writing default settings
				System.err.println("Writing default settings");
				EngineSettings.writeDefault();
				EngineSettings.read(); //reading again
			}
			try {
				Keyboard.createKeymapping();
				KeyboardMappings.read();
			} catch(Exception e) {
				e.printStackTrace();
				KeyboardMappings.writeDefault();
				KeyboardMappings.read();
			}

			if(indexOf >= 0 && indexOf + 1 < argsSet.size()) {

				String token = argsSet.get(indexOf + 1);
				System.err.println("[STARTER] AUTH TOKEN SUBMITTED VIA ARGUMENT");
				SessionNewStyle s = new SessionNewStyle("general");
				s.loginWithExistingToken(token);
				currentSession = s;
				System.err.println("[STARTER] RETRIEVED REGISTRY NAME: " + s.getRegistryName() + "; ID: " + s.getUserId());
				if(LoginRequest.isLoginNameValid(s.getRegistryName())) {
					if(EngineSettings.ONLINE_PLAYER_NAME.getString().trim().isEmpty()) {
						EngineSettings.ONLINE_PLAYER_NAME.setString(s.getRegistryName());
					}
					if(EngineSettings.OFFLINE_PLAYER_NAME.getString().trim().isEmpty()) {
						EngineSettings.OFFLINE_PLAYER_NAME.setString(s.getRegistryName());
					}
				}
			}

			if(isServerStart(argsSet)) {
				importCustom(gui);
				LanguageManager.loadCurrentLanguage(true);

			}
			if((isServerStart(argsSet)) && !gui) {
				hasGUI = false;
			}
			if(EngineSettings.FIRST_START.isOn()) {
				EngineSettings.FIRST_START.setOn(false);
				EngineSettings.AUTOSET_RESOLUTION.setOn(!(new FileExt("lpversion")).exists());
			}

			GameServerState.readDatabasePosition(hasGUI);

			if(EngineSettings.DELETE_SEVER_DATABASE_ON_STARTUP.isOn()) {
				cleanUniverseWithoutBackup();
			}

			if(!isServerStart(argsSet)) {
				loadLastUsedSkin();
			}
			GUIResizableGrabbableWindow.read();

			int simdLevel = -1;
			int simdIndex = argsSet.indexOf("-simd");
			if(simdIndex >= 0 && simdIndex + 1 < argsSet.size()) {
				try {
					simdLevel = Integer.parseInt(argsSet.get(simdIndex + 1));
				} catch(NumberFormatException e) {
					System.out.println("WARNING: Error parsing -simd parameter");
					simdLevel = -1;
				}
			}
			LibLoader.loadNativeLibs(isServerStart(argsSet), simdLevel, false);
			//if (!(argsSet.contains("-server"))) {
			//dont load libs for dedicated server
			//}
			//			if(EngineSettings.O_OCULUS_RENDERING.isOn() && !GLFrame.oculusInit){
			//
			//				if(!EngineSettings.F_FRAME_BUFFER_USE.isOn()){
			//					GLFrame.processErrorDialogException(new UnsupportedOperationException("you can only use occulus with frame buffer. Please turn it on in the settings"));
			//				}
			//				String error = null;
			//				try{
			//					TeraOVR.initSDK();
			//					OculusVrHelper.updateFromDevice();
			//					TeraOVR.clear();
			//
			//					com.oculusvr.capi.Hmd.initialize();
			//					GLFrame.hmd = com.oculusvr.capi.Hmd.create(0);
			//					GLFrame.hmd.startSensor(OvrLibrary.ovrSensorCaps.ovrSensorCap_Orientation | OvrLibrary.ovrSensorCaps.ovrSensorCap_YawCorrection
			//							| OvrLibrary.ovrSensorCaps.ovrSensorCap_Position, OvrLibrary.ovrSensorCaps.ovrSensorCap_Orientation);
			////								hmd.startSensor(0,0);
			//
			//					GLFrame.oculusInit = true;
			//					error = GLFrame.hmd.getLastError();
			//					if(error != null){
			//						System.err.println("OCULUS INIT ERROR: "+error);
			//						throw new Exception("Error initializing oculus rift module. Please send in logs\nError Message: "+error);
			//					}else{
			//
			//					}
			//					assert(error == null):error;
			//				}catch(Exception e){
			//					e.printStackTrace();
			//					GLFrame.processErrorDialogException(e);
			//				}
			//			}

			if(args.length == 0) {
				try {
					throw new IllegalStateException("Sorry, it doesn't work this way!\nplease start the game with the StarMade-Starter you downloaded");
				} catch(Exception e) {
					GLFrame.processErrorDialogException(e, null);
					return;
				}
			}
			for(String a : argsSet) {
				if(a.startsWith("-port:")) {
					a = a.replaceFirst("-port:", "");
					try {
						int port = Integer.parseInt(a.trim());

						ServerController.port = port;

					} catch(NumberFormatException e) {
						try {
							throw new Exception("\"-port\" parameter malformed. please use a port number. (Example: -port:4242)", e);
						} catch(Exception ex) {
							GLFrame.processErrorDialogException(ex, null);
						}

					}
				}
			}
			if(argsSet.contains("-cleansim")) {
				forceSimClean = true;
			}

			System.out.println("[MAIN] CHECKING IF MIGRATION NECESSARY");

			System.out.println("[MAIN] MIGRATION PROCESS DONE");
			copyDefaultBB(gui);

			System.err.println("[CLIENT][STARTUP] Initializing memory");

			//allocate memory for segmentData
			int blockSize = (1024 * 1024) * 1024; //1gb memory
			NativeMemoryManager.segmentDataManager = NativeMemoryManager.initialize(SegmentData.TOTAL_SIZE * ByteUtil.SIZEOF_INT, blockSize);
			if(args.length > 0) {

				/**
				 * The client needs to be started with "-force". The reason is
				 * that people double click the jar which starts the game
				 * with 64MB of memory. In this case, an error message will pop up
				 */
				System.out.println("[MAIN] STARTING WITH ARGUMENTS: " + Arrays.toString(args));
				if(isServerStart(argsSet)) {
					System.out.println("[MAIN] LOADED ENGINE SETTINGS");
					initializeServer(gui);
					initialize(gui);
					doMigration(null, false);
					System.out.println("[MAIN] INITIALIZATION COMPLETED");
					DEDICATED_SERVER_ARGUMENT = true;
					MeshLoader.loadVertexBufferObject = false; //don't load any VBO (no graphics)
					if(Controller.getResLoader() == null) {
						Controller.initResLoader(new GameResourceLoader());
						ResourceLoader.dedicatedServer = true;
					}
					Controller.getResLoader().loadServer();
					startServer(argsSet.contains("-gui"), argsSet.contains("-generateconfig"));

				} else if(argsSet.contains("-oldmenu")) {
					System.out.println("[MAIN] LOADED ENGINE SETTINGS");
					initializeServer(gui);
					initialize(gui);
					doMigration(new JDialog(), false);
					System.out.println("[MAIN] INITIALIZATION COMPLETED");
					LanguageManager.loadCurrentLanguage(false);
					//clientStartup();
				} else if(argsSet.contains("-locallastsettings")) {
					System.out.println("[MAIN] LOADED ENGINE SETTINGS");
					initializeServer(gui);
					initialize(gui);
					doMigration(new JDialog(), false);

					System.out.println("[MAIN] INITIALIZATION COMPLETED");
					LanguageManager.loadCurrentLanguage(false);
					if(argsSet.contains("-exitonesc")) {
						EngineSettings.S_EXIT_ON_ESC.setOn(true);
					}
					startLocal();
				} else if(argsSet.contains("-force")) {
					LanguageManager.loadCurrentLanguage(false);
					startMainMenu();
				} else {
					//					System.out.println("[MAIN] LOADED ENGINE SETTINGS");
					//					initializeServer(gui);
					//					initialize(gui);
					//					doMigration(new JDialog(), false);
					//					System.out.println("[MAIN] INITIALIZATION COMPLETED");
					//					createDefaultLanguage();
					//					loadLanguage();
					//					clientStartup();
					try {
						throw new IllegalStateException("Sorry, it doesn't work this way!\nplease start the game with the StarMade-Starter you downloaded.\nFor starting a GUI-less dedicated server, start with \"-server\"");
					} catch(Exception e) {
						if(gui) {
							GLFrame.processErrorDialogException(e, null);
						} else {
							e.printStackTrace();
						}
					}
				}
			} else {
				try {
					throw new IllegalStateException("Sorry, it doesn't work this way!\nplease start the game with the StarMade-Starter you downloaded.\nFor starting a GUI-less dedicated server, start with \"-server\"");
				} catch(Exception e) {
					if(gui) {
						GLFrame.processErrorDialogException(e, null);
					} else {
						e.printStackTrace();
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void copyDefaultBB(boolean gui) {
		File f = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
		f.mkdirs();
		f = new FileExt("." + File.separator + "blueprints");

		File defaultBP = new FileExt(GameServerState.ENTITY_BLUEPRINT_PATH_DEFAULT);

		if(defaultBP.exists()) {
			File[] listFiles = defaultBP.listFiles();
			for(File fdd : listFiles) {
				if(fdd.isDirectory() && (fdd.getName().equals("Isanth-VI") || fdd.getName().equals("Isanth Type-Zero Mm") || fdd.getName().equals("Isanth Type-Zero Bb") || fdd.getName().equals("Isanth Type-Zero Bp") || fdd.getName().equals("Isanth Type-Zero Cp") || fdd.getName().equals("Isanth Type-Zero Mp"))) {
					System.err.println("[START] Default blueprint migration. REMOVING DEFAULT INSANTH VARIANT " + fdd.getName());
					FileUtil.deleteDir(fdd);
				} else if(fdd.isDirectory()) {
					File[] lf = fdd.listFiles();
					for(File s : lf) {
						if(s.isDirectory() && s.getName().equals("DATA")) {
							File[] dataFiles = s.listFiles();
							for(File dataFile : dataFiles) {
								if(!dataFile.getName().startsWith("ENTITY")) {
									System.err.println("[START] Default blueprint migration. Deleting data file: " + dataFile.getName());
									dataFile.delete();
								}
							}
						}

					}
				}
			}
		}
		File old = new FileExt("." + File.separator + "blueprints" + File.separator + "Isanth Type-Zero B-" + File.separator + "header.smbph");
		File def = new FileExt("." + File.separator + GameServerState.ENTITY_BLUEPRINT_PATH_DEFAULT + File.separator + "Isanth Type-Zero B-" + File.separator + "header.smbph");
		//
		boolean replace = old.exists() && def.exists() && old.length() != def.length();

		File dtFile = new FileExt("." + File.separator + "blueprints" + File.separator + "Isanth Type-Zero B-" + File.separator + "DATA" + File.separator + "Isanth Type-Zero B-.0.0.0.smd3");
		if(dtFile.exists()) {
			replace = true;
		}
		if(!f.exists() || replace) {
			if(f.exists() && replace) {
				File[] dr = f.listFiles();
				for(File m : dr) {
					if(m.isDirectory() && (m.getName().equals("Isanth-VI") || m.getName().equals("Isanth Type-Zero Mm") || m.getName().equals("Isanth Type-Zero Bb") || m.getName().equals("Isanth Type-Zero Bp") || m.getName().equals("Isanth Type-Zero Cp") || m.getName().equals("Isanth Type-Zero Mp"))) {
						System.err.println("[START] REMOVING INSANTH VARIANT " + m.getName());
						FileUtil.deleteDir(m);
					}

					if(replace && (m.getName().equals("Isanth Type-Zero B-") || m.getName().equals("Isanth Type-Zero Bc") || m.getName().equals("Isanth Type-Zero Bm") || m.getName().equals("Isanth Type-Zero C-") || m.getName().equals("Isanth Type-Zero Cc") || m.getName().equals("Isanth Type-Zero Cb") || m.getName().equals("Isanth Type-Zero Cm") || m.getName().equals("Isanth Type-Zero M-") || m.getName().equals("Isanth Type-Zero Mb") || m.getName().equals("Isanth Type-Zero Mc"))

					) {
						System.err.println("[START] REMOVING INSANTH VARIANT (will be replaced) " + m.getName());
						FileUtil.deleteDir(m);
					}
				}
			}
			f.mkdirs();

			if(defaultBP.exists()) {
				try {

					System.out.println("[START] no Blueprint files detected: COPYING DEFAULT BLUEPRINTS");
					FileUtil.copyDirectory(new FileExt(GameServerState.ENTITY_BLUEPRINT_PATH_DEFAULT), new FileExt(GameServerState.ENTITY_BLUEPRINT_PATH));
				} catch(IOException e) {
					e.printStackTrace();
					if(gui) {
						GuiErrorHandler.processErrorDialogException(e);
					}
				}
			} else {
				System.err.println("[START] no default blueprints detected");
			}
		}
	}

	private static void createCustomEffectConfig() throws IOException {

		File customConfigPoolDir = new FileExt(GameResourceLoader.CUSTOM_EFFECT_CONFIG_PATH);
		if(!customConfigPoolDir.exists()) {
			System.err.println("[STARTER] custom effect config config dir does not exist. copying templates");
			customConfigPoolDir.mkdir();
			File template = new FileExt(GameResourceLoader.CUSTOM_EFFECT_CONFIG_PATH + "customEffectConfigTemplate.xml");
			if(template.exists()) {
				template.delete();
			}
			FileUtil.copyFile(new FileExt(ConfigPool.configPathTemplate), template);

			File howto = new FileExt(GameResourceLoader.CUSTOM_EFFECT_CONFIG_PATH + "customEffectConfigHOWTO.txt");
			if(howto.exists()) {
				howto.delete();
			}
			FileUtil.copyFile(new FileExt(ConfigPool.configPathHOWTO), howto);

		} else {
			System.err.println("[STARTER] template dir exists. overwriting template with current data");
		}
		File template = new FileExt(GameResourceLoader.CUSTOM_EFFECT_CONFIG_PATH + "customEffectConfigTemplate.xml");
		if(template.exists()) {
			template.delete();
		}
		FileUtil.copyFile(new FileExt(ConfigPool.configPathTemplate), template);

		File howto = new FileExt(GameResourceLoader.CUSTOM_EFFECT_CONFIG_PATH + "customEffectConfigHOWTO.txt");
		if(howto.exists()) {
			howto.delete();
		}
		FileUtil.copyFile(new FileExt(ConfigPool.configPathHOWTO), howto);

		File customConfigPool = new FileExt(GameResourceLoader.CUSTOM_EFFECT_CONFIG_PATH + "customEffectConfig.xml");
		if(customConfigPool.exists()) {
			(new FileExt(ConfigPool.configPathTemplate)).delete();
			FileUtil.copyFile(customConfigPool, (new FileExt(ConfigPool.configPathTemplate)));
			System.err.println("[STARTER] custom config file has been copied to config location" + (new FileExt(ConfigPool.configPathTemplate)).getAbsolutePath());
		}
	}

	private static void createCustomBlockBehaviorConfig() throws IOException {
		File customVoidElementManagerDir = new FileExt(GameResourceLoader.CUSTOM_BLOCK_BEHAVIOR_CONFIG_PATH);
		if(!customVoidElementManagerDir.exists()) {
			System.err.println("[STARTER] custom block behavior config dir does not exist. copying templates");
			customVoidElementManagerDir.mkdir();
			File template = new FileExt(GameResourceLoader.CUSTOM_BLOCK_BEHAVIOR_CONFIG_PATH + "customBlockBehaviorConfigTemplate.xml");
			if(template.exists()) {
				template.delete();
			}
			FileUtil.copyFile(new FileExt(VoidElementManager.configPath), template);

			File howto = new FileExt(GameResourceLoader.CUSTOM_BLOCK_BEHAVIOR_CONFIG_PATH + "customBlockBehaviorConfigHOWTO.txt");
			if(howto.exists()) {
				howto.delete();
			}
			FileUtil.copyFile(new FileExt(VoidElementManager.configPathHOWTO), howto);

		} else {
			System.err.println("[STARTER] template dir exists. overwriting template with current data");
		}
		File template = new FileExt(GameResourceLoader.CUSTOM_BLOCK_BEHAVIOR_CONFIG_PATH + "customBlockBehaviorConfigTemplate.xml");
		if(template.exists()) {
			template.delete();
		}
		FileUtil.copyFile(new FileExt(VoidElementManager.configPath), template);

		File howto = new FileExt(GameResourceLoader.CUSTOM_BLOCK_BEHAVIOR_CONFIG_PATH + "customBlockBehaviorConfigHOWTO.txt");
		if(howto.exists()) {
			howto.delete();
		}
		FileUtil.copyFile(new FileExt(VoidElementManager.configPathHOWTO), howto);

		File customVoidElementManager = new FileExt(GameResourceLoader.CUSTOM_BLOCK_BEHAVIOR_CONFIG_PATH + "customBlockBehaviorConfig.xml");
		if(customVoidElementManager.exists()) {
			(new FileExt(VoidElementManager.configPath)).delete();
			FileUtil.copyFile(customVoidElementManager, (new FileExt(VoidElementManager.configPath)));
			System.err.println("[STARTER] custom config file has been copied to config location" + (new FileExt(VoidElementManager.configPath)).getAbsolutePath());
		}
	}

	private static void createCustomFactionConfig() throws IOException {
		File customFactionConfigDir = new FileExt(GameResourceLoader.CUSTOM_FACTION_CONFIG_PATH);
		if(!customFactionConfigDir.exists()) {
			System.err.println("[STARTER] custom faction config dir does not exist. copying templates");
			customFactionConfigDir.mkdir();
			File template = new FileExt(GameResourceLoader.CUSTOM_FACTION_CONFIG_PATH + "FactionConfigTemplate.xml");
			if(template.exists()) {
				template.delete();
			}
			FileUtil.copyFile(new FileExt(FactionConfig.factionConfigPath), template);

			File howto = new FileExt(GameResourceLoader.CUSTOM_FACTION_CONFIG_PATH + "customFactionConfigHOWTO.txt");
			if(howto.exists()) {
				howto.delete();
			}
			FileUtil.copyFile(new FileExt(FactionConfig.factionConfigPathHOWTO), howto);

		} else {
			System.err.println("[STARTER] template dir exists. overwriting template with current data");
		}
		File template = new FileExt(GameResourceLoader.CUSTOM_FACTION_CONFIG_PATH + "FactionConfigTemplate.xml");
		if(template.exists()) {
			template.delete();
		}
		FileUtil.copyFile(new FileExt(FactionConfig.factionConfigPath), template);

		File howto = new FileExt(GameResourceLoader.CUSTOM_FACTION_CONFIG_PATH + "customFactionConfigHOWTO.txt");
		if(howto.exists()) {
			howto.delete();
		}
		FileUtil.copyFile(new FileExt(FactionConfig.factionConfigPathHOWTO), howto);

		File customFactionConfig = new FileExt(GameResourceLoader.CUSTOM_FACTION_CONFIG_PATH + "FactionConfig.xml");
		if(customFactionConfig.exists()) {
			(new FileExt(FactionConfig.factionConfigPath)).delete();
			FileUtil.copyFile(customFactionConfig, (new FileExt(FactionConfig.factionConfigPath)));
			System.err.println("[STARTER] custom config file has been copied to config location" + (new FileExt(FactionConfig.factionConfigPath)).getAbsolutePath());
		}

	}

	public static void registerRemoteClasses() {
		if(!registered) {
			modManager.registerNetworkClasses();
			modManager.registerRemoteClasses();
			registered = true;
		}
	}
	//	public static void registerRemoteClasses() {
	//		if(!registered){
	//			NetUtil.addSendableClass(Ship.class, StateInterface.class);
	//			NetUtil.addSendableClass(PlayerState.class, StateInterface.class);
	//			NetUtil.addSendableClass(ChatSystem.class, StateInterface.class);
	//			NetUtil.addSendableClass(PlayerCharacter.class, StateInterface.class);
	//			NetUtil.addSendableClass(EditableSendableSegmentController.class, StateInterface.class);
	//			NetUtil.addSendableClass(FloatingRockManaged.class, StateInterface.class);
	//			NetUtil.addSendableClass(FloatingRock.class, StateInterface.class);
	//			NetUtil.addSendableClass(ShopSpaceStation.class, StateInterface.class);
	//			NetUtil.addSendableClass(TeamDeathStar.class, StateInterface.class);
	//			NetUtil.addSendableClass(TeamDeathStar.class, StateInterface.class);
	//			NetUtil.addSendableClass(SendableSegmentProvider.class, StateInterface.class);
	//			NetUtil.addSendableClass(SendableGameState.class, StateInterface.class);
	//			NetUtil.addSendableClass(SpaceStation.class, StateInterface.class);
	//			NetUtil.addSendableClass(Vehicle.class, StateInterface.class);
	//			NetUtil.addSendableClass(Planet.class, StateInterface.class);
	//			NetUtil.addSendableClass(RemoteSector.class, StateInterface.class);
	//			NetUtil.addSendableClass(Sun.class, StateInterface.class);
	//			NetUtil.addSendableClass(BlackHole.class, StateInterface.class);
	//			NetUtil.addSendableClass(ClientChannel.class, StateInterface.class);
	//			NetUtil.addSendableClass(AICharacter.class, StateInterface.class);
	//			NetUtil.addSendableClass(AIRandomCompositeCreature.class, StateInterface.class);
	//			NetUtil.addSendableClass(PlanetCore.class, StateInterface.class);
	//			NetUtil.addSendableClass(SpaceCreature.class, StateInterface.class);
	//			NetUtil.addSendableClass(PlanetIco.class, StateInterface.class);
	//
	//			modManager.registerNetworkClasses();
	//			modManager.registerRemoteClasses();
	//			registered = true;
	//		}
	//	}

	public static void registerSerializableFactories() {
		SerializableTagRegister.register = new SerializableTagFactory[]{new ControlElementMapperFactory(), new ElementCountMapFactory(), new NPCFactionNewsEventFactory(), new TagSerializableLongSetFactory(), new BlockBuffer.BlockBufferFactory(), new TagSerializableLong2Vector3fMapFactory(), new TagSerializableLong2TransformMapFactory()};
	}

	public static void stopClient(GraphicsContext context) {
		if(clientRunnable != null) {
			Controller.setLoadMessage("Stopping current client");
			clientRunnable.stopClient();
			clientRunnable = null;
		}
	}

	//REPLACE METHOD
	public static void startClient(HostPortLoginName server, boolean startConnectDialog, GraphicsContext context) {
		MainMenuFrame.isMusicPlaying = false;

		System.err.println("[CLIENT][MEMORY] allocating and initializing cube mesh buffers");
		CubeMeshBufferContainerPool.initialize();

		System.err.println("[CLIENT][MEMORY] allocating and initializing lighting thread data");
		Occlusion.initializeOccluders(SegmentDrawer.LIGHTING_THREAD_COUNT + 1);

		//INSERTED CODE
		String loginName = server.host;
		DebugFile.log("Connecting to server: " + loginName);
		boolean allUptoDate = ModStarter.preClientConnect(loginName, server.port);
		if(allUptoDate) {
			stopClient(context);
			clientRunnable = new ClientRunnable(server, startConnectDialog, context);
			Thread var3;

			(var3 = new Thread(clientRunnable, "ClientThread")).setPriority(8);
			GameClientController.availableGUI = true;
			System.err.println("[Starloader] Start client thread");
			var3.start();
		} else {
			System.err.println("[Starloader] Not all mods up to date, not starting");
		}
	}

//	public static SteamAPIHandler apiHandler;

	public static void startLocal() {
		ModStarter.justStartedSinglePlayer = true;
		startServer(false, false);
		startClient(new HostPortLoginName("localhost", 4242, HostPortLoginName.STARMADE_CLIENT, null), true, null);
	}

	public static void startMainMenu() {
		System.err.println("[CLIENT][STARTUP] Starting main menu");
		//INSERTED CODE
//		if(apiHandler == null) apiHandler = new SteamAPIHandler();
		//
		GameMainMenuController c;
		try {
			c = new GameMainMenuController();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		try {
			System.err.println("[CLIENT][STARTUP] Starting Graphics");
			c.startGraphics();

		} catch(SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static void startServer(boolean startGUI, boolean generateConfigOlnly) {
		MainMenuFrame.isMusicPlaying = false;

		System.err.println("[SERVER] Starting server: GUI: " + startGUI + ", ConfigOnly: " + generateConfigOlnly);
		serverInitFinished = false;

		CONFIG_ONLY = generateConfigOlnly;
		(new Thread(getServerRunnable(startGUI), "ServerThread")).start();
	}

	public static Runnable getServerRunnable(boolean startGUI) {

		return new Runnable() {

			@Override
			public void run() {
				boolean restart = false;
				boolean error = false;
				try {
					System.err.println("[SERVER] initializing ");
					ServerState.setShutdown(false);
					Controller.initResLoader(new GameResourceLoader());
					GameServerState serverState = null;
					serverState = new GameServerState();
					GameServerController server = new GameServerController(serverState);
					if(cleanUpDBForStations) {
						try {
							System.out.println("--------------------------------------");
							System.out.println("--------DATABASE CLEANUP START--------");
							System.out.println("--------------------------------------");
							System.out.println("CLEANUP TARGET " + GameServerState.ENTITY_DATABASE_PATH);
							StationAndShipTransienceMigration m = new StationAndShipTransienceMigration();
							m.convertDatabase(GameServerState.ENTITY_DATABASE_PATH);
							System.out.println("--------------------------------------");
							System.out.println("--------DATABASE CLEANUP FINISHED-----");
							System.out.println("------------SYSTEM WILL EXIT----------");
							System.out.println("--------------------------------------");
						} catch(Exception e) {
							e.printStackTrace();
						} finally {
							try {
								throw new Exception("System.exit() called");
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							System.exit(0);
						}
					}
					sGUI = null;

					if(startGUI) {

						SwingUtilities.invokeLater(() -> {
							sGUI = new ServerGUI(server);
							sGUI.setVisible(true);
						});

					}
					serverUp = false;

					server.startServerAndListen();

					while(!server.isListenting()) {
						try {
							Thread.sleep(30);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					serverUp = true;

					if(DEDICATED_SERVER_ARGUMENT && ServerConfig.SECURE_UPLINK_ENABLED.isOn()) {
						String uplinkToken = ServerConfig.SECURE_UPLINK_TOKEN.getString();
					}

					File l = new FileExt("./debugPlayerLock.lock");
					if(l.exists()) {
						l.delete();
					}
					l.createNewFile();
					DataOutputStream s = new DataOutputStream(new FileOutputStream(l));
					synchronized(serverState.getClients()) {
						s.writeInt(serverState.getClients().size());
					}
					s.close();

					//INSERTED CODE
					//Todo: Fix steam implementation
					//if(SteamAPIHandler.initialized && GameCommon.isOnSinglePlayer() && SteamConfigManager.getConfig().getBoolean("allow-friends-to-join-local")) Starter.apiHandler.createLobby();
					//
				} catch(Exception e) {
					if(sGUI != null) {
						sGUI.setVisible(false);
					}

					if(e instanceof ServerPortNotAvailableException) {
						e = new Exception(Lng.str("A StarMade Server Instance is already running on port %s", ServerController.port), e);
					}

					if((e instanceof SQLException) && e.getMessage() != null && e.getMessage().contains("data file is modified but backup file does not exist")) {
//						e = new SQLException("You last session of StarMade before updating to the new sql library wasn't shutdown correctly, so the restore files are incompatible.\n" + "Please download http://files.star-made.org/build/starmade-build_20160924_005306/lib/hsqldb.jar and place it in /lib. Run the game once and exit without a forced shutdown.\n" + "Then do a starmade update again to revert the lib to the new one again.", e);
					}

					startupException = e;
					error = true;
					//					if (e instanceof ServerPortNotAvailableException) {
					//						ServerPortNotAvailableException ex = (ServerPortNotAvailableException) e;
					//						if (ex.isInstanceRunning()) {
					//							System.out.println("NOT STARTING SERVER. A StarMade Server Instance is already running on port " + ServerController.port + " DEDICATED: " + DEDICATED_SERVER_ARGUMENT);
					//							if (DEDICATED_SERVER_ARGUMENT) {
					//								System.err.println("HANDLING EXCEPTION NOW");
					//								handleServerAlreadyRunningError("A StarMade Server Instance is already running on port " + ServerController.port);
					//							}
					//						} else {
					//							ex.printStackTrace();
					//							System.out.println("[ERROR] Some other program is blocking port " + ServerController.port + ". Please end that program or choose another port for starmade");
					//							handlePortError("");
					//							restart = true;
					//						}
					//					} else {
					e.printStackTrace();
					//					}

				} finally {
					if(!error) {
						System.err.println("[SERVER] SERVER INIT FINISHED");
						//INSERTED CODE
						ServerInitializeEvent event = new ServerInitializeEvent(GameServer.getServerState().getController(), GameServer.getServerState());
						StarLoaderHooks.onServerInitialize(event);
						StarLoader.fireEvent(event, true);
						///
						if(!restart) {
							serverInitFinished = true;
							synchronized(serverLock) {
								serverLock.notify();
							}
						} else {
							run();
						}
					} else {
						ServerState.setShutdown(true);
						synchronized(serverLock) {
							serverLock.notify();
						}
					}
				}
				PassiveResourceManager.load();
				PersistentStructureDataManager.load();
			}
		};
	}

	public static void loadLastUsedSkin() throws IOException {
		File f = new FileExt("./.skin");
		if(f.exists()) {
			BufferedReader fr = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
			String readLine;
			String path = null;
			while((readLine = fr.readLine()) != null) path = readLine;
			fr.close();
			if(path != null) EngineSettings.PLAYER_SKIN.setString(path);
		}
	}

	/**
	 * @return the authStyle
	 */
	public static int getAuthStyle() {
		if(authStyle < 0) {
			if(EngineSettings.A_FORCE_AUTHENTICATION_METHOD.getInt() >= 0) {
				authStyle = EngineSettings.A_FORCE_AUTHENTICATION_METHOD.getInt();
				System.err.println("[INIT] set forced auth style: " + authStyle);
			} else {
				try {
					URL url = new URL("http://files.star-made.org/auth_method");
					HttpURLConnection c = (HttpURLConnection) url.openConnection();
					c.setReadTimeout(10000);
					BufferedReader rd = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
					String line;

					StringBuilder b = new StringBuilder();
					while((line = rd.readLine()) != null) {
						//				System.err.println(line);
						b.append(line);
					}

					rd.close();

					authStyle = Integer.parseInt(b.toString());
				} catch(Exception e) {
					e.printStackTrace();
					authStyle = 1;
				}
				System.err.println("[INIT] Retrieved auth style: " + authStyle);
			}
		}
		return authStyle;
	}

	private void getLastFromSavedList() throws Exception {

		File f = new FileExt("./.sessions");
		if(!f.exists()) {
			throw new FileNotFoundException();
		} else {
			BufferedReader fr = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
			String readLine;
			while((readLine = fr.readLine()) != null) {
				String[] nameHostPort = readLine.split(",", 21);
				String loginName = nameHostPort[0];
				String[] hostPort = nameHostPort[1].split(":", 2);
				String host = hostPort[0];
				int port = Integer.parseInt(hostPort[1]);
				HostPortLoginName hostPortLoginName = new HostPortLoginName(host, port, HostPortLoginName.STARMADE_CLIENT, loginName);
				history.remove(hostPortLoginName);
				history.add(hostPortLoginName);
				//					System.err.println("ADDING FROM HISTORY "+hostPortLoginName);

			}
			fr.close();
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg1 instanceof HostPortLoginName) {
			HostPortLoginName p = (HostPortLoginName) arg1;
			history.remove(p);
			history.add(p);
			updateSavedList(history);
			startClient(p, true, null);
		} else if(arg1 instanceof Exception) {
			System.err.println("CATCHED EXCEPTION");
		} else if(arg1 == null) {
			serverInitFinished = false;
			// since the client is timeRunning from gui anyway
			//	server can also be timeRunning with GUI
			startServer(false, false);
		}
	}

	private void updateSavedList(ArrayList<HostPortLoginName> history) {

		try {
			File f = new FileExt("./.sessions");
			if(!f.exists()) {
				f.createNewFile();
			}
			BufferedWriter fw = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8));
			for(HostPortLoginName p : history) {
				fw.append(p.loginName + "," + p.host + ":" + p.port + "\n");
			}
			fw.flush();
			fw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
