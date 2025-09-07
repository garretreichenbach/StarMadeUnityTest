package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollableInterface;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.input.BasicInputController;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;
import org.schema.schine.sound.controller.AudioController;

public abstract class GUIAbstractNewScrollBar extends GUIElement {

	private final GUIScrollableInterface scrollPanel;

	protected int cDist = UIScale.getUIScale().scale(8);

	protected int capDistance() {
		return cDist;
	}

	protected boolean grabWhenClickedInLane;

	private int orientation = GUIScrollablePanel.SCROLLABLE_VERTICAL;

	private GUIOverlay top;

	private GUIOverlay bottom;

	private GUIElement lane;

	private GUIOverlay bar;

	private int scClick = UIScale.getUIScale().scale(50);

	private int scArrow = UIScale.getUIScale().scale(1);

	private float scrollAmountClickInLane() {
		return scClick;
	}

	private float scrollAmountClickOnArrow() {
		return scArrow;
	}

	private boolean scrollContinuoslyWhenArrorPressed = true;

	private boolean firstDraw = true;

	private boolean barGrabbed;

	private boolean wasGrabbed;

	private boolean instantGrabScroll = true;

	private Sprite laneSpr;

	private boolean staticSlider;

	public GUIAbstractNewScrollBar(InputState state, GUIScrollableInterface scrollPanel, int orientation, boolean staticSlider) {
		super(state);
		this.scrollPanel = scrollPanel;
		this.orientation = orientation;
		Sprite tools = Controller.getResLoader().getSprite(getState().getGUIPath() + getStartEndTex());
		laneSpr = Controller.getResLoader().getSprite(getState().getGUIPath() + getLaneTex());
		top = new GUIOverlay(tools, getState());
		bottom = new GUIOverlay(tools, getState());
		if (isLaneRepeatable()) {
			lane = new GUITexDrawableArea(state, laneSpr.getMaterial().getTexture(), 0, 0);
		} else {
			lane = new GUIOverlay(laneSpr, getState());
		}
		lane.setMouseUpdateEnabled(true);
		bar = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + getBarTex()), getState());
		bar.setMouseUpdateEnabled(true);
		this.staticSlider = staticSlider;
	}

	public GUIAbstractNewScrollBar(InputState state, GUIScrollableInterface scrollPanel, boolean staticSlider) {
		super(state);
		this.scrollPanel = scrollPanel;
		Sprite tools = Controller.getResLoader().getSprite(getState().getGUIPath() + getStartEndTex());
		laneSpr = Controller.getResLoader().getSprite(getState().getGUIPath() + getLaneTex());
		top = new GUIOverlay(tools, getState());
		bottom = new GUIOverlay(tools, getState());
		if (isLaneRepeatable()) {
			lane = new GUITexDrawableArea(state, laneSpr.getMaterial().getTexture(), 0, 0);
		} else {
			lane = new GUIOverlay(laneSpr, getState());
		}
		lane.setMouseUpdateEnabled(true);
		bar = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + getBarTex()), getState());
		bar.setMouseUpdateEnabled(true);
		this.staticSlider = staticSlider;
	}

	protected abstract boolean isLaneRepeatable();

	protected abstract String getLaneTex();

	protected abstract String getStartEndTex();

	protected abstract String getBarTex();

	@Override
	public void cleanUp() {
	}

	public int getSettingsElementDistanceAfterButton() {
		// can be overwritten
		return UIScale.getUIScale().inset;
	}

	public int getTopPosX() {
		return (int) top.getPos().x;
	}

	public int getTopPosY() {
		return (int) top.getPos().y;
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();
		checkMouseInside();
		if (isHorizontal()) {
			top.setSpriteSubIndex(getHorizontalStart());
			bottom.setSpriteSubIndex(getHorizontalEnd());
			if (isLaneRepeatable()) {
				((GUITexDrawableArea) lane).setSpriteSubIndex(getHorizontalLane(), laneSpr.getMultiSpriteMaxX(), laneSpr.getMultiSpriteMaxY());
			} else {
				((GUIOverlay) lane).setSpriteSubIndex(getHorizontalLane());
			}
			bar.setSpriteSubIndex(getHorizontalBar());
		} else {
			top.setSpriteSubIndex(getVerticalStart());
			bottom.setSpriteSubIndex(getVerticalEnd());
			if (isLaneRepeatable()) {
				((GUITexDrawableArea) lane).setSpriteSubIndex(getVerticalLane(), laneSpr.getMultiSpriteMaxX(), laneSpr.getMultiSpriteMaxY());
			} else {
				((GUIOverlay) lane).setSpriteSubIndex(getVerticalLane());
			}
			bar.setSpriteSubIndex(getVerticalBar());
		}
		if (orientation == GUIScrollablePanel.SCROLLABLE_VERTICAL) {
			top.orientate(ORIENTATION_TOP | ORIENTATION_LEFT, 0, 0, (int) getWidth(), (int) getHeight());
			bottom.orientate(ORIENTATION_BOTTOM | ORIENTATION_LEFT, 0, 0, (int) getWidth(), (int) getHeight());
			lane.orientate(ORIENTATION_VERTICAL_MIDDLE | ORIENTATION_LEFT, 0, 0, (int) getWidth(), (int) getHeight());
			bar.orientate(ORIENTATION_VERTICAL_MIDDLE | ORIENTATION_LEFT, 0, 0, (int) getWidth(), (int) getHeight());
			bar.getPos().x -= getBarVerticalXSubstract();
		} else {
			top.orientate(ORIENTATION_LEFT | ORIENTATION_TOP, 0, 0, (int) getWidth(), (int) getHeight());
			bottom.orientate(ORIENTATION_RIGHT | ORIENTATION_TOP, 0, 0, (int) getWidth(), (int) getHeight());
			if (hasSeperateArrows()) {
				if (isSettingsElement()) {
					getSeperateArrowBottom().orientate(ORIENTATION_LEFT | ORIENTATION_VERTICAL_MIDDLE, 0, 0, (int) getWidth(), (int) getHeight());
					getSeperateArrowTop().orientate(ORIENTATION_LEFT | ORIENTATION_VERTICAL_MIDDLE, 0, 0, (int) getWidth(), (int) getHeight());
					getSettingsElement().orientate(ORIENTATION_LEFT | ORIENTATION_VERTICAL_MIDDLE, 0, 0, (int) getWidth(), (int) getHeight());
					getSettingsElement().getPos().x = getSeperateArrowBottom().getWidth();
					getSettingsElement().getPos().y = getHeight() * 0.5f - getSettingsElement().getHeight();
					getSeperateArrowBottom().getPos().x = getSettingsElement().getWidth() + getSeperateArrowBottom().getWidth();
					top.getPos().x += getSettingsElement().getWidth() + getSeperateArrowBottom().getWidth() + getSeperateArrowTop().getWidth() + getSettingsElementDistanceAfterButton();
				} else {
					getSeperateArrowBottom().orientate(ORIENTATION_RIGHT | ORIENTATION_VERTICAL_MIDDLE, 0, 0, (int) getWidth(), (int) getHeight());
					getSeperateArrowTop().orientate(ORIENTATION_LEFT | ORIENTATION_VERTICAL_MIDDLE, 0, 0, (int) getWidth(), (int) getHeight());
					top.getPos().x += getSeperateArrowTop().getWidth();
					bottom.getPos().x -= getSeperateArrowBottom().getWidth();
				}
			}
			lane.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_TOP, 0, 0, (int) getWidth(), (int) getHeight());
			bar.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_TOP, 0, 0, (int) getWidth(), (int) getHeight());
			bar.getPos().y -= getBarHorizontalYSubstract();
		}
		int spriteSize;
		if (isHorizontal()) {
			spriteSize = (int) Math.max(UIScale.getUIScale().scale(15), (bar.getWidth() * 0.5f));
		} else {
			spriteSize = (int) Math.max(UIScale.getUIScale().scale(15), (bar.getHeight() * 0.5f));
		}
		int barStart = UIScale.getUIScale().scale(3) + this.capDistance() + (isSettingsElement() ? getSettingsElementDistanceAfterButton() : 0);
		int barEnd = UIScale.getUIScale().scale(6);
		if (isHorizontal()) {
			// leave room at top and bottom
			float laneWidth;
			if (hasSeperateArrows()) {
				if (isSettingsElement()) {
					laneWidth = ((getWidth() - (getSeperateArrowTop().getWidth() + getSeperateArrowBottom().getWidth() + getSettingsElement().getWidth() + getSettingsElementDistanceAfterButton())) - 2 * spriteSize);
				} else {
					laneWidth = ((getWidth() - (getSeperateArrowTop().getWidth() + getSeperateArrowBottom().getWidth())) - 2 * spriteSize);
				}
			} else {
				laneWidth = (getWidth() - 2 * spriteSize);
			}
			if (isLaneRepeatable()) {
				if (hasSeperateArrows() && isSettingsElement()) {
					((GUITexDrawableArea) lane).setWidth((int) laneWidth);
					lane.getPos().x += top.getWidth();
				} else {
					((GUITexDrawableArea) lane).setWidth((int) ((bottom.getPos().x - top.getPos().x) - top.getWidth()));
				}
				((GUITexDrawableArea) lane).setHeight(laneSpr.getHeight());
			} else {
				lane.getScale().x = laneWidth / spriteSize;
			}
			if (!staticSlider) {
				bar.getScale().x = normalizeScale((1.0F / scrollPanel.getContentToPanelPercentageX()) * (laneWidth / bar.getHeight()));
				barEnd = (int) (bar.getScale().x * bar.getWidth()) - UIScale.getUIScale().scale(10);
			}
			bar.getPos().x = barStart + (int) (scrollPanel.getScolledPercentHorizontal() * (laneWidth - barEnd + spriteSize));
		} else {
			float laneHeight = (getHeight() - 2 * spriteSize);
			if (isLaneRepeatable()) {
				((GUITexDrawableArea) lane).setWidth(laneSpr.getWidth());
				((GUITexDrawableArea) lane).setHeight((int) ((bottom.getPos().y - top.getPos().y) - top.getHeight()));
			} else {
				lane.getScale().y = laneHeight / spriteSize + 0.04f;
			}
			if (!staticSlider) {
				bar.getScale().y = normalizeScale((1.0F / scrollPanel.getContentToPanelPercentageY()) * (laneHeight / bar.getHeight()));
				barEnd = (int) (bar.getScale().y * bar.getHeight()) - UIScale.getUIScale().scale(10);
			}
			bar.getPos().y = barStart + (scrollPanel.getScolledPercentVertical() * (laneHeight - barEnd));
		}
		lane.setMouseUpdateEnabled(scrollPanel.isInside());
		top.setMouseUpdateEnabled(scrollPanel.isInside());
		bottom.setMouseUpdateEnabled(scrollPanel.isInside());
		bar.setMouseUpdateEnabled(scrollPanel.isInside());
		if (hasSeperateArrows()) {
			getSeperateArrowBottom().setInside(false);
			getSeperateArrowBottom().draw();
			getSeperateArrowTop().setInside(false);
			getSeperateArrowTop().draw();
			if (isSettingsElement()) {
				getSettingsElement().setInside(false);
				getSettingsElement().draw();
			}
		}
		lane.setInside(false);
		if (isLaneRepeatable()) {
			((GUITexDrawableArea) lane).draw();
		} else {
			((GUIOverlay) lane).draw();
		}
		top.setInside(false);
		top.draw();
		bottom.setInside(false);
		bottom.draw();
		bar.setInside(false);
		bar.getPos().x = (int) bar.getPos().x;
		bar.getPos().y = (int) bar.getPos().y;
		bar.draw();
		if (bar.isInside() && bar.getRelMousePos().x < UIScale.getUIScale().scale(6)) {
			// make area to grab as small as the bar
			bar.setInside(false);
		}
		if (!barGrabbed && getTopArrowClickPane().isInside()) {
			if (scrollContinuoslyWhenArrorPressed && Mouse.isPrimaryMouseDownUtility()) {
				if (isHorizontal()) {
					scrollPanel.scrollHorizontal(-scrollAmountClickOnArrow());
				} else {
					scrollPanel.scrollVertical(-scrollAmountClickOnArrow());
				}
			} else if (!scrollContinuoslyWhenArrorPressed) {
				for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
					if (e.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(12);
						if (isHorizontal()) {
							scrollPanel.scrollHorizontal(-scrollAmountClickOnArrow());
						} else {
							scrollPanel.scrollVertical(-scrollAmountClickOnArrow());
						}
					}
				}
			}
		}
		if (!barGrabbed && getBottomArrowClickPane().isInside() && isActive()) {
			if (scrollContinuoslyWhenArrorPressed && Mouse.isPrimaryMouseDownUtility() && (scrollPanel.getScrollingListener() == null || scrollPanel.getScrollingListener().activeScrolling())) {
				if (isHorizontal()) {
					scrollPanel.scrollHorizontal(scrollAmountClickOnArrow());
				} else {
					scrollPanel.scrollVertical(scrollAmountClickOnArrow());
				}
			} else if (!scrollContinuoslyWhenArrorPressed) {
				for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
					if (e.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(13);
						if (isHorizontal()) {
							scrollPanel.scrollHorizontal(scrollAmountClickOnArrow());
						} else {
							scrollPanel.scrollVertical(scrollAmountClickOnArrow());
						}
					}
				}
			}
		}
		float scaleX = GlUtil.getModelviewScaleX(new Vector3f()).length();
		float scaleY = GlUtil.getModelviewScaleY(new Vector3f()).length();
		if (!grabWhenClickedInLane && isMouseInLane() && !bar.isInside() && (scrollPanel.getScrollingListener() == null || scrollPanel.getScrollingListener().activeScrolling())) {
			for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
				if (e.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(14);
					if (isHorizontal()) {
						if (scrollPanel.getRelMousePos().x > bar.getPos().x * scaleX * scaleX) {
							scrollPanel.scrollHorizontal(scrollAmountClickInLane());
						} else {
							scrollPanel.scrollHorizontal(-scrollAmountClickInLane());
						}
					} else {
						if (scrollPanel.getRelMousePos().y > bar.getPos().y * scaleY * scaleY) {
							scrollPanel.scrollVertical(scrollAmountClickInLane());
						} else {
							scrollPanel.scrollVertical(-scrollAmountClickInLane());
						}
					}
				}
			}
		}
		if (bar.isInside() || (grabWhenClickedInLane && isMouseInLane())) {
			for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
				AudioController.fireAudioEventID(15);
				if (e.pressedLeftMouse()) {
					barGrabbed = true;
					break;
				}
			}
		}
		if (!Mouse.isPrimaryMouseDownUtility() || !(scrollPanel.getScrollingListener() == null || scrollPanel.getScrollingListener().activeScrolling())) {
			barGrabbed = false;
		}
		if ((barGrabbed && !instantGrabScroll)) {
			if (isHorizontal()) {
				float width = getWidth();
				if (hasSeperateArrows()) {
					if (isSettingsElement()) {
						width -= (capDistance() + getSeperateArrowTop().getWidth() + getSeperateArrowBottom().getWidth() + getSettingsElement().getWidth() + getSettingsElementDistanceAfterButton() - spriteSize + UIScale.getUIScale().scale(3));
					} else {
						width -= (getSeperateArrowTop().getWidth());
					}
				}
				float v = Math.max(0, Math.min(1.0f, ((getRelMousePos().x - spriteSize - capDistance()) / (scaleX * scaleX)) / (width - (2 * spriteSize / (scaleX * scaleX)))));
				scrollPanel.scrollHorizontalPercentTmp(v);
			} else {
				float height = getHeight();
				if (hasSeperateArrows()) {
					height -= (getSeperateArrowBottom().getHeight() + getSeperateArrowTop().getHeight());
				}
				float v = Math.max(0, Math.min(1.0f, ((getRelMousePos().y - spriteSize) / (scaleY * scaleY)) / (height - (2 * spriteSize / (scaleY * scaleY)))));
				scrollPanel.scrollVerticalPercentTmp(v);
			}
		}
		if ((barGrabbed && instantGrabScroll) || (wasGrabbed && !barGrabbed && !instantGrabScroll)) {
			if (isHorizontal()) {
				float width = getWidth();
				if (hasSeperateArrows()) {
					if (isSettingsElement()) {
						width -= (getSeperateArrowTop().getWidth() + getSeperateArrowBottom().getWidth() + getSettingsElement().getWidth() + getSettingsElementDistanceAfterButton() - spriteSize + UIScale.getUIScale().scale(3));
					} else {
						width -= (getSeperateArrowTop().getWidth());
					}
				}
				float v = Math.max(0, Math.min(1.0f, ((getRelMousePos().x - spriteSize - capDistance()) / (scaleX * scaleX)) / (width - (2 * spriteSize / (scaleX * scaleX)))));
				scrollPanel.scrollHorizontalPercent(v);
			} else {
				float height = getHeight();
				if (hasSeperateArrows()) {
					height -= (getSeperateArrowBottom().getHeight() + getSeperateArrowTop().getHeight());
				}
				float v = Math.max(0, Math.min(1.0f, ((getRelMousePos().y - spriteSize) / (scaleY * scaleY)) / (height - (2 * spriteSize / (scaleY * scaleY)))));
				scrollPanel.scrollVerticalPercent(v);
			}
		}
		wasGrabbed = barGrabbed;
		if (barGrabbed) {
			BasicInputController.grabbedObjectLeftMouse = this;
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public boolean isActive() {
		return super.isActive() && scrollPanel.isActive();
	}

	public GUIActivatableTextBar getSettingsElement() {
		assert (isSettingsElement());
		return null;
	}

	public boolean isSettingsElement() {
		return false;
	}

	private float normalizeScale(float scaleX) {
		scaleX = Math.max(0.25f, scaleX);
		return scaleX;
	}

	@Override
	public void onInit() {
		if (!firstDraw) {
			return;
		}
		top.setMouseUpdateEnabled(true);
		bottom.setMouseUpdateEnabled(true);
		bar.setMouseUpdateEnabled(true);
		firstDraw = false;
	}

	@Override
	protected void doOrientation() {
	}

	@Override
	public float getHeight() {
		assert (top != null);
		assert (scrollPanel != null);
		return orientation == GUIScrollablePanel.SCROLLABLE_VERTICAL ? scrollPanel.getScrollBarHeight() : top.getHeight();
	}

	@Override
	public float getWidth() {
		return orientation == GUIScrollablePanel.SCROLLABLE_HORIZONTAL ? scrollPanel.getScrollBarWidth() - ((isHorizontal() && scrollPanel.isVerticalActive()) ? UIScale.getUIScale().scale(16) : 0) : top.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	protected abstract int getVerticalStart();

	protected abstract int getVerticalEnd();

	protected abstract int getVerticalLane();

	protected abstract int getVerticalBar();

	protected abstract int getHorizontalStart();

	protected abstract int getHorizontalEnd();

	protected abstract int getHorizontalLane();

	protected abstract int getHorizontalBar();

	private GUIElement getBottomArrowClickPane() {
		if (hasSeperateArrows()) {
			return getSeperateArrowBottom();
		}
		return bottom;
	}

	private GUIElement getTopArrowClickPane() {
		if (hasSeperateArrows()) {
			return getSeperateArrowTop();
		}
		return top;
	}

	protected abstract boolean hasSeperateArrows();

	protected abstract GUIOverlay getSeperateArrowBottom();

	protected abstract GUIOverlay getSeperateArrowTop();

	private boolean isMouseInLane() {
		return lane.isInside() || (hasSeperateArrows() && (top.isInside() || bottom.isInside()));
	}

	protected abstract int getSpriteSize();

	protected abstract float getBarHorizontalYSubstract();

	protected abstract float getBarVerticalXSubstract();

	public boolean isHorizontal() {
		return orientation == GUIScrollablePanel.SCROLLABLE_HORIZONTAL;
	}

	public boolean isVertical() {
		return orientation == GUIScrollablePanel.SCROLLABLE_VERTICAL;
	}

	/**
	 * @param scrollAmountClickInLane the scrollAmountClickInLane to set
	 */
	public void setScrollAmountClickInLaneUIScaled(int scrollAmountClickInLane) {
		this.scClick = UIScale.getUIScale().scale(scrollAmountClickInLane);
	}

	/**
	 * @return the scrollAmountClickOnArrow
	 */
	public float getScrollAmountClickOnArrow() {
		return scrollAmountClickOnArrow();
	}

	/**
	 * @param scrollAmountClickOnArrow the scrollAmountClickOnArrow to set
	 */
	public void setScrollAmountClickOnArrowUIScaled(int scrollAmountClickOnArrow) {
		this.scArrow = UIScale.getUIScale().scale(scrollAmountClickOnArrow);
	}

	/**
	 * @return the scrollContinuoslyWhenArrorPressed
	 */
	public boolean isScrollContinuoslyWhenArrorPressed() {
		return scrollContinuoslyWhenArrorPressed;
	}

	/**
	 * @param scrollContinuoslyWhenArrorPressed the scrollContinuoslyWhenArrorPressed to set
	 */
	public void setScrollContinuoslyWhenArrorPressed(boolean scrollContinuoslyWhenArrorPressed) {
		this.scrollContinuoslyWhenArrorPressed = scrollContinuoslyWhenArrorPressed;
	}

	/**
	 * @return the instantGrabScroll
	 */
	public boolean isInstantGrabScroll() {
		return instantGrabScroll;
	}

	/**
	 * @param instantGrabScroll the instantGrabScroll to set
	 */
	public void setInstantGrabScroll(boolean instantGrabScroll) {
		this.instantGrabScroll = instantGrabScroll;
	}
}
