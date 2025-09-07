package org.schema.game.client.view.gui;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.SendableAddedRemovedListener;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.sound.controller.AudioController;

public class PlayerStatisticsPanel extends GUIElement implements SendableAddedRemovedListener {

	private int nameSpace = 170;

	private int optionExtraSpace = 90;

	private GUIOverlay background;

	private GUIScrollablePanel scrollPanel;

	private GUIElementList panelList;

	private boolean firstDraw = true;

	private boolean reconstructionRequested;

	private ConcurrentHashMap<PlayerState, PlayerListElement> playerMap = new ConcurrentHashMap<PlayerState, PlayerListElement>();

	public PlayerStatisticsPanel(InputState state) {
		super(state);
		initialize();
		((GameClientState) state).getController().addSendableAddedRemovedListener(this);
	}

	// public ShipElementControllerManager getShipControlManager(){
	// return getInShipControlManager().getShipControlManager().getShipControllerManagerManager();
	// }
	public InShipControlManager getInShipControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
	}

	private void initialize() {
		background = new GUIOverlay(Controller.getResLoader().getSprite("panel-std-gui-"), getState());
		scrollPanel = new GUIScrollablePanel(512, 366, getState());
		// add sub-panels
		panelList = new GUIElementList(getState());
		scrollPanel.setContent(panelList);
		this.attach(background);
		background.attach(scrollPanel);
		scrollPanel.setPos(260, 64, 0);
	// panelList.setCallback(getShipControlManager());
	}

	private void reconstructList() {
		StateInterface state = (StateInterface) getState();
		synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (int i = 0; i < panelList.size(); i++) {
				PlayerState player = (PlayerState) panelList.get(i).getUserPointer();
				if (player != null) {
					if (!state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(player.getId())) {
						panelList.remove(i);
						playerMap.remove(player);
						i--;
					}
				}
			}
			for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (s instanceof PlayerState) {
					if (!playerMap.containsKey(s)) {
						panelList.add(new PlayerListElement((PlayerState) s, getState()));
					}
				}
			}
		}
		panelList.updateDim();
	}

	@Override
	public void cleanUp() {
	}

	private class PlayerListElement extends GUIListElement {

		private PlayerState player;

		public PlayerListElement(PlayerState player, InputState state) {
			super(state);
			PlayerTableElement playerTableElement = new PlayerTableElement(state, player);
			setContent(playerTableElement);
			setSelectContent(playerTableElement);
			GameClientState gs = (GameClientState) state;
			this.player = player;
			update(null);
			this.setUserPointer(player);
			playerMap.put(player, this);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return player.getId();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return player.equals(((PlayerListElement) obj).player);
		}

		@Override
		public void update(Timer timer) {
		// ((PlayerTableElement)getContent()).update(getPlayerName(), String.valueOf(player.getPing()), String.valueOf(player.getFactionId()));
		// ((PlayerTableElement)getSelectContent()).update(player.getName(), String.valueOf(player.getKills()), String.valueOf(player.getDeaths())+" / "+ String.valueOf(player.getPing()),String.valueOf( player.getFactionId()));
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#draw()
	 */
	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().hinderInteraction(30);
		GlUtil.glPushMatrix();
		transform();
		background.draw();
		GlUtil.glPopMatrix();
	}

	private class TopTableElement extends GUIElement {

		GUITextOverlay aText;

		GUITextOverlay bText;

		GUITextOverlay dText;

		GUITextOverlay eText;

		int space = 60;

		public TopTableElement(InputState state) {
			super(state);
			aText = new GUITextOverlay(FontSize.TINY_12, state);
			aText.setText(new ArrayList());
			aText.getText().add("");
			bText = new GUITextOverlay(FontSize.TINY_12, state);
			bText.setText(new ArrayList());
			bText.getText().add("");
			bText.getPos().x += space + nameSpace;
			dText = new GUITextOverlay(FontSize.TINY_12, state);
			dText.setText(new ArrayList());
			dText.getText().add("");
			dText.getPos().x += 2 * space + nameSpace;
			eText = new GUITextOverlay(FontSize.TINY_12, state);
			eText.setText(new ArrayList());
			eText.getText().add("");
			eText.getPos().x += 3 * space + nameSpace + optionExtraSpace;
		}

		@Override
		public void cleanUp() {
		}

		@Override
		public float getHeight() {
			return aText.getHeight();
		}

		@Override
		public void draw() {
			transform();
			aText.draw();
			bText.draw();
			dText.draw();
			eText.draw();
		}

		public void update(String a, String b, String d, String e) {
			this.aText.getText().set(0, a);
			this.bText.getText().set(0, b);
			this.dText.getText().set(0, d);
			this.eText.getText().set(0, e);
		}

		@Override
		public float getWidth() {
			return aText.getWidth() * 4 + 4 * space;
		}

		@Override
		public boolean isPositionCenter() {
			return false;
		}

		@Override
		public void onInit() {
			aText.onInit();
			bText.onInit();
			dText.onInit();
			eText.onInit();
		}
	}

	@Override
	public float getHeight() {
		return background.getHeight();
	}

	private class PlayerTableElement extends GUIElement {

		GUITextOverlay aText;

		GUITextOverlay bText;

		GUITextOverlay dText;

		GUITextButton eText;

		int space = 60;

		public PlayerTableElement(final InputState state, final PlayerState player) {
			super(state);
			aText = new GUITextOverlay(FontSize.TINY_12, state);
			aText.setText(new ArrayList());
			aText.getText().add(new Object() {

				/* (non-Javadoc)
				 * @see java.lang.Object#toString()
				 */
				@Override
				public String toString() {
					return player.getName() + (player.isUpgradedAccount() ? "*" : "");
				}
			});
			bText = new GUITextOverlay(FontSize.TINY_12, state);
			bText.setText(new ArrayList());
			bText.getText().add(new Object() {

				/* (non-Javadoc)
				 * @see java.lang.Object#toString()
				 */
				@Override
				public String toString() {
					return String.valueOf(player.getPing());
				}
			});
			bText.getPos().x += space + nameSpace;
			dText = new GUITextOverlay(FontSize.TINY_12, state);
			dText.setText(new ArrayList());
			dText.getText().add(new Object() {

				/* (non-Javadoc)
				 * @see java.lang.Object#toString()
				 */
				@Override
				public String toString() {
					return player.getFactionController().getFactionName();
				}
			});
			dText.getPos().x += 2 * space + nameSpace;
			eText = new GUITextButton(state, 50, 20, FontSize.TINY_12, new Object() {

				@Override
				public String toString() {
					if (((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
						return "options";
					} else {
						return "-";
					}
				}
			}, new GUICallback() {

				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse() && ((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
						final PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("PlayerStatisticsPanel_PLAYER_ADMIN_OPTIONS", (GameClientState) state, "Player Admin Options: " + player.getName(), "") {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
							}
						};
						c.getInputPanel().setOkButton(false);
						c.getInputPanel().onInit();
						GUITextButton kick = new GUITextButton(state, 140, 20, FontSize.TINY_12, new Object() {

							@Override
							public String toString() {
								if (((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
									return "Kick";
								} else {
									return "-";
								}
							}
						}, new GUICallback() {

							@Override
							public void callback(GUIElement callingGuiElement, MouseEvent event) {
								if (event.pressedLeftMouse() && ((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
									c.deactivate();
									PlayerGameTextInput p = new PlayerGameTextInput("PlayerStatisticsPanel_KICK", ((GameClientState) state), 100, "Kick", "Enter Reason") {

										@Override
										public String[] getCommandPrefixes() {
											return null;
										}

										@Override
										public boolean isOccluded() {
											return false;
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
											c.activate();
											/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
											AudioController.fireAudioEventID(611);
										}

										@Override
										public boolean onInput(String entry) {
											((GameClientState) state).getController().sendAdminCommand(AdminCommands.KICK_REASON, player.getName(), entry);
											return true;
										}
									};
									p.activate();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
									AudioController.fireAudioEventID(612);
								}
							}

							@Override
							public boolean isOccluded() {
								return false;
							}
						});
						GUITextButton banAccount = new GUITextButton(state, 140, 20, FontSize.TINY_12, new Object() {

							@Override
							public String toString() {
								if (((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
									return "Ban StarMade Account";
								} else {
									return "-";
								}
							}
						}, new GUICallback() {

							@Override
							public void callback(GUIElement callingGuiElement, MouseEvent event) {
								if (event.pressedLeftMouse() && ((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
									if (player.getStarmadeName() != null) {
										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
										AudioController.fireAudioEventID(614);
										((GameClientState) state).getController().sendAdminCommand(AdminCommands.BAN_ACCOUNT_BY_PLAYERNAME, player.getName());
									} else {
										((GameClientState) state).getController().popupAlertTextMessage(Lng.str("User not uplinked!"), 0);
										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
										AudioController.fireAudioEventID(613);
									}
								}
							}

							@Override
							public boolean isOccluded() {
								return false;
							}
						});
						GUITextButton banName = new GUITextButton(state, 140, 20, FontSize.TINY_12, new Object() {

							@Override
							public String toString() {
								if (((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
									return "Ban Player Name";
								} else {
									return "-";
								}
							}
						}, new GUICallback() {

							@Override
							public void callback(GUIElement callingGuiElement, MouseEvent event) {
								if (event.pressedLeftMouse() && ((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
									AudioController.fireAudioEventID(615);
									((GameClientState) state).getController().sendAdminCommand(AdminCommands.BAN, player.getName(), false);
								}
							}

							@Override
							public boolean isOccluded() {
								return false;
							}
						});
						GUITextButton banIp = new GUITextButton(state, 140, 20, FontSize.TINY_12, new Object() {

							@Override
							public String toString() {
								if (((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
									return "Ban IP";
								} else {
									return "-";
								}
							}
						}, new GUICallback() {

							@Override
							public void callback(GUIElement callingGuiElement, MouseEvent event) {
								if (event.pressedLeftMouse() && ((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
									AudioController.fireAudioEventID(616);
									((GameClientState) state).getController().sendAdminCommand(AdminCommands.BAN_IP_BY_PLAYERNAME, player.getName());
								}
							}

							@Override
							public boolean isOccluded() {
								return false;
							}
						});
						int e = 24;
						c.getInputPanel().getContent().attach(kick);
						banName.getPos().y = e * 1;
						c.getInputPanel().getContent().attach(banName);
						banAccount.getPos().y = e * 2;
						c.getInputPanel().getContent().attach(banAccount);
						banIp.getPos().y = e * 3;
						c.getInputPanel().getContent().attach(banIp);
						c.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(617);
					}
				}
			});
			eText.getPos().x += 3 * space + nameSpace + optionExtraSpace;
		}

		@Override
		public void cleanUp() {
		}

		@Override
		public void draw() {
			transform();
			aText.draw();
			bText.draw();
			dText.draw();
			eText.draw();
		}

		@Override
		public float getHeight() {
			return aText.getHeight();
		}

		@Override
		public float getWidth() {
			return aText.getWidth() * 4 + 4 * space;
		}

		@Override
		public boolean isPositionCenter() {
			return false;
		}

		@Override
		public void onInit() {
			aText.onInit();
			bText.onInit();
			dText.onInit();
			eText.onInit();
		}
	}

	@Override
	public float getWidth() {
		return background.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	@Override
	public void onInit() {
		background.onInit();
		scrollPanel.onInit();
		firstDraw = false;
		TopTableElement t = new TopTableElement(getState());
		t.update("NAME (*upgraded)", "PING", "FACTION", "OPTION");
		GUIListElement header = new GUIListElement(t, t, getState());
		panelList.add(header);
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		if (reconstructionRequested) {
			reconstructList();
			reconstructionRequested = false;
		}
		for (int i = 0; i < panelList.size(); i++) {
			panelList.get(i).update(timer);
		}
	}

	@Override
	public void onAddedSendable(Sendable s) {
		reconstructionRequested = true;
	}

	@Override
	public void onRemovedSendable(Sendable s) {
		reconstructionRequested = true;
	}
}
