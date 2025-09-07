package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

public class TargetPlayerHealthBar extends FillableHorizontalBar {

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
	public TargetPlayerHealthBar(InputState state) {
		super(state);
		// TODO Auto-generated constructor stub
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
	public float getFilled() {
		SimpleTransformableSendableObject targetObject = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();

		if (targetObject != null && targetObject.getOwnerState() != null) {
			AbstractOwnerState ownerState = targetObject.getOwnerState();
			return ownerState.getHealth() / ownerState.getMaxHealth();
		}
		return 0;
	}

	@Override
	public String getText() {
		SimpleTransformableSendableObject targetObject = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();

		if (targetObject != null && targetObject.getOwnerState() != null) {
			AbstractOwnerState ownerState = targetObject.getOwnerState();
			return Lng.str("Health ") + ownerState.getHealth();
		}
		return Lng.str("Health n/a");

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
		return "TargetPlayerHealthBar";
	}
}
