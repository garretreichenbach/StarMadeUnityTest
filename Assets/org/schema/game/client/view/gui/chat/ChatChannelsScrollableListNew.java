package org.schema.game.client.view.gui.chat;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerPasswordInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChannelRouter;
import org.schema.game.common.data.chat.ChatCallback;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.common.data.chat.DirectChatChannel;
import org.schema.game.common.data.chat.PublicChannel;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContextPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class ChatChannelsScrollableListNew extends ScrollableTableList<ChatChannel> implements GUIChangeListener {

	public static final int AVAILABLE = 0;

	public static final int PERSONAL = 1;

	public static final int ADMIN = 2;

	private ChatPanel mainPanel;

	public ChatChannelsScrollableListNew(InputState state, GUIElement p, ChatPanel mainPanel) {
		super(state, 100, 100, p);
		this.mainPanel = mainPanel;
		getState().getPlayer().getPlayerChannelManager().addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		getState().getPlayer().getPlayerChannelManager().deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 7, (o1, o2) -> {
			String a;
			String b;
			a = o1.getName();
			b = o2.getName();
			return a.compareToIgnoreCase(b);
		});
		addFixedWidthColumnScaledUI(Lng.str("Joined"), 52, (o1, o2) -> {
			Boolean a;
			Boolean b;
			a = o1.getMemberPlayerStates().contains(getState().getPlayer());
			b = o2.getMemberPlayerStates().contains(getState().getPlayer());
			return a.compareTo(b);
		});
		addFixedWidthColumnScaledUI(Lng.str("Options"), 100, (o1, o2) -> {
			Boolean a;
			Boolean b;
			a = o1.getMemberPlayerStates().contains(getState().getPlayer());
			b = o2.getMemberPlayerStates().contains(getState().getPlayer());
			return a.compareTo(b);
		});
		addFixedWidthColumnScaledUI(Lng.str("#"), 28, (o1, o2) -> o1.getMemberPlayerStates().size() - o2.getMemberPlayerStates().size());
		addTextFilter(new GUIListFilterText<ChatChannel>() {

			@Override
			public boolean isOk(String input, ChatChannel listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<ChatChannel> getElementList() {
		return getState().getPlayer().getPlayerChannelManager().getAvailableChannels();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<ChatChannel> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final ChatChannel f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState()) {

				private int wasUnread = 0;

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable#draw()
				 */
				@Override
				public void draw() {
					if (f.getUnread() > 0 && wasUnread > 30) {
						setColor(1, 1, 0.8f, 1.0f);
					} else {
						if (f.getUnread() == 0) {
							wasUnread = 0;
						} else {
							wasUnread++;
						}
						setColor(0.8f, 0.8f, 0.8f, 1.0f);
					}
					super.draw();
				}
			};
			GUITextOverlayTable joinedText = new GUITextOverlayTable(getState());
			GUITextOverlayTable membersText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(new Object() {

				private int wasUnread = 0;

				@Override
				public String toString() {
					if (f.getUnread() > 0 && wasUnread > 30) {
						return modName(f) + " (" + f.getUnread() + ")";
					} else {
						if (f.getUnread() == 0) {
							wasUnread = 0;
						} else {
							wasUnread++;
						}
						return modName(f);
					}
				}
			});
			joinedText.setTextSimple(new Object() {

				@Override
				public String toString() {
					if (f.getMemberPlayerStates().contains(getState().getPlayer())) {
						return "X";
					} else {
						return " ";
					}
				}
			});
			membersText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.getMemberPlayerStates().size());
				}
			});
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			joinedText.getPos().y = heightInset;
			membersText.getPos().y = heightInset;
			GUIAnchor c = new GUIAnchor(getState(), 80, 0);
			GUITextButton joinAndOpenButton = new GUITextButton(getState(), 44, 24, ColorPalette.OK, new Object() {

				@Override
				public String toString() {
					if (f.getMemberPlayerStates().contains(getState().getPlayer())) {
						return isChannelOpen(f) ? Lng.str("CLOSE") : Lng.str("OPEN");
					}
					return Lng.str("JOIN");
				}
			}, new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(420);
						if (f.getMemberPlayerStates().contains(player) && isChannelOpen(f)) {
							mainPanel.closeChat(f);
						} else {
							openChannel(f);
						}
					}
				}
			}) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
				 */
				@Override
				public void draw() {
					if (f.getMemberPlayerStates().contains(player) && isChannelOpen(f)) {
						setColorPalette(ColorPalette.CANCEL);
					} else {
						setColorPalette(ColorPalette.OK);
					}
					super.draw();
				}
			};
			GUITextButton leaveButton = new GUITextButton(getState(), 48, 24, ColorPalette.CANCEL, new Object() {

				@Override
				public String toString() {
					return "LEAVE";
				}
			}, new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
						AudioController.fireAudioEventID(421);
						leaveChannel(f);
					}
				}
			}) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
				 */
				@Override
				public void draw() {
					if (f.canLeave() && f.getMemberPlayerStates().contains(ChatChannelsScrollableListNew.this.getState().getPlayer())) {
						super.draw();
					}
				}
			};
			c.attach(joinAndOpenButton);
			c.attach(leaveButton);
			int inset = 3;
			int left = -2;
			joinAndOpenButton.setPos(left, c.getHeight(), 0);
			leaveButton.setPos(left + joinAndOpenButton.getWidth() + inset, c.getHeight(), 0);
			// c.attach(description);
			// r.expanded.add(new GUIListElement(c, c, getState()));
			ChatRow r = new ChatRow(getState(), f, nameText, joinedText, c, membersText);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	public String modName(ChatChannel c) {
		return c.hasPassword() ? c.getName() + " (PW)" : c.getName();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#getState()
	 */
	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	public boolean isPlayerAdmin() {
		return getState().getPlayer().getNetworkObject().isAdminClient.get();
	}

	public boolean canEdit(CatalogPermission f) {
		return f.ownerUID.toLowerCase(Locale.ENGLISH).equals(getState().getPlayer().getName().toLowerCase(Locale.ENGLISH)) || isPlayerAdmin();
	}

	private boolean isChannelOpen(final ChatChannel to) {
		return to.isClientOpen();
	}

	private void leaveChannel(ChatCallback to) {
		to.leave(getState().getPlayer());
		mainPanel.closeChat(to);
	}

	private void deleteChannel(final ChatChannel f) {
		PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("Do you really want to remove this channel?")) {

			@Override
			public void onDeactivate() {
			}

			@Override
			public void pressedOK() {
				f.requestChannelDeleteClient();
				deactivate();
			}
		};
		c.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(422);
	}

	private void openChannel(final ChatChannel to) {
		if (to instanceof DirectChatChannel) {
			getState().getPlayer().getPlayerChannelManager().addDirectChannel(to);
		} else {
			if (!to.getMemberPlayerStates().contains(getState().getPlayer())) {
				if (to.hasPassword()) {
					String a = Lng.str("(you can leave this blank as admin)");
					PlayerPasswordInput p = new PlayerPasswordInput("CHATCHANNELPW", getState(), 32, Lng.str("Enter Password"), Lng.str("Enter the channel password to join this channel\n%s", (ChannelRouter.allowAdminClient(getState().getPlayer()) ? a : ""))) {

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
							to.sendJoinRequestToServer(getState().getPlayer().getClientChannel(), entry);
							return true;
						}
					};
					p.setMinimumLength(0);
					p.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(423);
				} else {
					to.sendJoinRequestToServer(getState().getPlayer().getClientChannel(), "");
				}
			}
		}
		mainPanel.openChat(to);
	}

	private class ChatRow extends Row {

		public ChatRow(InputState state, ChatChannel f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#createContext()
		 */
		@Override
		protected GUIContextPane createContext() {
			if (((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.getBoolean()) {
				GUIContextPane p = new GUIContextPane(getState(), 140, 25);
				GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 1, 1, p);
				buttons.onInit();
				buttons.addButton(0, 0, Lng.str("DELETE CHANNEL"), HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
							AudioController.fireAudioEventID(424);
							deleteChannel(f);
							getState().getController().getInputController().setCurrentContextPane(null);
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}, new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return f instanceof PublicChannel;
					}
				});
				p.attach(buttons);
				return p;
			} else {
				return null;
			}
		}
	}
}
