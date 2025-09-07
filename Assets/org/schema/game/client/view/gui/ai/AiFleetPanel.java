package org.schema.game.client.view.gui.ai;

import java.util.Collection;
import java.util.List;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.input.InputState;

public class AiFleetPanel extends GUIAnchor {

	private ScrollableAiInterfaceList scrollableList;

	public AiFleetPanel(InputState state, int width, int height) {
		super(state, width, height);
	}

	protected List<AiInterfaceContainer> getEntries() {
		return ((GameClientState) getState()).getPlayer().getPlayerAiManager().getFleet();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		this.scrollableList = new ScrollableAiInterfaceList(getState(), (int) getWidth(), (int) getHeight(), ((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get()) {

			@Override
			public Collection<AiInterfaceContainer> getEntries() {
				return AiFleetPanel.this.getEntries();
			}

		};
		this.scrollableList.onInit();
		attach(scrollableList);
	}

}
