package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class GUIInnerTextbox extends GUIAnchor {

	public int tbHeight;
	private GUIInnerTextboxRim rim;
	private GUIInnerTextboxShadow shadow;
	private GUIAnchor content;
	private boolean init;
	
	public static final int INSET = 2;

	public GUIInnerTextbox(InputState state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#cleanUp()
	 */
	@Override
	public void cleanUp() {
		if (rim != null) {
			rim.cleanUp();
		}
		if (shadow != null) {
			shadow.cleanUp();
		}
		if (content != null) {
			content.cleanUp();
		}
		super.cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		assert (init);
		content.setWidth(getWidth() - INSET*2);
		content.setHeight(getHeight() - INSET*2);

		GlUtil.glPushMatrix();
		transform();
		rim.draw();
		GlUtil.glPopMatrix();

		GlUtil.glPushMatrix();
		GUIElement.renderModeSet = GUIElement.RENDER_MODE_SHADOW;
		super.draw();
		GUIElement.renderModeSet = GUIElement.RENDER_MODE_NORMAL;
		GlUtil.glPopMatrix();

		GlUtil.glPushMatrix();
		transform();
		shadow.draw();
		GlUtil.glPopMatrix();

		super.draw();
	}
	@Override
	public void drawWithoutTransform() {
		assert (init);
		content.setWidth(getWidth() - INSET*2);
		content.setHeight(getHeight() - INSET*2);
		GlUtil.glPushMatrix();
		rim.draw();
		GlUtil.glPopMatrix();

		GlUtil.glPushMatrix();
		GUIElement.renderModeSet = GUIElement.RENDER_MODE_SHADOW;
		super.drawWithoutTransform();
		GUIElement.renderModeSet = GUIElement.RENDER_MODE_NORMAL;
		GlUtil.glPopMatrix();

		GlUtil.glPushMatrix();
		shadow.draw();
		GlUtil.glPopMatrix();

		super.draw();
	}
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		this.rim = new GUIInnerTextboxRim(getState(), this, 0);
		this.shadow = new GUIInnerTextboxShadow(getState(), this, 2);
		this.content = (new GUIAnchor(getState()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
			 */
			@Override
			public boolean isActive() {
				return GUIInnerTextbox.this.isActive();
			}

		});

		content.setPos(INSET, INSET, 0);

		attach(content);
		
		content.setWidth(getWidth() - INSET*2);
		content.setHeight(getHeight() - INSET*2);
		
		init = true;
	}

	/**
	 * @return the content
	 */
	public GUIAnchor getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(GUIAnchor content) {
		if (this.content != null) {
			detach(this.content);
		}

		this.content = content;
		this.content.activationInterface = GUIInnerTextbox.this::isActive;
		attach(this.content);
	}
	public void setTint(float r, float g, float b, float a){
		rim.setTint(r, g, b, a);
		shadow.setTint(r, g, b, a);
	}
}
