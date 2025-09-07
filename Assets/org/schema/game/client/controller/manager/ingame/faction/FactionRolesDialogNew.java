package org.schema.game.client.controller.manager.ingame.faction;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.faction.newfaction.FactionRoleSettingGUIPlayerInputNew;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;

public class FactionRolesDialogNew extends PlayerInput {

	private final FactionRoleSettingGUIPlayerInputNew panel;
	private boolean sent;
	public FactionRolesDialogNew(GameClientState state, Faction faction) {
		super(state);
		this.panel = new FactionRoleSettingGUIPlayerInputNew(getState(), this, faction);
	}


	@Override
	protected void apply() {
		getState().getController().popupInfoTextMessage(Lng.str("Sending faction roles"), "sndFactionRoles", 0);
		if(!sent) {
			getState().getFactionManager().sendFactionRoles(panel.getRoles());
			sent = true;
		}else {
			assert(false);
			try {
				throw new Exception("Exception: ERROR: TRIED TO SEND FACTION ROLES TWICE");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	}

	@Override
	public GUIElement getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
	}


}
