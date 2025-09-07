package org.schema.game.client.view.gui.chat;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.SendableAddedRemovedListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChatCallback;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.util.ObjectArrayList;

public class IgnoredByPlayerScrollableListNew extends ScrollableTableList<String> implements SendableAddedRemovedListener {

	public static final int AVAILABLE = 0;

	public static final int PERSONAL = 1;

	public static final int ADMIN = 2;

	private ChatCallback cb;

	public IgnoredByPlayerScrollableListNew(InputState state, GUIElement p, ChatCallback cb, ChatPanel mainPanel) {
		super(state, 100, 100, p);
		this.cb = cb;
		((GameClientState) state).getController().addSendableAddedRemovedListener(this);
		((GUIObservable) cb).addObserver(this);
	}

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
		addColumn(Lng.str("Name"), 7, String::compareToIgnoreCase);
		addFixedWidthColumnScaledUI(Lng.str("Option"), 54, String::compareToIgnoreCase);
		addTextFilter(new GUIListFilterText<String>() {

			@Override
			public boolean isOk(String input, String listElement) {
				return listElement.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<String> getElementList() {
		ObjectArrayList<String> o = new ObjectArrayList<String>();
		String[] ignored = ((GameClientState) getState()).getPlayer().getIgnored();
		for (String s : ignored) {
			o.add(s);
		}
		return o;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<String> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		for (final String f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f;
				}
			});
			GUITextButton unmute = new GUITextButton(getState(), 48, 22, ColorPalette.CANCEL, Lng.str("UNIGNORE"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(435);
						cb.requestIgnoreUnignoreOnClient(f, false);
					}
				}

				@Override
				public boolean isOccluded() {
					return !IgnoredByPlayerScrollableListNew.this.isActive();
				}
			});
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			ChannelMemberRow r = new ChannelMemberRow(getState(), f, nameText, unmute);
			r.onInit();
			mainList.addWithoutUpdate(r);
		}
		mainList.updateDim();
	}

	public boolean isPlayerAdmin() {
		return ((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get();
	}

	public boolean canEdit(CatalogPermission f) {
		return true;
	}

	private class ChannelMemberRow extends Row {

		public ChannelMemberRow(InputState state, String f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
