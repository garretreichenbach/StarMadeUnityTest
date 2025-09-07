package org.schema.game.client.view.gui.chat;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.SendableAddedRemovedListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChannelRouter;
import org.schema.game.common.data.chat.ChatCallback;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.common.data.chat.DirectChatChannel;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContextPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.sound.controller.AudioController;

public class ChatChannelMembersScrollableListNew extends ScrollableTableList<PlayerState> implements SendableAddedRemovedListener {

	public static final int AVAILABLE = 0;

	public static final int PERSONAL = 1;

	public static final int ADMIN = 2;

	private ChatCallback cb;

	private ChatPanel mainPanel;

	public ChatChannelMembersScrollableListNew(InputState state, GUIElement p, ChatCallback cb, ChatPanel mainPanel) {
		super(state, 100, 100, p);
		this.cb = cb;
		this.mainPanel = mainPanel;
		((GameClientState) state).getController().addSendableAddedRemovedListener(this);
		((GUIObservable) cb).addObserver(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientState) getState()).getController().removeSendableAddedRemovedListener(this);
		((GUIObservable) cb).deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void onAddedSendable(Sendable s) {
		onChange(true);
	}

	@Override
	public void onRemovedSendable(Sendable s) {
		onChange(true);
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 7, (o1, o2) -> {
			String a;
			String b;
			a = modName(o1);
			b = modName(o2);
			return a.compareToIgnoreCase(b);
		});
		addTextFilter(new GUIListFilterText<PlayerState>() {

			@Override
			public boolean isOk(String input, PlayerState listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<PlayerState> getElementList() {
		return cb.getMemberPlayerStates();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<PlayerState> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final PlayerState f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return modName(f);
				}
			});
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			ChannelMemberRow r = new ChannelMemberRow(getState(), f, nameText);
			// r.expanded = new GUIElementList(getState());
			// 
			// GUITextOverlayTable description = new GUITextOverlayTable(10, 10, FontSize.SMALLEST, getState());
			// description.setTextSimple(new Object(){
			// public String toString(){
			// return "";
			// }
			// });
			// description.setPos(4, 2, 0);
			// GUIAncor c = new GUIAncor(getState(), 10, 0);
			// 
			// GUITextButton pmButton = new GUITextButton(getState(), 21, 24, ColorPalette.OK, "PM", new GUICallback() {
			// @Override
			// public boolean isOccluded() { return !isActive(); }
			// @Override
			// public void callback(GUIElement callingGuiElement, MouseEvent event) {
			// if(event.pressedLeftMouse()){
			// System.err.println("Pm");
			// openPMChannel(f);
			// }
			// }
			// });
			// GUITextButton kickButton = new GUITextButton(getState(), 28, 24, ColorPalette.CANCEL, "KICK", new
			// GUICallback() {
			// @Override
			// public boolean isOccluded() { return !isActive(); }
			// @Override
			// public void callback(GUIElement callingGuiElement, MouseEvent event) {
			// 
			// if(event.pressedLeftMouse()){
			// System.err.println("Kick");
			// }
			// }
			// }){
			// 
			// /* (non-Javadoc)
			// * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			// */
			// @Override
			// public void draw() {
			// if(cb.isModerator(player)){
			// super.draw();
			// }
			// }
			// 
			// };
			// GUITextButton banButton = new GUITextButton(getState(), 28, 24, ColorPalette.OK, "BAN", new GUICallback()
			// {
			// @Override
			// public boolean isOccluded() { return !isActive(); }
			// @Override
			// public void callback(GUIElement callingGuiElement, MouseEvent event) {
			// if(event.pressedLeftMouse()){
			// System.err.println("ban");
			// }
			// }
			// }){
			// 
			// /* (non-Javadoc)
			// * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			// */
			// @Override
			// public void draw() {
			// if(cb.isModerator(player)){
			// super.draw();
			// }
			// }
			// 
			// };
			// 
			// 
			// 
			// c.attach(pmButton);
			// c.attach(kickButton);
			// c.attach(banButton);
			// 
			// int inset = 1;
			// 
			// int left = -2;
			// pmButton.setPos(left, c.getHeight(), 0);
			// 
			// kickButton.setPos(left+pmButton.getWidth()+inset, c.getHeight(), 0);
			// 
			// banButton.setPos(left+pmButton.getWidth()+inset+kickButton.getWidth()+inset, c.getHeight(), 0);
			// 
			// 
			// c.attach(description);
			// 
			// r.expanded.add(new GUIListElement(c, c, getState()));
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	public String modName(PlayerState player) {
		return (ChannelRouter.allowAdminClient(player) ? "~" : "") + (cb.isModerator(player) ? "@" + player.getName() : player.getName());
	}

	/*
	 * (non-Javadoc)
	 *
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

	private void openPMChannel(PlayerState to) {
		ChatChannel createClientPMChannel = getState().getChannelRouter().createClientPMChannel(getState().getPlayer(), to);
		if (createClientPMChannel instanceof DirectChatChannel) {
			getState().getPlayer().getPlayerChannelManager().addDirectChannel((createClientPMChannel));
		}
		mainPanel.openChat(createClientPMChannel);
	}

	private class ChannelMemberRow extends Row {

		public ChannelMemberRow(InputState state, PlayerState f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}

		@Override
		protected GUIContextPane createContext() {
			// Start off with private message and ignore
			int rows = 2;
			int row = 0;
			boolean isAdminOrMod = (cb.isModerator(((GameClientState) getState()).getPlayer()) || ChannelRouter.allowAdminClient(((GameClientState) getState()).getPlayer()));
			if (isAdminOrMod && cb.hasChannelBanList() && cb.hasChannelModList()) {
				rows += 3;
			}
			if (isAdminOrMod && cb.hasChannelMuteList()) {
				rows += 1;
			}
			GUIContextPane p = new GUIContextPane(getState(), 140, (rows) * 25);
			GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 1, rows, p);
			buttons.onInit();
			buttons.addButton(0, row++, Lng.str("PRIVATE MESSAGE"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(414);
						openPMChannel(f);
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
					return f != ChatChannelMembersScrollableListNew.this.getState().getPlayer() && !(cb instanceof DirectChatChannel);
				}
			});
			if (rows > 2) {
				if (isAdminOrMod && cb.hasChannelBanList() && cb.hasChannelModList()) {
					buttons.addButton(0, row++, (cb.isModerator(f) ? Lng.str("UNMOD") : Lng.str("MOD")), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(415);
								cb.requestModUnmodOnClient(f.getName(), !cb.isModerator(f));
								getState().getController().getInputController().setCurrentContextPane(null);
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
					buttons.addButton(0, row++, Lng.str("KICK"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(416);
								cb.requestKickOnClient(f);
								getState().getController().getInputController().setCurrentContextPane(null);
							}
						}
					}, new GUIActivationCallback() {

						@Override
						public boolean isVisible(InputState state) {
							return true;
						}

						@Override
						public boolean isActive(InputState state) {
							return f != ChatChannelMembersScrollableListNew.this.getState().getPlayer();
						}
					});
					buttons.addButton(0, row++, Lng.str("BAN"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(417);
								getState().getController().getInputController().setCurrentContextPane(null);
								cb.requestBanUnbanOnClient(f.getName(), !cb.isBanned(f));
							}
						}
					}, new GUIActivationCallback() {

						@Override
						public boolean isVisible(InputState state) {
							return true;
						}

						@Override
						public boolean isActive(InputState state) {
							return f != ChatChannelMembersScrollableListNew.this.getState().getPlayer();
						}
					});
				}
				if (isAdminOrMod && cb.hasChannelMuteList()) {
					buttons.addButton(0, row++, (cb.isMuted(f) ? Lng.str("UNMUTE") : Lng.str("MUTE")), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(418);
								getState().getController().getInputController().setCurrentContextPane(null);
								cb.requestMuteUnmuteOnClient(f.getName(), !cb.isMuted(f));
							}
						}
					}, new GUIActivationCallback() {

						@Override
						public boolean isVisible(InputState state) {
							return true;
						}

						@Override
						public boolean isActive(InputState state) {
							return f != ChatChannelMembersScrollableListNew.this.getState().getPlayer();
						}
					});
				}
			}
			buttons.addButton(0, row++, (ChatChannelMembersScrollableListNew.this.getState().getPlayer().isIgnored(f.getName()) ? Lng.str("UNIGNORE") : Lng.str("IGNORE")), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(419);
						getState().getController().getInputController().setCurrentContextPane(null);
						cb.requestIgnoreUnignoreOnClient(f.getName(), !ChatChannelMembersScrollableListNew.this.getState().getPlayer().isIgnored(f.getName()));
					}
				}
			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return f != ChatChannelMembersScrollableListNew.this.getState().getPlayer() && (!f.getNetworkObject().isAdminClient.get() || ChatChannelMembersScrollableListNew.this.getState().getPlayer().getNetworkObject().isAdminClient.get());
				}
			});
			p.attach(buttons);
			return p;
		}
	}
}
