package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.InputState;

public class GUIEngineSettingsCheckBox extends GUICheckBox {

	private SettingsInterface booleanSettings;

	public GUIEngineSettingsCheckBox(InputState state, GUIActiveInterface activeInterface, EngineSettings booleanSettings) {
		super(state);
		this.booleanSettings = booleanSettings.getSettingsForGUI();
		this.activeInterface = activeInterface;
	}

	@Override
	protected void activate() throws StateParameterNotFoundException {
		booleanSettings.setOn(true);
	}

	@Override
	protected void deactivate() throws StateParameterNotFoundException {
		booleanSettings.setOn(false);

	}

	@Override
	protected boolean isActivated() {
		return booleanSettings.isOn();
	}

}
