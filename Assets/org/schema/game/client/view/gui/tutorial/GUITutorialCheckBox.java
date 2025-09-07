package org.schema.game.client.view.gui.tutorial;

import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIEngineSettingsCheckBox;
import org.schema.schine.input.InputState;

public class GUITutorialCheckBox extends GUIEngineSettingsCheckBox {

	public GUITutorialCheckBox(InputState state) {
		super(state, null, EngineSettings.TUTORIAL_NEW);
	}

}
