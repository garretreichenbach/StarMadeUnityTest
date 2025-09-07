package org.schema.game.client.view.gui.chat;

import org.schema.game.client.controller.PlayerChatInput;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.chat.ChannelRouter;
import org.schema.game.common.data.chat.ChatCallback;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContextPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIInnerTextbox;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.sound.controller.AudioController;

public class ChatInputPanel extends GUIInputPanel implements GUIActiveInterface {

	public PlayerChatInput playerInput;

	private GUIActiveInterface actInterface;

	private ChatPanel mainPanel;

	// private GUIContentPane personalTab;
	private boolean init;

	private ChatCallback chatCallback;

	public ChatInputPanel(String typeId, ClientState state, Object title, int initialWidth, int initialHeight, ChatCallback chatCallback, PlayerChatInput chatInput, GUIActiveInterface actInterface, ChatPanel mainPanel) {
		super("ChatPanel" + typeId, state, initialWidth, initialHeight, chatInput, title, "");
		this.mainPanel = mainPanel;
		this.chatCallback = chatCallback;
		this.actInterface = actInterface;
		this.autoOrientate = false;
		this.playerInput = chatInput;
		setCancelButton(false);
		setOkButton(false);
		setTitleOnTop(true);
		((GUIDialogWindow) this.getBackground()).innerHeightSubstraction = 0;
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public boolean isActive() {
		return actInterface.isActive();
	}

	@Override
	public void update(Timer timer) {
	}

	private void createPanel() {
		ChatPanel.createChatPane(((GUIDialogWindow) background).getMainContentPane(), getState(), chatCallback, false, true, mainPanel);
		GUIHorizontalButtonTablePane contextToggle = new GUIHorizontalButtonTablePane(getState(), 1, 1, ((GUIDialogWindow) background).getMainContentPane().getTextboxes(1).get(1).getContent());
		contextToggle.onInit();
		contextToggle.addButton(0, 0, "MANAGE", HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !actInterface.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(425);
					boolean isAdminOrMod = ChannelRouter.allowAdminClient(getState().getPlayer()) || chatCallback.isModerator(getState().getPlayer());
					// Ignore button
					int numRows = 1;
					if (isAdminOrMod && (chatCallback.hasChannelBanList() || chatCallback.hasChannelModList())) {
						numRows++;
					}
					if (isAdminOrMod && chatCallback.hasChannelMuteList()) {
						numRows++;
					}
					if (isAdminOrMod && chatCallback.hasPossiblePassword()) {
						numRows++;
					}
					GUIContextPane context = new GUIContextPane(getState(), 140, (numRows) * 25);
					GUIHorizontalButtonTablePane adminButtons = new GUIHorizontalButtonTablePane(getState(), 1, numRows, context);
					adminButtons.onInit();
					int row = 0;
					if (isAdminOrMod && chatCallback.hasChannelBanList()) {
						adminButtons.addButton(0, row++, Lng.str("BANS"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

							@Override
							public void callback(GUIElement callingGuiElement, MouseEvent event) {
								if (event.pressedLeftMouse()) {
									PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CHATMANAGEBANS", getState(), 380, 400, Lng.str("Manage Bans"), "") {

										@Override
										public void onDeactivate() {
										}

										@Override
										public void pressedOK() {
											deactivate();
										}
									};
									c.getInputPanel().onInit();
									c.getInputPanel().setCancelButton(false);
									c.getInputPanel().setOkButtonText(Lng.str("DONE"));
									GUIAnchor mc = ((GUIDialogWindow) c.getInputPanel().getBackground()).getMainContentPane().getContent(0);
									BannedFromChannelScrollableListNew xx = new BannedFromChannelScrollableListNew(getState(), mc, chatCallback, mainPanel);
									xx.onInit();
									mc.attach(xx);
									c.activate();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
									AudioController.fireAudioEventID(426);
								}
							}

							@Override
							public boolean isOccluded() {
								return !actInterface.isActive();
							}
						}, new GUIActivationCallback() {

							@Override
							public boolean isVisible(InputState state) {
								return true;
							}

							@Override
							public boolean isActive(InputState state) {
								return chatCallback.hasChannelBanList() && chatCallback.getBanned().length > 0 && (ChannelRouter.allowAdminClient(getState().getPlayer()) || chatCallback.isModerator(getState().getPlayer()));
							}
						});
					}
					if (isAdminOrMod && chatCallback.hasChannelMuteList()) {
						adminButtons.addButton(0, row++, "MUTES", HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

							@Override
							public boolean isOccluded() {
								return !actInterface.isActive();
							}

							@Override
							public void callback(GUIElement callingGuiElement, MouseEvent event) {
								if (event.pressedLeftMouse()) {
									PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CHATMANAGEMUTES", getState(), 380, 400, Lng.str("Manage Mutes"), "") {

										@Override
										public void pressedOK() {
											deactivate();
										}

										@Override
										public void onDeactivate() {
										}
									};
									c.getInputPanel().onInit();
									c.getInputPanel().setCancelButton(false);
									c.getInputPanel().setOkButtonText(Lng.str("DONE"));
									GUIAnchor mc = ((GUIDialogWindow) c.getInputPanel().getBackground()).getMainContentPane().getContent(0);
									MutedFromChannelScrollableListNew xx = new MutedFromChannelScrollableListNew(getState(), mc, chatCallback, mainPanel);
									xx.onInit();
									mc.attach(xx);
									c.activate();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
									AudioController.fireAudioEventID(427);
								}
							}
						}, new GUIActivationCallback() {

							@Override
							public boolean isVisible(InputState state) {
								return true;
							}

							@Override
							public boolean isActive(InputState state) {
								return chatCallback.hasChannelMuteList() && chatCallback.getMuted().length > 0 && (ChannelRouter.allowAdminClient(getState().getPlayer()) || chatCallback.isModerator(getState().getPlayer()));
							}
						});
					}
					if (isAdminOrMod && chatCallback.hasPossiblePassword()) {
						adminButtons.addButton(0, row++, Lng.str("PW"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

							@Override
							public boolean isOccluded() {
								return !actInterface.isActive();
							}

							@Override
							public void callback(GUIElement callingGuiElement, MouseEvent event) {
								if (event.pressedLeftMouse()) {
									PlayerGameTextInput playerTextInput = new PlayerGameTextInput("ccChatPanelPW", getState(), 16, Lng.str("Change password"), Lng.str("Leave blank to make the channel public.\n\nIn case you lose the password,\nplease ask an admin to reset it!")) {

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
										public boolean onInput(String entry) {
											chatCallback.requestPasswordChangeOnClient(entry);
											return true;
										}
									};
									playerTextInput.setMinimumLength(0);
									playerTextInput.activate();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
									AudioController.fireAudioEventID(428);
								}
							}
						}, new GUIActivationCallback() {

							@Override
							public boolean isVisible(InputState state) {
								return true;
							}

							@Override
							public boolean isActive(InputState state) {
								return chatCallback.hasPossiblePassword() && (ChannelRouter.allowAdminClient(getState().getPlayer()) || chatCallback.isModerator(getState().getPlayer()));
							}
						});
					}
					adminButtons.addButton(0, row++, Lng.str("IGNORED"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return !actInterface.isActive();
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CHATMANAGEIGNORED", getState(), 380, 400, Lng.str("Manage Ignored"), "") {

									@Override
									public void pressedOK() {
										deactivate();
									}

									@Override
									public void onDeactivate() {
									}
								};
								c.getInputPanel().onInit();
								c.getInputPanel().setCancelButton(false);
								c.getInputPanel().setOkButtonText(Lng.str("DONE"));
								GUIAnchor mc = ((GUIDialogWindow) c.getInputPanel().getBackground()).getMainContentPane().getContent(0);
								IgnoredByPlayerScrollableListNew xx = new IgnoredByPlayerScrollableListNew(getState(), mc, chatCallback, mainPanel);
								xx.onInit();
								mc.attach(xx);
								c.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(429);
							}
						}
					}, new GUIActivationCallback() {

						@Override
						public boolean isVisible(InputState state) {
							return true;
						}

						@Override
						public boolean isActive(InputState state) {
							return getState().getPlayer().hasIgnored();
						}
					});
					context.attach(adminButtons);
					getState().getController().getInputController().setCurrentContextPane(context);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		((GUIDialogWindow) background).getMainContentPane().getTextboxes(1).get(1).getContent().attach(contextToggle);
		GUIInnerTextbox context = ((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 3, 1, context);
		buttons.onInit();
		buttons.addButton(0, 0, Lng.str("ON HUD"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !actInterface.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(430);
					chatCallback.setSticky(!chatCallback.isSticky());
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isHighlighted(InputState state) {
				return chatCallback.isSticky();
			}

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		buttons.addButton(1, 0, Lng.str("FULL"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !actInterface.isActive() || !chatCallback.isSticky();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(431);
					chatCallback.setFullSticky(!chatCallback.isFullSticky());
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return chatCallback.isSticky();
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return chatCallback.isFullSticky();
			}
		});
		buttons.addButton(2, 0, Lng.str("LEAVE"), HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !actInterface.isActive() || !chatCallback.canLeave();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && chatCallback.canLeave()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(432);
					chatCallback.leave(getOwnPlayer());
					mainPanel.closeChat(chatCallback);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return chatCallback.canLeave();
			}
		});
		context.attach(buttons);
	}

	public PlayerState getOwnPlayer() {
		return ChatInputPanel.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return ChatInputPanel.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return ((GUIDialogWindow) background).getHeight();
	}

	@Override
	public float getWidth() {
		return ((GUIDialogWindow) background).getWidth();
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if (isTitleOnTop()) {
			infoText.setPos((int) (background.getWidth() / 2 - infoText.getMaxLineWidth() / 2), -16, 0);
		} else {
			infoText.setPos((int) (background.getWidth() / 2 - infoText.getMaxLineWidth() / 2), 8, 0);
		}
		((GUIDialogWindow) background).draw();
	}

	@Override
	public void onInit() {
		super.onInit();
		createPanel();
		((GUIDialogWindow) background).activeInterface = actInterface;
		init = true;
	}
}
