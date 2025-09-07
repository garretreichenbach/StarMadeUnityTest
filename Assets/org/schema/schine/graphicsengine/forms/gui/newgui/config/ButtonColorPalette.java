package org.schema.schine.graphicsengine.forms.gui.newgui.config;

import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;

public class ButtonColorPalette extends GuiConfig {

	@ConfigurationElement(name = "Ok")
	public static Vector4f ok;
	@ConfigurationElement(name = "OkMouseOver")
	public static Vector4f okMouseOver;
	@ConfigurationElement(name = "OkPressed")
	public static Vector4f okPressed;
	@ConfigurationElement(name = "OkText")
	public static Vector4f okText;
	@ConfigurationElement(name = "OkTextMouseOver")
	public static Vector4f okTextMouseOver;
	@ConfigurationElement(name = "OkTextPressed")
	public static Vector4f okTextPressed;

	@ConfigurationElement(name = "Cancel")
	public static Vector4f cancel;
	@ConfigurationElement(name = "CancelMouseOver")
	public static Vector4f cancelMouseOver;
	@ConfigurationElement(name = "CancelPressed")
	public static Vector4f cancelPressed;
	@ConfigurationElement(name = "CancelText")
	public static Vector4f cancelText;
	@ConfigurationElement(name = "CancelTextMouseOver")
	public static Vector4f cancelTextMouseOver;
	@ConfigurationElement(name = "CancelTextPressed")
	public static Vector4f cancelTextPressed;

	@ConfigurationElement(name = "Tutorial")
	public static Vector4f tutorial;
	@ConfigurationElement(name = "TutorialMouseOver")
	public static Vector4f tutorialMouseOver;
	@ConfigurationElement(name = "TutorialPressed")
	public static Vector4f tutorialPressed;
	@ConfigurationElement(name = "TutorialText")
	public static Vector4f tutorialText;
	@ConfigurationElement(name = "TutorialTextMouseOver")
	public static Vector4f tutorialTextMouseOver;
	@ConfigurationElement(name = "TutorialTextPressed")
	public static Vector4f tutorialTextPressed;

	@ConfigurationElement(name = "Hostile")
	public static Vector4f hostile;
	@ConfigurationElement(name = "HostileMouseOver")
	public static Vector4f hostileMouseOver;
	@ConfigurationElement(name = "HostilePressed")
	public static Vector4f hostilePressed;
	@ConfigurationElement(name = "HostileText")
	public static Vector4f hostileText;
	@ConfigurationElement(name = "HostileTextMouseOver")
	public static Vector4f hostileTextMouseOver;
	@ConfigurationElement(name = "HostileTextPressed")
	public static Vector4f hostileTextPressed;

	@ConfigurationElement(name = "Friendly")
	public static Vector4f friendly;
	@ConfigurationElement(name = "FriendlyMouseOver")
	public static Vector4f friendlyMouseOver;
	@ConfigurationElement(name = "FriendlyPressed")
	public static Vector4f friendlyPressed;
	@ConfigurationElement(name = "FriendlyText")
	public static Vector4f friendlyText;
	@ConfigurationElement(name = "FriendlyTextMouseOver")
	public static Vector4f friendlyTextMouseOver;
	@ConfigurationElement(name = "FriendlyTextPressed")
	public static Vector4f friendlyTextPressed;

	@ConfigurationElement(name = "Neutral")
	public static Vector4f neutral;
	@ConfigurationElement(name = "NeutralMouseOver")
	public static Vector4f neutralMouseOver;
	@ConfigurationElement(name = "NeutralPressed")
	public static Vector4f neutralPressed;
	@ConfigurationElement(name = "NeutralText")
	public static Vector4f neutralText;
	@ConfigurationElement(name = "NeutralTextMouseOver")
	public static Vector4f neutralTextMouseOver;
	@ConfigurationElement(name = "NeutralTextPressed")
	public static Vector4f neutralTextPressed;
	
	@ConfigurationElement(name = "HButtonBlue")
	public static Vector4f HButtonBlue;
	@ConfigurationElement(name = "HButtonRed")
	public static Vector4f HButtonRed;
	@ConfigurationElement(name = "HButtonGreen")
	public static Vector4f HButtonGreen;
	@ConfigurationElement(name = "HButtonPink")
	public static Vector4f HButtonPink;
	@ConfigurationElement(name = "HButtonYellow")
	public static Vector4f HButtonYellow;
	@ConfigurationElement(name = "HButtonOrange")
	public static Vector4f HButtonOrange;
	
	

	public ButtonColorPalette() {
	}

	@Override
	protected String getTag() {
		return "ButtonColorPalette";
	}

}
