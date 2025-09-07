package org.schema.game.common.gui.worldmanager;

public class WorldInfo {

	public final String name;
	public final String path;
	private final boolean defaultWorld;

	public WorldInfo(String name, String path,
	                 boolean def) {
		this.name = name;
		this.path = path;
		this.defaultWorld = def;
	}

	public boolean isDefault() {
		return defaultWorld;
	}

}
