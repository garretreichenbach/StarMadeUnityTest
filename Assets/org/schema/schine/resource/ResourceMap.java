package org.schema.schine.resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.schema.schine.resource.CreatureStructure.PartType;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ResourceMap {
	public final Map<PartType, List<String>> creatureMap = new HashMap<>();
	public final Map<String, ResourceLoadEntry> resources = new HashMap<>();

	public ResourceMap() {
		super();
		for (PartType p : PartType.values()) {
			creatureMap.put(p, new ObjectArrayList<>());
		}
	}

	public void initForServer(List<ResourceLoadEntry> loadQueue) {
		for (ResourceLoadEntry resourceLoadEntry : loadQueue) {
			resources.put(resourceLoadEntry.name, resourceLoadEntry);
			if (resourceLoadEntry instanceof ResourceLoadEntryMesh && ((ResourceLoadEntryMesh) resourceLoadEntry).creature != null) {
				for (PartType p : ((ResourceLoadEntryMesh) resourceLoadEntry).creature.partType) {
					creatureMap.get(p).add(resourceLoadEntry.name);
				}
			}
		}
	}

	public ResourceLoadEntry get(String s) {
		return resources.get(s);
	}
	public ResourceLoadEntryMesh getMesh(String s) {
		return (ResourceLoadEntryMesh)resources.get(s);
	}

	public List<String> getType(PartType type) {
		return creatureMap.get(type);
	}

}
