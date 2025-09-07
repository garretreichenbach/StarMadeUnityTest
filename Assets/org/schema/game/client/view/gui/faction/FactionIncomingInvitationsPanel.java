package org.schema.game.client.view.gui.faction;

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
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class FactionIncomingInvitationsPanel extends FactionInvitationsAbstractScrollList {

	public FactionIncomingInvitationsPanel(float width, float height, InputState state) {
		super(width, height, state);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof FactionIncomingInvitationsPanel;
	}

	@Override
	protected void updateInvitationList(GUIElementList list) {
		int i = 0;
		list.clear();
		System.err.println("[GUI] UPDATING LIST: " + ((GameClientState) getState()).getPlayer().getFactionController().getInvitesIncoming().size());
		for (FactionInvite f : ((GameClientState) getState()).getPlayer().getFactionController().getInvitesIncoming()) {
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
			setContent(bg);
			setSelectContent(bg);
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			if (event.pressedLeftMouse()) {
				if ("ACCEPT".equals(callingGuiElement.getUserPointer())) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(445);
					((GameClientState) getState()).getPlayer().getFactionController().joinFaction(invite.getFactionUID());
				} else if ("DECLINE".equals(callingGuiElement.getUserPointer())) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(444);
					((GameClientState) getState()).getFactionManager().removeFactionInvitationClient(invite);
				}
			}
		}

		@Override
		public boolean isOccluded() {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.GUIListElement#onInit()
		 */
		@Override
		public void onInit() {
			super.onInit();
			GUITextOverlay info = new GUITextOverlay(getState());
			info.setTextSimple((new Date(invite.getDate())).toString());
			Faction f = ((GameClientState) getState()).getFactionManager().getFaction(invite.getFactionUID());
			info.getText().add("from: " + invite.getToPlayerName());
			info.getText().add(f != null ? f.getName() : "(ERROR)unknown");
			GUITextButton decline = new GUITextButton(getState(), 50, 20, "Decline", this);
			decline.setUserPointer("DECLINE");
			GUITextButton accept = new GUITextButton(getState(), 50, 20, "Accept", this);
			accept.setUserPointer("ACCEPT");
			bg.attach(info);
			bg.attach(decline);
			bg.attach(accept);
			accept.getPos().x = 310;
			decline.getPos().x = 250;
		}
	}
}
