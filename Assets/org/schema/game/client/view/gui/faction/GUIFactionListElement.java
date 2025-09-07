package org.schema.game.client.view.gui.faction;

import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIEnterableList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.network.client.ClientState;

public class GUIFactionListElement extends GUIListElement {

	private final Faction faction;

	public GUIFactionListElement(GUIEnterableList content,
	                             GUIEnterableList selectedContent, ClientState state, Faction f) {
		super(content, selectedContent, state);
		this.faction = f;
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
	public Faction getFaction() {
		return faction;
	}

}
