package org.schema.game.client.view.mainmenu.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.view.mainmenu.LocalUniverse;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.game.client.view.mainmenu.MainMenuInputDialog;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GUILocalUniversePanel extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow universePanel;

	private GUIContentPane localUniversesTab;

	private MainMenuInputDialog diag;

	private List<GUIElement> toCleanUp = new ObjectArrayList<GUIElement>();
	private boolean createBackupOnDelete = false;
	private int universeCount;

	public GUILocalUniversePanel(InputState state, MainMenuInputDialog diag) {
		super(state);
		this.diag = diag;
	}

	@Override
	public void cleanUp() {
		for(GUIElement e : toCleanUp) e.cleanUp();
		toCleanUp.clear();
	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		transform();
		universePanel.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		universeCount = LocalUniverse.readUniverses().size();

		universePanel = new GUIMainWindow(getState(), GLFrame.getWidth() - 410, GLFrame.getHeight() - 20, 400, 10, "UniversePanelWindow");
		universePanel.onInit();
		universePanel.setPos(435, 35, 0);
		universePanel.setWidth(GLFrame.getWidth() - 470);
		universePanel.setHeight(GLFrame.getHeight() - 70);
		universePanel.clearTabs();
		localUniversesTab = createLocalTab();
		universePanel.activeInterface = this;
		universePanel.setCloseCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(772);
					diag.deactivate();
				}
			}
		});
	}

	@Override
	public boolean isInside() {
		return universePanel.isInside();
	}

	private GUIContentPane createLocalTab() {
		GUIContentPane t = universePanel.addTab(Lng.str("LOCAL"));
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(28));
		GUITabbedContent p = new GUITabbedContent(getState(), t.getContent(0));
		p.activationInterface = this;
		p.onInit();
		t.getContent(0).attach(p);
		GUIContentPane tabUniverses = p.addTab(Lng.str("UNIVERSES"));
		tabUniverses.setTextBoxHeightLast(UIScale.getUIScale().scale(128));
		GUISimpleSettingsList ll = new GUISimpleSettingsList(getState(), tabUniverses.getContent(0), this);
		ll.onInit();
		toCleanUp.add(ll);
		tabUniverses.getContent(0).attach(ll);
		tabUniverses.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUILocalUniverseList l = new GUILocalUniverseList(getState(), tabUniverses.getContent(1));
		l.selCallback = (GameStarterState) getState();
		toCleanUp.add(l);
		l.onInit();
		tabUniverses.getContent(1).attach(l);
		tabUniverses.setListDetailMode(tabUniverses.getTextboxes().get(1));
		tabUniverses.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 4, 1, tabUniverses.getContent(2));
		buttons.onInit();
		createLocalUniversesButtons(buttons);
		tabUniverses.getContent(2).attach(buttons);
		GUIContentPane tabSettings = p.addTab(Lng.str("ADV. SETTINGS"));
		tabSettings.setTextBoxHeightLast(UIScale.getUIScale().scale(28));
		GUITabbedContent settingsTabbedPane = new GUITabbedContent(getState(), tabSettings.getContent(0));
		settingsTabbedPane.activationInterface = this;
		settingsTabbedPane.onInit();
		settingsTabbedPane.setPos(0, UIScale.getUIScale().smallinset, 0);
		tabSettings.getContent(0).attach(settingsTabbedPane);
		addSettingsTab(Lng.str("GAME"), settingsTabbedPane, ServerConfig.ServerConfigCategory.GAME_SETTING);
		addSettingsTab(Lng.str("NPC"), settingsTabbedPane, ServerConfig.ServerConfigCategory.NPC_SETTING);
		addSettingsTab(Lng.str("PERFORMANCE"), settingsTabbedPane, ServerConfig.ServerConfigCategory.PERFORMANCE_SETTING);
		addSettingsTab(Lng.str("NETWORK"), settingsTabbedPane, ServerConfig.ServerConfigCategory.NETWORK_SETTING);
		addSettingsTab(Lng.str("DATABASE"), settingsTabbedPane, ServerConfig.ServerConfigCategory.DATABASE_SETTING);
		return t;
	}

	private void createLocalUniversesButtons(GUIHorizontalButtonTablePane buttons) {
		buttons.addButton(0, 0, Lng.str("PLAY"), HButtonType.BUTTON_PINK_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUILocalUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(773);
					((GameStarterState) getState()).startSelectedLocalGame();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUILocalUniversePanel.this.isActive() && ((GameMainMenuController) getState()).hasCurrentLocalSelected();
			}
		});
		// buttons.setButtonSpacing(0, 0, 2);
		buttons.addButton(1, 0, Lng.str("CREATE NEW"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUILocalUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					PlayerTextInput t = (new PlayerTextInput("NameDiag", getState(), 64, Lng.str("Create World"), Lng.str("Please enter a name for your Universe.")) {

						@Override
						public void onFailedTextCheck(String msg) {
						}

						@Override
						public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
							return null;
						}

						@Override
						public String[] getCommandPrefixes() {
							return null;
						}

						@Override
						public boolean onInput(String entry) {
							if(entry.trim().length() > 0) {
								GameMainMenuController.createWorld(entry.trim(), getState());
								universeCount = LocalUniverse.readUniverses().size();
								return true;
							}
							return false;
						}

						@Override
						public void onDeactivate() {
						}
					});
					t.setInputChecker((entry, callback) -> {
						File f = new FileExt(entry);
						try {
							// checks if this name is a valid file name
							f.getCanonicalPath();
							return true;
						} catch(IOException e) {
							callback.onFailedTextCheck("Name must be a legal file name");
							return false;
						}
					});
					t.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(774);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUILocalUniversePanel.this.isActive();
			}
		});
		// buttons.addButton(1, 1, Lng.str("EXPORT"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
		//
		// @Override
		// public boolean isOccluded() {
		// return !GUILocalUniversePanel.this.isActive();
		// }
		//
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse()){
		// PlayerOkCancelInput backUpExport = new PlayerOkCancelInput("BACKUP", getState(), 300, 150, Lng.str("Export?"), Lng.str("The export/backup will be created in your StarMade installation folder.")) {
		//
		// @Override
		// public void pressedOK() {
		// ((GameMainMenuController)getState()).backupAndDeleteSelected(true, false);
		// deactivate();
		// }
		//
		// @Override
		// public void onDeactivate() {
		// }
		// };
		// backUpExport.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
		// }
		//
		// }
		// }, new GUIActivationCallback(){
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		//
		// @Override
		// public boolean isActive(InputState state) {
		// return GUILocalUniversePanel.this.isActive() &&((GameMainMenuController)getState()).hasCurrentLocalSelected();
		// }
		//
		// });
		buttons.addButton(2, 0, Lng.str("IMPORT"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUILocalUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(!MainMenuGUI.runningSwingDialog) {
						MainMenuGUI.runningSwingDialog = true;
						SwingUtilities.invokeLater(() -> {
							JFileChooser fc = new JFileChooser(new FileExt("./"));
							fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
							fc.setAcceptAllFileFilterUsed(false);
							fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

								@Override
								public boolean accept(File arg0) {
									if(arg0.isDirectory()) {
										return true;
									}
									if(arg0.getName().endsWith(".smdb")) {
										return true;
									}
									return false;
								}

								@Override
								public String getDescription() {
									return "StarMade Database (.smdb)";
								}
							});
							fc.setAcceptAllFileFilterUsed(false);
							JDialog d = new JDialog();
							d.setAlwaysOnTop(true);
							d.setVisible(true);
							// Show it.
							int returnVal = fc.showDialog(d, Lng.str("Choose database to import"));
							// Process the results.
							if(returnVal == JFileChooser.APPROVE_OPTION) {
								final File file = fc.getSelectedFile();
								PlayerTextInput t = (new PlayerTextInput("NameDiag", getState(), 64, Lng.str("Create World"), Lng.str("Please enter a name for your Universe.")) {

									@Override
									public void onFailedTextCheck(String msg) {
									}

									@Override
									public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
										return null;
									}

									@Override
									public String[] getCommandPrefixes() {
										return null;
									}

									@Override
									public boolean onInput(String entry) {
										if(entry.trim().length() > 0) {
											File f = new FileExt(GameServerState.SERVER_DATABASE + entry.trim());
											if(!f.exists()) {
												((GameMainMenuController) getState()).importDB(file, entry.trim());
												universeCount = LocalUniverse.readUniverses().size();
											} else {
												onFailedTextCheck(Lng.str("Already Exists!"));
											}
											return true;
										}
										return false;
									}

									@Override
									public void onDeactivate() {
									}
								});
								t.setInputChecker((entry, callback) -> {
									File f = new FileExt(entry);
									try {
										// checks if this name is a valid file name
										f.getCanonicalPath();
										return true;
									} catch(IOException e) {
										callback.onFailedTextCheck(Lng.str("Name must be a legal file name"));
										return false;
									}
								});
								t.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(775);
							}
							d.dispose();
							MainMenuGUI.runningSwingDialog = false;
						});
					}
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUILocalUniversePanel.this.isActive();
			}
		});
		buttons.addButton(3, 0, Lng.str("DELETE"), HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUILocalUniversePanel.this.isActive() || ((GameMainMenuController) getState()).getSelectedLocalUniverse() == null || universeCount <= 0;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse() && universeCount > 1) { //Stupid GUI quirks mean I have to check the universe count multiple times >:U
					PlayerOkCancelInput delCheck = new PlayerOkCancelInput("CONFIRM_DEL", getState(), 300, 150, Lng.str("DELETE?"), Lng.str("Do you really want to delete this universe?")) {

						@Override
						public void pressedOK() {
							((GameMainMenuController) getState()).deleteSelectedWorld(createBackupOnDelete);
							deactivate();
						}

						@Override
						public void onDeactivate() {
							universeCount = LocalUniverse.readUniverses().size();
						}
					};

					delCheck.getInputPanel().onInit();

					GUICheckBoxTextPair backup = new GUICheckBoxTextPair(getState(), Lng.str("Create Backup"), 100, 24) {

						@Override
						public boolean isActivated() {
							return createBackupOnDelete;
						}

						@Override
						public void deactivate() {
							createBackupOnDelete = false;
						}

						@Override
						public void activate() {
							createBackupOnDelete = true;
						}
					};
					((GUIDialogWindow) delCheck.getInputPanel().background).getMainContentPane().getContent(0).attach(backup);
					backup.setPos(10, 50, 0);
					delCheck.activate();
					//AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE); This throws an exception

				}

			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUILocalUniversePanel.this.isActive() && ((GameMainMenuController) getState()).getSelectedLocalUniverse() != null && universeCount > 1;
			}
		});
	}

	private void addSettingsTab(String name, GUITabbedContent settingsTabbedPane, ServerConfig.ServerConfigCategory cat) {
		GUIContentPane game = settingsTabbedPane.addTab(name);
		GUIServerSettingsList list = new GUIServerSettingsList(getState(), game.getContent(0), cat, this);
		list.onInit();
		toCleanUp.add(list);
		game.getContent(0).attach(list);
		addOkCancel(game);
	}

	private void addOkCancel(GUIContentPane t) {
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(10));
		t.setListDetailMode(t.getTextboxes().get(t.getTextboxes().size() - 1));
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		int index = t.getTextboxes().size() - 1;
		GUIHorizontalButtonTablePane pane = new GUIHorizontalButtonTablePane(getState(), 2, 1, t.getContent(index));
		pane.onInit();
		pane.addButton(0, 0, Lng.str("SAVE"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUILocalUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					pressedApply();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(776);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUILocalUniversePanel.this.isActive();
			}
		});
		pane.addButton(1, 0, Lng.str("DISCARD CHANGES"), HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUILocalUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(777);
					pressedCancel();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUILocalUniversePanel.this.isActive();
			}
		});
		t.getContent(index).attach(pane);
	}

	private void pressedCancel() {
		ServerConfig.read();
		for(GUIElement a : toCleanUp) {
			if(a instanceof GUIServerSettingsList) {
				((GUIServerSettingsList) a).clear();
			}
		}
	}

	private void pressedApply() {
		try {
			ServerConfig.write();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public float getHeight() {
		return 0;
	}

	@Override
	public float getWidth() {
		return 0;
	}

	@Override
	public boolean isActive() {
		List<DialogInterface> playerInputs = getState().getController().getInputController().getPlayerInputs();
		return !MainMenuGUI.runningSwingDialog && (playerInputs.isEmpty() || playerInputs.get(playerInputs.size() - 1).getInputPanel() == this);
	}
}
