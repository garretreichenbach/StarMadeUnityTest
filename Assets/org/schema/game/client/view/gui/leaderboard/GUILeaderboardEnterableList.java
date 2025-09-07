package org.schema.game.client.view.gui.leaderboard;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIEnterableList;
import org.schema.schine.input.InputState;

public class GUILeaderboardEnterableList extends GUIEnterableList {
	private final GUIColoredRectangle p;

	public GUILeaderboardEnterableList(InputState state, GUIElementList list,
	                                   GUIElement collapsedButton, GUIElement backButton, GUIColoredRectangle p) {
		super(state, list, collapsedButton, backButton);
		this.p = p;
	}

	public void updateIndex(int i) {
		p.getColor().set(i % 2 == 0 ? new Vector4f(0.0f, 0.0f, 0.0f, 0.0f) : new Vector4f(0.1f, 0.1f, 0.1f, 0.5f));
		((GUILoaderboardElement) collapsedButton).setIndex(i);
		((GUILoaderboardElement) backButton).setIndex(i);
	}
}
