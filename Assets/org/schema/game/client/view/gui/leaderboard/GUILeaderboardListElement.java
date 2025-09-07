package org.schema.game.client.view.gui.leaderboard;

import java.util.Map.Entry;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.gamemode.battle.KillerEntity;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIEnterableList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.network.client.ClientState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUILeaderboardListElement extends GUIListElement {

	private final Entry<String, ObjectArrayList<KillerEntity>> entry;

	public GUILeaderboardListElement(GUIEnterableList content,
	                                 GUIEnterableList selectedContent, ClientState state, Entry<String, ObjectArrayList<KillerEntity>> f) {
		super(content, selectedContent, state);
		this.entry = f;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIListElement#getContent()
	 */
	@Override
	public GUIEnterableList getContent() {
		return (GUIEnterableList) super.getContent();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIListElement#setContent(org.schema.schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void setContent(GUIElement content) {
		assert (content instanceof GUIEnterableList);
		super.setContent(content);
	}

	/**
	 * @return the faction
	 */
	public Entry<String, ObjectArrayList<KillerEntity>> getEntry() {
		return entry;
	}

	public int getDeaths() {
		return ((GameClientState) getState()).getGameState().getClientDeadCount().getInt(entry.getKey());
	}

}
