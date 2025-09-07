package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

public class PowerBatteryBar extends FillableBarOne {

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

	public PowerBatteryBar(InputState state) {
		super(state);
		drawExtraText = true;
	}

	@Override
	protected String getDisplayTitle() {
		return Lng.str("Auxiliary power");
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
	public boolean isDrawn(){
		SimpleTransformableSendableObject currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController && currentPlayerObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer() instanceof PowerManagerInterface) {
			PowerManagerInterface sc = (PowerManagerInterface) ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
			return sc.getPowerAddOn().getBatteryMaxPower() > 0d;
		} else {
			return false;
		}
	}
	@Override
	public float getFilledOne() {
		SimpleTransformableSendableObject currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController && currentPlayerObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer() instanceof PowerManagerInterface) {
			PowerManagerInterface sc = (PowerManagerInterface) ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
			return sc.getPowerAddOn().getBatteryPercentOne();
		} else {
			return 0;
		}
	}

	@Override
	public String getText(int i) {
		SimpleTransformableSendableObject currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController && currentPlayerObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer() instanceof PowerManagerInterface) {
			PowerManagerInterface sc = (PowerManagerInterface) ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
			if (sc.getPowerAddOn().isBatteryActive()) {
				return "[- " + StringTools.formatPointZero(sc.getPowerAddOn().getPowerBatteryConsumedPerSecond()) + "]";
			} else {
				return "";
			}
		} else {
			return "";
		}
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
		return "PowerBatteryBar";
	}

	

}
