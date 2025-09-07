package org.schema.game.common.data.blockeffects.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortSet;

public class EffectAccumulator {
	private final ShortArrayList notFound = new ShortArrayList();
	private final List<ConfigGroup> active = new ObjectArrayList<ConfigGroup>();
	private final Map<StatusEffectType, EffectModule> modules = new Object2ObjectOpenHashMap<StatusEffectType, EffectModule>();
	private final List<EffectModule> modulesList = new ObjectArrayList<EffectModule>();
	private final List<EffectConfigElement> activeElements = new ObjectArrayList<EffectConfigElement>();
	private final Map<StatusEffectType, List<EffectConfigElement>> effectMap = new Object2ObjectOpenHashMap<StatusEffectType, List<EffectConfigElement>>();
	
	public void calculate(ShortSet effects, ConfigPool configPool) {
		
		notFound.clear();
		active.clear();
		activeElements.clear();
		modulesList.clear();
		effectMap.clear();
		modules.clear();
		for(short s : effects){
			ConfigGroup configGroup = configPool.ntMap.get(s);
			if(configGroup != null){
				active.add(configGroup);
				for(EffectConfigElement e : configGroup.elements){
					activeElements.add(e);
					List<EffectConfigElement> list = effectMap.get(e.getType());
					if(list == null){
						list = new ObjectArrayList();
						effectMap.put(e.getType(), list);
					}
					list.add(e);
				}
			}else{
				notFound.add(s);
			}
		}
		
		for(Entry<StatusEffectType, List<EffectConfigElement>> e : effectMap.entrySet()){
			Collections.sort(e.getValue());
			EffectModule m = new EffectModule();
			m.create(e.getKey(), e.getValue());
			assert(m.getType() != null);
			modulesList.add(m);
			modules.put(e.getKey(), m);
		}
		
		
		
//		System.err.println("[EFFECT] Calculated Effects "+active.size()+" into modules -> "+modulesList.size()+"; not found: "+notFound.size());
		
	}

	public List<ConfigGroup> getActive() {
		return active;
	}

	public Map<StatusEffectType, EffectModule> getModules() {
		return modules;
	}
	public List<EffectModule> getModulesList() {
		return modulesList;
	}

	public ShortArrayList getNotFound() {
		return notFound;
	}
}
