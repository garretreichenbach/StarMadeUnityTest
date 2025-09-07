package org.schema.game.client.view.gui.structurecontrol;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;

public class EmptyValueEntry implements GUIKeyValueEntry {
	public EmptyValueEntry() {
		super();
	}

	@Override
	public GUIAnchor get(GameClientState state) {
		GUIAnchor c = new GUIAnchor(state);
		return c;
	}
}
