package org.schema.schine.graphicsengine.forms.gui;

import org.lwjgl.glfw.GLFW;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUINewScrollBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Keyboard;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class GUIScrollablePanel extends GUIResizableElement implements GUIScrollableInterface, TooltipProvider {
	private static final int SCROLL_AMOUNT() {
		return UIScale.getUIScale().scale(30);
	}
	private static final float clipInset() {
		return UIScale.getUIScale().scale( 16);
	}
	public static int SCROLLABLE_NONE = 0;
	public static int SCROLLABLE_VERTICAL = 1;
	public static int SCROLLABLE_HORIZONTAL = 2;
	private final Vector4f clip;
	public GUIElement dependent;
	public int dependendHeightDiff;
	public int dependendWidthDiff;
	public boolean setLeftRightClipOnly;
	private GUIElement content;
	private float scrollY;
	private float scrollX = 0;
	private GUIElement verticalScrollBar;
	private GUIElement horizontalScrollBar;
	private int scrollable = SCROLLABLE_VERTICAL;
	private ScrollingListener scrollingListener;
	private float width;
	private float height;
	private boolean firstDraw = true;
	private boolean verticalActive;
	private boolean horizontalActive;
	private boolean scrollLocking;
	private Vector3f origPos = new Vector3f();

	public GUIScrollablePanel(float width, float height, GUIElement dependent, InputState state) {
		super(state);

		clip = new Vector4f(0, width, 0, height);
		this.dependent = dependent;
		this.setWidth(width);
		this.setHeight(height);
	}

	public GUIScrollablePanel(float width, float height, InputState state) {
		this(width, height, null, state);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#checkMouseInside()
	 */
	@Override
	public void checkMouseInside() {
		super.checkMouseInside();
		if (isInside() && (scrollingListener == null || scrollingListener.activeScrolling()) && isActive()) {
			for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
				
				if (e.dWheel != 0) {

					int amount = -((int) Math.signum(e.dWheel) * SCROLL_AMOUNT());
					
					//Scroll horizontal when holding a shift key, or when vertical scroll is not available
					if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) || !verticalActive) {
						scrollHorizontal(amount);
					} else {
						scrollVertical(amount);
					}
					
				}
			}
		}
	}

	@Override
	public void doOrientation() {
		if ((scrollable & SCROLLABLE_VERTICAL) == SCROLLABLE_VERTICAL) {
			verticalScrollBar.orientate(ORIENTATION_RIGHT, 0, 0, (int) width, (int) height);

		}
		if ((scrollable & SCROLLABLE_HORIZONTAL) == SCROLLABLE_HORIZONTAL) {
			horizontalScrollBar.orientate(ORIENTATION_BOTTOM | ORIENTATION_LEFT, 0, 0, (int) width, (int) height);

		}
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	@Override
	public boolean isActive() {
		if (dependent != null) {
			return dependent.isActive();
		}
		return true;
	}

	/**
	 * @param width the width to set
	 */
	@Override
	public void setWidth(float width) {
		this.width = width;
		clip.set(0, width, 0, height);
	}

	/**
	 * @param height the height to set
	 */
	@Override
	public void setHeight(float height) {
		this.height = height;
		clip.set(0, width, 0, height);
	}

	private void checkScrollSize() {
		//		if(content != null){
		//			scrollX = Math.min(Math.max(0, scrollX), content.getWidth()-width);
		//			scrollY = Math.min(Math.max(0, scrollY), content.getHeight()-height);
		//		}
	}

	@Override
	public void cleanUp() {
		if(content != null){
			content.cleanUp();
		}
		if(verticalScrollBar != null){
			verticalScrollBar.cleanUp();
		}
		if(horizontalScrollBar != null){
			horizontalScrollBar.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		if (dependent != null) {
			setWidth(dependent.getWidth() + dependendWidthDiff);
			setHeight(dependent.getHeight() + dependendHeightDiff);
		}
		
		
		//this fixes any content changes if the croll is too high
		scrollHorizontal(0);
		scrollVertical(0);
		
		
		doOrientation();
		GlUtil.glPushMatrix();
		clip.set(0, width, 0, height);

		if (verticalActive) {
			clip.y -= clipInset();
		}
		if (horizontalActive) {
			clip.w -= clipInset();
		}

		origPos.set(content.getPos());
		
		content.getPos().x -= scrollX;
		content.getPos().y -= scrollY;
		
		float sXB = scrollX;
		float sYB = scrollY;
		GlUtil.addScroll(sXB, sYB);
		
		transform();

		if (setLeftRightClipOnly) {
			GlUtil.pushClipLR(clip);
		} else {
			GlUtil.pushClipSubtract(clip);
		}

		checkMouseInside();

		if(scrollLocking && isInside()){
			scrollLock();
		}
		GlUtil.glPushMatrix();
		content.draw();
		GlUtil.subScroll(sXB, sYB);
		if (setLeftRightClipOnly) {
			GlUtil.popClipLR(clip);
		} else {
			GlUtil.popClip();
		}
		GlUtil.glPopMatrix();
		if ((scrollable & SCROLLABLE_VERTICAL) == SCROLLABLE_VERTICAL) {
			if (content.getHeight() > this.height) {
				verticalActive = true;
				verticalScrollBar.draw();
			} else {
				verticalActive = false;
				scrollY = 0;
			}
		} else {
			verticalActive = false;
		}
		if ((scrollable & SCROLLABLE_HORIZONTAL) == SCROLLABLE_HORIZONTAL) {
			if (content.getWidth() > this.width) {
				horizontalActive = true;
				horizontalScrollBar.draw();
			} else {
				horizontalActive = false;
				scrollX = 0;
			}
		} else {
			horizontalActive = false;
		}
		

		content.setPos(origPos);
		GlUtil.glPopMatrix();
		
		
	}

    @Override
	public float getContentToPanelPercentageY() {
        return content.getHeight() / this.height;
    }

    @Override
	public float getContentToPanelPercentageX() {
        return content.getWidth() / this.width;
    }

	@Override
	public void onInit() {
		if (!firstDraw) {
			return;
		}
		content.onInit();
		if ((scrollable & SCROLLABLE_VERTICAL) == SCROLLABLE_VERTICAL) {
			verticalScrollBar = new GUINewScrollBar(getState(), this, SCROLLABLE_VERTICAL, false);
			verticalScrollBar.onInit();

		}
		if ((scrollable & SCROLLABLE_HORIZONTAL) == SCROLLABLE_HORIZONTAL) {
			//			System.err.println("ADDING HORIZONTAL Scroll");
			horizontalScrollBar = new GUINewScrollBar(getState(), this, SCROLLABLE_HORIZONTAL, false);

			horizontalScrollBar.onInit();

		}
		doOrientation();
		firstDraw = false;

	}

	/**
	 * @return the clip
	 */
	public Vector4f getClip() {
		return clip;
	}

	/**
	 * @return the content
	 */
	public GUIElement getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(GUIElement content) {

		if (this.content != null && getChilds().contains(this.content)) {
			this.detach(this.content);
		}
		this.content = content;
		this.attach(content);

		checkScrollSize();
	}

	@Override
	public float getScolledPercentHorizontal() {
		return scrollX / (content.getWidth() - width);
	}

	@Override
	public float getScolledPercentVertical() {
		return scrollY / (content.getHeight() - height);
	}

	/**
	 * scrolls to the horizontal edges of content
	 *
	 * @param step
	 */
	@Override
	public void scrollHorizontal(float step) {
		if(isScrollLocked()){
			return;
		}
		if (content.getWidth() > this.width) {
			scrollX = Math.min(Math.max(0, scrollX + step), content.getWidth() - width);
		}
	}

	public boolean isScrollLocked(){
		return getState().getController().getInputController().isScrollLockOn(this);
	}
	/**
	 * scrolls to the vertical edges of content
	 *
	 * @param step
	 */
	@Override
	public void scrollVertical(float step) {
		if(isScrollLocked()){
			return;
		}
		if (content.getHeight() > this.height) {
			scrollY = Math.min(Math.max(0, scrollY + step), content.getHeight() - height);
		}
	}

	/**
	 * @return the scrollingListener
	 */
	@Override
	public ScrollingListener getScrollingListener() {
		return scrollingListener;
	}

	/**
	 * scrolls to the horizontal edges of content
	 *
	 * @param step
	 */
	@Override
	public void scrollHorizontalPercent(float percent) {
		if(isScrollLocked()){
			return;
		}
		if (content.getWidth() > this.width) {
			scrollX = percent * ((content.getWidth() - width));
		}
	}

	/**
	 * scrolls to the vertical edges of content
	 *
	 * @param step
	 */
	@Override
	public void scrollVerticalPercent(float percent) {
		if(isScrollLocked()){
			return;
		}
		if (content.getHeight() > this.height) {
			scrollY = percent * ((content.getHeight() - height));
		}
	}

	@Override
	public float getScrollBarHeight() {
		return height;
	}

	@Override
	public float getScrollBarWidth() {
		return width;
	}

	@Override
	public void scrollHorizontalPercentTmp(float v) {
	}

	@Override
	public void scrollVerticalPercentTmp(float v) {
	}

	@Override
	public boolean isVerticalActive() {
		return verticalActive;
	}

	/**
	 * @param scrollingListener the scrollingListener to set
	 */
	public void setScrollingListener(ScrollingListener scrollingListener) {
		this.scrollingListener = scrollingListener;
	}

	/**
	 * @return the scrollX
	 */
	public float getScrollX() {
		return scrollX;
	}

	/**
	 * @return the scrollY
	 */
	public float getScrollY() {
		return scrollY;
	}

	public void reset() {
		scrollX = 0;
		scrollY = 0;

	}

	@Override
	public void drawToolTip() {
		if (content != null && content instanceof TooltipProvider) {
			((TooltipProvider) content).drawToolTip();
		}
	}

	/**
	 * @return the scrollable
	 */
	public int getScrollable() {
		return scrollable;
	}

	/**
	 * @param scrollable the scrollable to set
	 */
	public void setScrollable(int scrollable) {
		this.scrollable = scrollable;
	}

	/**
	 * @param width the width to set
	 */
	public float getClipWidth() {
		return width - (verticalActive ? clipInset() : 0);
	}

	/**
	 * @param height the height to set
	 */
	public float setClipHeight() {
		return height - (horizontalActive ? clipInset() : 0);
	}

	public boolean isInsideScrollBar() {
		return (verticalScrollBar != null && ((scrollable & SCROLLABLE_VERTICAL) == SCROLLABLE_VERTICAL) && verticalScrollBar.isInside()) ||
				(horizontalScrollBar != null && (scrollable & SCROLLABLE_HORIZONTAL) == SCROLLABLE_HORIZONTAL && horizontalScrollBar.isInside());
	}

	public void scrollLock() {
		getState().getController().getInputController().scrollLockOn(this);		
	}

	/**
	 * blocks other scrol panels from scrolling while mouse inside this panel
	 * @param b
	 */
	public void setScrollLocking(boolean b) {
		scrollLocking = b;
	}
}
