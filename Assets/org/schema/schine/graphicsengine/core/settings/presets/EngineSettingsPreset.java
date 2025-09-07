package org.schema.schine.graphicsengine.core.settings.presets;

import java.util.List;
import java.util.Set;

import org.schema.common.util.settings.SettingsXMLValue;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.NamedValueInterface;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public abstract class EngineSettingsPreset implements NamedValueInterface, SettingsXMLValue {
	private final String id;
	public EngineSettingsPreset(String id){
		this.id = id;
	}
	public String getId() {
		return id;
	}
	protected void addSetting(EngineSettings s, Object value){
		assert(s != null);
		settings.put(s, value);
	}
	private final Object2ObjectOpenHashMap<EngineSettings, Object> settings = new Object2ObjectOpenHashMap<EngineSettings, Object>();
	
	public void apply(){
		init();
		for(Entry<EngineSettings, Object> e : settings.object2ObjectEntrySet()){
			assert(e.getKey() != null):e.getKey();
			assert(e.getValue() != null):e.getValue();
			e.getKey().setValueByObject(e.getValue());
		}
	}
	public abstract void init();
	
	@Override
	public String toString(){
		return id;
	}
	public static void checkSanity(List<EngineSettingsPreset> statelist) {
		if(statelist.size() > 0){
			Set<EngineSettings> baseSettings = new ObjectOpenHashSet<EngineSettings>();
			baseSettings.addAll(statelist.get(0).settings.keySet());
			for(int i = 0; i < statelist.size(); i++){
				EngineSettingsPreset p = statelist.get(i);
				assert(baseSettings.size() == p.settings.size()):"invalid settings preset "+p.id+"; compared with "+statelist.get(0).id;
				for(EngineSettings e : p.settings.keySet()){
					assert(!baseSettings.add(e)):"invalid settings preset "+p.id+"; missing setting "+e.name();
				}
			}
		}
	}
	@Override
	public String getStringID() {
		return id;
	}
	
	
}
