package org.schema.schine.graphicsengine.core.settings.presets;

import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.ShadowQuality;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

public class GraphicsPresetCompatibilityLow extends EngineSettingsPreset{

	public GraphicsPresetCompatibilityLow() {
		super("GRAPHICS_COMPATIBILITY_LOW");
	}

	@Override
	public String getName() {
		return Lng.str("Compatibility Mode Low");
	}

	@Override
	public void init() {
		addSetting(EngineSettings.F_FRAME_BUFFER, false);
		
		addSetting(EngineSettings.G_MULTI_SAMPLE, 0);
		addSetting(EngineSettings.G_DRAW_SURROUNDING_GALAXIES_IN_MAP, false);
		
		
		addSetting(EngineSettings.D_LIFETIME_NORM, 0); // debris
		addSetting(EngineSettings.G_DEBRIS_THRESHOLD_SLOW_MS, 1); //debris slowdown
		
		
		addSetting(EngineSettings.G_TEXTURE_PACK_RESOLUTION, 64); // texture quality
		addSetting(EngineSettings.G_NORMAL_MAPPING, false); // shadow
		addSetting(EngineSettings.G_SHADOW_QUALITY, ShadowQuality.OFF); // shadow quality
		addSetting(EngineSettings.G_PROD_BG, false); // procedural
		addSetting(EngineSettings.G_PROD_BG_QUALITY, 1024); // quality procedural background

		addSetting(EngineSettings.F_BLOOM, false); // bloom
		
		addSetting(EngineSettings.G_STAR_COUNT, 512); // star count
		
		addSetting(EngineSettings.G_DRAW_EXHAUST_PLUMES, false); // exhaust plums

		addSetting(EngineSettings.G_USE_VERTEX_LIGHTING_ONLY, true); // vertex lighting

		addSetting(EngineSettings.LIGHT_RAY_COUNT, 16); // ray count for block baked light

		addSetting(EngineSettings.G_MAX_MISSILE_TRAILS, 8); // max missile trails

		addSetting(EngineSettings.G_MAX_SEGMENTSDRAWN, 250); // max segments

		addSetting(EngineSettings.G_MAX_BEAMS, 64); // max segments

		addSetting(EngineSettings.G_BASIC_SELECTION_BOX, true); // max segments

		addSetting(EngineSettings.LOD_DISTANCE_IN_THRESHOLD, 10f); // lod distance
		
		addSetting(EngineSettings.CREATE_MANAGER_MESHES, false); // outline effects
		
		
		
		
	}

}
