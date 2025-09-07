package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITooltipBackground;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUIToolTipGraphics extends GUIElement {

	private GUITooltipBackground bg;
	private GUITextOverlay text;
	private GUIScrollablePanel textScroll;
	private float speed = 1200;
	private Object toolTipText = "";
	private float targetWidth;
	private float currentWidth;
	private Vector4f color = new Vector4f(1f, 1f, 1f, 0.75f);
	private boolean init;
	public boolean shortenText;

	public GUIToolTipGraphics(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {
		if (bg != null) {
			bg.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if (currentWidth > 0) {

			GlUtil.glPushMatrix();

			setInside(false);

			transform();

			bg.setColor(color);
			text.setColor(color);

			bg.setWidth((int) currentWidth);
			bg.setHeight(text.getTextHeight() + UIScale.getUIScale().inset);
			text.getPos().x = UIScale.getUIScale().inset;
			text.getPos().y = UIScale.getUIScale().inset;

//			bg.getPos().x = (int)(-currentWidth/2f);
			textScroll.getPos().x = (int) bg.getPos().x;

			bg.draw();
			textScroll.draw();

			if (isMouseUpdateEnabled()) {
				checkMouseInside();
			}

			for (AbstractSceneNode f : getChilds()) {
				f.draw();
			}
			GlUtil.glPopMatrix();
		}
	}

	@Override
	public void onInit() {
		if (init) {
			return;
		}
		bg = new GUITooltipBackground(getState(), UIScale.getUIScale().scale(100), UIScale.getUIScale().h);

		text = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		text.setTextSimple(new Object(){
			@Override
			public String toString(){
				return getToolTipText().toString();
			}
		});
		bg.onInit();

		textScroll = new GUIScrollablePanel(10, 10, bg, getState());
		textScroll.setContent(text);
		init = true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
//		System.err.println("TE "+text.getText()+": "+getToolTipText().length());
		if (text != null && toolTipText.toString().length() > 0) {
//			System.err.println("TEXT: "+text.getText()+"::: "+targetWidth);
			targetWidth = text.getMaxLineWidth() + UIScale.getUIScale().inset * 2;
		} else {
			targetWidth = 0;
		}
		if (currentWidth < targetWidth) {
			float newVal = currentWidth + timer.getDelta() * speed;
			currentWidth = Math.min(targetWidth, newVal);
		} else if (currentWidth > targetWidth) {
			float newVal = currentWidth - timer.getDelta() * speed;
			currentWidth = Math.max(targetWidth, newVal);
		}
	}

	@Override
	public float getHeight() {
		if (bg == null) {
			return UIScale.getUIScale().h;
		}
		return bg.getHeight();
	}

	@Override
	public float getWidth() {
		if (bg == null) {
			return 100;
		}
		return bg.getWidth();
	}

	/**
	 * @return the toolTipText
	 */
	public Object getToolTipText() {
		return toolTipText;
	}

	/**
	 * @param toolTipText the toolTipText to set
	 */
	public void setToolTipText(Object toolTipText) {
		this.toolTipText = toolTipText;
	}

	public boolean isStillVisible() {
		return currentWidth > 0;
	}
}