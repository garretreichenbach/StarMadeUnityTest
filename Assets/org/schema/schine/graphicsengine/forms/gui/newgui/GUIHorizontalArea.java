package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.IconDatabase;
import org.schema.schine.graphicsengine.forms.gui.TooltipProviderCallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ButtonColorPalette;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

public class GUIHorizontalArea extends GUIAbstractHorizontalArea implements TooltipProviderCallback{

	
	private GUITexDrawableArea left;
	private GUITexDrawableArea right;
	private GUITexDrawableArea middle;

	protected HButtonType type;

	private boolean init = false;
	private Sprite sprite;
	private Sprite mS;
	
	private boolean blinking;
	SinusTimerUtil t = new SinusTimerUtil();
	public boolean rightDependentHalf;
	public boolean leftDependentHalf;
	private HButtonColor color = HButtonColor.BLUE;
	private boolean colored;
	private GUIToolTip toolTip;
	public GUIHorizontalArea(InputState state, HButtonColor color, int width) {
		super(state);
		this.color = color;
		this.colored = true;
		this.type = HButtonType.BUTTON_NEUTRAL_NORMAL;
		this.width = width;
	}
	public GUIHorizontalArea(InputState state, HButtonType type, int width) {
		super(state);

		this.type = type;
		this.width = width;
	}

