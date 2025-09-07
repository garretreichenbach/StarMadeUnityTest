package org.schema.game.client.view.mainmenu.gui;

import api.mod.*;
import api.utils.gui.SimplePopup;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.game.client.view.mainmenu.MainMenuInputDialog;
import org.schema.game.common.api.SessionNewStyle;
import org.schema.game.common.util.StarMadeCredentials;
import org.schema.schine.auth.exceptions.WrongUserNameOrPasswordException;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsChangeListener;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.input.InputState;
import org.schema.schine.network.ServerInfo;
import org.schema.schine.network.server.ServerEntry;
import org.schema.schine.sound.controller.AudioController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GUIOnlineUniversePanel extends GUIElement implements GUIActiveInterface, EngineSettingsChangeListener {

	public GUIMainWindow universePanel;

	private GUIContentPane onlineUniversesTab;

	private MainMenuInputDialog diag;

	private List<GUIElement> toCleanUp = new ObjectArrayList<GUIElement>();

	private OnlineServerFilter filter = new OnlineServerFilter();

	private long lastRefreshClick;

	private GUIOnlineUniverseList onlineServerList;

	private String uplinkName = "";

	private StarMadeCredentials creds;

	private GUIActivatableTextBar playerName;

	private static int W_EXANDABLE_DETAILS_HEIGHT = 100;

	private static int W_FILTER_TABS_HEIGHT = 50;

	private static int W_INGAME_NAME_HEIGHT = 50;

	public GUIOnlineUniversePanel(InputState state, MainMenuInputDialog diag) {
		super(state);
		this.diag = diag;
	}

	@Override
	public void cleanUp() {
		for (GUIElement e : toCleanUp) {
			e.cleanUp();
		}
		toCleanUp.clear();
		EngineSettings.ONLINE_PLAYER_NAME.removeChangeListener(this);
	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		transform();
		universePanel.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public boolean isInside() {
		return universePanel.isInside();
	}

	@Override
	public void onInit() {
		universePanel = new GUIMainWindow(getState(), GLFrame.getWidth() - 410, GLFrame.getHeight() - 20, 400, 10, "UniversePanelWindow");
		universePanel.onInit();
		universePanel.setPos(435, 35, 0);
		universePanel.setWidth(GLFrame.getWidth() - 470);
		universePanel.setHeight(GLFrame.getHeight() - 70);
		universePanel.clearTabs();
		onlineUniversesTab = createOnlineTab();
		universePanel.activeInterface = this;
		universePanel.setCloseCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(781);
					diag.deactivate();
				}
			}
		});
	}

	private GUIContentPane createOnlineTab() {
		GUIContentPane contentPane = universePanel.addTab(Lng.str("ONLINE"));
		contentPane.setTextBoxHeightLast(UIScale.getUIScale().scale(27));
		createOnlinePlayerNameSettings(contentPane.getContent(0));
		createOnlineUplinkSettings(contentPane.getContent(0));
		GUITextOverlay ingamePlayerName = new GUITextOverlay(FontSize.BIG_20, getState()) {

			@Override
			public void draw() {
				if (playerName.getText().trim().isEmpty()) {
					setColor(1, 0.3f, 0.3f, 1);
				} else {
					setColor(1, 1, 1, 1);
				}
				super.draw();
			}
		};
		ingamePlayerName.setTextSimple(Lng.str("Ingame Player Name: "));
		ingamePlayerName.setPos(4, 0, 0);
		contentPane.getContent(0).attach(ingamePlayerName);
		contentPane.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		// This includes the refresh button
		createFilterTabs(contentPane.getContent(1));
		contentPane.addNewTextBox(UIScale.getUIScale().scale(10));
		// Tried to break out the heights as much as possible, but ultimately had an offset in there as well.
		onlineServerList = new GUIOnlineUniverseList(getState(), getWidth(), getHeight() - UIScale.getUIScale().scale(W_EXANDABLE_DETAILS_HEIGHT) - UIScale.getUIScale().scale(W_FILTER_TABS_HEIGHT) - UIScale.getUIScale().scale(W_INGAME_NAME_HEIGHT), filter, contentPane.getContent(2));
		onlineServerList.selCallback = (GameStarterState) getState();
		onlineServerList.onInit();
		contentPane.getContent(2).attach(onlineServerList);
		contentPane.setListDetailMode(contentPane.getTextboxes().get(2));
		contentPane.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		detailsButton(contentPane.getTextboxes().get(3), contentPane.getContent(3));
		contentPane.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		createOnlineCustomSettings(contentPane.getContent(4));
		GUITextOverlay c = new GUITextOverlay(getState());
		c.setTextSimple(Lng.str("Manual Server Entry: "));
		c.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		contentPane.getContent(4).attach(c);
		return contentPane;
	}
	public static StringBuilder getNiceModsString(ArrayList<ModIdentifier> serverInfo){
		StringBuilder mods = new StringBuilder();
		if(serverInfo == null){
			mods.append("This server does not require any mods to join");
		}else {
			for (ModIdentifier i : serverInfo) {
				SMDModInfo modData = SMDModData.getInstance().getModData(i.id);
				if (modData != null) {
					mods.append(modData.getName()).append(" v").append(i.version)
							.append(" [By: ").append(modData.getUsername()).append("]").append(": ").append(modData.getTagLine()).append("\n");
				} else if(i.id == -1){
					mods.append("StarLoader v").append(StarLoader.getVersionString()).append("\n");
				}else{
					mods.append("Unknown Mod: ").append(i).append("\n");
					System.err.println("Could not find mod: " + i);
				}
			}
		}
		return mods;
	}

	private void detailsButton(GUIInnerTextbox textBox, final GUIAnchor dep) {
		GUIScrollablePanel expanded = new GUIScrollablePanel(10.0F, UIScale.getUIScale().scale(W_EXANDABLE_DETAILS_HEIGHT), this.getState());GUITextOverlay serverDescText;
		(serverDescText = new GUITextOverlay(FontSize.SMALL_14, this.getState())).setTextSimple(new Object() {


			public String toString() {
				return ((GameMainMenuController) GUIOnlineUniversePanel.this.getState()).hasCurrentOnlineSelected() ? ((GameMainMenuController) GUIOnlineUniversePanel.this.getState()).getCurrentOnlineSelected().getDesc() : "";
			}
		});
		serverDescText.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		serverDescText.autoWrapOn = textBox;
		expanded.setContent(serverDescText);
		GUIExpandableButton detailsButton;
		(detailsButton = new GUIExpandableButton(this.getState(), textBox, Lng.str("Show Server Details"), Lng.str("Hide Server Details"), new GUIActivationCallback() {

			public boolean isVisible(InputState var1) {
				return true;
			}

			public boolean isActive(InputState var1) {
				return GUIOnlineUniversePanel.this.isActive();
			}
		}, expanded, true) {

			@Override
			public boolean isOccluded() {
				return !GUIOnlineUniversePanel.this.isActive() || super.isOccluded();
			}

			public void draw() {
				this.buttonWidthAdd = -((int)(dep.getWidth() / 3.0F * 2F) + 5);
				super.draw();
			}
		}).onInit();
		GUIHorizontalButton connectButton;
		(connectButton = new GUIHorizontalButton(this.getState(), HButtonType.BUTTON_PINK_MEDIUM, Lng.str("PLAY"), new GUICallback() {
			public boolean isOccluded() {
				return !GUIOnlineUniversePanel.this.isActive();
			}

			public void callback(GUIElement var1, MouseEvent var2) {
				if (var2.pressedLeftMouse()) {
					((GameMainMenuController)GUIOnlineUniversePanel.this.getState()).startSelectedOnlineGame();
				}

			}
		}, this, new GUIActivationCallback() {
			public boolean isVisible(InputState var1) {
				return true;
			}

			public boolean isActive(InputState var1) {
				return GUIOnlineUniversePanel.this.isActive() && ((GameMainMenuController)GUIOnlineUniversePanel.this.getState()).hasCurrentOnlineSelected();
			}
		}) {
			public void draw() {
				this.setPos((float)((int) dep.getWidth() / 3 *2 ), 0.0F, 0.0F);this.setWidth((int)dep.getWidth() / 3);
				super.draw();
			}
		}).onInit();
		//INSERTED CODE
		GUIHorizontalButton modsButton = new GUIHorizontalButton(getState(), GUIHorizontalArea.HButtonColor.GREEN, "View Mods", new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIOnlineUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(782);
					ServerInfo selected =((GameMainMenuController) GUIOnlineUniversePanel.this.getState()).getSelectedOnlineUniverse();
					String uid = ServerModInfo.getServerUID(selected.getHost(), selected.getPort());
					System.err.println("SERVER UID: " + uid);
					ArrayList<ModIdentifier> serverInfo = ServerModInfo.getServerInfo(uid);
					StringBuilder mods = getNiceModsString(serverInfo);
					SimplePopup popup = new SimplePopup(getState(), "Mod List", mods.toString());
				}
			}
		}, this, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIOnlineUniversePanel.this.isActive() && ((GameMainMenuController) getState()).hasCurrentOnlineSelected();
			}
		}) {

			@Override
			public void draw() {
				setPos((int) dep.getWidth() / 3F, 0, 0);
				setWidth((int) dep.getWidth() / 3);
				super.draw();
			}
		};
		dep.attach(modsButton);
		///
		dep.attach(connectButton);
		dep.attach(detailsButton);
	}

	private void createOnlineCustomSettings(final GUIAnchor dep) {
		final GUIActivatableTextBar hostPortBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, Lng.str("host:port"), dep, new TextCallback() {

			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
				String hostAndPort = entry.trim();
				((GameMainMenuController) getState()).connectToCustomServer(hostAndPort);
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void newLine() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}
		}, t -> {
			EngineSettings.SERVERLIST_LAST_SERVER_USED.setString(t.trim());
			return t;
		}) {

			@Override
			protected void onBecomingInactive() {
				try {
					EngineSettings.write();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		hostPortBar.setText(EngineSettings.SERVERLIST_LAST_SERVER_USED.getString().trim());
		hostPortBar.setDeleteOnEnter(false);
		hostPortBar.rightDependentHalf = true;
		hostPortBar.dependendDistanceFromRight = 170;
		GUIHorizontalButton connectButton = new GUIHorizontalButton(getState(), HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("CONNECT"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIOnlineUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(783);
					String hostAndPort = hostPortBar.getText().trim();
					((GameMainMenuController) getState()).connectToCustomServer(hostAndPort);
				}
			}
		}, this, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIOnlineUniversePanel.this.isActive() && hostPortBar.getText().trim().length() > 0;
			}
		}) {

			@Override
			public void draw() {
				setPos(dep.getWidth() - hostPortBar.dependendDistanceFromRight, 0, 0);
				setWidth(hostPortBar.dependendDistanceFromRight - 80);
				super.draw();
			}
		};
		GUIHorizontalButton saveButton = new GUIHorizontalButton(getState(), HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("SAVE"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIOnlineUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					String trim = hostPortBar.getText().trim();
					final String[] hostAndPort = trim.split(":");
					if (hostAndPort.length == 2) {
						final String host = hostAndPort[0];
						try {
							final int port = Integer.parseInt(hostAndPort[1]);
							PlayerOkCancelInput p = new PlayerOkCancelInput("HOSTNAME", getState(), Lng.str("Custom Server Name"), Lng.str("Save entry: %s:%s", host, port)) {

								@Override
								public void onDeactivate() {
								}

								@Override
								public void pressedOK() {
									List<ServerEntry> read = new ObjectArrayList<ServerEntry>();
									try {
										read.addAll(ServerEntry.read("customservers.smsl"));
									} catch (IOException e) {
										e.printStackTrace();
									}
									ServerEntry newEn = new ServerEntry(host, port);
									if (!read.contains(newEn)) {
										read.add(newEn);
										try {
											ServerEntry.write(read, "customservers.smsl");
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									refreshServers();
									deactivate();
								}
							};
							p.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(784);
						} catch (Exception e) {
							e.printStackTrace();
							throw new NumberFormatException("bad host format. use host:port (e.g. play.star-made.org:4242)");
						}
					} else {
						throw new NumberFormatException("bad host format. use host:port (e.g. play.star-made.org:4242)");
					}
				}
			}
		}, this, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIOnlineUniversePanel.this.isActive() && hostPortBar.getText().trim().length() > 0;
			}
		}) {

			@Override
			public void draw() {
				setPos(dep.getWidth() - 80, 0, 0);
				setWidth(80);
				super.draw();
			}
		};
		connectButton.onInit();
		dep.attach(hostPortBar);
		dep.attach(connectButton);
		dep.attach(saveButton);
	}

	public void refreshServers() {
		onlineServerList.clear();
		((GameMainMenuController) getState()).getServerListRetriever().startRetrieving();
		lastRefreshClick = System.currentTimeMillis();
	}

	private GUIElement createFilterTabs(final GUIElement dep) {
		GUICheckBoxTextPair compatible = new GUICheckBoxTextPair(getState(), Lng.str("Compatible"), 80, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				return filter.isCompatible();
			}

			@Override
			public void deactivate() {
				filter.setCompatible(false);
			}

			@Override
			public void activate() {
				filter.setCompatible(true);
			}
		};
		GUICheckBoxTextPair responsive = new GUICheckBoxTextPair(getState(), Lng.str("Only Responsive"), 100, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				return filter.isResponsive();
			}

			@Override
			public void deactivate() {
				filter.setResponsive(false);
			}

			@Override
			public void activate() {
				filter.setResponsive(true);
			}
		};
		GUICheckBoxTextPair favorites = new GUICheckBoxTextPair(getState(), Lng.str("Only Favorites"), 90, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				return filter.isFavorites();
			}

			@Override
			public void deactivate() {
				filter.setFavorites(false);
			}

			@Override
			public void activate() {
				filter.setFavorites(true);
			}
		};
		GUICheckBoxTextPair customs = new GUICheckBoxTextPair(getState(), Lng.str("Only Custom"), 90, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				return filter.isCustoms();
			}

			@Override
			public void deactivate() {
				filter.setCustoms(false);
			}

			@Override
			public void activate() {
				filter.setCustoms(true);
			}
		};
		List<GUICheckBoxTextPair> elements = new ObjectArrayList<GUICheckBoxTextPair>();
		elements.add(compatible);
		elements.add(responsive);
		elements.add(favorites);
		elements.add(customs);
		int curPos = 0;
		for (int i = 0; i < elements.size(); i++) {
			GUICheckBoxTextPair guiElement = elements.get(i);
			guiElement.textPosY = -1;
			guiElement.onInit();
			if (i == 0) {
				guiElement.setPos(5, 3, 0);
			} else {
				guiElement.setPos((int) (elements.get(i - 1).getPos().x + elements.get(i - 1).getWidth() + 50), 3, 0);
			}
			dep.attach(guiElement);
			if (i == elements.size() - 1) {
				curPos = (int) (elements.get(i).getPos().x + elements.get(i).getWidth() + 50);
			}
		}
		final int cPosX = curPos;
		GUIHorizontalButton button = new GUIHorizontalButton(getState(), HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("REFRESH"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIOnlineUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(785);
					refreshServers();
				}
			}
		}, this, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIOnlineUniversePanel.this.isActive() && (System.currentTimeMillis() - lastRefreshClick > 1000);
			}
		}) {

			@Override
			public void draw() {
				setPos(cPosX, 0, 0);
				setWidth((int) (dep.getWidth() - cPosX));
				super.draw();
			}
		};
		button.onInit();
		dep.attach(button);
		return dep;
	}

	private void createOnlineUplinkSettings(final GUIAnchor dep) {
		GUIHorizontalButtonTablePane pane = new GUIHorizontalButtonTablePane(getState(), 3, 1, dep);
		pane.onInit();
		try {
			creds = StarMadeCredentials.read();
			uplinkName = creds.getUser();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pane.addButton(0, 0, "PLACEHOLDER", HButtonType.BUTTON_BLUE_MEDIUM, null, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return false;
			}

			@Override
			public boolean isActive(InputState state) {
				return false;
			}
		});
		pane.addButton(1, 0, "PLACEHOLDER", HButtonType.BUTTON_BLUE_MEDIUM, null, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return false;
			}

			@Override
			public boolean isActive(InputState state) {
				return false;
			}
		});
		// pane.addButton(1, 0, "PLACEHOLDER", HButtonType.BUTTON_BLUE_MEDIUM, null, new GUIActivationCallback() {
		// @Override
		// public boolean isVisible(InputState state) { return false; }
		// @Override
		// public boolean isActive(InputState state) { return false; }
		// });
		pane.addButton(2, 0, new Object() {

			@Override
			public String toString() {
				return uplinkName.length() == 0 ? Lng.str("UPLINK TO STARMADE REGISTRY") : Lng.str("CHANGE UPLINK (CURRENT: %s )", uplinkName);
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIOnlineUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					(new PlayerTextInput("UPLINK", getState(), 200, Lng.str("Username"), Lng.str("Enter your StarMade Registry Username.\nIf you don't have one, please register at www.star-made.org\nas some servers only allow registered Users."), creds != null ? creds.getUser() : "") {

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
						public boolean onInput(final String user) {
							PlayerTextInput pw = new PlayerTextInput("UPLINK", getState(), 200, Lng.str("Password"), Lng.str("Please enter your password.")) {

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
								public void onDeactivate() {
								}

								@Override
								public boolean onInput(String pass) {
									creds = new StarMadeCredentials(user, pass);
									SessionNewStyle session = new SessionNewStyle("starMadeOrg");
									try {
										session.login(user, pass);
										uplinkName = user;
										PlayerOkCancelInput p = new PlayerOkCancelInput("CONFIRM", getState(), 300, 100, Lng.str("SAVE CREDENTIALS?"), Lng.str("Save (encrypted) credentials to disk?")) {

											@Override
											public void pressedOK() {
												try {
													creds.write();
												} catch (Exception e) {
													e.printStackTrace();
												}
												deactivate();
											}

											@Override
											public void onDeactivate() {
											}
										};
										p.activate();
										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
										AudioController.fireAudioEventID(788);
									} catch (WrongUserNameOrPasswordException e) {
										e.printStackTrace();
										PlayerOkCancelInput p = new PlayerOkCancelInput("ERR_CONNECT", getState(), 400, 230, Lng.str("ERROR"), Lng.str("Username or password wrong.\n\nIf you forgot your username or password, please visit registry.star-made.org!")) {

											@Override
											public void pressedOK() {
												deactivate();
											}

											@Override
											public void onDeactivate() {
											}
										};
										p.getInputPanel().setCancelButton(false);
										p.activate();
										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
										AudioController.fireAudioEventID(786);
									} catch (Exception e) {
										e.printStackTrace();
										PlayerOkCancelInput p = new PlayerOkCancelInput("ERR_CONNECT", getState(), 400, 230, Lng.str("ERROR"), Lng.str("Could not establish connection to StarMade registry.\nPlease check your internet connection, and try again in a few moments.\nFor more help or info about maintenance, please visit www.star-made.org!")) {

											@Override
											public void pressedOK() {
												deactivate();
											}

											@Override
											public void onDeactivate() {
											}
										};
										p.getInputPanel().setCancelButton(false);
										p.activate();
										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
										AudioController.fireAudioEventID(787);
									}
									return true;
								}
							};
							pw.setPassworldField((true));
							pw.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(789);
							return true;
						}

						@Override
						public void onDeactivate() {
						}
					}).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(790);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIOnlineUniversePanel.this.isActive();
			}
		});
		dep.attach(pane);
		pane.totalButtonWidthOffset = -30;
		GUIHorizontalButton remove = new GUIHorizontalButton(getState(), HButtonType.BUTTON_RED_MEDIUM, Lng.str("X"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIOnlineUniversePanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					try {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
						AudioController.fireAudioEventID(791);
						StarMadeCredentials.removeFile();
						uplinkName = "";
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, this, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return uplinkName.length() > 0;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIOnlineUniversePanel.this.isActive();
			}
		}) {

			@Override
			public void draw() {
				setWidth(29);
				setPos(dep.getWidth() - 30, 0, 0);
				super.draw();
			}
		};
		dep.attach(remove);
	}

	private void createOnlinePlayerNameSettings(final GUIAnchor dep) {
		EngineSettings.ONLINE_PLAYER_NAME.addChangeListener(this);
		playerName = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, Lng.str("Online Player Name (Required)"), dep, new TextCallback() {

			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void newLine() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}
		}, t -> {
			EngineSettings.ONLINE_PLAYER_NAME.setString(t.trim());
			return t;
		}) {

			@Override
			protected void onBecomingInactive() {
				try {
					EngineSettings.write();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void draw() {
				rightDependentHalf = true;
				dependendDistanceFromRight = 140;
				offsetX = -(int) (dep.getWidth() / 3 + 20) + dependendDistanceFromRight;
				super.draw();
			}
		};
		playerName.setDeleteOnEnter(false);
		playerName.setText(EngineSettings.ONLINE_PLAYER_NAME.getString().trim());
		dep.attach(playerName);
	}

	@Override
	public float getHeight() {
		return this.universePanel.getHeight();
	}

	@Override
	public float getWidth() {
		return this.universePanel.getWidth();
	}

	@Override
	public boolean isActive() {
		List<DialogInterface> playerInputs = getState().getController().getInputController().getPlayerInputs();
		return !MainMenuGUI.runningSwingDialog && (playerInputs.isEmpty() || playerInputs.get(playerInputs.size() - 1).getInputPanel() == this);
	}

	@Override
	public void onSettingChanged(SettingsInterface setting) {
		if(setting == EngineSettings.ONLINE_PLAYER_NAME) {
			playerName.setTextWithoutCallback(EngineSettings.ONLINE_PLAYER_NAME.getString().trim());
		}
	}
}
