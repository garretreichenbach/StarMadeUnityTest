package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface;
import org.schema.schine.graphicsengine.forms.gui.IconDatabase;
import org.schema.schine.input.InputState;

public abstract class GUISliderBar extends GUIAbstractNewScrollBar {

	private GUIOverlay leftButton;
	private GUIOverlay rightButton;
	private GUIActivatableTextBar textBar;
	public GUISliderBar(InputState state,
	                    GUIScrollableInterface scrollPanel, int orientation) {
		super(state, scrollPanel, orientation, true);
		
		grabWhenClickedInLane = true;

		this.leftButton = IconDatabase.getLeftArrowInstance16(state);
		this.rightButton = IconDatabase.getRightArrowInstance16(state);

		leftButton.setMouseUpdateEnabled(true);
		rightButton.setMouseUpdateEnabled(true);
		
		textBar = getTextBar();
		
		cDist = (int) (textBar.getWidth()+UIScale.getUIScale().SLIDER_W);
	}

	public abstract GUIActivatableTextBar getTextBar();

	public GUISliderBar(InputState state, GUIScrollableInterface scrollPanel) {
		super(state, scrollPanel, true);

		grabWhenClickedInLane = true;
		

		this.leftButton = IconDatabase.getLeftArrowInstance16(state);
		this.rightButton = IconDatabase.getRightArrowInstance16(state);


		leftButton.setMouseUpdateEnabled(true);
		rightButton.setMouseUpdateEnabled(true);
		
		textBar = getTextBar();
		cDist = (int) (textBar.getWidth()+22);
	}

	@Override
	protected boolean isLaneRepeatable() {
		return true;
	}

	@Override
	public GUIActivatableTextBar getSettingsElement() {
		return textBar;
	}
	@Override
	public int getSettingsElementDistanceAfterButton() {
		return UIScale.getUIScale().SLIDER_distanceAfterButton;
	}

	@Override
	public boolean isSettingsElement() {
		return true;
	}

	@Override
	protected String getLaneTex() {
		return "UI 32px-horizontals-1x32-gui-";
	}

	@Override
	protected String getStartEndTex() {
		return "UI 32px Corners-8x8-gui-";
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
		return 45;
	}

	@Override
	protected int getHorizontalEnd() {
		return 46;
	}

	@Override
	protected int getHorizontalLane() {
		return 23;
	}

	@Override
	protected int getHorizontalBar() {
		return 53;
	}

	@Override
	protected boolean hasSeperateArrows() {
		return true;
	}

	@Override
	protected GUIOverlay getSeperateArrowBottom() {
		return rightButton;
	}

	@Override
	protected GUIOverlay getSeperateArrowTop() {
		return leftButton;
	}

	@Override
	protected int getSpriteSize() {
		return UIScale.getUIScale().SLIDER_SPRITE_SIZE;
	}

	@Override
	protected float getBarHorizontalYSubstract() {
		return 0;
	}

	@Override
	protected float getBarVerticalXSubstract() {
		return 0;
	}
	public void scrollLock() {
		getState().getController().getInputController().scrollLockOn(this);
	}

	


}
