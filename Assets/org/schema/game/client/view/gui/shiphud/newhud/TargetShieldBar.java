package org.schema.game.client.view.gui.shiphud.newhud;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.ShieldLocal;
import org.schema.game.common.controller.elements.ShieldLocalAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector2f;

public class TargetShieldBar extends FillableHorizontalBar {

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
	public TargetShieldBar(InputState state) {
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
		SimpleTransformableSendableObject<?> targetObject = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();

		if (targetObject != null && targetObject instanceof SegmentController && targetObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) targetObject).getManagerContainer() instanceof ShieldContainerInterface) {
			ManagerContainer<?> o = ((ManagedSegmentController<?>) targetObject).getManagerContainer();
			ShieldContainerInterface sc = (ShieldContainerInterface)o;
			if(o.isUsingPowerReactors()){
				ShieldLocalAddOn s = sc.getShieldAddOn().getShieldLocalAddOn();
				if(s.isAtLeastOneActive()){
					ShieldLocal l = s.getLastHitShield();
					return l.getPercentOne();
				}else{
					return 0;
				}
			}else{
				return sc.getShieldAddOn().getPercentOne();
			}
		} else {
			return 0;
		}
	}

	@Override
	public String getText() {
		SimpleTransformableSendableObject<?> targetObject = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
		if (targetObject != null && targetObject instanceof SegmentController && targetObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) targetObject).getManagerContainer() instanceof ShieldContainerInterface) {
			ManagerContainer<?> o = ((ManagedSegmentController<?>) targetObject).getManagerContainer();
			ShieldContainerInterface sc = (ShieldContainerInterface)o;
			if(o.isUsingPowerReactors()){
				ShieldLocalAddOn s = sc.getShieldAddOn().getShieldLocalAddOn();
				if(s.isAtLeastOneActive()){
					ShieldLocal l = s.getLastHitShield();
					return Lng.str("Shield [%s] ",l.getPosString()) + StringTools.massFormat(l.getShields()) + " / " + StringTools.massFormat(l.getShieldCapacity());
				}else{
					return Lng.str("Shield n/a");
				}
			}else{
				return Lng.str("Shield ") + StringTools.massFormat(sc.getShieldAddOn().getShields()) + " / " + StringTools.massFormat(sc.getShieldAddOn().getShieldCapacity());
			}
		} else {
			return Lng.str("Shield n/a");
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
		return "TargetShieldBar";
	}
}
