package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

public class PlayerHealthBar extends FillableHorizontalBar {

	@ConfigurationElement(name = "Color")
	public static Vector4i COLOR;

	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;

	@ConfigurationElement(name = "FlipX")
	public static boolean FLIPX;
	@ConfigurationElement(name = "FlipY")
	public static boolean FLIPY;

	@ConfigurationElement(name = "FillStatusTextOnTop")
	public static boolean FILL_ON_TOP;
	@ConfigurationElement(name = "TextPos")
	public static Vector2f TEXT_POS;

	@ConfigurationElement(name = "TextDescPos")
	public static Vector2f TEXT_DESC_POS;
	
	public Vector2f getTextPos() {
		return TEXT_POS;
	}
	public Vector2f getTextDescPos() {
		return TEXT_DESC_POS;
	}
	public PlayerHealthBar(InputState state) {
		super(state);
	}

	@Override
	public boolean isBarFlippedX() {
		return FLIPX;
	}

	@Override
	public boolean isBarFlippedY() {
		return FLIPY;
	}

	@Override
	public boolean isFillStatusTextOnTop() {
		return FILL_ON_TOP;
	}

	@Override
	protected boolean isLongerBar() {
		return true;
	}

	@Override
	public float getFilled() {
		return ((GameClientState) getState()).getPlayer().getHealth() / ((GameClientState) getState()).getPlayer().getMaxHealth();
	}

	@Override
	public String getText() {
		return Lng.str("Health ") + (int) ((GameClientState) getState()).getPlayer().getHealth() + " / " + (int) ((GameClientState) getState()).getPlayer().getMaxHealth();
	}

	@Override
	public Vector4i getConfigColor() {
		return COLOR;
	}

	@Override
	public GUIPosition getConfigPosition() {
		return null;
	}

	@Override
	public Vector2f getConfigOffset() {
		return OFFSET;
	}

	@Override
	protected String getTag() {
		return "PlayerHealthBar";
	}

}
