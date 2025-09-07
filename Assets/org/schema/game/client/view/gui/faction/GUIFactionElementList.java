package org.schema.game.client.view.gui.faction;

import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.input.InputState;

public class GUIFactionElementList extends GUIElementList {

	private final Faction faction;

	public GUIFactionElementList(InputState state, Faction f) {
		super(state);
		this.faction = f;
	}

	/**
	 * @return the faction
	 */
	public Faction getFaction() {
		return faction;
	}

}
