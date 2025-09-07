package org.schema.game.client.view.gui.mail;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.PlayerMailInputNew;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.playermessage.PlayerMessage;
import org.schema.game.common.data.player.playermessage.PlayerMessageController;
import org.schema.game.network.objects.remote.RemotePlayerMessage;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIEnterableListOnExtendedCallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableInnerDescription;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.GuiDateFormats;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIMailScrollableList extends ScrollableTableList<PlayerMessage> implements DrawerObserver {

	private PlayerMessageController messageController;

	public GUIMailScrollableList(InputState state, GUIElement p) {
		super(state, 100, 100, p);
		messageController = ((GameClientState) getState()).getController().getClientChannel().getPlayerMessageController();
		messageController.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		messageController.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("New"), 0.1f, (o1, o2) -> ((Boolean) o1.isRead()).compareTo(o2.isRead()));
		addColumn(Lng.str("From"), 5, (o1, o2) -> o1.getFrom().compareToIgnoreCase(o2.getFrom()));
		addColumn(Lng.str("Topic"), 9, (o1, o2) -> o1.getTopic().compareToIgnoreCase(o2.getTopic()));
		addColumn(Lng.str("Date"), 4, (o1, o2) -> o1.getSent() > o2.getSent() ? 1 : (o1.getSent() < o2.getSent() ? -1 : 0), true);
		addTextFilter(new GUIListFilterText<PlayerMessage>() {

			@Override
			public boolean isOk(String input, PlayerMessage listElement) {
				return listElement.getFrom().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY SENDER"), FilterRowStyle.LEFT);
		addTextFilter(new GUIListFilterText<PlayerMessage>() {

			@Override
			public boolean isOk(String input, PlayerMessage listElement) {
				return listElement.getTopic().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY TOPIC"), FilterRowStyle.RIGHT);
		addTextFilter(new GUIListFilterText<PlayerMessage>() {

			@Override
			public boolean isOk(String input, PlayerMessage listElement) {
				return listElement.getMessage().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY MESSAGE"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<PlayerMessage> getElementList() {
		return messageController.messagesReceived;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<PlayerMessage> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final PlayerMessage f : collection) {
			GUITextOverlayTable newText = new GUITextOverlayTable(getState());
			GUITextOverlayTable senderText = new GUITextOverlayTable(getState());
			GUITextOverlayTable topicText = new GUITextOverlayTable(getState());
			GUITextOverlayTable dateText = new GUITextOverlayTable(getState());
			newText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.isRead() ? "" : Lng.str("NEW");
				}
			});
			senderText.setTextSimple(f.getFrom());
			topicText.setTextSimple(f.getTopic());
			dateText.setTextSimple(GuiDateFormats.mailTime.format(f.getSent()));
			GUIClippedRow senderAnchorP = new GUIClippedRow(getState());
			senderAnchorP.attach(senderText);
			GUIClippedRow topicAnchorP = new GUIClippedRow(getState());
			topicAnchorP.attach(topicText);
			newText.getPos().y = 5;
			senderText.getPos().y = 5;
			topicText.getPos().y = 5;
			dateText.getPos().y = 5;
			final PlayerMessageRow r = new PlayerMessageRow(getState(), f, newText, senderAnchorP, topicAnchorP, dateText);
			r.expanded = new GUIElementList(getState());
			GUITextOverlayTableInnerDescription msg = new GUITextOverlayTableInnerDescription(10, 10, getState());
			msg.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getMessage();
				}
			});
			msg.setPos(4, 2, 0);
			GUIAnchor c = new GUIAnchor(getState(), 100, 100) {

				@Override
				public void draw() {
					setWidth(r.l.getInnerTextbox().getWidth());
					super.draw();
				}
			};
			GUITextButton replyButton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, Lng.str("REPLY"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(543);
						int reCount = 0;
						String re = f.getTopic();
						while (!re.startsWith("RE: ")) {
							re = "RE: " + re;
						}
						PlayerMailInputNew mailInput = new PlayerMailInputNew((GameClientState) getState(), f.getFrom(), re);
						mailInput.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(544);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton deleteButton = new GUITextButton(getState(), 80, 24, ColorPalette.CANCEL, Lng.str("DELETE"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
						AudioController.fireAudioEventID(545);
						messageController.clientDelete(f);
					}
				}
			});
			replyButton.setPos(4, c.getHeight(), 0);
			deleteButton.setPos(4 + replyButton.getWidth() + 10, c.getHeight(), 0);
			GUIScrollablePanel p = new GUIScrollablePanel(10, 10, c, getState());
			p.setContent(msg);
			c.attach(p);
			c.attach(replyButton);
			c.attach(deleteButton);
			r.expanded.add(new GUIListElement(c, c, getState()));
			r.onExpanded = new GUIEnterableListOnExtendedCallback() {

				@Override
				public void extended() {
					if (!f.isRead()) {
						f.setRead(true);
						((GameClientState) getState()).getController().getClientChannel().getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(f, false));
					}
				}

				@Override
				public void collapsed() {
				}
			};
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(PlayerMessage e) {
		return e.isDeleted() || super.isFiltered(e);
	}

	@Override
	public void update(DrawerObservable observer, Object userdata, Object message) {
		flagDirty();
	}

	private class PlayerMessageRow extends Row {

		public PlayerMessageRow(InputState state, PlayerMessage f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
