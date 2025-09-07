package org.schema.schine.graphicsengine.forms.gui.newgui.config;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;

public class DialogWindowFramePalette extends GuiConfig {

	@ConfigurationElement(name = "WindowGradientStripeStart")
	public static Vector4f windowGradientStripeStart;
	
	@ConfigurationElement(name = "WindowGradientStripeMid")
	public static Vector4f windowGradientStripeMid;
	
	@ConfigurationElement(name = "WindowGradientStripeEnd")
	public static Vector4f windowGradientStripeEnd;
	


	
	@ConfigurationElement(name = "TopSizeModifierOffset")
	public static Vector2f topSizeModifierOffset = new Vector2f();
	
	@ConfigurationElement(name = "RightSizeModifierOffset")
	public static Vector2f rightSizeModifierOffset = new Vector2f();
	
	@ConfigurationElement(name = "BottomSizeModifierOffset")
	public static Vector2f bottomSizeModifierOffset = new Vector2f();
	
	@ConfigurationElement(name = "LeftSizeModifierOffset")
	public static Vector2f leftSizeModifierOffset = new Vector2f();
	
	@ConfigurationElement(name = "MoveModifierOffset")
	public static Vector2f moveModifierOffset = new Vector2f();
	
	
	
	

	public DialogWindowFramePalette() {
	}

	@Override
	protected String getTag() {
		return "DialogWindowFramePalette";
	}

}
