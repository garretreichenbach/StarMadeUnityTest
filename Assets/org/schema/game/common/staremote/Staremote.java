package org.schema.game.common.staremote;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.ConnectException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.Starter;
import org.schema.game.common.staremote.gui.StarmoteConnectionFrame;
import org.schema.game.common.staremote.gui.StarmoteFrame;
import org.schema.game.common.staremote.gui.connection.StarmoteConnection;
import org.schema.game.common.staremote.gui.entity.StarmoteEntitySettingsPanel;
import org.schema.game.common.staremote.gui.player.StarmoteOfflinePlayerList;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.network.LoginFailedException;
import org.schema.schine.network.client.HostPortLoginName;
import org.schema.schine.network.commands.LoginRequest.LoginCode;


public class Staremote {

	public static StarmoteEntitySettingsPanel currentlyVisiblePanel;
	public boolean exit = false;
	private GameClientState state;
	private StarmoteFrame guiFrame;
	private StarmoteOfflinePlayerList playersRequest;

	public static String getConnectionFilePath() {
		return "./.starmotecon";
	}

	public static void main(String[] args) {
		
		VersionContainer.loadVersion();

		try {
			EngineSettings.read();
			Starter.initialize(true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Staremote m = new Staremote();
		m.exit = true;
		m.startConnectionGUI();
		//		m.connect();
	}

	public void connect(StarmoteConnection c) {
		final HostPortLoginName server = new HostPortLoginName(c.url, c.port, HostPortLoginName.STAR_MOTE, c.username);
		Thread t = new Thread(() -> {

			try {
				state = new GameClientState(true);
				GameClientController c1 = new GameClientController(state, null);
				c1.connect(server);
				c1.initialize();
//					GLFrame.state = state;
				startGUI(state);

				Timer t1 = new Timer();
				t1.initialize(false);
				while (true) {
					c1.update(t1);
					t1.updateFPS(false);
					Thread.sleep(1000);

					update(state);
				}
				//					EngineSettings.print();
				//					c.startGraphics();
			} catch (LoginFailedException e) {
				e.printStackTrace();
				String msg = null;

				LoginCode code = LoginCode.getById(e.getErrorCode());
				msg = code.errorMessage();
				if (code == LoginCode.ERROR_WRONG_CLIENT_VERSION) {
					msg = "Server: your client version is not allowed to connect.\n" +
							"Please download the correct version from www.star-made.org\n" +
							"Client Version: " + VersionContainer.VERSION + "\n" +
							"Server Version: " + GameClientState.serverVersion;
					if (VersionContainer.compareVersion(GameClientState.serverVersion) < 0) {
						msg += "\n\nThe server has not yet updated to your version.\nPlease wait for the server to update and then try again.";
					} else {
						msg += "\n\nYour version is older then the server-version. Please update.";
					}
				}

				JFrame loginError = new JFrame("LoginError");
				loginError.setAlwaysOnTop(true);
				int n = JOptionPane.showOptionDialog(loginError, msg, "Login Failed",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,
						null, new String[]{"Back To Login Screen", "EXIT"}, "Back To Login Screen");
				switch (n) {
					case 0:

						break;
					case 1:
						System.err.println("Exiting because of login failed");
						try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
						break;

				}
			} catch (Exception e) {
				String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
				if (e instanceof ConnectException) {
					ConnectException ce = (ConnectException) e;
					msg += ", \n\nfailed to connect to \"" + server.host + ":" + server.port + "\"." +
							"\nEither server is down, blocking, or the adress is wrong!";
				}
				e.printStackTrace();
				int n = JOptionPane.showOptionDialog(null, msg, "ERROR",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,
						null, new String[]{"Ok", "Exit"}, "Exit");
				switch (n) {
					case 0:
						//						connectDialog.exit = false;
						//						clientStartup();
						break;
					case 1:
						System.err.println("Exiting because of failed conenct");
						try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
						break;
					case 2:
						System.err.println("Exiting because of failed conenct");
						try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
						break;

				}
			} finally {
				//					connectDialog.dispose();
			}
		});
		t.start();
	}

	public void disconnect() {
		try {
			state.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exit() {

		disconnect();

		if(exit){
			try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
		}
	}

	public void requestAllPlayers(StarmoteOfflinePlayerList offlineModel) {
		this.playersRequest = offlineModel;
	}

	public void startConnectionGUI() {
		EventQueue.invokeLater(() -> {
			JFrame guiFrame = new StarmoteConnectionFrame(Staremote.this);
			guiFrame.setVisible(true);
			guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			guiFrame.requestFocus();

		});
	}

	private void startGUI(final GameClientState state) {
		EventQueue.invokeLater(() -> {
			assert (state != null);
			guiFrame = new StarmoteFrame(state, Staremote.this);
			guiFrame.setVisible(true);
			guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		});
	}

	public void update(GameClientState state) {
//		if (currentlyVisiblePanel != null) {
//			currentlyVisiblePanel.updateSettings();
//		}
//		StarMadeServerStats stats;
//		try {
//			stats = state.getController().requestServerStats();
//			guiFrame.updateStats(stats);
//
//			if (playersRequest != null) {
//				StarMadePlayerStats requestPlayerStats = state.getController().requestPlayerStats(0);
//				playersRequest.update(requestPlayerStats);
//				playersRequest = null;
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

	}

}
