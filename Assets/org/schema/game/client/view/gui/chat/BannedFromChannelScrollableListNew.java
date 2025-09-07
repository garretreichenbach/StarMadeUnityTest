package org.schema.game.client.view.gui.chat;

import com.bulletphysics.util.ObjectArrayList;
import org.schema.game.client.controller.SendableAddedRemovedListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChatCallback;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.sound.controller.AudioController;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class BannedFromChannelScrollableListNew extends ScrollableTableList<String> implements SendableAddedRemovedListener {

	public static final int AVAILABLE = 0;

	public static final int PERSONAL = 1;

	public static final int ADMIN = 2;

	private ChatCallback cb;

	public BannedFromChannelScrollableListNew(InputState state, GUIElement p, ChatCallback cb, ChatPanel mainPanel) {
		super(state, 100, 100, p);
		this.cb = cb;
		((GameClientState) state).getController().addSendableAddedRemovedListener(this);
		((GUIObservable) cb).addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientState) getState()).getController().removeSendableAddedRemovedListener(this);
		((GUIObservable) cb).deleteObserver(this);
		super.cleanUp();
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
		String[] banned = cb.getBanned();
		for (String s : banned) {
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
			GUITextButton unban = new GUITextButton(getState(), 48, 22, ColorPalette.CANCEL, Lng.str("UNBAN"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(413);
						cb.requestBanUnbanOnClient(f, false);
					}
				}

				@Override
				public boolean isOccluded() {
					return !BannedFromChannelScrollableListNew.this.isActive();
				}
			});
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			ChannelMemberRow r = new ChannelMemberRow(getState(), f, nameText, unban);
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
			// GUITextButton kickButton = new GUITextButton(getState(), 28, 24, ColorPalette.CANCEL, "KICK", new GUICallback() {
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
			// GUITextButton banButton = new GUITextButton(getState(), 28, 24, ColorPalette.OK, "BAN", new GUICallback() {
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
		}
		mainList.updateDim();
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

	private class ChannelMemberRow extends Row {

		public ChannelMemberRow(InputState state, String f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}

	@Override
	public void onAddedSendable(Sendable s) {
		onChange(true);
	}

	@Override
	public void onRemovedSendable(Sendable s) {
		onChange(true);
	}
}
