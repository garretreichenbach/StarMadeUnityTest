package org.schema.game.client.view.gui.faction;

import org.schema.game.client.controller.manager.ingame.faction.FactionRelationDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.faction.FactionRelationOffer;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class FactionRelationEditPanel extends GUIInputPanel {
	private final Faction from;
	private final Faction to;
	private final FactionRelationDialog dialog;

	public FactionRelationEditPanel(InputState state, Faction from, Faction to, FactionRelationDialog dialog) {
		super("FACTION_RELATION_EDIT_PANEL", state, dialog, Lng.str("Relationship to %s", to.getName()), "");
		this.from = from;
		this.to = to;
		this.dialog = dialog;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {

		super.onInit();

		GUITextOverlay current = new GUITextOverlay(getState());

		RType relation = ((GameClientState) getState()).getFactionManager()
				.getRelation(from.getIdFaction(), to.getIdFaction());

		GUITextButton declareWar = new GUITextButton(getState(), 100, 25, ColorPalette.HOSTILE, Lng.str("Declare War"), dialog);
		GUITextButton offerPeace = new GUITextButton(getState(), 100, 25, ColorPalette.NEUTRAL, Lng.str("Offer Peace"), dialog);
		GUITextButton offerAlly = new GUITextButton(getState(), 100, 25, ColorPalette.FRIENDLY, Lng.str("Offer Alliance"), dialog);

		offerPeace.getPos().y = 15;
		declareWar.getPos().y = 45;
		offerAlly.getPos().y = 75;

		GUITextButton offerPeaceRevoke = new GUITextButton(getState(), 150, 25, ColorPalette.HOSTILE, Lng.str("Revoke Peace Offer"), dialog);
		GUITextButton offerAllyRevoke = new GUITextButton(getState(), 150, 25, ColorPalette.NEUTRAL, Lng.str("Revoke Alliance Offer"), dialog);
		GUITextButton revokeAlly = new GUITextButton(getState(), 150, 25, ColorPalette.NEUTRAL, Lng.str("Revoke Alliance"), dialog);

		offerPeaceRevoke.getPos().x = 100;
		offerAllyRevoke.getPos().x = 100;
		offerPeaceRevoke.getPos().y = 45;
		offerAllyRevoke.getPos().y = 75;
		revokeAlly.getPos().y = 75;

		switch (relation) {
			case ENEMY:

				current.setTextSimple(Lng.str("You are in war with %s", to.getName()));
				FactionRelationOffer offerPeaceRel = new FactionRelationOffer();
				offerPeaceRel.a = from.getIdFaction();
				offerPeaceRel.b = to.getIdFaction();
				offerPeaceRel.rel = RType.NEUTRAL.code;
				if (((GameClientState) getState()).getPlayer().getFactionController().getRelationshipOutOffers().contains(offerPeaceRel)) {
					getContent().attach(offerPeaceRevoke);
					getContent().attach(offerAlly);
				} else {
					getContent().attach(offerPeace);
					getContent().attach(offerAlly);
				}

				break;
			case FRIEND:
				current.setTextSimple(Lng.str("You are allied to %s", to.getName()));

				FactionRelationOffer offerAllyRel = new FactionRelationOffer();
				offerAllyRel.a = from.getIdFaction();
				offerAllyRel.b = to.getIdFaction();
				offerAllyRel.rel = RType.FRIEND.code;
				if (((GameClientState) getState()).getPlayer().getFactionController().getRelationshipOutOffers().contains(offerAllyRel)) {
					getContent().attach(declareWar);
					getContent().attach(revokeAlly);
				} else {
					getContent().attach(declareWar);
					getContent().attach(revokeAlly);
				}
				break;
			case NEUTRAL:
				current.setTextSimple(Lng.str("You are neutral to %s", to.getName()));

				FactionRelationOffer offerAllyRelN = new FactionRelationOffer();
				offerAllyRelN.a = from.getIdFaction();
				offerAllyRelN.b = to.getIdFaction();
				offerAllyRelN.rel = RType.FRIEND.code;
				if (((GameClientState) getState()).getPlayer().getFactionController().getRelationshipOutOffers().contains(offerAllyRelN)) {
					getContent().attach(declareWar);
					getContent().attach(offerAllyRevoke);
				} else {
					getContent().attach(declareWar);
					getContent().attach(offerAlly);
				}

				break;
			default:
				assert (false);
		}
		getContent().attach(current);
		declareWar.setUserPointer("WAR");
		offerPeace.setUserPointer("PEACE");
		offerAlly.setUserPointer("ALLY");

		revokeAlly.setUserPointer("ALLY_REVOKE");
		offerAllyRevoke.setUserPointer("ALLY_OFFER_REVOKE");
		offerPeaceRevoke.setUserPointer("PEACE_OFFER_REVOKE");

	}

}
