package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredAnchor;
import org.schema.schine.input.InputState;

public abstract class GUILeftRightArea extends GUIColoredAnchor {

	boolean init = false;
	private Vector4f color;
	private GUITexDrawableArea left;

	//	private GUIOverlay elements;
	private GUITexDrawableArea right;

	public GUILeftRightArea(InputState state, int width, int height) {
		super(state, width, height);
	}

//	private GUITexDrawableArea elements;

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}

		GlUtil.glPushMatrix();
		transform();

		checkMouseInside();
		drawWindow();

		int size = getChilds().size();

		for (int i = 0; i < size; i++) {
			getChilds().get(i).draw();
		}

		GlUtil.glPopMatrix();

	}

	@Override
	public void onInit() {
//		elements = new GUIOverlay(, getState());
//		elements.onInit();

		left = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getVertical()).getMaterial().getTexture(), getLeftOffset(), 0.0f);
		right = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getVertical()).getMaterial().getTexture(), getRightOffset(), 0.0f);

		left.onInit();
		right.onInit();

		this.init = true;
	}

	private void drawWindow() {
		startStandardDraw();
		left.setColor(color);
		left.setWidth(getPXWidth());
		left.setPos(0, 0, 0);
		left.setHeight((int) Math.max(0, (getHeight())));
		left.drawRaw();

		right.setColor(color);
		right.setWidth(getPXWidth());
		right.setPos(getWidth() - getPXWidth(), 0, 0);
		right.setHeight((int) getHeight());
		right.drawRaw();
		endStandardDraw();

	}

	public abstract int getPXWidth();

	public abstract int getPXHeight();

	protected abstract String getVertical();

	protected abstract float getLeftOffset();

	protected abstract float getRightOffset();

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIColoredAncor#getColor()
	 */
	@Override
	public Vector4f getColor() {
		return color;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIColoredAncor#setColor(javax.vecmath.Vector4f)
	 */
	@Override
	public void setColor(Vector4f c) {
		color = c;
	}

}
