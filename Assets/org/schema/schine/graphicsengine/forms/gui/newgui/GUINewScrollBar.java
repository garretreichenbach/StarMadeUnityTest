package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface;
import org.schema.schine.input.InputState;

public class GUINewScrollBar extends GUIAbstractNewScrollBar {

	public GUINewScrollBar(InputState state,
	                       GUIScrollableInterface scrollPanel, int orientation, boolean staticSlider) {
		super(state, scrollPanel, orientation, staticSlider);
	}

	public GUINewScrollBar(InputState state, GUIScrollableInterface scrollPanel, boolean staticSlider) {
		super(state, scrollPanel, staticSlider);
	}

	@Override
	protected boolean isLaneRepeatable() {
		return false;
	}

	@Override
	protected String getLaneTex() {
		return "UI 16px-8x8-gui-";
	}

	@Override
	protected String getStartEndTex() {
		return "UI 16px-8x8-gui-";
	}

	@Override
	protected String getBarTex() {
		return "UI 32px Corners-8x8-gui-";
	}

	@Override
	protected int getVerticalStart() {
		return 9;
	}

	@Override
	protected int getVerticalEnd() {
		return 11;
	}

	@Override
	protected int getVerticalLane() {
		return 10;
	}

	@Override
	protected int getVerticalBar() {
		return 20;
	}

	@Override
	protected int getHorizontalStart() {
		return 12;
	}

	@Override
	protected int getHorizontalEnd() {
		return 14;
	}

	@Override
	protected int getHorizontalLane() {
		return 13;
	}

	@Override
	protected int getHorizontalBar() {
		return 36;
	}

	@Override
	protected boolean hasSeperateArrows() {
		return false;
	}

	@Override
	protected GUIOverlay getSeperateArrowBottom() {
		return null;
	}

	@Override
	protected GUIOverlay getSeperateArrowTop() {
		return null;
	}

	@Override
	protected int getSpriteSize() {
		return UIScale.getUIScale().scale(16);
	}

	@Override
	protected float getBarHorizontalYSubstract() {
		return UIScale.getUIScale().scale(9);
	}

	@Override
	protected float getBarVerticalXSubstract() {
		return UIScale.getUIScale().scale(7);
	}

}
