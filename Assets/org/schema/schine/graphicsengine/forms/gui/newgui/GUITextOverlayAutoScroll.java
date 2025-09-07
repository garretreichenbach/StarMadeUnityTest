package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;

public class GUITextOverlayAutoScroll extends GUITextOverlay {

	public static final float TEXT_SCROLL_SPEED = 30f;
	public static final float TEXT_SCROLL_DELAY = 1.5f;
	public static final float TEXT_SCROLL_DELAY_BACK = 0.5f;
	private static final int SCROLL_STATE_START = 0;
	private static final int SCROLL_STATE_LEFT_HOLD = 1;
	private static final int SCROLL_STATE_RIGHT = 2;
	private static final int SCROLL_STATE_RIGHT_HOLD = 3;
	private static final int SCROLL_STATE_LEFT = 4;
	public float scrollSpeed = TEXT_SCROLL_SPEED;
	public float scrollDelay = TEXT_SCROLL_DELAY;
	public float scrollDelayBack = TEXT_SCROLL_DELAY_BACK;
	private int scrollState;
	private GUIDrawnTimerInterface tdInterface;
	private GUIScrollablePanel scrollPanel;
	private int rightPos;
	private int rightPosB;

	public GUITextOverlayAutoScroll(int width, int height, GUIScrollablePanel scrollPanel, GUIDrawnTimerInterface tdInterface, InputState state) {
		super(state);
		scrollPanel.setContent(this);
		this.scrollPanel = scrollPanel;
		this.tdInterface = tdInterface;

		scrollPanel.setLeftRightClipOnly = true;
		scrollPanel.setScrollable(0);
	}

	public GUITextOverlayAutoScroll(int width, int height, FontInterface font,
	                                GUIScrollablePanel scrollPanel, GUIDrawnTimerInterface tdInterface, InputState state) {
		super(font, state);
		scrollPanel.setContent(this);
		this.scrollPanel = scrollPanel;
		this.tdInterface = tdInterface;

		scrollPanel.setLeftRightClipOnly = true;
		scrollPanel.setScrollable(0);
	}

	public GUITextOverlayAutoScroll(int width, int height, FontInterface font,
	                                Color color, GUIScrollablePanel scrollPanel, GUIDrawnTimerInterface tdInterface, ClientState state) {
		super(font, color, state);
		scrollPanel.setContent(this);
		this.scrollPanel = scrollPanel;
		this.tdInterface = tdInterface;

		scrollPanel.setLeftRightClipOnly = true;
		scrollPanel.setScrollable(0);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#draw()
	 */
	@Override
	public void draw() {
		if (getMaxLineWidth() > scrollPanel.getWidth()) {
			switch (scrollState) {
				case (SCROLL_STATE_START):
					tdInterface.setTimeDrawn(0);
					scrollState = SCROLL_STATE_LEFT_HOLD;
					break;
				case (SCROLL_STATE_LEFT_HOLD):
					if (tdInterface.getTimeDrawn() >= scrollDelay) {
						tdInterface.setTimeDrawn(0);
						scrollState = SCROLL_STATE_RIGHT;
					}
					break;
				case (SCROLL_STATE_RIGHT):
					if (getPos().x + getMaxLineWidth() > scrollPanel.getWidth()) {
						getPos().x = -(int) (tdInterface.getTimeDrawn() * scrollSpeed);

						if (getPos().x + getMaxLineWidth() < scrollPanel.getWidth()) {
							getPos().x = scrollPanel.getWidth() - getMaxLineWidth();
							assert (getPos().x < 0);
							tdInterface.setTimeDrawn(0);
							scrollState = SCROLL_STATE_RIGHT_HOLD;
							rightPos = (int) getPos().x;
							rightPosB = (int) getPos().x;

						}
					} else {
						tdInterface.setTimeDrawn(0);
						scrollState = SCROLL_STATE_RIGHT_HOLD;
						rightPos = (int) getPos().x;
						rightPosB = (int) getPos().x;
					}
					break;
				case (SCROLL_STATE_RIGHT_HOLD):
					getPos().x = rightPos;
					if (tdInterface.getTimeDrawn() >= scrollDelayBack) {
						tdInterface.setTimeDrawn(0);
						scrollState = SCROLL_STATE_LEFT;
					}
					break;
				case (SCROLL_STATE_LEFT):
					if (rightPos < 0) {
						rightPos = rightPosB + ((int) (tdInterface.getTimeDrawn() * scrollSpeed));
						getPos().x = Math.min(0, rightPos);
					} else {
						tdInterface.setTimeDrawn(0);
						scrollState = SCROLL_STATE_START;
					}
					break;
			}
		}
		super.draw();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#onDirty()
	 */
	@Override
	public void onDirty() {
		scrollState = SCROLL_STATE_START;
	}
}
