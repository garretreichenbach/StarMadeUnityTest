package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

public class SpeedBarRight extends FillableBarOne {

	@ConfigurationElement(name = "Color")
	public static Vector4i COLOR;

	@ConfigurationElement(name = "Position")
	public static GUIPosition POSITION;

	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;

	@ConfigurationElement(name = "FlipX")
	public static boolean FLIPX;
	@ConfigurationElement(name = "FlipY")
	public static boolean FLIPY;
	@ConfigurationElement(name = "FillStatusTextOnTop")
	public static boolean FILL_ON_TOP;
	@ConfigurationElement(name = "OffsetText")
	public static Vector2f OFFSET_TEXT;
	private Vector3f tmp = new Vector3f();

	public SpeedBarRight(InputState state) {
		super(state);
		drawPercent = false;
	}

	@Override
	protected String getDisplayTitle() {
		return Lng.str("Speed");
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
	public Vector2f getOffsetText() {
		return OFFSET_TEXT;
	}
	

	@Override
	public float getFilledOne() {

		if (((GameClientState) getState()).getShip() != null) {
			float current = ((GameClientState) getState()).getShip().getLinearVelocity(tmp).length();
			float max = ((GameClientState) getState()).getShip().getCurrentMaxVelocity();

			return current / max;

		}
		return 0;
	}

	//
	@Override
	public String getText(int i) {
		return ((GameClientState) getState()).getShip() != null ? (StringTools.formatPointZero(((GameClientState) getState()).getShip().getLinearVelocity(tmp).length()) + " m/s") : "0 m/s";
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
		return "SpeedBarRight";
	}

}
