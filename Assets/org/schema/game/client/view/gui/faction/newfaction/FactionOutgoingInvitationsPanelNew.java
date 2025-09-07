package org.schema.game.client.view.gui.faction.newfaction;

import java.util.Date;

import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionInvite;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.sound.controller.AudioController;

public class FactionOutgoingInvitationsPanelNew extends FactionInvitationsAbstractScrollListNew {

	public FactionOutgoingInvitationsPanelNew(GUIElement dep, ClientState state) {
		super(state, dep);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof FactionOutgoingInvitationsPanelNew;
	}

	@Override
	protected void updateInvitationList(GUIElementList list) {
		int i = 0;
		list.clear();
		for (FactionInvite f : ((GameClientState) getState()).getPlayer().getFactionController().getInvitesOutgoing()) {
			// System.err.println("[GUI[ ADDING OUT INVITATION: "+f);
			list.add(new IncomingInvitationListElement(getState(), f, i));
			i++;
		}
	}

	private class IncomingInvitationListElement extends GUIListElement implements GUICallback {

		GUIColoredRectangle bg;

		private FactionInvite invite;

		public IncomingInvitationListElement(InputState state, FactionInvite invite, int index) {
			super(state);
			this.invite = invite;
			bg = new GUIColoredRectangle(getState(), 410, 45, index % 2 == 0 ? new Vector4f(0.1f, 0.1f, 0.1f, 1f) : new Vector4f(0.2f, 0.2f, 0.2f, 2f));
			bg.renderMode = RENDER_MODE_SHADOW;
			setContent(bg);
			setSelectContent(bg);
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.GUIListElement#draw()
		 */
		@Override
		public void draw() {
			bg.setWidth(dependent.getWidth());
			super.draw();
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.GUIListElement#onInit()
		 */
		@Override
		public void onInit() {
			GUITextOverlayTable info = new GUITextOverlayTable(getState());
			info.setTextSimple((new Date(invite.getDate())).toString());
			Faction f = ((GameClientState) getState()).getFactionManager().getFaction(invite.getFactionUID());
			info.getText().add("to: " + invite.getToPlayerName());
			info.getText().add(f != null ? f.getName() : "(ERROR)unknown");
			GUITextButton decline = new GUITextButton(getState(), 50, 20, "Revoke", this);
			decline.setUserPointer("REVOKE");
			bg.attach(info);
			bg.attach(decline);
			decline.getPos().x = 300;
			super.onInit();
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			if (event.pressedLeftMouse()) {
				if ("REVOKE".equals(callingGuiElement.getUserPointer())) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(477);
					((GameClientState) getState()).getFactionManager().removeFactionInvitationClient(invite);
				}
			}
		}

		@Override
		public boolean isOccluded() {
			return false;
		}
	}
}
