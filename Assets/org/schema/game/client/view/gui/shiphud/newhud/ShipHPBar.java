package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

public class ShipHPBar extends FillableBarOne {

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

	public ShipHPBar(InputState state) {
		super(state);
	}

	@Override
	protected String getDisplayTitle() {
		return Lng.str("Struct HP");
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
		SimpleTransformableSendableObject currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController) {
			SegmentController c = (SegmentController) currentPlayerObject;
			double effectiveZero = VoidElementManager.HP_CONDITION_TRIGGER_LIST.get(true).get(0).hpPercent; //overheat percentage
			double pct = c.getHpController().getHpPercent();
			if(pct <= effectiveZero) return 0;

			double livingHPRange = 1-effectiveZero;
			return (float) ((pct-effectiveZero)/livingHPRange); //show distance above overheat
		} else {
			return 0;
		}
	}

	@Override
	public String getText(int i) {

		return "";

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
		return "ShipHPBar";
	}
}
