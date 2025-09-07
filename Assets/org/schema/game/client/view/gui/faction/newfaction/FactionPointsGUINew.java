package org.schema.game.client.view.gui.faction.newfaction;

import org.schema.game.client.controller.manager.ingame.faction.FactionRolesDialogNew;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRoles;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.input.InputState;

public class FactionPointsGUINew extends GUIInputPanel {

	private Faction faction;
	private FactionRolesScrollableListNew sc;

	public FactionPointsGUINew(InputState state,
	                           FactionRolesDialogNew d, Faction faction) {
		super("FactionPointsGUINew", state, 300, 450, d, Lng.str("Edit Roles"), "");
		this.faction = faction;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		sc = new FactionRolesScrollableListNew(getState(), ((GUIDialogWindow) background).getMainContentPane().getContent(0), faction);
		sc.onInit();
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(sc);

	}

	public FactionRoles getRoles() {
		return sc.getRoles();
	}

}
