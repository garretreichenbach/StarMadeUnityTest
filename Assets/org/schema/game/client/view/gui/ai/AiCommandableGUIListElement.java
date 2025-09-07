package org.schema.game.client.view.gui.ai;

import java.util.Comparator;

import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.input.InputState;

public class AiCommandableGUIListElement extends GUIListElement implements Comparable<AiCommandableGUIListElement> {

	private final AiInterfaceContainer aiInterface;
	Comparator<AiCommandableGUIListElement> comperator;

	public AiCommandableGUIListElement(AiInterfaceContainer aiInterface, GUIChangeListener topPanel, boolean showAdmin, InputState state, int index, boolean expanded) {
		super(state);
		AiInterfaceEnterableList createContent = createContent(topPanel, aiInterface, showAdmin, index);
		setContent(createContent);
		setSelectContent(createContent);
		createContent.setExpanded(expanded);
		this.aiInterface = aiInterface;
	}

	private AiInterfaceEnterableList createContent(GUIChangeListener topPanel, AiInterfaceContainer aiInterface, boolean showAdmin, int index) {
		return new AiInterfaceEnterableList(getState(), topPanel, aiInterface, showAdmin, index);
	}

	@Override
	public int compareTo(AiCommandableGUIListElement other) {
		return comperator.compare(this, other);
	}

	/**
	 * @return the aiInterface
	 */
	public AiInterfaceContainer getAiInterface() {
		return aiInterface;
	}

}

