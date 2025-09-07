package org.schema.game.client.view.gui.shiphud.newhud;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentControllerHpControllerInterface;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector2f;

public class TargetShipHPBar extends FillableHorizontalBar {

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

	public TargetShipHPBar(InputState state) {
		super(state);
	}

	public Vector2f getTextPos() {
		return TEXT_POS;
	}

	public Vector2f getTextDescPos() {
		return TEXT_DESC_POS;
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

		if(targetObject != null && targetObject instanceof SegmentController) {
			SegmentController c = (SegmentController) targetObject;
			double effectiveZero = VoidElementManager.HP_CONDITION_TRIGGER_LIST.get(true).get(0).hpPercent; //overheat percentage
			double pct = c.getHpController().getHpPercent();
			if(pct <= effectiveZero) return 0;

			double livingHPRange = 1 - effectiveZero;
			return (float) ((pct - effectiveZero) / livingHPRange); //show distance above overheat
		} else {
			return 0;
		}
	}

	@Override
	public String getText() {

		SimpleTransformableSendableObject targetObject = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();

		if(targetObject != null && targetObject instanceof SegmentController) {
			SegmentController c = (SegmentController) targetObject;
			SegmentControllerHpControllerInterface h = c.getHpController();
			return (c.isUsingPowerReactors() ? Lng.str("Reactor HP ") : Lng.str("Struct ")) + StringTools.massFormat(h.getHp()) + " / " + StringTools.massFormat(h.getMaxHp());
		}

		return Lng.str("Struct n/a");
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
		return "TargetStructureHPBar";
	}
}
