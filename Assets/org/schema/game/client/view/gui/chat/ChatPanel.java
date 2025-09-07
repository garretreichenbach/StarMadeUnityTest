package org.schema.game.client.view.gui.chat;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector3f;

import org.lwjgl.glfw.GLFW;
import org.schema.common.FastMath;
import org.schema.game.client.controller.PlayerChatInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.chat.ChatCallback;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.common.data.chat.LoadedClientChatChannel;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.network.objects.ChatMessage;
import org.schema.game.network.objects.ChatMessage.ChatMessageType;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChatLogPanel;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollableTextPanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUICheckBoxTextPair;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITabbedContent;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Keyboard;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ChatPanel extends GUIElement implements GUIActiveInterface {

	public static boolean flagActivate = false;

	public static int windowIdGen;

	final GUITextOverlayTable visibileChatDefault;

	private final ObjectArrayList<ChatWindow> chatWindows = new ObjectArrayList<ChatWindow>();

	public GUIDialogWindow chatPanel;

	// private GUIContentPane personalTab;
	private boolean init;

	private boolean flagFactionTabRecreate;

	private ChatWindow currentlyDrawing;

	private ChatCallback mainChatCallback;

	private ChatCallback cb;

	private ChatWindow selectedWindowNextFrame;

	private GUITabbedContent c;

	private FontInterface fontSize = FontSize.MEDIUM_15;

	@SuppressWarnings("unchecked")
	public ChatPanel(InputState state) {
		super(state);
		visibileChatDefault = new GUITextOverlayTable(getState());
		visibileChatDefault.onInit();
		visibileChatDefault.setText((List<Object>) ((List<? extends Object>) getState().getChannelRouter().getDefaultVisibleChatLog()));
		visibileChatDefault.setBeginTextAtLast(true);
		GUIAnchor defaultChatDependent = new GUIAnchor(state, 400, 300);
		visibileChatDefault.autoWrapOn = defaultChatDependent;
	}

	public static boolean isChannelNameValid(String entry) {
		return (entry.length() > 2 && !entry.startsWith("#") && !entry.toLowerCase(Locale.ENGLISH).equals("general") && !entry.toLowerCase(Locale.ENGLISH).equals("faction") && !entry.toLowerCase(Locale.ENGLISH).equals("all"));
	}

	@SuppressWarnings("unchecked")
	public static void createChatPane(GUIContentPane pane, final GameClientState state, final ChatCallback chatCallback, final boolean scrollLockOnBar, boolean extraTBMemberListBottom, final ChatPanel mainPanel) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		pane.setListDetailMode(0, pane.getTextboxes(0).get(0));
		pane.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		pane.addDivider(1);
		pane.setDividerWidth(1, 100);
		pane.setDividerMovable(0, true);
		pane.setDividerDetail(0);
		if (extraTBMemberListBottom) {
			pane.setTextBoxHeight(1, 0, UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
			pane.addNewTextBox(1, 28);
			pane.setListDetailMode(1, 0, pane.getTextboxes(1).get(0));
		}
		GUIScrollableTextPanel scroll;
		GUITextOverlayTable text = new GUITextOverlayTable(state) {

			@Override
			public float getHeight() {
				return getTextHeight();
			}

			@Override
			public void draw() {
				// for(Object o : getText()){
				// if(o instanceof ChatMessage){
				// ChatMessage c = (ChatMessage)o;
				// System.err.println("MSG: "+c.receiver+"; "+c.receiverType.name()+": "+c.getColor()+"; "+c.text);
				// }
				// }
				super.draw();
			}
		};
		final GUIActivatableTextBar chatInputBar = new GUIActivatableTextBar(state, mainPanel.fontSize, 300, 1, Lng.str("CHAT"), pane.getContent(0, 1), new TextCallback() {

			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
				if (entry.trim().length() > 0) {
					if (send) {
						chatCallback.chat(entry);
					} else {
						chatCallback.localChatOnClient(entry);
					}
				}
				if (EngineSettings.CHAT_CLOSE_ON_ENTER.isOn() && (send || !onAutoComplete)) {
					if (state.getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive()) {
						state.getGlobalGameControlManager().getIngameControlManager().getChatControlManager().setActive(false);
					}
				}
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void newLine() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				if (s.trim().length() == 0 & mainPanel.chatWindows.size() > 1) {
					int selected = mainPanel.chatWindows.size() - 1;
					int newSel;
					if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
						newSel = FastMath.cyclicBWModulo(selected - 1, mainPanel.chatWindows.size());
						if (!(mainPanel.chatWindows.get(newSel).drawable instanceof GUIInputPanel) && mainPanel.c.getSelectedTab() != 0) {
							// move on if channel tab is selected
							newSel = FastMath.cyclicBWModulo(newSel - 1, mainPanel.chatWindows.size());
						}
					} else {
						newSel = FastMath.cyclicBWModulo(selected + 1, mainPanel.chatWindows.size());
						if (!(mainPanel.chatWindows.get(newSel).drawable instanceof GUIInputPanel) && mainPanel.c.getSelectedTab() != 0) {
							// move on if channel tab is selected
							newSel = FastMath.cyclicBWModulo(newSel + 1, mainPanel.chatWindows.size());
						}
					}
					mainPanel.selectedWindow(mainPanel.chatWindows.get(newSel));
					mainPanel.chatWindows.get(newSel).ident.getChatBar().activateBar();
					return s;
				} else {
					return state.onAutoComplete(s, callback, prefix);
				}
			}

			@Override
			public String[] getCommandPrefixes() {
				return state.getCommandPrefixes();
			}
		}, pane.getTextboxes(0).get(1), null);
		chatInputBar.setMinimumLength(0);
		chatInputBar.onInit();
		chatCallback.setChatBar(chatInputBar);
		text.setLimitTextDraw(10000000);
		text.setText((List<Object>) chatCallback.getChatLog());
		scroll = new GUIScrollableTextPanel(10, 10, pane.getContent(0, 0), state) {

			/*
			 * (non-Javadoc)
			 *
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableTextPanel#isScrollLock()
			 */
			@Override
			public boolean isScrollLock() {
				return true;
			}
		};
		scroll.markReadInterface = chatCallback;
		scroll.setContent(text);
		scroll.onInit();
		pane.getContent(0, 0).attach(scroll);
		// too slow on not monospaced text
		text.autoWrapOn = scroll;
		text.wrapSimple = true;
		pane.getContent(0, 1).attach(chatInputBar);
		ChatChannelMembersScrollableListNew l = new ChatChannelMembersScrollableListNew(state, pane.getContent(1, 0), chatCallback, mainPanel);
		l.onInit();
		pane.getContent(1, 0).attach(l);
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if (flagFactionTabRecreate) {
			recreateTabs();
			flagFactionTabRecreate = false;
		}
		drawDefaultOnHud();
		// chatPanel.draw();
		for (int i = 0; i < chatWindows.size(); i++) {
			if (!chatWindows.get(i).ident.getMemberPlayerStates().contains(getState().getPlayer())) {
				closeChat(chatWindows.get(i).ident);
				break;
			}
		}
		boolean found = false;
		for (int i = chatWindows.size() - 1; i >= 0; i--) {
			if (!found) {
				chatWindows.get(i).drawAnchor();
				if (chatWindows.get(i).anchorInside()) {
					for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
						if (e.pressedLeftMouse()) {
							selectedWindow(chatWindows.get(i));
						}
					}
					// we cant select a lower window
					found = true;
				}
			} else {
				chatWindows.get(i).setAnchorInside(false);
			}
		}
		if (!found) {
		// dropToSpaceAnchor.setInside(false);
		// dropToSpaceAnchor.draw();
		} else {
		}
		if (selectedWindowNextFrame != null) {
			selectedWindow(selectedWindowNextFrame);
			selectedWindowNextFrame = null;
		}
		for (int i = 0; i < chatWindows.size(); i++) {
			currentlyDrawing = chatWindows.get(i);
			chatWindows.get(i).draw();
		}
		currentlyDrawing = null;
		if (ChatPanel.flagActivate) {
			boolean fd = false;
			// System.err.println("############### FLAG ACTIVATE "+getState().lastSelectedInput);
			if (getState().getController().getInputController().getLastSelectedInput() != null) {
				for (int i = 0; i < chatWindows.size(); i++) {
					ChatWindow chatWindow = chatWindows.get(i);
					if (chatWindow.ident.getChatBar().getTextArea() == getState().getController().getInputController().getLastSelectedInput()) {
						chatWindow.ident.getChatBar().activateBar();
						fd = true;
						break;
					}
				}
			}
			if (!fd) {
				if (!chatWindows.isEmpty()) {
					chatWindows.get(0).ident.getChatBar().activateBar();
				}
			}
			ChatPanel.flagActivate = false;
		}
	}

	@Override
	public void onInit() {
		if (chatPanel != null) {
			chatPanel.cleanUp();
		}
		chatPanel = new GUIDialogWindow(getState(), 750, 550, "ChatPanelMain") {

			/*
			 * (non-Javadoc)
			 *
			 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow#getInset()
			 */
			@Override
			public int getTopDist() {
				return 0;
			}
		};
		chatPanel.onInit();
		chatPanel.innerHeightSubstraction = 0;
		chatPanel.setMouseUpdateEnabled(true);
		chatPanel.setCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (chatPanel.isInside()) {
				}
			}
		});
		chatPanel.setCloseCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(433);
					getState().getGlobalGameControlManager().getIngameControlManager().getChatControlManager().setActive(false);
				}
			}
		});
		chatPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		addChat(new ChatWindow(chatPanel, cb));
		init = true;
	}

	public void drawAsHud() {
		if (!init) {
			onInit();
		}
		for (int i = chatWindows.size() - 1; i >= 0; i--) {
			ChatWindow chatWindow = chatWindows.get(i);
			if (chatWindow.ident.isSticky()) {
				drawOnHud(chatWindow);
			}
		}
		drawDefaultOnHud();
	}

	private void drawDefaultOnHud() {
		GlUtil.glPushMatrix();
		visibileChatDefault.orientate(GUIElement.ORIENTATION_HORIZONTAL_MIDDLE | GUIElement.ORIENTATION_BOTTOM);
		visibileChatDefault.getPos().x -= (256 + 86);
		visibileChatDefault.getPos().y -= getOnHudPosY();
		visibileChatDefault.draw();
		GlUtil.glPopMatrix();
	}

	public int getOnHudPosY() {
		return (116 + 24 + visibileChatDefault.getTextHeight());
	}

	private void drawOnHud(ChatWindow chatWindow) {
		GlUtil.glPushMatrix();
		chatWindow.windowAnchor.transform();
		GlUtil.translateModelview(8, 8, 0);
		if (chatWindow.ident.isFullSticky()) {
			chatWindow.chatLogPanel.draw();
		} else {
			GlUtil.translateModelview(0, -chatWindow.visibileChatOnSticky.getTextHeight(), 0);
			chatWindow.visibileChatOnSticky.draw();
		}
		GlUtil.glPopMatrix();
	}

	public void doTransforms() {
	}

	private void selectedWindow(ChatWindow invWindow) {
		int iSel = chatWindows.indexOf(invWindow);
		if (iSel >= 0) {
			chatWindows.remove(iSel);
			chatWindows.add(invWindow);
		} else {
		// window may have been already remove from the closeAncore in InvInventory
		// it deligates to the close callback for input panels so the x doesnt have to be pressed twice
		}
	}

	private class MainChatCallback extends GUIObservable implements ChatCallback {

		private GUIActivatableTextBar chatBar;

		@Override
		public List<Object> getChatLog() {
			return getState().getGeneralChatLog();
		}

		@Override
		public List<? extends Object> getVisibleChatLog() {
			return getState().getVisibleChatLog();
		}

		@Override
		public boolean localChatOnClient(String text) {
			if (getState().getChannelRouter().getAllChannel() != null) {
				return getState().getChannelRouter().getAllChannel().localChatOnClient(text);
			}
			return false;
		}

		@Override
		public void chat(String text) {
			ChatMessage m = new ChatMessage();
			m.sender = getState().getPlayerName();
			m.receiver = "all";
			m.text = text;
			m.receiverType = ChatMessageType.CHANNEL;
			if (getState().getChannelRouter().getAllChannel() != null) {
				getState().getChannelRouter().getAllChannel().send(m);
			}
		// getState().getChat().addToVisibleChat(text, "", true);
		}

		@Override
		public Collection<PlayerState> getMemberPlayerStates() {
			return getState().getOnlinePlayersLowerCaseMap().values();
		}

		@Override
		public String getName() {
			return Lng.str("All");
		}

		@Override
		public Object getTitle() {
			return Lng.str("All");
		}

		@Override
		public void onWindowDeactivate() {
		}

		@Override
		public boolean isSticky() {
			return false;
		}

		@Override
		public void setSticky(boolean b) {
		}

		@Override
		public boolean isFullSticky() {
			return false;
		}

		@Override
		public void setFullSticky(boolean b) {
		}

		@Override
		public void leave(PlayerState player) {
		}

		@Override
		public boolean canLeave() {
			return false;
		}

		@Override
		public GUIActivatableTextBar getChatBar() {
			return chatBar;
		}

		@Override
		public void setChatBar(GUIActivatableTextBar chatBar) {
			this.chatBar = chatBar;
		}

		@Override
		public boolean isClientOpen() {
			return false;
		}

		@Override
		public void setClientOpen(boolean clientOpen) {
		}

		@Override
		public boolean isModerator(PlayerState player) {
			return false;
		}

		@Override
		public boolean hasChannelBanList() {
			return false;
		}

		@Override
		public boolean hasChannelMuteList() {
			if (getState().getChannelRouter().getAllChannel() != null) {
				return getState().getChannelRouter().getAllChannel().hasChannelMuteList();
			}
			return false;
		}

		@Override
		public boolean hasChannelModList() {
			return false;
		}

		@Override
		public boolean hasPossiblePassword() {
			return false;
		}

		@Override
		public void requestModUnmodOnClient(String f, boolean b) {
		}

		@Override
		public void requestPasswordChangeOnClient(String passwd) {
		}

		@Override
		public void requestKickOnClient(PlayerState f) {
		}

		@Override
		public boolean isBanned(PlayerState f) {
			return false;
		}

		@Override
		public void requestBanUnbanOnClient(String f, boolean b) {
		}

		@Override
		public String[] getBanned() {
			return null;
		}

		@Override
		public boolean isMuted(PlayerState f) {
			if (getState().getChannelRouter().getAllChannel() != null) {
				return getState().getChannelRouter().getAllChannel().isMuted(f);
			}
			return false;
		}

		@Override
		public void requestMuteUnmuteOnClient(String f, boolean b) {
			if (getState().getChannelRouter().getAllChannel() != null) {
				getState().getChannelRouter().getAllChannel().requestMuteUnmuteOnClient(f, b);
				;
			}
		}

		@Override
		public String[] getMuted() {
			if (getState().getChannelRouter().getAllChannel() != null) {
				return getState().getChannelRouter().getAllChannel().getMuted();
			}
			return null;
		}

		@Override
		public void requestIgnoreUnignoreOnClient(String f, boolean b) {
			if (getState().getChannelRouter().getAllChannel() != null) {
				getState().getChannelRouter().getAllChannel().requestIgnoreUnignoreOnClient(f, b);
			}
		}

		@Override
		public void markRead() {
			if (getState().getChannelRouter().getAllChannel() != null) {
				getState().getChannelRouter().getAllChannel().markRead();
			}
		}

		@Override
		public int getUnread() {
			if (getState().getChannelRouter().getAllChannel() != null) {
				return getState().getChannelRouter().getAllChannel().getUnread();
			} else {
				return 0;
			}
		}
	}

	public void recreateTabs() {
		c = new GUITabbedContent(getState(), chatPanel.getMainContentPane().getContent(0));
		c.onInit();
		GUIContentPane chatTab = c.addTab(Lng.str("CHAT"));
		cb = new MainChatCallback();
		createChatPane(chatTab, getState(), cb, true, true, this);
		GUICheckBoxTextPair cb = new GUICheckBoxTextPair(getState(), Lng.str("Hide on ret."), 65, FontSize.SMALL_13, 10) {

			@Override
			public void activate() {
				EngineSettings.CHAT_CLOSE_ON_ENTER.setOn(true);
			}

			@Override
			public boolean isActivated() {
				return EngineSettings.CHAT_CLOSE_ON_ENTER.isOn();
			}

			@Override
			public void deactivate() {
				EngineSettings.CHAT_CLOSE_ON_ENTER.setOn(false);
			}
		};
		cb.setPos(5, 6, 0);
		GUIScrollablePanel lr = new GUIScrollablePanel(1, 1, chatTab.getTextboxes(1).get(1), getState());
		lr.setScrollable(0);
		lr.setLeftRightClipOnly = true;
		lr.setContent(cb);
		chatTab.getTextboxes(1).get(1).attach(lr);
		GUIContentPane channelsTab = c.addTab(Lng.str("CHANNELS"));
		ChatChannelsScrollableListNew channelsSc = new ChatChannelsScrollableListNew(getState(), channelsTab.getContent(0), this);
		channelsSc.onInit();
		channelsTab.getContent(0).attach(channelsSc);
		chatPanel.getMainContentPane().getContent(0).attach(c);
		channelsTab.setTextBoxHeightLast(UIScale.getUIScale().scale(100));
		channelsTab.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		channelsTab.setListDetailMode(channelsTab.getTextboxes().get(0));
		GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 2, 1, channelsTab.getContent(1));
		buttons.onInit();
		buttons.addButton(0, 0, Lng.str("Create Channel"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			private boolean createPermanent;

			@Override
			public boolean isOccluded() {
				return !ChatPanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameTextInput playerTextInput = new PlayerGameTextInput("ccChatPanel", getState(), 32, Lng.str("Create New Channel"), "") {

						@Override
						public String[] getCommandPrefixes() {
							return null;
						}

						@Override
						public void onFailedTextCheck(String msg) {
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
							return null;
						}

						@Override
						public boolean onInput(String entry) {
							if (isChannelNameValid(entry.trim())) {
								getState().getChannelRouter().createNewChannelOnClient(getState().getPlayer(), entry.trim(), "", createPermanent);
								return true;
							} else {
								getState().getController().popupAlertTextMessage(Lng.str("Invalid Channel Name!\n1.: No '#' at start.\n2.: May not be equal to\ndefault channels.\n3.: At least 3 characters"), 0);
							}
							return false;
						}
					};
					playerTextInput.getInputPanel().onInit();
					createPermanent = false;
					GUICheckBoxTextPair cb = new GUICheckBoxTextPair(getState(), Lng.str("Permanent"), 65, FontSize.SMALL_13, 10) {

						@Override
						public boolean isActivated() {
							return createPermanent;
						}

						@Override
						public void deactivate() {
							if (((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get()) {
								createPermanent = false;
							} else {
								((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot create permanent channel.\nOnly admins may do so."), 0);
							}
						}

						@Override
						public void activate() {
							if (((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get()) {
								createPermanent = true;
							} else {
								((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot create permanent channel.\nOnly admins may do so."), 0);
							}
						}
					};
					cb.onInit();
					cb.setPos(20, 30, 0);
					((GUIDialogWindow) playerTextInput.getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(cb);
					playerTextInput.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(434);
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return false;
			}
		});
		channelsTab.getContent(1).attach(buttons);
		chatPanel.activeInterface = this;
	// if(beforeTab != null){
	// for(int i = 0; i < chatPanel.getTabs().size(); i++){
	// if(chatPanel.getTabs().get(i).getTabName().equals(beforeTab)){
	// chatPanel.setSelectedTab(i);
	// break;
	// }
	// }
	// }
	}

	public void deactivate(PlayerChatInput playerInput) {
		for (int i = 0; i < chatWindows.size(); i++) {
			if (chatWindows.get(i).drawable == playerInput.getInputPanel()) {
				removeChat(i);
				return;
			}
		}
	// can happen on double click with closedAnchor of InvWindow
	// assert(false):"Couldnt deactivate "+playerInventoryInput+": "+playerInventoryInput.getInputPanel()+": "+otherInventories;
	}

	public void openChat(ChatCallback cb) {
		GlUtil.printGlErrorCritical();
		cb.setClientOpen(true);
		ChatWindow chatWindow = null;
		for (int i = 0; i < chatWindows.size(); i++) {
			if (chatWindows.get(i).ident == cb) {
				chatWindow = chatWindows.get(i);
				break;
			}
		}
		if (chatWindow == null) {
			PlayerChatInput p = new PlayerChatInput(cb.getName(), getState(), cb.getTitle(), this, this, cb);
			p.getInputPanel().onInit();
			chatWindow = new ChatWindow(p.getInputPanel(), cb);
			addChat(chatWindow);
			arrangeWindows();
		}
		selectedWindowNextFrame = chatWindow;
		GlUtil.printGlErrorCritical("GL ERROR OPENING CHAT");
	}

	public void closeChat(ChatCallback cb) {
		for (int i = 0; i < chatWindows.size(); i++) {
			if (chatWindows.get(i).ident == cb) {
				removeChat(i);
				break;
			}
		}
	}

	private void arrangeWindows() {
		if (chatWindows.size() == 1) {
			// inventoryPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
			boolean newPanel = chatPanel.savedSizeAndPosition.newPanel;
			chatPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
			if (newPanel) {
				chatPanel.getPos().x -= (int) (chatPanel.getWidth() / 2);
			}
		} else {
			int s = 24;
			// for(int i = 0; i < otherInventories.size(); i++){
			// if(otherInventories.get(i).drawable != inventoryPanel &&
			// !(otherInventories.get(i).inventoryIcons.getInventory() instanceof PersonalFactoryInventory)){
			// //at least one stash here now
			// break;
			// }
			// }
			for (int i = 0; i < chatWindows.size(); i++) {
				int dist = (int) (chatPanel.getPos().x + chatPanel.getWidth() + 10);
				boolean newPanel = chatWindows.get(i).isNewPanel();
				if (newPanel) {
					if (chatWindows.get(i).ident == mainChatCallback) {
					// already aligned
					} else {
						chatWindows.get(i).getPos().x = dist;
						chatWindows.get(i).getPos().y = chatPanel.getPos().y;
						chatWindows.get(i).getPos().y += s;
						s += chatWindows.get(i).drawable.getHeight() + 20;
					}
				} else {
					chatWindows.get(i).orientate(0);
				}
			}
			// if(isCapsuleRefineryOpen() || isMacroFactioryBlockFactoryOpen() || isMicroFactoryOpen()){
			// 
			// }else{
			boolean newPanel = chatWindows.get(1).isNewPanel();
			chatWindows.get(1).orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
			if (newPanel) {
				chatWindows.get(1).getPos().x += chatWindows.get(0).drawable.getWidth() / 2;
			}
		// }
		}
	}

	public ShopControllerManager getShopControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager();
	}

	public PlayerState getOwnPlayer() {
		return ChatPanel.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return ChatPanel.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	private void addChat(ChatWindow chatWindow) {
		chatWindow.ident.setClientOpen(true);
		chatWindows.add(chatWindow);
	}

	@Override
	public float getHeight() {
		return chatPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return chatPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		// System.err.println("CUDRA: "+currentlyDrawing+"; "+getState().getController().getPlayerInputs().size());
		return getState().getController().getPlayerInputs().isEmpty() && (currentlyDrawing == null || (currentlyDrawing.anchorInside() || chatWindows.get(chatWindows.size() - 1) == currentlyDrawing));
	}

	public void reset() {
		if (chatPanel != null) {
			chatPanel.reset();
		}
	}

	public void onActivateChat(boolean active) {
	}

	public void deactivateAllOther() {
		for (int i = 0; i < chatWindows.size(); i++) {
			if (chatWindows.get(i).drawable instanceof GUIInputPanel) {
				removeChat(i);
				i--;
			}
		}
		assert (chatWindows.size() == 1);
	}

	private void removeChat(int i) {
		chatWindows.get(i).ident.setClientOpen(false);
		chatWindows.remove(i);
	}

	public void handleLoaded(ChatChannel c, LoadedClientChatChannel ld) {
		assert (ld.uid.equals(c.getUniqueChannelName())) : c.getUniqueChannelName() + "; " + ld.uid;
		c.setSticky(ld.sticky);
		c.setFullSticky(ld.fullSticky);
		c.setClientCurrentPassword(ld.password);
		if (ld.open) {
			openChat(c);
		}
	}

	private class ChatWindow implements GUICallback {

		public final ChatCallback ident;

		final GUITextOverlayTable visibileChatOnSticky;

		final GUIChatLogPanel chatLogPanel;

		private final int id;

		GUIElement drawable;

		GUIAnchor windowAnchor;

		GUIAnchor windowAnchorClose;

		@SuppressWarnings("unchecked")
		public ChatWindow(GUIElement drawable, ChatCallback chatCallback) {
			this.id = windowIdGen++;
			this.drawable = drawable;
			this.windowAnchor = new GUIAnchor(getState());
			this.windowAnchor.setMouseUpdateEnabled(true);
			this.windowAnchor.setCallback(this);
			this.windowAnchorClose = new GUIAnchor(getState());
			this.windowAnchorClose.setMouseUpdateEnabled(true);
			this.windowAnchorClose.setCallback(this);
			this.windowAnchorClose.setUserPointer("X");
			this.windowAnchor.attach(windowAnchorClose);
			this.ident = chatCallback;
			assert (chatCallback != null);
			visibileChatOnSticky = new GUITextOverlayTable(getState());
			visibileChatOnSticky.onInit();
			visibileChatOnSticky.setText((List<Object>) chatCallback.getVisibleChatLog());
			visibileChatOnSticky.setBeginTextAtLast(true);
			if (drawable instanceof GUIInputPanel) {
				visibileChatOnSticky.autoWrapOn = ((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getMainContentPane().getContent(0, 0);
				chatLogPanel = new GUIChatLogPanel(400, 150, fontSize, ((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getMainContentPane().getContent(0, 0), getState());
			} else {
				visibileChatOnSticky.autoWrapOn = ((GUIDialogWindow) drawable).getMainContentPane().getContent(0, 0);
				chatLogPanel = new GUIChatLogPanel(400, 150, fontSize, ((GUIDialogWindow) drawable).getMainContentPane().getContent(0, 0), getState());
			}
			chatLogPanel.setText((List<Object>) chatCallback.getChatLog());
			chatLogPanel.onInit();
		}

		public Vector3f getPos() {
			if (drawable instanceof GUIInputPanel) {
				return ((GUIInputPanel) drawable).background.getPos();
			} else {
				return chatPanel.getPos();
			}
		}

		public void orientate(int o) {
			if (drawable instanceof GUIInputPanel) {
				((GUIInputPanel) drawable).background.orientate(o);
				;
			} else {
				chatPanel.orientate(o);
			}
		}

		public boolean isNewPanel() {
			if (drawable instanceof GUIInputPanel) {
				return ((GUIInputPanel) drawable).background.savedSizeAndPosition.newPanel;
			} else {
				return chatPanel.savedSizeAndPosition.newPanel;
			}
		}

		public void setAnchorInside(boolean b) {
			windowAnchor.setInside(b);
			windowAnchorClose.setInside(b);
		}

		// public void rewrap(){
		// GUIAncor content;
		// if(drawable instanceof GUIInputPanel){
		// content = ((GUIDialogWindow)((GUIInputPanel)drawable).getBackground()).getMainContentPane().getContent(0, 0);
		// }else{
		// content = ((GUIDialogWindow)drawable).getMainContentPane().getContent(0, 0);
		// }
		// 
		// }
		public boolean anchorInside() {
			return windowAnchor.isInside() || windowAnchorClose.isInside();
		}

		public void drawAnchor() {
			if (drawable instanceof GUIInputPanel) {
				windowAnchor.setWidth(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getWidth());
				windowAnchor.setHeight(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getHeight());
				windowAnchor.getPos().set(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getPos());
				windowAnchorClose.setWidth(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getCloseCross().getWidth());
				windowAnchorClose.setHeight(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getCloseCross().getHeight());
				windowAnchorClose.getPos().set(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getCloseCross().getPos());
			} else {
				windowAnchor.setWidth(((GUIDialogWindow) drawable).getWidth());
				windowAnchor.setHeight(((GUIDialogWindow) drawable).getHeight());
				windowAnchor.getPos().set(((GUIDialogWindow) drawable).getPos());
				windowAnchorClose.setWidth(((GUIDialogWindow) drawable).getCloseCross().getWidth());
				windowAnchorClose.setHeight(((GUIDialogWindow) drawable).getCloseCross().getHeight());
				windowAnchorClose.getPos().set(((GUIDialogWindow) drawable).getCloseCross().getPos());
			}
			// System.err.println("ANCHOR FOR "+drawable+": "+windowAnchor.getWidth()+"x"+windowAnchor.getHeight()+": "+windowAnchor.getPos());
			windowAnchor.draw();
		}

		public void draw() {
			drawable.draw();
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse() && callingGuiElement == windowAnchorClose){
		// if(drawable instanceof GUIInputPanel){
		// ((GUIInputPanel)drawable).getCallback().callback(callingGuiElement, event);
		// }
		// }
		// if(event.pressedLeftMouse()){
		// selectedWindow(this);
		// }
		// selection is done immediately in draw() of InventoryPanelNew
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return obj != null && obj instanceof ChatWindow && id == ((ChatWindow) obj).id;
		}

		@Override
		public boolean isOccluded() {
			return !getState().getController().getPlayerInputs().isEmpty();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "INV:" + drawable;
		}
	}
}
