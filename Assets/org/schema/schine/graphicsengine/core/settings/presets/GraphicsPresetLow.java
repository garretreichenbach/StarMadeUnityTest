package org.schema.schine.graphicsengine.core.settings.presets;

import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.ShadowQuality;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

public class GraphicsPresetLow extends EngineSettingsPreset {

	public GraphicsPresetLow() {
		super("GRAPHICS_LOW");
	}

	@Override
	public String getName() {
		return Lng.str("Low");
	}

	@Override
	public void init() {
		addSetting(EngineSettings.F_FRAME_BUFFER, true);

		addSetting(EngineSettings.G_MULTI_SAMPLE, 0);
		addSetting(EngineSettings.G_DRAW_SURROUNDING_GALAXIES_IN_MAP, false);

		addSetting(EngineSettings.D_LIFETIME_NORM, 0); // debris
		addSetting(EngineSettings.G_DEBRIS_THRESHOLD_SLOW_MS, 1); //debris slowdown

		addSetting(EngineSettings.G_TEXTURE_PACK_RESOLUTION, 128); // texture quality
		addSetting(EngineSettings.G_NORMAL_MAPPING, false); // shadow
		addSetting(EngineSettings.G_SHADOW_QUALITY, ShadowQuality.SIMPLE); // shadow quality
		addSetting(EngineSettings.G_PROD_BG, true); // procedural
		addSetting(EngineSettings.G_PROD_BG_QUALITY, 1024); // quality procedural background

		addSetting(EngineSettings.F_BLOOM, false); // bloom

		addSetting(EngineSettings.G_STAR_COUNT, 2048); // star count

		addSetting(EngineSettings.G_DRAW_EXHAUST_PLUMES, true); // exhaust plums

		addSetting(EngineSettings.G_USE_VERTEX_LIGHTING_ONLY, true); // vertex lighting

		addSetting(EngineSettings.LIGHT_RAY_COUNT, 32); // ray count for block baked light

		addSetting(EngineSettings.G_MAX_MISSILE_TRAILS, 32); // max missile trails

		addSetting(EngineSettings.G_MAX_SEGMENTSDRAWN, 500); // max segments

		addSetting(EngineSettings.G_MAX_BEAMS, 128); // max segments

		addSetting(EngineSettings.G_BASIC_SELECTION_BOX, false); // max segments

		addSetting(EngineSettings.LOD_DISTANCE_IN_THRESHOLD, 25f); // lod distance

		addSetting(EngineSettings.CREATE_MANAGER_MESHES, true); // outline effects

		addSetting(EngineSettings.LOD_DISTANCE_IN_THRESHOLD, 30f); // lod distance
	}
}
