package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.input.InputState;

public class GUIPlainWindow extends GUIAbstractPlainWindow {

	public static final int INNER_FB_INSET = 29;
	public int xInnerOffset = 8;
	public int yInnerOffset = 8;
	public float innerHeightSubstraction = 64;
	public float innerWidthSubstraction = 32;
	boolean init = false;
	private int topDist = 0;
	
	public int insetCornerDistTop = 0;
	public int insetCornerDistBottom = 0;
	private boolean closable;
	
	public GUIPlainWindow(final InputState state, int initialWidth, int initialHeight, String windowId) {
		super(state, initialWidth, initialHeight, windowId);
	}
	public GUIPlainWindow(InputState state, int initialWidth,
			int initialHeight, int initialPosX, int initalPosY, String windowId) {
		super(state, initialWidth, initialHeight, initialPosX, initalPosY, windowId);
	}
	
	

	@Override
	public void onInit() {
		if(init){
			return;
		}
		super.onInit();

		
		this.init = true;
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

	

	@Override
	public int getInnerCornerDistX() {
		return 0;
	}

	@Override
	public int getInnerCornerTopDistY() {
		return insetCornerDistTop;
	}
	@Override
	public int getInnerCornerBottomDistY() {
		return insetCornerDistBottom;
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
		return INNER_FB_INSET;
	}
	@Override
	protected void drawContent(GUIContentPane mainContentPane) {
		mainContentPane.draw();
		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		if(closable){
			drawCross(-30, 16);
		}
	}
	
	public void setClosable(boolean closable) {
		this.closable = closable;
	}


}
