package org.schema.game.client.view.gui.structurecontrol;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public abstract class ActivateValueEntry implements GUIKeyValueEntry, GUICallback {
	private final Object name;

	public ActivateValueEntry(Object name) {
		super();
		this.name = name;
	}

	@Override
	public GUIAnchor get(GameClientState state) {
		GUITextButton c = new GUITextButton(state, UIScale.getUIScale().scale(200), UIScale.getUIScale().smallButtonHeight, name, this);
		c.setTextPos(4, 0);
		return c;
	}

	@Override
	public boolean isOccluded() {
		return false;
	}

}
