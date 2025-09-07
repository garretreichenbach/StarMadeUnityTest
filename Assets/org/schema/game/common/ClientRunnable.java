package org.schema.game.common;

import api.StarLoaderHooks;
import api.listener.events.controller.ClientInitializeEvent;
import api.mod.StarLoader;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.ClientStatics;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.util.DesktopUtils;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.LoginFailedException;
import org.schema.schine.network.NetUtil;
import org.schema.schine.network.client.HostPortLoginName;
import org.schema.schine.network.commands.LoginRequest.LoginCode;
import org.schema.schine.network.server.ServerController;
import org.schema.schine.resource.FileExt;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;

public class ClientRunnable implements Runnable {

	private HostPortLoginName server;
	private GameClientState gameClientState;
	private GameClientController gameClientController;
	private GraphicsContext context;

	public ClientRunnable(HostPortLoginName server, boolean startConnectDialog, GraphicsContext context) {
		this.server = server;
		this.context = context;
		if(this.context == null){
			throw new NullPointerException("Starting Client without Graphics Context");
		}
	}

	public static void openWebpage(URI uri) {
		DesktopUtils.browseURL(uri);
	}	
	
	
	@Override
	public void run() {

		System.err.println("[CLIENT] initializing " + VersionContainer.VERSION + " (" + VersionContainer.build + ")");

		KeyboardMappings.read();
		try {
			KeyboardMappings.write();
		} catch (IOException e1) {
			e1.printStackTrace();
			GuiErrorHandler.processNormalErrorDialogException(e1, true);
		}

		File f = new FileExt(ClientStatics.SEGMENT_DATA_DATABASE_PATH);
		f.mkdirs();
		boolean removeConnectionDialog = true;
		try {
			synchronized (Starter.serverLock) {
				while (!Starter.serverInitFinished) {
					Controller.setLoadMessage(Lng.str("Initializing Universe... waiting for local server to initialize"));
					System.err.println("[CLIENT] WAITING FOR SERVER");
					Starter.serverLock.wait(NetUtil.WAIT_TIMEOUT);
					
					if(Starter.startupException != null){
						System.err.println("[CLIENT] Intercepted exception "+Starter.startupException.getClass().getSimpleName());
						Exception e = Starter.startupException; 
						Starter.startupException = null;
						throw(e);
					}
				}
			}
			Controller.setLoadMessage(Lng.str("Initializing Universe... server initialized"));
			if (GameServerState.isCreated()) {
				System.err.println("[LOCAL GAME] CHANGED PORT TO " + ServerController.port);
				server.port = ServerController.port;
			}
			
			if(Starter.startupException != null){
				System.err.println("[CLIENT] Intercepted exception "+Starter.startupException.getClass().getSimpleName());
				Exception e = Starter.startupException; 
				Starter.startupException = null;
				throw(e);
			}
			
			gameClientState = new GameClientState();
			gameClientController = new GameClientController(gameClientState, context);
			//INSERTED CODE
			ClientInitializeEvent event = new ClientInitializeEvent(gameClientController, gameClientState);
			StarLoader.fireEvent(event, false);
			StarLoaderHooks.onClientInitialize(event);
			///
			System.err.println("[CLIENT] Client State and Controller successfully created");
			if(GameServerState.isCreated()){
				GameClientState.singleplayerCreativeMode = 
						EngineSettings.G_SINGLEPLAYER_CREATIVE_MODE.isOn() ? 
						GameClientState.CREATIVE_MODE_ON : GameClientState.CREATIVE_MODE_OFF;
				System.err.println("[CLIENT] Singleplayer Creative mode: "+EngineSettings.G_SINGLEPLAYER_CREATIVE_MODE.isOn()+": "+GameClientState.singleplayerCreativeMode);
			}
			Controller.setLoadMessage(Lng.str("Connecting to Universe..."));
			(new Thread(() -> {
				int i = 0;

				while(i < 10){
					if(context != null && Starter.startupException != null){
						System.err.println("[CLIENT] Intercepted exception "+Starter.startupException.getClass().getSimpleName());
						Exception e = Starter.startupException;
						Starter.startupException = null;
						context.handleError(e);
					}
					i++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			})).start();
			gameClientController.connect(server);
			System.err.println("[CLIENT] Client State and Controller successfully connected");
			gameClientController.initialize();
			System.err.println("[CLIENT] Client State and Controller successfully initialized");
			EngineSettings.print();
			removeConnectionDialog = gameClientController.startGraphics(context);
			System.err.println("[CLIENT] AFTER GRAPHICS RUN!");
		} catch (LoginFailedException e) {
			try{
				e.printStackTrace();
				
				
				String msg = null;
	
				LoginCode code = LoginCode.getById(e.getErrorCode());
				msg = code.errorMessage()+gameClientState.getExtraLoginFailReason();
				if (code == LoginCode.ERROR_WRONG_CLIENT_VERSION) {
					msg = Lng.str("Server %s Your client version is not allowed to connect.\n" +
							"Please download the correct version with the launcher\n" +
							"Client Version: %s\n" +
							"Server Version: %s", server, VersionContainer.VERSION, GameClientState.serverVersion);
					if (VersionContainer.compareVersion(GameClientState.serverVersion) < 0) {
						msg += "\n\n" + Lng.str("The server has not yet updated to your version.\nPlease wait for the server to update and then try again.");
					} else {
						msg += "\n\n" + Lng.str("Your version is older then the server-version. Please update.");
					}
				}
				if(context != null){
					removeConnectionDialog = false;
					Starter.stopClient(context);
					System.err.println("[LOGINERROR] "+msg);
					context.handleError(msg);
				}else{
					JFrame loginError = new JFrame("LoginError");
					loginError.setAlwaysOnTop(true);
					int n;
					if (code == LoginCode.ERROR_AUTHENTICATION_FAILED_REQUIRED) {
						
						n = JOptionPane.showOptionDialog(loginError, msg, "Login Failed",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,
								null, new String[]{"Back To Login Screen", "EXIT", "Uplink", "Register on www.star-made.org"}, "Back To Login Screen");
						
					} else {
						n = JOptionPane.showOptionDialog(loginError, msg, "Login Failed",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,
								null, new String[]{"Back To Login Screen", "EXIT"}, "Back To Login Screen");
					}
					switch(n) {
						default -> {
							System.err.println("[CLIENT] Exiting because of login failed");
							try {throw new Exception("System.exit() called");} catch(Exception ex) {ex.printStackTrace();}
							System.exit(0);
						}
					}
	
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception e) {
			try{
				String msg;
				e.printStackTrace();
				if(GameServerState.isCreated()){
					msg = Lng.str("We're sorry!\n\nSomething went wrong initializing the universe!\nTry reinstalling the game in case of a bad install.\nIf that doesn't help, please contact our support.");
					
				}else{
				
					
					msg = e.getClass().getSimpleName() + ": " + e.getMessage();
					if (e.getCause() != null) {
						msg += Lng.str("\nError Cause: %s: %s", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
					}
					if (e instanceof ConnectException) {
						ConnectException ce = (ConnectException) e;
						msg += Lng.str(", \n\nfailed to connect to \"%s:%s\"." +
								"\nEither server is down, blocking, or the adress is wrong!", server.host, server.port);
					}
				}
				if(context != null){
					removeConnectionDialog = false;
					Starter.stopClient(context);
					context.handleError(msg);
					System.err.println("[LOGINERROR] "+msg);
				}else{
					String str = Lng.str("Exit");
					int n = JOptionPane.showOptionDialog(null, msg, "ERROR",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,
							null, new String[]{str}, str);
					switch(n) {
						default -> {
							System.err.println("Exiting because of failed conenct");
							try {throw new Exception("System.exit() called");} catch(Exception ex) {ex.printStackTrace();}
							System.exit(0);
						}
					}
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		} finally {
			if (removeConnectionDialog) {
//				try{
//					throw new Exception("CLIENT RUNNABLE EXITED WITH UNNATURAL CAUSE!");
//				}catch(Exception e){
//					e.printStackTrace();
//				}
				System.err.println("[CLIENT] REMOVING CONNECTION DIALOG");
			}
		}
	}

	public void callback() {
		run();
	}


	public void stopClient() {
		if(Controller.getResLoader() != null){
			Controller.getResLoader().onStopClient();
		}
		if(gameClientController != null){
			gameClientController.onStopClient();
		}
		
		
	}


}
