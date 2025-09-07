package org.schema.game.client.view.gui.faction;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelationOffer;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;
import java.util.ArrayList;

public class FactionOfferAcceptPanel extends GUIInputPanel implements GUIChangeListener {

	private GUIScrollablePanel scrollPanel;

	private GUIElementList list;

	private boolean needsUpdate = true;

	public FactionOfferAcceptPanel(InputState state, GUICallback guiCallback) {
		super("FactionOfferAcceptPanel", state, guiCallback, Lng.str("Faction Relationship Offers"), "");
		((GameClientState) getState()).getPlayer().getFactionController().deleteObserver(this);
		((GameClientState) getState()).getPlayer().getFactionController().addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#draw()
	 */
	@Override
	public void draw() {
		super.draw();
		if(needsUpdate) {
			updateList();
			needsUpdate = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		scrollPanel = new GUIScrollablePanel(410, 110, getState());
		list = new GUIElementList(getState());
		scrollPanel.setContent(list);
		this.getContent().attach(scrollPanel);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof FactionRelationEditPanel;
	}

	@Override
	public void onChange(boolean updateListDim) {
		needsUpdate = true;
	}

	private void updateList() {
		list.clear();
		int i = 0;
		System.err.println("[GUI] UPDATING FACTION OFFER LIST: " + ((GameClientState) getState()).getPlayer().getFactionController().getRelationshipInOffers().size());
		for(FactionRelationOffer f : ((GameClientState) getState()).getPlayer().getFactionController().getRelationshipInOffers()) {
			FactionOfferListElement factionOfferListElement = new FactionOfferListElement(getState(), f, i);
			factionOfferListElement.onInit();
			list.add(factionOfferListElement);
			i++;
		}
	}

	private class FactionOfferListElement extends GUIListElement {

		GUIColoredRectangle bg;

		private FactionRelationOffer offer;

		public FactionOfferListElement(InputState state, FactionRelationOffer offer, int index) {
			super(state);
			this.offer = offer;
			bg = new GUIColoredRectangle(getState(), 410, 100, index % 2 == 0 ? new Vector4f(0.1f, 0.1f, 0.1f, 1) : new Vector4f(0.2f, 0.2f, 0.2f, 1));
			setContent(bg);
			setSelectContent(bg);
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.GUIListElement#onInit()
		 */
		@Override
		public void onInit() {
			super.onInit();
			GUITextOverlay info = new GUITextOverlay(getState());
			info.setText(new ArrayList());

			Faction from = ((GameClientState) getState()).getFactionManager().getFaction(offer.a);
			final Faction to = ((GameClientState) getState()).getFactionManager().getFaction(offer.b);
			if(from != null && to != null) {
				if(offer.isEnemy()) {
					info.getText().add(Lng.str("WAR DECLARATION"));
				} else if(offer.isFriend()) {
					info.getText().add(Lng.str("ALLIANCE PROPOSAL"));
				} else if(offer.isNeutral()) {
					info.getText().add(Lng.str("PEACE OFFER"));
				}

				//Todo: For a future faction rework, these relationships should be more dynamic and handled inside each relationship class rather then all in here

				info.getText().add(Lng.str("From: ") + "[" + from.getName() + "]" + offer.getInitiator());
				String[] split = offer.getMessage().split("\\\\n");
				for(String s : split) info.getText().add(s);
				GUITextButton accept = new GUITextButton(getState(), 80, 18, Lng.str("Accept"), new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(451);
							((GameClientState) getState()).getFactionManager().sendRelationshipAccept(((GameClientState) getState()).getPlayerName(), offer, true);
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				});
				GUITextButton decline = new GUITextButton(getState(), 80, 18, Lng.str("Decline"), new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(452);
							((GameClientState) getState()).getFactionManager().sendRelationshipAccept(((GameClientState) getState()).getPlayerName(), offer, false);
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				});
				accept.getPos().x = 220;
				decline.getPos().x = 310;
				bg.attach(info);
				bg.attach(accept);
				bg.attach(decline);
				System.err.println("[GUI] attached faction offer! " + from.getName() + " -> " + to.getName());
			} else
				System.err.println("Invalid offer: " + offer.a + " / " + offer.b);
			}
		}
}
