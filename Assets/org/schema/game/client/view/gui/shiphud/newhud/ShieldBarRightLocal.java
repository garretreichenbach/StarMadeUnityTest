package org.schema.game.client.view.gui.shiphud.newhud;

import java.util.List;

import javax.vecmath.Vector2f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.ShieldLocal;
import org.schema.game.common.controller.elements.ShieldLocalAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.util.timer.LinearTimerUtil;
import org.schema.schine.graphicsengine.util.timer.TimerUtil;
import org.schema.schine.input.InputState;

public class ShieldBarRightLocal extends FillableBar {
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
	
	
	@ConfigurationElement(name = "SpriteSpan")
	public static float SPRITE_SPAN;
	
	
	@ConfigurationElement(name = "ExtraTextYOffset")
	public static float EXTRA_TEXT_Y_OFFSET;
	
	@ConfigurationElement(name = "TitleTextYOffset")
	public static float TITLE_TEXT_Y_OFFSET;
	
	@ConfigurationElement(name = "TextXIndent")
	public static float TEXT_X_INDENT;

	@ConfigurationElement(name = "XOffset")
	public static float X_OFFSET;
	
	@ConfigurationElement(name = "StaticTextPos")
	public static Vector2f STATIC_TEXT_POS;
	
	@ConfigurationElement(name = "StaticExtraTextPos")
	public static Vector2f STATIC_EXTRA_TEXT_POS;

	@ConfigurationElement(name = "StaticTitleTextPos")
	public static Vector2f STATIC_TITLE_TEXT_POS;
	
	
	public TimerUtil l = new LinearTimerUtil(0.5f);
	public ShieldBarRightLocal(InputState state) {
		super(state);
		drawExtraText = true;
	}

	
	@Override
	public float getSpriteSpan() {
		return SPRITE_SPAN;
	}
	@Override
	public float getTextIndent() {
		return TEXT_X_INDENT;
	}
	@Override
	public float getExtraTextYOffset() {
		return EXTRA_TEXT_Y_OFFSET;
	}
	@Override
	public float getTitleTextYOffset() {
		return TITLE_TEXT_Y_OFFSET;
	}
	@Override
	public float getXOffset() {
		return X_OFFSET;
	}
	@Override
	public Vector2f getStaticTextPos() {
		return STATIC_TEXT_POS;
	}
	@Override
	public Vector2f getStaticExtraTextPos() {
		return STATIC_EXTRA_TEXT_POS;
	}
	@Override
	public Vector2f getStaticTitleTextPos() {
		return STATIC_TITLE_TEXT_POS;
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
	public void update(Timer timer) {
		super.update(timer);
		l.update(timer);
	}

	@Override
	public float[] getFilled() {
//		if(true){
//			//debug
//			return new float[]{l.getTime(), l.getTime(), l.getTime()};
//		}
		SimpleTransformableSendableObject<?> currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController && currentPlayerObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer() instanceof ShieldContainerInterface &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer().isUsingPowerReactors()) {
			ShieldContainerInterface sc = (ShieldContainerInterface) ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
			
			ShieldLocalAddOn loc = sc.getShieldAddOn().getShieldLocalAddOn();
			
			if(loc.isAtLeastOneActive()){
				List<ShieldLocal> activeShields = loc.getActiveShields();
				
				float vals[] = new float[activeShields.size()];
				for(int i = 0; i < activeShields.size(); i++){
					vals[i] = activeShields.get(i).getPercentOne();
				}
				return vals;
			}else{
				return new float[]{0};
			}
		} else {
			return new float[]{0};
		}
	}

	@Override
	public String getText(int i) {
		SimpleTransformableSendableObject<?> currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController && currentPlayerObject instanceof ManagedSegmentController<?> &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer() instanceof ShieldContainerInterface &&
				((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer().isUsingPowerReactors()) {
			ShieldContainerInterface sc = (ShieldContainerInterface) ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer();
			
			ShieldLocalAddOn loc = sc.getShieldAddOn().getShieldLocalAddOn();
			List<ShieldLocal> activeShields = loc.getActiveShields();
			if(i < activeShields.size()){
				ShieldLocal l = activeShields.get(i);
				String s = "["+l.getPosString()+"]";
				
				if(l.getPercentOne() < 0.99){
					s += Lng.str(" %s/sec", StringTools.formatPointZero(l.getRechargeRateIncludingPrevent()));
				}
				if(l.getRechargePrevented() < 0.98) {
					s += Lng.str(" [Under Fire] -%s%%", StringTools.formatPointZero((1.0-l.getRechargePrevented())*100));
				}
				return s;
			}else{
				return Lng.str("No active Shields");
			}
		} else {
			return Lng.str("No active Shields");
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
		return "ShieldBarLeft";
	}
	

}
