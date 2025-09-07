package org.schema.game.client.view.gui.leaderboard;

import java.util.Map.Entry;

import org.schema.game.common.data.gamemode.battle.KillerEntity;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUILearderboardElementList extends GUIElementList {

	private final Entry<String, ObjectArrayList<KillerEntity>> entry;

	public GUILearderboardElementList(InputState state, final Entry<String, ObjectArrayList<KillerEntity>> entry) {
		super(state);
		this.entry = entry;
	}

	/**
	 * @return the faction
	 */
	public Entry<String, ObjectArrayList<KillerEntity>> getEntry() {
		return entry;
	}

}
