package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public abstract class GUIInnerWindow extends GUIElement {

	public int cornerUpperOffsetX;
	public int cornerUpperOffsetY;
	public int cornerDistanceX;
	public int cornerDistanceTopY;
	public int cornerDistanceBottomY;
	public boolean upperCap = true;
	public boolean hasBackground = true;
	boolean init = false;
	private GUIElement p;
	private GUITexDrawableArea top;
	private final Vector4f tint = new Vector4f(1,1,1,1);
	//	private GUIOverlay elements;
	private GUITexDrawableArea bottom;
	private GUITexDrawableArea left;
	private GUITexDrawableArea right;
	private GUITexDrawableArea bg;
	private Sprite cs;
	private GUITexDrawableArea topLeft;

	//	private GUITexDrawableArea elements;
	private GUITexDrawableArea topRight;
	private GUITexDrawableArea bottomLeft;
	private GUITexDrawableArea bottomRight;
	public int extraHeight;

	public GUIInnerWindow(InputState state, GUIElement element, int cornerDistance) {
		super(state);
		this.cornerDistanceX = cornerDistance;
		this.cornerDistanceTopY = cornerDistance;
		this.cornerDistanceBottomY = cornerDistance;
		this.p = element;
	}

	@Override
	public void cleanUp() {
		if (top != null) {
			top.cleanUp();
		}
		if (bottom != null) {
			bottom.cleanUp();
		}
		if (left != null) {
			left.cleanUp();
		}
		if (right != null) {
			right.cleanUp();
		}
		if (bg != null) {
			bg.cleanUp();
		}
		if (topLeft != null) {
			topLeft.cleanUp();
		}
		if (topRight != null) {
			topRight.cleanUp();
		}
		if (bottomLeft != null) {
			bottomLeft.cleanUp();
		}
		if (bottomRight != null) {
			bottomRight.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}

		setPos(cornerDistanceX + cornerUpperOffsetX, cornerDistanceTopY + cornerUpperOffsetY, 0);
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

		this.cs = Controller.getResLoader().getSprite(getState().getGUIPath()+getCorners());

		topLeft = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getCorners()).getMaterial().getTexture(), 0, 0);
		topLeft.setSpriteSubIndex(getLeftTop(), cs.getMultiSpriteMaxX(), cs.getMultiSpriteMaxY());
		topLeft.onInit();

		topRight = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getCorners()).getMaterial().getTexture(), 0, 0);
		topRight.setSpriteSubIndex(getRightTop(), cs.getMultiSpriteMaxX(), cs.getMultiSpriteMaxY());
		topRight.onInit();

		bottomLeft = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getCorners()).getMaterial().getTexture(), 0, 0);
		bottomLeft.setSpriteSubIndex(getBottomLeft(), cs.getMultiSpriteMaxX(), cs.getMultiSpriteMaxY());
		bottomLeft.onInit();

		bottomRight = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getCorners()).getMaterial().getTexture(), 0, 0);
		bottomRight.setSpriteSubIndex(getBottomRight(), cs.getMultiSpriteMaxX(), cs.getMultiSpriteMaxY());
		bottomRight.onInit();

		top = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getHorizontal()).getMaterial().getTexture(), 0.0f, getTopOffset());
		bottom = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getHorizontal()).getMaterial().getTexture(), 0.0f, getBottomOffset());

		left = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getVertical()).getMaterial().getTexture(), getLeftOffset(), 0.0f);
		right = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getVertical()).getMaterial().getTexture(), getRightOffset(), 0.0f);

		bg = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getBackground()).getMaterial().getTexture(), 0.0f, 0.0f);

		top.onInit();
		bottom.onInit();
		left.onInit();
		right.onInit();

		this.init = true;
	}

	private void drawWindow() {
		startStandardDraw();
		
		topLeft.setColor(tint);
		topRight.setColor(tint);
		bottomLeft.setColor(tint);
		bottomRight.setColor(tint);
		bg.setColor(tint);
		bottom.setColor(tint);
		top.setColor(tint);
		left.setColor(tint);
		right.setColor(tint);
		
		topLeft.setPos(0, 0, 0);
		topLeft.setHeight((int) Math.min(cs.getHeight(), getHeight() / 2));
		topLeft.setWidth((int) Math.min(cs.getWidth(), getWidth()));

		topRight.setPos(getWidth() - topLeft.getWidth(), 0, 0);
		topRight.setWidth((int) Math.min(cs.getWidth(), getWidth()));
		topRight.setHeight((int) Math.min(cs.getHeight(), getHeight() / 2));
		if (upperCap) {
			topLeft.drawRaw();
			topRight.drawRaw();
		}

		bottomLeft.setPos(0, getHeight() - topLeft.getHeight(), 0);
		bottomLeft.setWidth((int) Math.min(cs.getWidth(), getWidth()));
		bottomLeft.setHeight((int) Math.min(cs.getHeight(), getHeight() / 2));
		bottomLeft.drawRaw();

		bottomRight.setPos(getWidth() - bottomLeft.getWidth(), getHeight() - topLeft.getHeight(), 0);
		bottomRight.setWidth((int) Math.min(cs.getWidth(), getWidth()));
		bottomRight.setHeight((int) Math.min(cs.getHeight(), getHeight() / 2));
		bottomRight.drawRaw();

		if (upperCap) {
			bg.setPos(topLeft.getWidth(), topLeft.getHeight(), 0);
			bg.setWidth((int) Math.max(0, (getWidth() - (bottomLeft.getWidth() + bottomRight.getWidth()))));
			bg.setHeight((int) Math.max(0, (getHeight() - (topRight.getHeight() + bottomRight.getHeight()))));
		} else {
			bg.setPos(topLeft.getWidth(), 0, 0);
			bg.setWidth((int) Math.max(0, (getWidth() - (bottomLeft.getWidth() + bottomRight.getWidth()))));
			bg.setHeight((int) Math.max(0, (getHeight() - (bottomRight.getHeight()))));
		}
		if (hasBackground) {
			bg.drawRaw();
		}

		left.setWidth((int) topLeft.getWidth());
		if (upperCap) {
			left.setPos(0, topLeft.getHeight(), 0);
			left.setHeight((int) Math.max(0, (getHeight() - (topLeft.getHeight() + bottomLeft.getHeight()))));
		} else {
			left.setPos(0, 0, 0);
			left.setHeight((int) Math.max(0, (getHeight() - (bottomLeft.getHeight()))));
		}
		left.drawRaw();

		right.setWidth((int) topRight.getWidth());
		if (upperCap) {
			right.setPos(getWidth() - topRight.getWidth(), topRight.getHeight(), 0);
			right.setHeight((int) Math.max(0, (getHeight() - (topRight.getHeight() + bottomRight.getHeight()))));
		} else {
			right.setPos(getWidth() - topRight.getWidth(), 0, 0);
			right.setHeight((int) Math.max(0, (getHeight() - (bottomRight.getHeight()))));
		}
		right.drawRaw();

		top.setPos(topLeft.getWidth(), 0, 0);
		top.setHeight((int) topLeft.getHeight());
		top.setWidth((int) Math.max(0, (getWidth() - (topLeft.getWidth() + topRight.getWidth()))));
		if (upperCap) {
			top.drawRaw();
		}

		bottom.setPos(bottomLeft.getWidth(), getHeight() - bottomLeft.getHeight(), 0);
		bottom.setHeight((int) bottomLeft.getHeight());
		bottom.setWidth((int) Math.max(0, (getWidth() - (bottomLeft.getWidth() + bottomRight.getWidth()))));
		bottom.drawRaw();
		
		
		topLeft.setColor(null);
		topRight.setColor(null);
		bottomLeft.setColor(null);
		bottomRight.setColor(null);
		bg.setColor(null);
		bottom.setColor(null);
		top.setColor(null);
		left.setColor(null);
		right.setColor(null);
		
		endStandardDraw();
		
	}

	protected abstract int getLeftTop();

	protected abstract int getRightTop();

	protected abstract int getBottomLeft();

	protected abstract int getBottomRight();

	protected abstract String getCorners();

	protected abstract String getVertical();

	protected abstract String getHorizontal();

	protected abstract String getBackground();

	protected abstract float getTopOffset();

	protected abstract float getBottomOffset();

	protected abstract float getLeftOffset();

	protected abstract float getRightOffset();

	@Override
	public float getHeight() {
		return p.getHeight() - (cornerDistanceTopY + cornerDistanceBottomY) - cornerUpperOffsetY + extraHeight;
	}

	@Override
	public float getWidth() {
		return p.getWidth() - cornerDistanceX * 2 - cornerUpperOffsetX;
	}

	public void setTint(float r, float g, float b, float a){
		tint.set(r, g, b, a);
	}
	public Vector4f getTint() {
		return tint;
	}

}
