package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector2f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.DialogWindowFramePalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.WindowPaletteInterface;
import org.schema.schine.input.InputState;

public class GUIDialogWindow extends GUIResizableGrabbableWindow {

	public int inset = UIScale.getUIScale().W_inset;
	public int xInnerOffset = UIScale.getUIScale().W_xInnerOffset;
	public int yInnerOffset = UIScale.getUIScale().W_yInnerOffset;
	public float innerHeightSubstraction = UIScale.getUIScale().W_innerHeightSubstraction;
	public float innerWidthSubstraction = UIScale.getUIScale().W_innerWidthSubstraction;
	public int topDist = UIScale.getUIScale().W_topDist;
	
	boolean init = false;
	private GUIContentPane mainContentPane;
	private int selectedTab = 0;
	private GUIOverlay elements;
	private GUITexDrawableArea top;
	private GUITexDrawableArea bottom;
	private GUITexDrawableArea left;
	private GUITexDrawableArea right;
	private GUITexDrawableArea bg;
	
	private GUITextOverlay title;
	public GUIDialogWindow(final InputState state, int initialWidth, int initialHeight, String windowId) {
		super(state, initialWidth, initialHeight, windowId);

	}
	public void setTitle(Object title) {
		
		if(this.title != null){
			detach(this.title);
		}
		this.title = new GUITextOverlay(FontSize.BIG_20, getState()){
			@Override
			public void draw() {
				setPos((int)(GUIDialogWindow.this.getWidth()/2 - this.getMaxLineWidth() / 2), -UIScale.getUIScale().h, 0);
				super.draw();
			}
			
		};
		this.title.setTextSimple(title);
		
		
		attach(this.title);
		
	}
	public GUIDialogWindow(InputState state, int initialWidth,
			int initialHeight, int initialPosX, int initalPosY, String windowId) {
		super(state, initialWidth, initialHeight, initialPosX, initalPosY, windowId);
	}
	private static WindowPaletteInterface w = new WindowPaletteInterface() {
		
		
		@Override
		public Vector2f getTopModifierOffset() {
			return DialogWindowFramePalette.topSizeModifierOffset;
		}
		
		@Override
		public Vector2f getRightModifierOffset() {
			return DialogWindowFramePalette.rightSizeModifierOffset;
		}
		
		@Override
		public Vector2f getLeftModifierOffset() {
			return DialogWindowFramePalette.leftSizeModifierOffset;
		}
		
		@Override
		public Vector2f getBottomModifierOffset() {
			return DialogWindowFramePalette.bottomSizeModifierOffset;
		}

		@Override
		public Vector2f getMoveModifierOffset() {
			return DialogWindowFramePalette.moveModifierOffset;
		}
	};
	
	
	@Override
	public WindowPaletteInterface getWindowPalette() {
		return w;
	}
	@Override
	public void cleanUp() {
		if (mainContentPane != null) {
			mainContentPane.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		GlUtil.glPushMatrix();

		transform();

		checkMouseInside();

		checkGrabbedResize();

		GlUtil.glPopMatrix();

		GlUtil.glPushMatrix();

		transform();

		drawWindow();

		//no need to transform, as we already should be at this point

//		innerBackground.draw();
//
//		innerForeground.cornerDistanceY = INNER_FB_INSET;
//		innerForeground.upperCap = true;
//		innerForeground.draw();

		mainContentPane.draw();
		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		int size = getChilds().size();

		for (int i = 0; i < size; i++) {
			getChilds().get(i).draw();
		}

		drawMouseResizeIndicators();

		GlUtil.glPopMatrix();

	}

	private void drawWindow() {
		elements.setPos(0, 0, 0);
		elements.setSpriteSubIndex(12);
		elements.draw();

		elements.setPos(getWidth() - elements.getWidth(), 0, 0);
		elements.setSpriteSubIndex(13);
		elements.draw();

		elements.setPos(0, getHeight() - elements.getHeight(), 0);
		elements.setSpriteSubIndex(14);
		elements.draw();

		elements.setPos(getWidth() - elements.getWidth(), getHeight() - elements.getHeight(), 0);
		elements.setSpriteSubIndex(15);
		elements.draw();

		left.setPos(0, elements.getHeight(), 0);
		left.setWidth((int) elements.getWidth());
		left.setHeight((int) Math.max(0, (getHeight() - 2 * elements.getHeight())));
		left.draw();

		right.setPos(getWidth() - elements.getWidth(), elements.getHeight(), 0);
		right.setWidth((int) elements.getWidth());
		right.setHeight((int) Math.max(0, (getHeight() - 2 * elements.getHeight())));
		right.draw();

		top.setPos(elements.getWidth(), 0, 0);
		top.setHeight((int) elements.getHeight());
		top.setWidth((int) Math.max(0, (getWidth() - 2 * elements.getWidth())));
		top.draw();

		bottom.setPos(elements.getWidth(), getHeight() - elements.getHeight(), 0);
		bottom.setHeight((int) elements.getHeight());
		bottom.setWidth((int) Math.max(0, (getWidth() - 2 * elements.getWidth())));
		bottom.draw();

		bg.setPos(elements.getWidth(), elements.getHeight(), 0);
		bg.setWidth((int) Math.max(0, (getWidth() - 2 * elements.getWidth())));
		bg.setHeight((int) Math.max(0, (getHeight() - 2 * elements.getHeight())));
		bg.draw();

		drawCross(UIScale.getUIScale().W_DIALOG_CROSS_X, UIScale.getUIScale().W_DIALOG_CROSS_Y);

	}

	@Override
	public void onInit() {
		if(init){
			return;
		}
		super.onInit();
		elements = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 8px Corners-8x8-gui-"), getState());
		elements.onInit();

		top = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 8px Horizontal-1x32-gui-").getMaterial().getTexture(), 0.0f, 8f * 0.03125f);
		bottom = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 8px Horizontal-1x32-gui-").getMaterial().getTexture(), 0.0f, 9f * 0.03125f);

		left = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 8px Vertical-32x1-gui-").getMaterial().getTexture(), 8f * 0.03125f, 0.0f);
		right = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 8px Vertical-32x1-gui-").getMaterial().getTexture(), 9f * 0.03125f, 0.0f);

		bg = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 8px Center_PopUp-gui-").getMaterial().getTexture(), 0.0f, 0.0f);

		top.onInit();
		bottom.onInit();
		left.onInit();
		right.onInit();







		mainContentPane = (new GUIContentPane(getState(), this, "default"));
		mainContentPane.onInit();

		this.init = true;
	}

	@Override
	protected int getMinWidth() {
		return UIScale.getUIScale().W_MinWidth;
	}

	@Override
	protected int getMinHeight() {
		return UIScale.getUIScale().W_MinHeight;
	}

	@Override
	public int getTopDist() {
		return topDist;
	}

	/**
	 * @param topDist the topDist to set
	 */
	public void setTopDist(int topDist) {
		this.topDist = topDist;
	}

	public void attachSuper(GUIElement e) {
		super.attach(e);
	}

	public void detachSuper(GUIElement o) {
		super.detach(o);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#attach(org.schema.schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void attach(GUIElement o) {
		mainContentPane.getTextboxes().get(0).getContent().attach(o);
//		super.attach(o);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#detach(org.schema.schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void detach(GUIElement o) {
		mainContentPane.getTextboxes().get(0).getContent().detach(o);
	}

	/**
	 * @return the selectedTab
	 */
	public int getSelectedTab() {
		return selectedTab;
	}

	/**
	 * @param selectedTab the selectedTab to set
	 */
	public void setSelectedTab(int selectedTab) {
		this.selectedTab = selectedTab;
	}

	@Override
	public int getInnerCornerDistX() {
		return 0;
	}

	@Override
	public int getInnerCornerTopDistY() {
		return 0;
	}
	@Override
	public int getInnerCornerBottomDistY() {
		return 0;
	}
	@Override
	public int getInnerHeigth() {
		return (int) (getHeight() - innerHeightSubstraction);
	}

	@Override
	public int getInnerWidth() {
		return (int) (getWidth() - innerWidthSubstraction);
	}

	@Override
	public int getInnerOffsetX() {
		return xInnerOffset;
	}

	@Override
	public int getInnerOffsetY() {
		return yInnerOffset;
	}

	@Override
	public int getInset() {
		return inset;
	}

	/**
	 * @return the mainContentPane
	 */
	public GUIContentPane getMainContentPane() {
		return mainContentPane;
	}
	

}
