package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.schine.input.InputState;

public class NegativeEffectBar extends EffectBar {

	@ConfigurationElement(name = "BlendOutInSec")
	public static float blendOutTime;
	@ConfigurationElement(name = "StayTime")
	public static float stayTime;
	@ConfigurationElement(name = "Color")
	public static Vector4i COLOR;
	@ConfigurationElement(name = "Position")
	public static GUIPosition POSITION;
	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;
	@ConfigurationElement(name = "Flipped")
	public static boolean FLIPPED;
	@ConfigurationElement(name = "TextColor")
	public static Vector4f TEXT_COLOR;

	public NegativeEffectBar(InputState state) {
		super(state);
	}

	@Override
	public float getBlendOutTime() {
		return blendOutTime;
	}

	@Override
	public float getStayTime() {
		return stayTime;
	}

	@Override
	public Vector4f getTextColor() {
		return TEXT_COLOR;
	}

	@Override
	public boolean isFlipped() {
		return FLIPPED;
	}

	@Override
	public Vector4i getConfigColor() {
		return COLOR;
	}

	@Override
	public GUIPosition getConfigPosition() {
		return POSITION;
	}

	@Override
	public Vector2f getConfigOffset() {
		return OFFSET;
	}

	@Override
	protected String getTag() {
		return "NegativeEffectBar";
	}

}