	@Override
	public void cleanUp() {
		if(middle != null){
			middle.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if (changedSize) {
			adjustWidth();
		}
		if(blinking && getState().getGraphicsContext() != null){
			updateBlinking(getState().getGraphicsContext().timer);
		}else{
			left.setColor(getColor());
			middle.setColor(getColor());
			right.setColor(getColor());
		}
		GlUtil.glPushMatrix();

		transform();
		if (isRenderable()) {
			left.draw();
			middle.setPos(left.getPos().x + left.getWidth(), left.getPos().y, 0);
			middle.draw();
			right.setPos(width - right.getWidth(), left.getPos().y, 0);
			right.draw();
		}

		
		for (AbstractSceneNode e : getChilds()) {
			e.draw();
		}

//		System.err.println("CHHHH "+isRenderable() +" "+isMouseUpdateEnabled()+"; "+getCallback());
		if (isRenderable() && isMouseUpdateEnabled()) {
			
			checkMouseInside();
		}else{
			checkBlockingOnly();
		}

		GlUtil.glPopMatrix();
	}
	public Vector4f getColor(){
		if(colored){
			return color.getColor();
		}else{
			return null;
		}
	}
	
	private void updateBlinking(Timer timer) {
		t.update(timer);
		Vector4f color = new Vector4f(0,0,0,0);
		if(getColor() != null){
			color = getColor();
		}
		Vector4f cc = new Vector4f(color.x,color.y+t.getTime(),color.z+t.getTime(),1);
		left.setColor(cc);
		middle.setColor(cc);
		right.setColor(cc);
	}

	@Override
	public void onInit() {
		sprite = IconDatabase.getCorners32(getState());
		mS = IconDatabase.getHorizontal32(getState());
		left = new GUITexDrawableArea(getState(), sprite.getMaterial().getTexture(), 0, 0);
		right = new GUITexDrawableArea(getState(), sprite.getMaterial().getTexture(), 0, 0);
		middle = new GUITexDrawableArea(getState(), mS.getMaterial().getTexture(), 0, type.centerIndex / HORIZONTALS_TILING);

		middle.onInit();
		left.onInit();
		right.onInit();

		adjustWidth();

		init = true;
	}

	public void setType(HButtonType t) {
		if (t != this.type) {
			changedSize = true;
		}
		this.type = t;

	}

	@Override
	protected void adjustWidth() {

		//sets offsets
		left.setSpriteSubIndex(type.leftIndex, sprite.getMultiSpriteMaxX(), sprite.getMultiSpriteMaxY());
		right.setSpriteSubIndex(type.rightIndex, sprite.getMultiSpriteMaxX(), sprite.getMultiSpriteMaxY());
		middle.yOffset = type.centerIndex / HORIZONTALS_TILING;
		middle.setSizeChanged(true);

		left.yOffset += (1f / sprite.getMaterial().getTexture().getHeight()) * type.extraOffset;
		right.yOffset += (1f / sprite.getMaterial().getTexture().getHeight()) * type.extraOffset;
		middle.yOffset += (1f / mS.getMaterial().getTexture().getHeight()) * type.extraOffset;
		;

		if (width < sprite.getWidth() * 2) {
			int diff = (int) (width - sprite.getWidth() * 2);
			
			
			
			//diff is negative
			int smallerSize = sprite.getWidth() + diff / 2 - diff % 2;
			left.setWidth(smallerSize);
			
			int neededShift = sprite.getWidth() - smallerSize;
			
			//how much in texture coordinates is one pixel of this sprite
			float xTile = 1f / sprite.getMaterial().getTexture().getWidth();
			right.xOffset += xTile * neededShift;
//			System.err.println("NEEDED SHIFT :: "+width+"; "+smallerSize+" "+neededShift+"; :: "+sprite.getWidth()+"x"+sprite.getHeight()+";; "+sprite.getMaterial().getTexture().getWidth()+"x"+sprite.getMaterial().getTexture().getHeight());
			right.setWidth(sprite.getWidth() + diff / 2);
			assert(!Float.isNaN(right.xOffset));
				
		} else {
			left.setWidth(sprite.getWidth());
			right.setWidth(sprite.getWidth());
		}
//		right.setWidth(cS.getWidth());
		left.setHeight(type.buttonHeight);
		right.setHeight(type.buttonHeight);
		middle.setHeight(type.buttonHeight);

//		width = Math.max(left.getWidth()*2, width);
		middle.setWidth((int) (Math.max(sprite.getWidth() * 2, width) - 64));
		left.setSizeChanged(true);
		right.setSizeChanged(true);
		changedSize = false;
	}

	@Override
	public float getHeight() {
		return type.buttonHeight;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		if (this.width != width) {
			changedSize = true;
		}
		
		this.width = width;
		super.setWidth(width);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
	}

	@Override
	public void setWidth(float width) {
		if (this.width != width) {
			changedSize = true;
		}
		
		this.width = width;

	}

	@Override
	public void setHeight(float height) {
	}
	public enum HButtonColor {
		RED(() -> ButtonColorPalette.HButtonRed),
		PINK(() -> ButtonColorPalette.HButtonPink),
		ORANGE(() -> ButtonColorPalette.HButtonOrange),
		GREEN(() -> ButtonColorPalette.HButtonGreen),
		BLUE(() -> ButtonColorPalette.HButtonBlue),
		YELLOW(() -> ButtonColorPalette.HButtonYellow),
		
		;
		private final ColorGetter c;

		private HButtonColor(ColorGetter c){
			this.c = c;
		}
		
		public Vector4f getColor(){
			return c.getColor();
		}
	}
	private static interface ColorGetter{
		public Vector4f getColor();
	}
	public enum HButtonType {

		
		
		TEXT_FIELD(4, 5, 2, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_GREY_MEDIUM(6, 7, 3, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_GREY_LIGHT(8, 9, 4, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_GREY_DARK(10, 11, 5, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		
		BUTTON_BLUE_MEDIUM(12, 13, 6, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_BLUE_LIGHT(14, 15, 7, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_BLUE_DARK(16, 17, 8, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),

		BUTTON_RED_MEDIUM(37, 38, 16, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_RED_LIGHT(39, 40, 17, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_RED_DARK(41, 42, 18, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),

		TEXT_FILED_LIGHT(18, 19, 9, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),

		BUTTON_PINK_MEDIUM(47, 48,19, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_PINK_LIGHT(49, 50, 20, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_PINK_DARK(51, 52, 21, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		
		BUTTON_BLUE_SCNLINE_DARK(18, 19, 9, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_BLUE_SCNLINE_GREY(18, 19, 9, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		
		BUTTON_NEUTRAL_NORMAL(30, 31,13, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight), //12 is red
		BUTTON_NEUTRAL_MOUSEOVER(32, 33, 14, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_NEUTRAL_TOGGLED(34, 35, 15, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		BUTTON_NEUTRAL_INACTIVE(6, 7, 3, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		

		SLIDER_BACKGROUND(42, 43, 22, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),
		SLIDER_FRAME(44, 45, 23, UIScale.getUIScale().smallButtonOffsetPX, UIScale.getUIScale().smallButtonHeight),;
		final int leftIndex;
		final int rightIndex;
		final int centerIndex;
		final float extraOffset;
		final int buttonHeight;
		private HButtonType(int leftIndex, int rightIndex, int centerIndex, float extraOffset, int buttonHeight) {
			this.leftIndex = leftIndex;
			this.rightIndex = rightIndex;
			this.centerIndex = centerIndex;
			this.extraOffset = extraOffset;
			this.buttonHeight = buttonHeight;

		}

		public static HButtonType getType(HButtonType inputType, boolean mouseOver, boolean active, boolean highlight) {
			if (!GlUtil.isColorMask()) {
				return inputType;
			}
			if (active) {
				boolean buttonDown = Mouse.isPrimaryMouseDownUtility();
				if (highlight && !(mouseOver && buttonDown)) {
					return switch(inputType) {
						case BUTTON_BLUE_DARK -> BUTTON_BLUE_DARK;
						case BUTTON_BLUE_LIGHT -> BUTTON_BLUE_DARK;
						case BUTTON_BLUE_MEDIUM -> BUTTON_BLUE_DARK;
						case BUTTON_GREY_DARK -> BUTTON_GREY_DARK;
						case BUTTON_GREY_LIGHT -> BUTTON_GREY_DARK;
						case BUTTON_GREY_MEDIUM -> BUTTON_GREY_DARK;
						case BUTTON_RED_DARK -> BUTTON_RED_DARK;
						case BUTTON_RED_LIGHT -> BUTTON_RED_DARK;
						case BUTTON_RED_MEDIUM -> BUTTON_RED_DARK;
						case BUTTON_PINK_DARK -> BUTTON_PINK_DARK;
						case BUTTON_PINK_LIGHT -> BUTTON_PINK_DARK;
						case BUTTON_PINK_MEDIUM -> BUTTON_PINK_DARK;
						case TEXT_FIELD -> TEXT_FIELD;
						case TEXT_FILED_LIGHT -> TEXT_FILED_LIGHT;
						case BUTTON_NEUTRAL_TOGGLED -> BUTTON_NEUTRAL_TOGGLED;
						case BUTTON_NEUTRAL_MOUSEOVER -> BUTTON_NEUTRAL_TOGGLED;
						case BUTTON_NEUTRAL_NORMAL -> BUTTON_NEUTRAL_TOGGLED;
						default -> inputType;
					};
				}
				if (mouseOver) {

					if (buttonDown) {
						return switch(inputType) {
							case BUTTON_BLUE_DARK -> BUTTON_BLUE_DARK;
							case BUTTON_BLUE_LIGHT -> BUTTON_BLUE_DARK;
							case BUTTON_BLUE_MEDIUM -> BUTTON_BLUE_DARK;
							case BUTTON_GREY_DARK -> BUTTON_GREY_DARK;
							case BUTTON_GREY_LIGHT -> BUTTON_GREY_DARK;
							case BUTTON_GREY_MEDIUM -> BUTTON_GREY_DARK;
							case BUTTON_RED_DARK -> BUTTON_RED_DARK;
							case BUTTON_RED_LIGHT -> BUTTON_RED_DARK;
							case BUTTON_RED_MEDIUM -> BUTTON_RED_DARK;
							case TEXT_FIELD -> TEXT_FIELD;
							case TEXT_FILED_LIGHT -> TEXT_FILED_LIGHT;
							case BUTTON_NEUTRAL_TOGGLED -> BUTTON_NEUTRAL_TOGGLED;
							case BUTTON_NEUTRAL_MOUSEOVER -> BUTTON_NEUTRAL_TOGGLED;
							case BUTTON_NEUTRAL_NORMAL -> BUTTON_NEUTRAL_TOGGLED;
							default -> inputType;
						};
					} else {
						return switch(inputType) {
							case BUTTON_BLUE_DARK -> BUTTON_BLUE_LIGHT;
							case BUTTON_BLUE_LIGHT -> BUTTON_BLUE_LIGHT;
							case BUTTON_BLUE_MEDIUM -> BUTTON_BLUE_LIGHT;
							case BUTTON_GREY_DARK -> BUTTON_GREY_LIGHT;
							case BUTTON_GREY_LIGHT -> BUTTON_GREY_LIGHT;
							case BUTTON_GREY_MEDIUM -> BUTTON_GREY_LIGHT;
							case BUTTON_RED_DARK -> BUTTON_RED_LIGHT;
							case BUTTON_RED_LIGHT -> BUTTON_RED_LIGHT;
							case BUTTON_RED_MEDIUM -> BUTTON_RED_LIGHT;
							case BUTTON_PINK_DARK -> BUTTON_PINK_LIGHT;
							case BUTTON_PINK_LIGHT -> BUTTON_PINK_LIGHT;
							case BUTTON_PINK_MEDIUM -> BUTTON_PINK_LIGHT;
							case TEXT_FIELD -> TEXT_FIELD;
							case TEXT_FILED_LIGHT -> TEXT_FILED_LIGHT;
							case BUTTON_NEUTRAL_TOGGLED -> BUTTON_NEUTRAL_MOUSEOVER;
							case BUTTON_NEUTRAL_MOUSEOVER -> BUTTON_NEUTRAL_MOUSEOVER;
							case BUTTON_NEUTRAL_NORMAL -> BUTTON_NEUTRAL_MOUSEOVER;
							default -> inputType;
						};
					}
				} else {
					return inputType;
				}
			} else {
				return switch(inputType) {
					case BUTTON_BLUE_DARK -> BUTTON_GREY_DARK;
					case BUTTON_BLUE_LIGHT -> BUTTON_GREY_LIGHT;
					case BUTTON_BLUE_MEDIUM -> BUTTON_GREY_MEDIUM;
					case BUTTON_GREY_DARK -> BUTTON_BLUE_DARK;
					case BUTTON_GREY_LIGHT -> BUTTON_BLUE_LIGHT;
					case BUTTON_GREY_MEDIUM -> BUTTON_BLUE_MEDIUM;
					case BUTTON_RED_DARK -> BUTTON_GREY_DARK;
					case BUTTON_RED_LIGHT -> BUTTON_GREY_LIGHT;
					case BUTTON_RED_MEDIUM -> BUTTON_GREY_MEDIUM;
					case BUTTON_PINK_DARK -> BUTTON_GREY_DARK;
					case BUTTON_PINK_LIGHT -> BUTTON_GREY_LIGHT;
					case BUTTON_PINK_MEDIUM -> BUTTON_GREY_MEDIUM;
					case TEXT_FIELD -> TEXT_FIELD;
					case TEXT_FILED_LIGHT -> TEXT_FILED_LIGHT;
					case BUTTON_NEUTRAL_TOGGLED -> BUTTON_GREY_DARK;
					case BUTTON_NEUTRAL_MOUSEOVER -> BUTTON_GREY_LIGHT;
					case BUTTON_NEUTRAL_NORMAL -> BUTTON_NEUTRAL_INACTIVE;
					default -> inputType;
				};
			}
		}
	}

	public void setBlinking(boolean on) {
		this.blinking = on;
	}
	public void setColor(HButtonColor color) {
		this.color = color;
	}
	@Override
	public GUIToolTip getToolTip() {
		return toolTip;
	}
	@Override
	public void setToolTip(GUIToolTip toolTip) {
		this.toolTip = toolTip;
	}

}
