package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.DialogWindowFramePalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.WindowPaletteInterface;
import org.schema.schine.input.InputState;

public abstract class GUIAbstractPlainWindow extends GUIResizableGrabbableWindow {

	public GUIInnerForeground innerForeground;
	boolean init = false;
	GUIInnerBackground innerBackground;
	private GUIContentPane mainContentPane;
	private int selectedTab = 0;
	private GUIOverlay elements;
	private GUITexDrawableArea top;
	private GUITexDrawableArea bottom;
	private GUITexDrawableArea left;
	private GUITexDrawableArea right;
	private GUITexDrawableArea bg;
	
	public Vector4f backgroundTint = new Vector4f(1,1,1,1);
	public boolean selectedBackground;
	
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

	public GUIAbstractPlainWindow(final InputState state, int initialWidth, int initialHeight, String windowId) {
		super(state, initialWidth, initialHeight, windowId);
	}
	public GUIAbstractPlainWindow(InputState state, int initialWidth,
			int initialHeight, int initialPosX, int initalPosY, String windowId) {
		super(state, initialWidth, initialHeight, initialPosX, initalPosY, windowId);
	}
	@Override
	public int getTopHeightSubtract(){
		//don't shift individually with the top task bar 
		return 0;
	}

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
		if(translateOnlyMode){
			translate();
		}else{
			GlUtil.glPushMatrix();
			transform();
		}

		if(isMouseUpdateEnabled()){
			checkMouseInside();
		}

		checkGrabbedResize();

		
		drawWindow();
		drawContent(mainContentPane);
		final int size = getChilds().size();

		for (int i = 0; i < size; i++) {
			getChilds().get(i).draw();
		}

		drawMouseResizeIndicators();
		if(translateOnlyMode){
			translateBack();
		}else{
			GlUtil.glPopMatrix();
		}

	}
	protected abstract void drawContent(GUIContentPane mainContentPane);
	private void setElementsColor(Vector4f backgroundTint){
		elements.getSprite().setTint(backgroundTint);
		left.color = (backgroundTint);
		right.color = (backgroundTint);
		top.color = (backgroundTint);
		bottom.color = (backgroundTint);
		bg.color = (backgroundTint);
	}
	private void drawWindow() {
		
		startStandardDraw();
		setElementsColor(backgroundTint);
		
		
		
		elements.setPos(0, 0, 0);
		elements.setSpriteSubIndex(12);
		elements.drawRaw();

		elements.setPos(getWidth() - elements.getWidth(), 0, 0);
		elements.setSpriteSubIndex(13);
		elements.drawRaw();

		elements.setPos(0, getHeight() - elements.getHeight(), 0);
		elements.setSpriteSubIndex(14);
		elements.drawRaw();

		elements.setPos(getWidth() - elements.getWidth(), getHeight() - elements.getHeight(), 0);
		elements.setSpriteSubIndex(15);
		elements.drawRaw();

		left.setPos(0, elements.getHeight(), 0);
		left.setWidth((int) elements.getWidth());
		left.setHeight((int) Math.max(0, (getHeight() - 2 * elements.getHeight())));
		left.drawRaw();

		right.setPos(getWidth() - elements.getWidth(), elements.getHeight(), 0);
		right.setWidth((int) elements.getWidth());
		right.setHeight((int) Math.max(0, (getHeight() - 2 * elements.getHeight())));
		right.drawRaw();

		top.setPos(elements.getWidth(), 0, 0);
		top.setHeight((int) elements.getHeight());
		top.setWidth((int) Math.max(0, (getWidth() - 2 * elements.getWidth())));
		top.drawRaw();

		bottom.setPos(elements.getWidth(), getHeight() - elements.getHeight(), 0);
		bottom.setHeight((int) elements.getHeight());
		bottom.setWidth((int) Math.max(0, (getWidth() - 2 * elements.getWidth())));
		bottom.drawRaw();

		bg.setPos(elements.getWidth(), elements.getHeight(), 0);
		bg.setWidth((int) Math.max(0, (getWidth() - 2 * elements.getWidth())));
		bg.setHeight((int) Math.max(0, (getHeight() - 2 * elements.getHeight())));
		bg.drawRaw();

		
		
		if(selectedBackground){
			int ol = 4;
			GL11.glLineWidth(3);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(1,1,1,1);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			GL11.glVertex2f(ol, 0);
			GL11.glVertex2f(getWidth()-ol, 0);
			GL11.glVertex2f(getWidth(), ol);
			GL11.glVertex2f(getWidth(), getHeight()-ol);
			GL11.glVertex2f(getWidth()-ol, getHeight());
			GL11.glVertex2f(ol, getHeight());
			GL11.glVertex2f(0, getHeight()-ol);
			GL11.glVertex2f(0, ol);
			GL11.glVertex2f(ol, 0);
			GL11.glEnd();
		}
		
		setElementsColor(null);
		endStandardDraw();
//		drawCross(-36, -16);

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
		return (128);
	}

	@Override
	protected int getMinHeight() {
		return (128);
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


	/**
	 * @return the mainContentPane
	 */
	public GUIContentPane getMainContentPane() {
		return mainContentPane;
	}
	

}
