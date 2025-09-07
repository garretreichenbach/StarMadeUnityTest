package org.schema.game.common.data.blockeffects.config;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ConfigContainer {
	public final Map<String, ConfigGroup> active = new Object2ObjectOpenHashMap<String, ConfigGroup>();
}
