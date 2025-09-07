package org.schema.game.client.controller;

import api.mod.ModIdentifier;
import api.mod.ModStarter;
import api.mod.ServerModInfo;
import api.utils.gui.SimplePopup;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.glfw.GLFW;
import org.schema.common.ParseException;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.gui.LoadingScreenDetailed;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.LocalUniverse;
import org.schema.game.client.view.mainmenu.MainMenuFrame;
import org.schema.game.client.view.mainmenu.gui.GUIOnlineUniversePanel;
import org.schema.game.client.view.mainmenu.gui.GameStarterState;
import org.schema.game.common.Starter;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.util.DataUtil;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.common.util.FolderZipper.ZipCallback;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.util.ZipGUICallback;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.GraphicsMainMenuController;
import org.schema.schine.common.JoystickAxisMapping;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ButtonColorImpl;
import org.schema.schine.graphicsengine.movie.subtitles.Subtitle;
import org.schema.schine.input.*;
import org.schema.schine.network.ServerInfo;
import org.schema.schine.network.ServerListRetriever;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.client.HostPortLoginName;
import org.schema.schine.network.server.ServerState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class GameMainMenuController extends GraphicsMainMenuController implements GameStarterState, InputState, InputController, ErrorHandlerInterface{




	public static GameMainMenuController currentMainMenu;

	private final BasicInputController inputController = new BasicInputController();

	private final ServerListRetriever serverListRetriever = new ServerListRetriever();

	private final List<Object> generalChatLog = new ObjectArrayList<Object>();

	private final List<Object> visibleChatLog = new ObjectArrayList<Object>();

	private short updateNumber;

	public long updateTime;

	private final MainMenuFrame frame;

	private LocalUniverse selectedLocalUniverse;

	private int maxFile;

	private int file;

	private ServerInfo selectedOnlineUniverse;

	private Exception queuedException;

	private boolean showedWarning;

	private boolean inTextBox;

	public GameMainMenuController() throws IOException {
		super();
		if (GUITextButton.cp == null) {
			GUITextButton.cp = new ButtonColorImpl();
		}
		if (Controller.getResLoader() == null) {
			Controller.initResLoader(new GameResourceLoader());
		}
		AudioController.initialize();

		this.frame = new MainMenuFrame(this);
		setFrame(frame, false);
		graphicsContext.errorHandler = this;
		GameMainMenuController.currentMainMenu = this;
	}

	public void switchFrom(final GameClientState state) {
		switchFrom(state, null);
	}

	public void switchFrom(final GameClientState state, final Exception ex) {
		// try {
		// throw new Exception("Switching to Main Menu");
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }
		if (state.getScene() != null) {
			GraphicsContext.cleanUpScene = state.getScene();
		}
		(new Thread(() -> {
			setFrame(null, false);
			state.stopClient();
			state.setDoNotDisplayIOException(true);
			state.setExitApplicationOnDisconnect(false);
			ClientState.setFinishedFrameAfterLocalServerShutdown = false;
			if (ServerState.isCreated()) {
				ServerState.setFlagShutdown(true);
			}
			int i = 0;
			String[] ld = new String[] { "/", "-", "\\" };
			int e = 0;
			long ct = System.currentTimeMillis();
			while (ServerState.isCreated()) {
				Controller.setLoadMessage("WAITING FOR LOCAL SERVER TO SHUT DOWN... " + ld[e % ld.length]);
				if (System.currentTimeMillis() - ct > 30) {
					e++;
					ct = System.currentTimeMillis();
				}
				if (e > 1000 && e % 500 == 0) {
					System.err.println("[MAINMENU] SOMETHING WENT WRONG SHUTTING DOWN SERVER: " + ServerState.isCreated() + "; " + ServerState.isShutdown());
					if (ServerState.isShutdown()) {
						break;
					}
				}
				i++;
			}
			Controller.setLoadMessage("RESETTING VARIABLES... ");
			ServerState.clearStatic();
			Controller.setLoadMessage("LOADING MAIN MENU... ");
			setFrame(frame, false);
			GLFrame.activeForInput = true;
			queuedException = ex;
		})).start();
		;
	}

	public void update() {
		final Timer timer = graphicsContext.timer;
		updateTime = timer.currentTime;
		if (queuedException != null) {
			errorDialog(queuedException);
			queuedException = null;
			assert (inputController.getPlayerInputs().size() > 0);
		}
		if (!VersionContainer.is64Bit() && EngineSettings.SHOW_32BIT_WARNING.isOn() && !showedWarning) {
			PlayerOkCancelInput p = new PlayerOkCancelInput("32BIT_SSss", getState(), 460, 280, Lng.str("32BIT WARNING"), Lng.str("You are running in 32-Bit mode. If your system is 64-Bit,\n" + "this can limit the performance and memory given to the game and crash it.\n\n" + "To fix this, please run the beta launcher in STEAM.\n" + "To do this, just right click on the game and click on\n\"Launch with Beta Launcher\".\n\n" + "If you don't run the game in STEAM,\n" + "please download the 64-Bit launcher from www.star-made.org")) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}

				@Override
				public void pressedSecondOption() {
					super.pressedSecondOption();
					EngineSettings.SHOW_32BIT_WARNING.setOn(false);
					try {
						EngineSettings.write();
					} catch (IOException e) {
						e.printStackTrace();
					}
					deactivate();
				}
			};
			p.getInputPanel().setSecondOptionButton(true);
			p.getInputPanel().setSecondOptionButtonWidth(120);
			p.getInputPanel().setSecondOptionButtonText(Lng.str("don't show again"));
			p.getInputPanel().setCancelButton(false);
			p.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(53);
			showedWarning = true;
		}
		frame.update(timer);
		inputController.updateInput(this, timer);
		((LoadingScreenDetailed) graphicsContext.getLoadingScreen()).setMainMenu(this);
		


		updateNumber++;
	}

	@Override
	public List<DialogInterface> getPlayerInputs() {
		return inputController.getPlayerInputs();
	}

	@Override
	public boolean isChatActive() {
		return false;
	}

	public static void createWorld(final String newUniverseName, final InputState state) {
		if (newUniverseName.toLowerCase(Locale.ENGLISH).equals("old")) {
			PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", state, 300, 150, Lng.str("Error"), Lng.str("Cannot create universe. This name is not permitted.")) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			c.getInputPanel().setCancelButton(false);
			c.getInputPanel().onInit();
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(56);
		} else {
			File f = new FileExt(GameServerState.SERVER_DATABASE + newUniverseName);
			if (f.exists()) {
				PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", state, 300, 150, Lng.str("Error"), Lng.str("Cannot create universe. A universe with that name already exists.")) {

					@Override
					public void pressedOK() {
						deactivate();
					}

					@Override
					public void onDeactivate() {
					}
				};
				c.getInputPanel().setCancelButton(false);
				c.getInputPanel().onInit();
				c.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(55);
			} else {
				File pp = new FileExt(GameServerState.SERVER_DATABASE + newUniverseName + File.separator);
				pp.mkdirs();
				List<LocalUniverse> readUniverses = LocalUniverse.readUniverses();
				if (readUniverses.size() > 0) {
					GUIElement[] worlds = new GUIElement[readUniverses.size() - 1];
					int i = 0;
					for (LocalUniverse localUniverse : readUniverses) {
						if (localUniverse.name.equals(newUniverseName)) {
							continue;
						}
						GUIAnchor w = new GUIAnchor(state, UIScale.getUIScale().scale(200), UIScale.getUIScale().h);
						GUITextOverlay t = new GUITextOverlay(FontSize.MEDIUM_15, state);
						t.setTextSimple(localUniverse.name);
						t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
						w.attach(t);
						w.setUserPointer(localUniverse);
						worlds[i] = w;
						i++;
					}
					String msg = Lng.str("Copy catalog from another world?\n\n" + "Pressing cancel will not delete your blueprints,\n" + "but all catalog entries will reset their permissions (owner, pirate usable, etc).");
					PlayerDropDownInput dd = new PlayerDropDownInput("DD", state, UIScale.getUIScale().scale(450), UIScale.getUIScale().scale(240), Lng.str("Copy Catalog?"), UIScale.getUIScale().h, msg, worlds) {

						@Override
						public void pressedOK(GUIListElement current) {
							try {
								LocalUniverse v = (LocalUniverse) current.getContent().getUserPointer();
								File from = new FileExt(GameServerState.SERVER_DATABASE + newUniverseName + File.separator + CatalogManager.CALAOG_FILE_NAME_FULL);
								File to = new FileExt(GameServerState.SERVER_DATABASE + v.name + File.separator + CatalogManager.CALAOG_FILE_NAME_FULL);
								try {
									FileUtil.copyFile(from, to);
								} catch (IOException e) {
									e.printStackTrace();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							deactivate();
						}

						@Override
						public void onDeactivate() {
							((GameMainMenuController) state).notifyObservers();
						}
					};
					dd.setDropdownYPos(120);
					dd.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(54);
				}
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e, Timer timer) {
		switch(e.getKey()) {
			case (GLFW.GLFW_KEY_ENTER):
				if (frame != null) {
					if (inputController.getPlayerInputs().isEmpty()) {
						startLastUsed();
					}
				}
				break;
			case (GLFW.GLFW_KEY_F11):
				if (frame != null) {
					try {
						frame.enqueueFrameResources();
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					} catch (ResourceException ex) {
						ex.printStackTrace();
					} catch (ParseException ex) {
						ex.printStackTrace();
					} catch (SAXException ex) {
						ex.printStackTrace();
					} catch (IOException ex) {
						ex.printStackTrace();
					} catch (ParserConfigurationException ex) {
						ex.printStackTrace();
					}
				}
				break;
		}
		if(e.isTriggered(KeyboardMappings.FULLSCREEN_TOGGLE)) {
			EngineSettings.G_FULLSCREEN.setOn(!EngineSettings.G_FULLSCREEN.isOn());
			DialogInput.applyScreenSettings();
			currentMainMenu.frame.resizeBG();
		}
	}

	@Override
	public void handleLocalMouseInput() {
	}

	@Override
	public boolean beforeInputUpdate() {
		return true;
	}

	@Override
	public BasicInputController getInputController() {
		return inputController;
	}

	@Override
	public boolean isJoystickOk() {
		return JoystickMappingFile.ok();
	}

	@Override
	public double getJoystickAxis(JoystickAxisMapping map) {
		return inputController.getJoystick().getAxis(map);
	}

	@Override
	public InputState getState() {
		return this;
	}

	@Override
	public InputController getController() {
		return this;
	}

	@Override
	public short getNumberOfUpdate() {
		return updateNumber;
	}

	@Override
	public List<Object> getGeneralChatLog() {
		return generalChatLog;
	}

	@Override
	public List<Object> getVisibleChatLog() {
		return visibleChatLog;
	}

	@Override
	public void onSwitchedSetting(EngineSettings engineSettings) {
	}

	@Override
	public String onAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
		return null;
	}

	public ServerListRetriever getServerListRetriever() {
		return serverListRetriever;
	}

	@Override
	public void onSelected(Object t) {
		assert (t != null);
		if (t instanceof LocalUniverse) {
			System.err.println("[GUI] SELECTED SP INFO " + t);
			this.selectedLocalUniverse = (LocalUniverse) t;
		} else if (t instanceof ServerInfo) {
			System.err.println("[GUI] SELECTED ONLINE SERVER INFO " + t);
			this.selectedOnlineUniverse = (ServerInfo) t;
		}
	}

	@Override
	public void onDoubleClick(Object t) {
		if (t instanceof LocalUniverse) {
			startSelectedLocalGame();
		} else if (t instanceof ServerInfo) {
			startSelectedOnlineGame();
		}
	}

	@Override
	public void startSelectedLocalGame() {
		if (selectedLocalUniverse != null) {
			EngineSettings.LAST_GAME.setString("SP;" + selectedLocalUniverse.name + ";" + "4242;" + EngineSettings.OFFLINE_PLAYER_NAME.getString());
			try {
				EngineSettings.write();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			try {
				// need to save as server reloads it on start
				ServerConfig.write();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			String playerName = EngineSettings.OFFLINE_PLAYER_NAME.getString().trim();
			boolean ok = true;
			boolean none = false;
			if (playerName.trim().length() <= 0) {
				none = true;
				ok = false;
			} else if (playerName.length() > 32) {
				ok = false;
			} else if (playerName.length() <= 2) {
				ok = false;
			} else if (playerName.matches("[_-]+")) {
				ok = false;
			} else {
				if (!playerName.matches("[a-zA-Z0-9_-]+")) {
					ok = false;
				}
			}
			if (!ok) {
				if (!none) {
					PlayerOkCancelInput r = new PlayerOkCancelInput("ERROR", getState(), 300, 150, Lng.str("PlayerName Invalid"), Lng.str("Your username is not allowed.\nMust be at least 3 characters.\nOnly letters, numbers, '-' and '_' are allowed.")) {

						@Override
						public void pressedOK() {
							deactivate();
						}

						@Override
						public void onDeactivate() {
						}
					};
					r.getInputPanel().setCancelButton(false);
					r.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(59);
				} else {
					PlayerTextInput r = new PlayerTextInput("PLAYERNAME", getState(), 32, Lng.str("Choose Player Name"), Lng.str("Please enter a name for your character.")) {

						@Override
						public void onDeactivate() {
						}

						@Override
						public String[] getCommandPrefixes() {
							return null;
						}

						@Override
						public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
							return null;
						}

						@Override
						public void onFailedTextCheck(String msg) {
						}

						@Override
						public boolean onInput(String playerName) {
							boolean ok = true;
							if (playerName.trim().length() <= 0) {
								ok = false;
							} else if (playerName.length() > 32) {
								ok = false;
							} else if (playerName.length() <= 2) {
								ok = false;
							} else if (playerName.matches("[_-]+")) {
								ok = false;
							} else {
								if (!playerName.matches("[a-zA-Z0-9_-]+")) {
									ok = false;
								}
							}
							if (!ok) {
								PlayerOkCancelInput r = new PlayerOkCancelInput("ERROR", getState(), 300, 150, Lng.str("PlayerName Invalid"), Lng.str("Your username is not allowed.\nMust be at least 3 characters.\nOnly letters, numbers, '-' and '_' are allowed.")) {

									@Override
									public void pressedOK() {
										deactivate();
									}

									@Override
									public void onDeactivate() {
									}
								};
								r.getInputPanel().setCancelButton(false);
								r.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(57);
							} else {
								AudioController.instance.stopMusic();
								EngineSettings.OFFLINE_PLAYER_NAME.setString(playerName);
								try {
									EngineSettings.write();
									EngineSettings.OFFLINE_PLAYER_NAME.notifyChanged();
									startSelectedLocalGame();
								} catch (IOException e) {
									e.printStackTrace();
								}

							}
							return ok;
						}
					};
					r.getInputPanel().setCancelButton(false);
					r.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(58);
				}
				return;
			}
			setFrame(null, false);
			// final Object synch = new Object();
			// boolean loading = false;
			// (new Thread(new Runnable(){
			//
			// @Override
			// public void run() {
			//
			// }
			// })).start();
			new Thread(() -> {
				Controller.getResLoader().loadAll();
				try {
					ServerConfig.WORLD.setString(selectedLocalUniverse.name);
					// need to save as server reloads it on start
					ServerConfig.write();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				try {
					Controller.setLoadMessage(Lng.str("Initializing Universe... reading configs"));
					GameServerState.readDatabasePosition(false);
					Starter.initializeServer(false);
					try {
						Starter.initialize(false);
					} catch (SecurityException e1) {
						e1.printStackTrace();
					}
					Controller.setLoadMessage(Lng.str("Initializing Universe... doing startup checks and migration if necessary"));
					Starter.doMigration(new JDialog(), false);
				} catch (Exception e) {
					e.printStackTrace();
					String error = Lng.str("Initializing Universe failed! Please send in an error report to help.star-made.org! %s", e.getClass().getSimpleName());
					Controller.setLoadMessage(error);
					graphicsContext.handleError(error);
					return;
				}
				Controller.setLoadMessage(Lng.str("Initializing Universe... starting local server"));
				HostPortLoginName n = new HostPortLoginName("localhost", 4242, HostPortLoginName.STARMADE_CLIENT, EngineSettings.OFFLINE_PLAYER_NAME.getString());
				ModStarter.justStartedSinglePlayer = true;
				Starter.startServer(false, false);
				Controller.setLoadMessage(Lng.str("Initializing Universe... connecting to local server and synchronizing"));
				Starter.startClient(n, false, graphicsContext);
			}).start();
		}
	}

	public void deleteSelectedWorld(boolean backup) {
		if (selectedLocalUniverse != null) {
			backupAndDeleteSelected(backup, true);
		}
	}

	public void importDB(final File zipFile, final String name) {
		new Thread(() -> {
			try {
				setFrame(null, false);
				FileUtil.extract(zipFile, GameServerState.SERVER_DATABASE + name + File.separator, "server-database", f -> Controller.setLoadMessage("Extracting " + f.getName() + "..."));
			} catch (Exception e) {
				e.printStackTrace();
				errorDialog(e);
			} finally {
				setFrame(frame, false);
				GameMainMenuController.this.notifyObservers();
			}
		}).start();
	}

	public void backupAndDeleteSelected(final boolean backup, final boolean delete) {
		setFrame(null, false);
		if (selectedLocalUniverse == null) {
			System.err.println("[MAINMENU] ERROR: NO UNIVERSE SELECTED");
			return;
		}
		new Thread(() -> {
			try {
				if (backup) {
					if (selectedLocalUniverse != null) {
						System.err.println("[CLIENT] BACKING UP " + selectedLocalUniverse.name);
						final ZipGUICallback guiCallBack = new ZipGUICallback();
						ZipCallback zipCallback = f -> {
							Controller.setLoadMessage(Lng.str("Backing up %s / %s ... File %s", file, maxFile, f.getName()));
							guiCallBack.f = f;
							guiCallBack.fileMax = maxFile;
							guiCallBack.fileIndex = file;
							file++;
						};
						backUp(selectedLocalUniverse.name, zipCallback);
					}
				}
				if (delete) {
					System.err.println("[CLIENT] DELETING UNIVERSE " + selectedLocalUniverse.name);
					Controller.setLoadMessage(Lng.str("Deleting Universe... %s", selectedLocalUniverse.name));
					if (selectedLocalUniverse.name.toLowerCase(Locale.ENGLISH).equals("old")) {
						File dir = new FileExt(GameServerState.SERVER_DATABASE);
						File[] listFiles = dir.listFiles(pathname -> {
							if(pathname.getName().toLowerCase(Locale.ENGLISH).equals("index") || pathname.getName().toLowerCase(Locale.ENGLISH).equals("data")) {
								return true;
							}
							return !pathname.isDirectory();
						});
						for (File f : listFiles) {
							if (f.isDirectory()) {
								try {
									FileUtil.deleteRecursive(f);
								} catch (IOException e) {
									e.printStackTrace();
									errorDialog(e);
								}
							} else {
								f.delete();
							}
						}
					} else {
						try {
							FileUtil.deleteRecursive(new FileExt(GameServerState.SERVER_DATABASE + selectedLocalUniverse.name));
						} catch (IOException e) {
							e.printStackTrace();
							String error = Lng.str("Initializing Universe failed! Please send in an error report to help.star-made.org! %s", e.getClass().getSimpleName());
							Controller.setLoadMessage(error);
							graphicsContext.handleError(error);
							return;
						}
					}
				}
			} finally {
				setFrame(frame, false);
				GameMainMenuController.this.notifyObservers();
			}
		}).start();
	}

	public void backUp(final String name, ZipCallback callback) {
		try {
			FileFilter f = pathname -> {
				System.err.println("[BACKUP] CHECKING ZIPPING " + pathname);
				if (pathname.getName().toLowerCase(Locale.ENGLISH).equals(name.toLowerCase(Locale.ENGLISH)) || pathname.getName().toLowerCase(Locale.ENGLISH).equals("index") || pathname.getName().toLowerCase(Locale.ENGLISH).equals("data") || pathname.getName().toLowerCase(Locale.ENGLISH).equals("server-database")) {
					return true;
				}
				return !pathname.isDirectory();
			};
			if (name.equals("old")) {
				backUp("./", GameServerState.SERVER_DATABASE, String.valueOf(System.currentTimeMillis()), ".smdb", false, true, f, callback);
			} else {
				backUp("./", GameServerState.SERVER_DATABASE + name, name + String.valueOf(System.currentTimeMillis()), ".smdb", false, true, f, callback);
			}
		} catch (IOException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}
	}

	public void backUp(String installDir, String databasePath, String backupName, String fileExtension, boolean removeOldVersion, boolean databaseOnly, FileFilter filter, ZipCallback zipCallback) throws IOException {
		File dir = new FileExt(installDir);
		if (dir.exists() && dir.list().length > 0) {
			notifyObservers();
			String backup = ("backup-StarMade-" + VersionContainer.VERSION + "-" + VersionContainer.build + "_" + backupName + (!fileExtension.startsWith(".") ? ("." + fileExtension) : fileExtension));
			System.out.println("Backing Up (archiving files)");
			file = 0;
			if (databaseOnly) {
				maxFile = FileUtil.countFilesRecusrively((new FileExt(installDir)).getAbsolutePath() + File.separator + databasePath);
			} else {
				maxFile = FileUtil.countFilesRecusrively((new FileExt(installDir)).getAbsolutePath());
			}
			System.err.println("[BACKUP] Total files: " + maxFile);
			if (databaseOnly) {
				// zip everything except backups themselves
				File f = new FileExt(installDir + File.separator + databasePath + File.separator);
				if (f.exists() && f.isDirectory()) {
					FolderZipper.zipFolder(f.getAbsolutePath(), backup + ".tmp", "backup-StarMade-", zipCallback, "", filter, true);
				}
			// for (File fg : f.listFiles()) {
			// System.err.println("[BACKUP] looking for database: "+databasePath);
			// if (fg.isDirectory() && fg.getName().equals(databasePath)) {
			// FolderZipper.zipFolder(fg.getAbsolutePath(), backup + ".tmp", "backup-StarMade-", zipCallback, "", filter);
			// }
			// }
			} else {
				// zip everything except backups themselves
				FolderZipper.zipFolder(installDir, backup + ".tmp", "backup-StarMade-", zipCallback, filter);
			}
			notifyObservers();
			System.out.println("Copying Backup mFile to install dir...");
			File backUpFile = new FileExt(backup + ".tmp");
			if (backUpFile.exists()) {
				File file = new FileExt(new FileExt(installDir).getAbsolutePath() + File.separator + backup);
				System.err.println("Copy to: " + file.getAbsolutePath());
				DataUtil.copy(backUpFile, file);
				backUpFile.delete();
			} else {
				assert (false) : backUpFile.getAbsolutePath() + " doesnt exist";
			}
			if (removeOldVersion) {
				notifyObservers();
				System.out.println("Cleaning up current installation");
				// File oldCatalog = new FileExt(INSTALL_DIR+"/blueprints/Catalog.txt");
				// if(oldCatalog.exists()){
				// File backupCatalog = new FileExt(INSTALL_DIR+"/blueprints/Catalog-old-"+System.currentTimeMillis()+".txt");
				// oldCatalog.renameTo(backupCatalog);
				// }
				File[] list = dir.listFiles();
				for (int i = 0; i < list.length; i++) {
					File f = list[i];
					if (f.getName().equals("data") || f.getName().equals("native") || f.getName().startsWith("StarMade") || // f.getName().startsWith("server-database") ||
					// f.getName().startsWith("client-database") ||
					f.getName().equals("MANIFEST.MF") || f.getName().equals("version.txt")) {
						FileUtil.deleteDir(f);
						f.delete();
					}
				}
			}
			System.out.println("[BACKUP] DONE");
		}
	}

	public void startSelectedOnlineGame() {
		if (selectedOnlineUniverse != null) {
			AudioController.instance.stopMusic();
			String playerName = EngineSettings.ONLINE_PLAYER_NAME.getString().trim();
			boolean ok = true;
			boolean none = false;
			if (playerName.trim().length() <= 0) {
				ok = false;
				none = true;
			} else if (playerName.length() > 32) {
				ok = false;
			} else if (playerName.length() <= 2) {
				ok = false;
			} else if (playerName.matches("[_-]+")) {
				ok = false;
			} else {
				if (!playerName.matches("[a-zA-Z0-9_-]+")) {
					ok = false;
				}
			}
			if (!ok) {
				if (!none) {
					PlayerOkCancelInput r = new PlayerOkCancelInput("ERROR", getState(), 300, 150, Lng.str("PlayerName Invalid"), Lng.str("Your username is not allowed.\nMust be at least 3 characters.\nOnly letters, numbers, '-' and '_' are allowed.")) {

						@Override
						public void pressedOK() {
							deactivate();
						}

						@Override
						public void onDeactivate() {
						}
					};
					r.getInputPanel().setCancelButton(false);
					r.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(62);
					;
					return;
				} else {
					PlayerTextInput r = new PlayerTextInput("PLAYERNAME", getState(), 32, Lng.str("Choose Player Name"), Lng.str("Please enter a name for your character.")) {

						@Override
						public void onDeactivate() {
						}

						@Override
						public String[] getCommandPrefixes() {
							return null;
						}

						@Override
						public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
							return null;
						}

						@Override
						public void onFailedTextCheck(String msg) {
						}

						@Override
						public boolean onInput(String playerName) {
							boolean ok = true;
							if (playerName.trim().length() <= 0) {
								ok = false;
							} else if (playerName.length() > 32) {
								ok = false;
							} else if (playerName.length() <= 2) {
								ok = false;
							} else if (playerName.matches("[_-]+")) {
								ok = false;
							} else {
								if (!playerName.matches("[a-zA-Z0-9_-]+")) {
									ok = false;
								}
							}
							if (!ok) {
								PlayerOkCancelInput r = new PlayerOkCancelInput("ERROR", getState(), 300, 150, Lng.str("PlayerName Invalid"), Lng.str("Your username is not allowed.\nMust be at least 3 characters.\nOnly letters, numbers, '-' and '_' are allowed.")) {

									@Override
									public void pressedOK() {
										deactivate();
									}

									@Override
									public void onDeactivate() {
									}
								};
								r.getInputPanel().setCancelButton(false);
								r.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(60);
							} else {
								EngineSettings.ONLINE_PLAYER_NAME.setString(playerName);
								EngineSettings.ONLINE_PLAYER_NAME.notifyChanged();
								startSelectedOnlineGame();
								try {
									EngineSettings.write();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							return ok;
						}
					};
					r.getInputPanel().setCancelButton(false);
					r.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(61);
				}
			}
			System.err.println("[CLIENT] starting selected online universe: " + selectedOnlineUniverse);
			EngineSettings.LAST_GAME.setString("MP;" + selectedOnlineUniverse.getHost() + ";" + selectedOnlineUniverse.getPort() + ";" + EngineSettings.ONLINE_PLAYER_NAME.getString().trim());
			try {
				EngineSettings.write();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
	String serverUID = ServerModInfo.getServerUID(selectedOnlineUniverse.getHost(), selectedOnlineUniverse.getPort());
			ArrayList<ModIdentifier> serverMods = ServerModInfo.getServerInfo(serverUID);
			if(serverMods != null && serverMods.size() > 1){
				StringBuilder niceModsString = GUIOnlineUniversePanel.getNiceModsString(serverMods);
				SimplePopup security = new SimplePopup(getState(), "Security Info",
						"This server requires mods to join\n" +
								"Schine/Contributors are NOT responsible for any harm that may come from running mods.\n" +
								"Only join if you trust the authors of these mods:\n" +
								"\n\n"
								+ niceModsString){
					@Override
					public void pressedOK() {
						dispatchClientThread();
						deactivate();
					}
				};
			}else{
				dispatchClientThread();
			}
		}
	}
	//INSERTED CODE
	private void dispatchClientThread(){		Starter.serverInitFinished = true;
			setFrame(null, false);
			new Thread(() -> {
				try {
					Starter.initialize(false);
				} catch (Exception e) {
					e.printStackTrace();
					String error = Lng.str("Initializing Universe failed! Please send in an error report to help.star-made.org! %s", e.getClass().getSimpleName());
					Controller.setLoadMessage(error);
					graphicsContext.handleError(error);
				}
				final HostPortLoginName n = new HostPortLoginName(selectedOnlineUniverse.getHost(), selectedOnlineUniverse.getPort(), HostPortLoginName.STARMADE_CLIENT, EngineSettings.ONLINE_PLAYER_NAME.getString());
				Starter.startClient(n, false, graphicsContext);
}).start();
	}
	public boolean hasCurrentLocalSelected() {
		return selectedLocalUniverse != null;
	}

	public boolean hasCurrentOnlineSelected() {
		return selectedOnlineUniverse != null;
	}

	public void connectToCustomServer(String trim) {
		Starter.serverInitFinished = true;
		setFrame(null, false);
		String[] hostAndPort = trim.split(":");
		if (hostAndPort.length == 2) {
			final String host = hostAndPort[0];
			try {
				final int port = Integer.parseInt(hostAndPort[1]);
				EngineSettings.LAST_GAME.setString("MP;" + host + ";" + port + ";" + EngineSettings.ONLINE_PLAYER_NAME.getString().trim());
				EngineSettings.write();
				new Thread(() -> {
					try {
						Starter.initialize(false);
					} catch (IOException e1) {
						e1.printStackTrace();
						errorDialog(e1);
					}
					HostPortLoginName n = new HostPortLoginName(host, port, HostPortLoginName.STARMADE_CLIENT, EngineSettings.ONLINE_PLAYER_NAME.getString());
					Starter.startClient(n, false, graphicsContext);
				}).start();
			} catch (NumberFormatException e) {
				e.printStackTrace();
				errorDialog(Lng.str("Malformed host port (port must be a number). Please use a host:port format like 'play.star-made.org:4242'"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			errorDialog(Lng.str("Malformed host port. Please use a host:port format like 'play.star-made.org:4242'"));
		}
	}

	public void errorDialog(Exception e) {
		setFrame(frame, false);
		frame.handleError(e);
	}

	public void errorDialog(String str) {
		throw new RuntimeException(str);
	}

	@Override
	public void startGraphics() throws FileNotFoundException, ResourceException, ParseException, SAXException, IOException, ParserConfigurationException {
		System.err.println("[CLIENT][STARTUP] Initialize Open GL");
		graphicsContext.params.title = "StarMade " + VersionContainer.getVersionTitle();

		graphicsContext.init();
		System.err.println("[CLIENT][STARTUP] Initialize Open GL Done");
		graphicsContext.initializeLoadingScreen(new LoadingScreenDetailed());
		System.err.println("[CLIENT][STARTUP] Initialize LoadingScreen Done. Starting up");
		graphicsContext.startMainLoop();
	}

	public ServerInfo getCurrentOnlineSelected() {
		return selectedOnlineUniverse;
	}

	@Override
	public void handleError(String msg) {
		setFrame(frame, false);
		queuedException = new RuntimeException(msg);
	}

	@Override
	public void handleError(Exception e) {
		setFrame(frame, false);
		queuedException = e;
	}

	public boolean hasLastUsed() {
		return EngineSettings.LAST_GAME.getString().trim().length() > 0;
	}

	public void startLastUsed() {
		ServerInfo online = null;
		LocalUniverse local = null;
		try {
			String[] split = EngineSettings.LAST_GAME.getString().trim().split(";");
			boolean singlePlayer = split[0].equals("SP");
			String worldOrHost = split[1];
			int port = Integer.parseInt(split[2]);
			String playerName = split[3];
			if (!singlePlayer) {
				online = new ServerInfo(worldOrHost, port);
				EngineSettings.ONLINE_PLAYER_NAME.setString(playerName);
				selectedOnlineUniverse = online;
			} else {
				local = new LocalUniverse(worldOrHost, 0);
				EngineSettings.OFFLINE_PLAYER_NAME.setString(playerName);
				selectedLocalUniverse = local;
			}
		} catch (Exception e) {
			e.printStackTrace();
			EngineSettings.LAST_GAME.setString("");
			try {
				EngineSettings.write();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (online != null) {
			startSelectedOnlineGame();
		} else if (local != null) {
			startSelectedLocalGame();
		}
	}

	@Override
	public String getGUIPath() {
		return UIScale.getUIScale().getGuiPath() + "menu/";
	}

	public ServerInfo getSelectedOnlineUniverse() {
		return selectedOnlineUniverse;
	}

	public void setSelectedOnlineUniverse(ServerInfo selectedOnlineUniverse) {
		this.selectedOnlineUniverse = selectedOnlineUniverse;
	}

	public LocalUniverse getSelectedLocalUniverse() {
		return selectedLocalUniverse;
	}

	public void loadLanguage(String language) {
		frame.switchLanguage(language);
	}

	@Override
	public void setActiveSubtitles(List<Subtitle> activeSubtitles) {
	}

	@Override
	public void setInTextBox(boolean b) {
		this.inTextBox = b;
	}

	@Override
	public boolean isInTextBox() {
		return inTextBox;
	}

	@Override
	public void popupAlertTextMessage(String message) {
	}

	@Override
	public void handleCharEvent(KeyEventInterface e) {
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}
}
