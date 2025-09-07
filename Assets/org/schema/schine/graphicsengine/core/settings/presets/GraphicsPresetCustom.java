package org.schema.schine.graphicsengine.core.settings.presets;

import org.schema.schine.common.language.Lng;

public class GraphicsPresetCustom extends EngineSettingsPreset{

	public GraphicsPresetCustom() {
		super("GRAPHICS_CUSTOM");
	}

	@Override
	public String getName() {
		return Lng.str("Custom");
	}

	@Override
	public void init() {
	}

}
