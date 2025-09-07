package org.schema.schine.graphicsengine.forms.gui;

import java.util.List;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.network.client.ClientState;

public class GUIChatLogPanel extends GUIElement {

	private GUIColoredRectangle background;
	private GUIScrollableTextPanel scroll;
	private GUITextOverlay text;
	private GUIAnchor textAnc;
	public GUIChatLogPanel(int width, int height,
	                       FontInterface fontSize, GUIResizableElement dependent, ClientState state) {
		super(state);
		if (isNewHud()) {
//			FontSize.SMALL_15
			text = new GUITextOverlay(fontSize, state) {

				@Override
				public float getHeight() {
					return getTextHeight();
				}

			};
			if (dependent != null) {
				text.autoWrapOn = dependent;
			}
		} else {
			text = new GUITextOverlay(fontSize, state);
		}
		text.setLimitTextDraw(100);
		text.setText(getState().getGeneralChatLog());
//		text.setBeginTextAtLast(true);

		textAnc = new GUIAnchor(getState());
		textAnc.attach(text);

		scroll = new GUIScrollableTextPanel(width, height, dependent, state) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIScrollableTextPanel#isScrollLock()
			 */
			@Override
			public boolean isScrollLock() {
				return true;
			}

		};
		scroll.setScrollable(0);
		background = new GUIColoredRectangle(state, width, height, dependent, new Vector4f(1, 1, 1, 0.1f));
		background.rounded = 1;
	}

	public void setText(List<Object> text) {
		this.text.setText(text);
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		background.draw();
	}

	@Override
	public void onInit() {

		background.rounded = 5;

		background.attach(scroll);
		scroll.setContent(text);

		background.onInit();
		scroll.onInit();
		text.onInit();
	}

	@Override
	public float getHeight() {
				return 0;
	}

	@Override
	public float getWidth() {
				return 0;
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

	public void onChatDirty() {
		text.updateTextSize();
//		textAnc.setWidth(text.getMaxLineWidth());
//		textAnc.setHeight(text.getTextHeight());
	}

}
