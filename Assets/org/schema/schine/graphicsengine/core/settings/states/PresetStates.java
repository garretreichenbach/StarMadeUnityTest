package org.schema.schine.graphicsengine.core.settings.states;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.core.settings.presets.*;

import java.util.List;
import java.util.Locale;

public class PresetStates {

	
	private static final List<EngineSettingsPreset> stateList = new ObjectArrayList<EngineSettingsPreset>();
	private static final Object2ObjectOpenHashMap<String, EngineSettingsPreset> stateMap = new Object2ObjectOpenHashMap<String, EngineSettingsPreset>();
	
	static{
		register();
	}
	
	public static void register(){
		stateList.add(new GraphicsPresetCompatibilityLow());
		stateList.add(new GraphicsPresetCompatibilityMedium());
		stateList.add(new GraphicsPresetCompatibilityHigh());
		stateList.add(new GraphicsPresetVeryLow());
		stateList.add(new GraphicsPresetLow());
		stateList.add(new GraphicsPresetMedium());
		stateList.add(new GraphicsPresetHigh());
		stateList.add(new GraphicsPresetVeryHigh());
		stateList.add(new GraphicsPresetUltra());
		stateList.add(new GraphicsPresetCustom());
		
		for(EngineSettingsPreset p : stateList){
			stateMap.put(p.getId(), p);
		}
		
		EngineSettingsPreset.checkSanity(stateList);
	}


	public static EngineSettingsPreset[] values() {
		return stateList.toArray(new EngineSettingsPreset[stateList.size()]);
	}


	public boolean contains(EngineSettingsPreset state) {
		return state != null && stateMap.containsKey(state.getId());
	}

	public EngineSettingsPreset getFromString(String arg) throws StateParameterNotFoundException {
		return get(arg);
	}
	public static EngineSettingsPreset get(String s){
		EngineSettingsPreset engineSettingsPreset = stateMap.get(s.toUpperCase(Locale.ENGLISH));
		assert(engineSettingsPreset != null):"PRESET "+s+" -> "+stateMap;
		return engineSettingsPreset;
	}
	public String getType() {
		return "Preset";
	}

	public static EngineSettingsPreset getDefault() {
		String def = "GRAPHICS_MEDIUM";
		EngineSettingsPreset engineSettingsPreset = stateMap.get(def);
		assert(engineSettingsPreset != null):def+" -> "+stateMap;
		return engineSettingsPreset;
	}
}
