package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITooltipBackground;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;

public class GUIToolTipGraphicsStatic extends GUIElement {

	public GUIElement dependent;
	private GUITooltipBackground bg;
	private GUITextOverlay text;
	private GUIScrollablePanel textScroll;
	private Object toolTipText = "";
	private Vector4f color = new Vector4f(1f, 1f, 1f, 0.75f);
	private boolean init;
	private FontInterface font;

	public GUIToolTipGraphicsStatic(InputState state, FontInterface font) {
		super(state);
		this.font = font;
		bg = new GUITooltipBackground(getState(), 100, UIScale.getUIScale().h);
	}

	@Override
	public void cleanUp() {
		if(bg != null) {
			bg.cleanUp();
		}
	}

	@Override
	public void draw() {
		if(!init) {
			onInit();
		}
		assert (bg != null);
		assert (dependent != null);
		GlUtil.glPushMatrix();

		setInside(false);

		transform();

		bg.setColor(color);
		text.setColor(color);

		bg.setWidth((int) dependent.getWidth());
		bg.setHeight(text.getTextHeight() + UIScale.getUIScale().inset);
		text.getPos().x = UIScale.getUIScale().inset;
		text.getPos().y = UIScale.getUIScale().inset;

//			bg.getPos().x = (int)(-currentWidth/2f);
		textScroll.getPos().x = (int) bg.getPos().x;

		bg.draw();
		textScroll.draw();

		if(isMouseUpdateEnabled()) {
			checkMouseInside();
		}

		for(AbstractSceneNode f : getChilds()) {
			f.draw();
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if(init) {
			return;
		}
		bg = new GUITooltipBackground(getState(), 100, UIScale.getUIScale().h);

		text = new GUITextOverlay(font, getState());
		text.setTextSimple(new Object() {
			@Override
			public String toString() {
				return getToolTipText().toString();
			}
		});
		bg.onInit();

		textScroll = new GUIScrollablePanel(10, 10, bg, getState());
		textScroll.setContent(text);
		init = true;
	}

	@Override
	public float getHeight() {
		return bg.getHeight();
	}

	@Override
	public float getWidth() {
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

}