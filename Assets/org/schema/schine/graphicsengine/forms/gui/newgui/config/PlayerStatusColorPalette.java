package org.schema.schine.graphicsengine.forms.gui.newgui.config;

import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;

public class PlayerStatusColorPalette extends GuiConfig {

	@ConfigurationElement(name = "OnlineActive")
	public static Vector4f onlineActive;
	@ConfigurationElement(name = "OnlineInactive")
	public static Vector4f onlineInactive;
	@ConfigurationElement(name = "OfflineActive")
	public static Vector4f offlineActive;
	@ConfigurationElement(name = "OfflineInactive")
	public static Vector4f offlineInactive;

	public PlayerStatusColorPalette() {
	}

	@Override
	protected String getTag() {
		return "PlayerStatusColorPalette";
	}

}