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

public class TargetPowerBar extends FillableHorizontalBar {

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
	private float currentUsage;

	public TargetPowerBar(InputState state) {
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

		if (targetObject != null && targetObject instanceof SegmentController && targetObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) targetObject).getManagerContainer() instanceof PowerManagerInterface) {
			ManagerContainer<?> sc =  ((ManagedSegmentController<?>) targetObject).getManagerContainer();
			if(sc.isUsingPowerReactors()){
				currentUsage = (float) sc.getPowerInterface().getPowerConsumptionAsPercent();
				return currentUsage;
			}else{
				PowerManagerInterface pwi = (PowerManagerInterface) ((ManagedSegmentController<?>) targetObject).getManagerContainer();
				return pwi.getPowerAddOn().getPercentOne();
			}
		} else {
			return 0;
		}
		
		
	}

	@Override
	public void draw() {
		if(currentUsage > 0.99f){
			this.getColor().set(COLOR_WARN.x / 255f, COLOR_WARN.y / 255f, COLOR_WARN.z / 255f, COLOR_WARN.w / 255f);
		}else{
			this.getColor().set(COLOR.x / 255f, COLOR.y / 255f, COLOR.z / 255f, COLOR.w / 255f);
		}
		super.draw();
	}

	@Override
	public String getText() {
		SimpleTransformableSendableObject targetObject = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
		if (targetObject != null && targetObject instanceof SegmentController && targetObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) targetObject).getManagerContainer() instanceof PowerManagerInterface) {
			ManagerContainer<?> sc =  ((ManagedSegmentController<?>) targetObject).getManagerContainer();
			if(sc.isUsingPowerReactors()){
				PowerInterface p = sc.getPowerInterface();
				
				return Lng.str("Reactor Usage %s %%", StringTools.formatPointZero(p.getPowerConsumptionAsPercent()*100d));
			}else{
				PowerManagerInterface pwi = (PowerManagerInterface) ((ManagedSegmentController<?>) targetObject).getManagerContainer();
				return Lng.str("Power ") + StringTools.massFormat(pwi.getPowerAddOn().getPower()) + " / " + StringTools.massFormat(pwi.getPowerAddOn().getMaxPower());
			}
		} else {
			return Lng.str("Power n/a");
		}

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
		return "TargetPowerBar";
	}
}
