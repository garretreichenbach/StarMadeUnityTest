package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

public class ReactorPowerBar extends FillableBarOne {

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

	public ReactorPowerBar(InputState state) {
		super(state);
		drawExtraText = true;
	}

	@Override
	protected String getDisplayTitle() {
		return Lng.str("Power");
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
		if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController && currentPlayerObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer() instanceof PowerManagerInterface) {
			ManagerContainer<?> sc =  ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
			return (float) getPI().getPowerAsPercent();
		} else {
			return 0;
		}
	}

	@Override
	public String getText(int i) {
		SimpleTransformableSendableObject currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController && currentPlayerObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer() instanceof PowerManagerInterface) {
			ManagerContainer<?> sc =  ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
			if (getPI().getCurrentConsumption() > 0) {
				return "[" + StringTools.formatPointZero(getPI().getPower()) +  " / " + StringTools.formatPointZero(getPI().getMaxPower()) + "]; - "+StringTools.formatPointZero(getPI().getCurrentConsumptionPerSec())+"/sec; + "+StringTools.formatPointZero(getPI().getRechargeRatePowerPerSec())+"/sec]";
			} else {
				return "[" + StringTools.formatPointZero(getPI().getPower()) +  " / " + StringTools.formatPointZero(getPI().getMaxPower()) + "]; + "+StringTools.formatPointZero(getPI().getRechargeRatePowerPerSec())+"/sec]";
			}
		} else {
			return "";
		}
	}
	public PowerInterface getPI(){
		SimpleTransformableSendableObject currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		ManagerContainer<?> sc =  ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
		if(sc.getSegmentController().railController.isDockedAndExecuted()){
			if(sc.getSegmentController().railController.getRoot() instanceof ManagedSegmentController<?>){
				return ((ManagedSegmentController<?>)sc.getSegmentController().railController.getRoot()).getManagerContainer().getPowerInterface();
			}
		}
		return sc.getPowerInterface();
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
		return "PowerBar";
	}

	

}
