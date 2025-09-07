package org.schema.game.client.view.gui.ai;

import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIEnterableList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.input.InputState;

public class AiInterfaceEnterableList extends GUIEnterableList {

	private GUIColoredRectangle p;

	public AiInterfaceEnterableList(InputState state, GUIChangeListener topPanel, AiInterfaceContainer f, boolean showAdminSettings, int initialIndex) {
		super(state);

		p = new GUIColoredRectangle(getState(), 510, 80,
				initialIndex % 2 == 0 ? new Vector4f(0.0f, 0.0f, 0.0f, 0.0f) : new Vector4f(0.1f, 0.1f, 0.1f, 0.5f));

		AiInterfaceExtendedPanel pt = new AiInterfaceExtendedPanel(getState(), f, showAdminSettings);
		pt.onInit();

		p.attach(pt);
		this.list.add(new GUIListElement(p, p, getState()));
		//		this.list.setScrollPane(topPanel.getScrollPanel());

		collapsedButton = new AiInterfaceEnterableTop(getState(), f, "+", initialIndex);
		backButton = new AiInterfaceEnterableTop(getState(), f, "-", initialIndex);
		collapsedButton.onInit();
		backButton.onInit();

		onInit();
		addObserver(topPanel);
	}

	@Override
	protected boolean canClick() {
		return ((GameClientState) getState()).getPlayerInputs().isEmpty();
	}

	public void updateIndex(int i) {
		p.getColor().set(i % 2 == 0 ? new Vector4f(0.0f, 0.0f, 0.0f, 0.0f) : new Vector4f(0.1f, 0.1f, 0.1f, 0.5f));
		((AiInterfaceEnterableTop) collapsedButton).setIndex(i);
		((AiInterfaceEnterableTop) backButton).setIndex(i);
	}
}
