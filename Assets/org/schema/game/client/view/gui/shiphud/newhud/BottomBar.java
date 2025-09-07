package org.schema.game.client.view.gui.shiphud.newhud;

import org.schema.schine.input.InputState;

public abstract class BottomBar extends HudConfig {


	public BottomBar(InputState state) {
		super(state);
	}

	public abstract int getStartIconX();

	public abstract int getStartIconY();

	public abstract int getIconSpacing();
}
