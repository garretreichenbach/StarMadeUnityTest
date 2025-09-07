package api.utils;

import api.SMModLoader;
import api.mod.ModStarter;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.common.Starter;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.GraphicsFrame;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.network.client.HostPortLoginName;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Jake on 1/22/2021.
 * <insert description here>
 */
public class GameRestartHelper {
	public static String getJavaInstallation() {
		String javaHome = System.getProperty("java.home");
		File f = new File(javaHome);
		f = new File(f, "bin");
		f = new File(f, "java.exe");
		System.err.println("Getting java path... " + f + "    exists: " + f.exists());
		if(!f.exists()) {
			f = new File(javaHome);
			f = new File(f, "bin");
			f = new File(f, "java");
			System.err.println("*nix user detected");
			System.err.println("Getting java path... " + f + "    exists: " + f.exists());
		}
		return f.getPath();
	}

	public static List<String> getVMArguments() {
		return ManagementFactory.getRuntimeMXBean().getInputArguments();
	}

	public static void runWithUplink(String serverHost, int serverPort, List<Integer> mods) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(Integer mod : mods) {
			sb.append(mod).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		runWithArguments(SMModLoader.uplinkArgs, "-uplink", serverHost, String.valueOf(serverPort), sb.toString());
	}

	public static void runWithArguments(String[] baseArgs, String... extraArgs) throws IOException {
		ArrayList<String> starArgs = new ArrayList<String>();
		starArgs.add(getJavaInstallation());
		//Carry over VM arguments for memory and stuff
		starArgs.addAll(getVMArguments());

		starArgs.add("-jar");
		starArgs.add("StarMade.jar");

		//Carry over program arguments
		starArgs.addAll(Arrays.asList(baseArgs));
		starArgs.addAll(Arrays.asList(extraArgs));
		System.err.println("!! EXECUTING NEW JAR !!");
		System.err.println("[GameRestartHelper] Args: " + starArgs.toString());
		ProcessBuilder startSM = new ProcessBuilder(starArgs);
		try {
			Map<String, String> env = startSM.environment();
			env.put("__GL_THREADED_OPTIMIZATIONS", "0");
			startSM.inheritIO();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("InheritIO requires java 7 to run");
		}

		Process start = startSM.start();
		//Exit after done running starmade
		System.exit(0);
	}

	/**
	 * Equivalent code to clicking an online server
	 */
	public static void startOnlineWorld(final String host, final int port) {

		System.err.println("[CLIENT] starting selected online universe: " + host + ":" + port);
		EngineSettings.LAST_GAME.setString("MP;" + host + ";" + port + ";" + EngineSettings.ONLINE_PLAYER_NAME.getString().toString().trim());

		try {
			EngineSettings.write();
		} catch(IOException var4) {
			var4.printStackTrace();
		}

		Starter.serverInitFinished = true;
		final GameMainMenuController inst = GameMainMenuController.currentMainMenu;
		inst.setFrame((GraphicsFrame) null, false);
		(new Thread(() -> {
			ElementKeyMap.skipNextBlockInitializeForMods = true;
			ElementKeyMap.needToReparseBlockDataForMods = true;
			try {
				Starter.initialize(false);
			} catch(Exception var2) {
				var2.printStackTrace();
				String var1 = StringTools.format("...", new Object[]{var2.getClass().getSimpleName()});
				//inst.graphicsContext.setLoadMessage(var1);
				inst.graphicsContext.handleError(var1);
			}
			Starter.startClient(new HostPortLoginName(host, port, (byte) 0, EngineSettings.ONLINE_PLAYER_NAME.getString()), false, inst.graphicsContext);
		})).start();
	}

	/**
	 * Equivalent code to clicking on a world in the selection menu
	 */
	public static void startLocalWorld() {
		final GraphicsContext graphicsContext = GameMainMenuController.currentMainMenu.getGraphicsContext();
		graphicsContext.setFrame(null, false);
		new Thread(() -> {
			GraphicsContext graphicsContext1 = GameMainMenuController.currentMainMenu.getGraphicsContext();
			Controller.getResLoader().loadAll();
			try {
				//need to save as server reloads it on start
				ServerConfig.write();
			} catch(IOException e2) {
				e2.printStackTrace();
			}

			try {

				//graphicsContext.setLoadMessage(Lng.str("Initializing Universe... reading configs"));
				GameServerState.readDatabasePosition(false);

				Starter.initializeServer(false);
				try {
					Starter.initialize(false);
				} catch(SecurityException e1) {
					e1.printStackTrace();
				}
				//graphicsContext.setLoadMessage(Lng.str("Initializing Universe... doing startup checks and migration if necessary"));
				Starter.doMigration(new JDialog(), false);

				//INSERTED CODE
//                if(SteamAPIHandler.initialized && SteamConfigManager.getConfig().getBoolean("allow-friends-to-join-local")) Starter.apiHandler.getMatchmaking().createLobby(SteamMatchmaking.LobbyType.FriendsOnly, (int) ServerConfig.MAX_CLIENTS.getInt());
				//
			} catch(Exception e) {
				e.printStackTrace();
				String error = Lng.str("Initializing Universe failed! Please send in an error report to help.star-made.org! %s", e.getClass().getSimpleName());
				//graphicsContext.setLoadMessage(error);
				graphicsContext1.handleError(error);
				return;
			}
			//graphicsContext.setLoadMessage(Lng.str("Initializing Universe... starting local server"));
			HostPortLoginName n = new HostPortLoginName("localhost", 4242, HostPortLoginName.STARMADE_CLIENT, EngineSettings.OFFLINE_PLAYER_NAME.getString());
			ModStarter.justStartedSinglePlayer = true;
			Starter.startServer(false, false);
			//graphicsContext.setLoadMessage(Lng.str("Initializing Universe... connecting to local server and synchronizing"));
			Starter.startClient(n, false, graphicsContext1);
		}).start();
	}
}
