package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.InputState;

public class GUIAnchor extends GUIResizableElement {

	public GUIActiveInterface activationInterface;
	protected float height;
	protected float width;

	public GUIAnchor(InputState state) {
		super(state);
	}

	public GUIAnchor(InputState state, float width, float height) {
		this(state);
		this.width = width;
		this.height = height;
	}

	@Override
	public void cleanUp() {
		for (AbstractSceneNode e : getChilds()) {
			e.cleanUp();
		}
	}

	@Override
	public void draw() {
		if(translateOnlyMode){
			translate();
		}else{
			GlUtil.glPushMatrix();
			transform();
		}
		final int size = getChilds().size();
		for (int i = 0; i < size; i++) {
			AbstractSceneNode e = getChilds().get(i);
			e.draw();
		}
		if(isRenderable()) {
			setInside(false);
			if (isRenderable() && isMouseUpdateEnabled()) {
				checkMouseInside();
			} 
		}
		if(translateOnlyMode){
			translateBack();
		}else{
			GlUtil.glPopMatrix();
		}
	}
	public void drawWithoutTransform() {
		GlUtil.glPushMatrix();
		final int size = getChilds().size();
		for (int i = 0; i < size; i++) {
			AbstractSceneNode e = getChilds().get(i);
			e.draw();
		}
		if (isRenderable() && isMouseUpdateEnabled()) {
			checkMouseInside();
		} 
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
	}

	@Override
	protected void doOrientation() {
	}

	@Override
	public float getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	@Override
	public void setHeight(float height) {
		this.height = height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	@Override
	public void setWidth(float width) {
		this.width = width;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
	 */
	@Override
	public boolean isActive() {
		return activationInterface == null ? super.isActive() : activationInterface.isActive();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#toString()
	 */
	@Override
	public String toString() {
		return "GUIAncor" + super.toString();
	}

}
