package org.schema.schine.graphicsengine.core.settings.presets;

import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.ShadowQuality;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

public class GraphicsPresetVeryHigh extends EngineSettingsPreset{

	public GraphicsPresetVeryHigh() {
		super("GRAPHICS_VERY_HIGH");
	}

	@Override
	public String getName() {
		return Lng.str("Very High");
	}

	@Override
	public void init() {
		addSetting(EngineSettings.F_FRAME_BUFFER, true);
		
		addSetting(EngineSettings.G_MULTI_SAMPLE, 4);
		addSetting(EngineSettings.G_DRAW_SURROUNDING_GALAXIES_IN_MAP, false);
		
		
		addSetting(EngineSettings.D_LIFETIME_NORM, 30); // debris
		addSetting(EngineSettings.G_DEBRIS_THRESHOLD_SLOW_MS, 2); //debris slowdown
		
		
		addSetting(EngineSettings.G_TEXTURE_PACK_RESOLUTION, 256); // texture quality
		addSetting(EngineSettings.G_NORMAL_MAPPING, true); // shadow
		addSetting(EngineSettings.G_SHADOW_QUALITY, ShadowQuality.ULTRA); // shadow quality
		addSetting(EngineSettings.G_PROD_BG, true); // procedural
		addSetting(EngineSettings.G_PROD_BG_QUALITY, 4096); // quality procedural background

		addSetting(EngineSettings.F_BLOOM, true); // bloom
		
		addSetting(EngineSettings.G_STAR_COUNT, 32768); // star count
		
		addSetting(EngineSettings.G_DRAW_EXHAUST_PLUMES, true); // exhaust plums

		addSetting(EngineSettings.G_USE_VERTEX_LIGHTING_ONLY, false); // vertex lighting

		addSetting(EngineSettings.LIGHT_RAY_COUNT, 64); // ray count for block baked light

		addSetting(EngineSettings.G_MAX_MISSILE_TRAILS, 512); // max missile trails

		addSetting(EngineSettings.G_MAX_SEGMENTSDRAWN, 16000); // max segments

		addSetting(EngineSettings.G_MAX_BEAMS, 2048); // max segments

		addSetting(EngineSettings.G_BASIC_SELECTION_BOX, false); // max segments

		addSetting(EngineSettings.LOD_DISTANCE_IN_THRESHOLD, 100f); // lod distance

		addSetting(EngineSettings.CREATE_MANAGER_MESHES, true); // outline effects
		
	}

}
