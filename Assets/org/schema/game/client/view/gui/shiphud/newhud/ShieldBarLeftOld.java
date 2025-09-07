package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

public class ShieldBarLeftOld extends FillableBarOne {

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

	public ShieldBarLeftOld(InputState state) {
		super(state);
		drawExtraText = true;
	}

	@Override
	protected String getDisplayTitle() {
		return Lng.str("Shields");
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
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer() instanceof ShieldContainerInterface) {
			ShieldContainerInterface sc = (ShieldContainerInterface) ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
			return sc.getShieldAddOn().getPercentOne();
		} else {
			return 0;
		}
	}

	@Override
	public String getText(int i) {
		SimpleTransformableSendableObject currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController && currentPlayerObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer() instanceof ShieldContainerInterface) {
			ShieldContainerInterface sc = (ShieldContainerInterface) ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
			if (sc.getShieldAddOn().getShields() < sc.getShieldAddOn().getShieldCapacity() && sc.getShieldAddOn().getShieldRechargeRate() > 0) {
				
				String sh = "+ " + StringTools.formatPointZero(sc.getShieldAddOn().getShieldRechargeRate());
				double dirRecovery = sc.getShieldAddOn().getRecovery();
				
				if(dirRecovery > 0){
					sh += " ((!) Under Fire Rate: "+Math.ceil(sc.getShieldAddOn().getNerf() * 100) + "%)";
				}
				
				return sh;
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
		return "ShieldBarRight";
	}

}
