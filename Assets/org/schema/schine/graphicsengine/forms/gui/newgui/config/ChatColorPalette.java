package org.schema.schine.graphicsengine.forms.gui.newgui.config;

import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;

public class ChatColorPalette extends GuiConfig {

	@ConfigurationElement(name = "SystemMessage")
	public static Vector4f system;
	
	@ConfigurationElement(name = "GeneralChat")
	public static Vector4f general;
	
	@ConfigurationElement(name = "FactionChat")
	public static Vector4f faction;
	
	@ConfigurationElement(name = "WhisperChat")
	public static Vector4f whisper;
	
	@ConfigurationElement(name = "AllianceChat")
	public static Vector4f alliance;
	
	@ConfigurationElement(name = "VicinityChat")
	public static Vector4f vicinity;
	
	@ConfigurationElement(name = "OtherChannelChat")
	public static Vector4f other;
	

	public ChatColorPalette() {
	}

	@Override
	protected String getTag() {
		return "ChatColorPalette";
	}

}
