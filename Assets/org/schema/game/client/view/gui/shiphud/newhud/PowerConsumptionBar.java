package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.input.InputState;

public class PowerConsumptionBar extends FillableVerticalBar {

	@ConfigurationElement(name = "Color")
	public static Vector4i COLOR;

	@ConfigurationElement(name = "ColorWarn")
	public static Vector4i COLOR_WARN;

	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;

	@ConfigurationElement(name = "FlipX")
	public static boolean FLIPX;
	@ConfigurationElement(name = "FlipY")
	public static boolean FLIPY;

	@ConfigurationElement(name = "FillStatusTextOnTop")
	public static boolean FILL_ON_TOP;

	@ConfigurationElement(name = "Position")
	public static GUIPosition POSITION;
	
	public PowerConsumptionBar(InputState state) {
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
		final PowerInterface pi = getPI();
		if(pi == null){
			return 0;
		}
		return (float) pi.getPowerConsumptionAsPercent();
	}

	@Override
	public String getText() {
		final PowerInterface pi = getPI();
		return "";
	}
	public PowerInterface getPI(){
		SimpleTransformableSendableObject<?> currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if(currentPlayerObject == null || !(currentPlayerObject instanceof ManagedSegmentController<?>)){
			return null;
		}
		return ((ManagedSegmentController<?>)currentPlayerObject).getManagerContainer().getPowerInterface();
	}
	@Override
	public Vector4i getConfigColor() {
		return COLOR;
	}
	@Override
	public Vector4i getConfigColorWarn() {
		return COLOR_WARN;
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
		return "PowerConsumptionBar";
	}

	public void resetDrawn() {
		
	}

	public void drawText() {
		
	}

	@Override
	public double getFilledMargin() {
		return 0.95d;
	}

	@Override
	protected String getBarName() {
		return "\n";
	}
	@Override
	protected int getTextOffsetX() {
		return -20;
	}

	@Override
	protected int getTextOffsetY() {
		return 30;
	}
	
	@Override
	protected Vector4f getColor(float filled) {
		return filled < 0.90 ? color : colorWarn;
	}
}
