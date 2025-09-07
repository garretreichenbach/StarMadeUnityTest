package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredAnchor;
import org.schema.schine.input.InputState;

public abstract class GUIFilledArea extends GUIColoredAnchor {

	boolean init = false;
	private Vector4f color;
	private GUITexDrawableArea top;

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


	
	public GUIFilledArea(InputState state, int width, int height) {
		super(state, width, height);
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}

		if(translateOnlyMode){
			translate();
		}else{
			GlUtil.glPushMatrix();
			transform();
		}

		checkMouseInside();
		if(color == null || color.w > 0){
			drawWindow();
		}

		final int size = getChilds().size();
		for (int i = 0; i < size; i++) {
			getChilds().get(i).draw();
		}

		if(translateOnlyMode){
			translateBack();
		}else{
			GlUtil.glPopMatrix();
		}

	}

	@Override
	public void onInit() {
//		elements = new GUIOverlay(, getState());
//		elements.onInit();
		assert(Controller.getResLoader() != null);
		assert(getState() != null);
		assert(getState().getGUIPath() != null);
		assert(getCorners() != null);
		this.cs = Controller.getResLoader()
					.getSprite(getState().getGUIPath()+getCorners());

		topLeft = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getCorners()).getMaterial().getTexture(), 0, 0);
		
		topLeft.onInit();

		topRight = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getCorners()).getMaterial().getTexture(), 0, 0);
		
		topRight.onInit();

		bottomLeft = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getCorners()).getMaterial().getTexture(), 0, 0);
		
		bottomLeft.onInit();

		bottomRight = new GUITexDrawableArea(getState(), Controller.getResLoader().getSprite(getState().getGUIPath()+getCorners()).getMaterial().getTexture(), 0, 0);
		
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
		updateSprites();
		this.init = true;
	}
	public void updateSprites(){
		topLeft.setSpriteSubIndex(getLeftTop(), cs.getMultiSpriteMaxX(), cs.getMultiSpriteMaxY());
		topRight.setSpriteSubIndex(getRightTop(), cs.getMultiSpriteMaxX(), cs.getMultiSpriteMaxY());
		bottomLeft.setSpriteSubIndex(getBottomLeft(), cs.getMultiSpriteMaxX(), cs.getMultiSpriteMaxY());
		bottomRight.setSpriteSubIndex(getBottomRight(), cs.getMultiSpriteMaxX(), cs.getMultiSpriteMaxY());
		
		top.setOffset(0.0f, getTopOffset());
		bottom.setOffset(0.0f, getBottomOffset());

		left.setOffset(getLeftOffset(), 0.0f);
		right.setOffset(getRightOffset(), 0.0f);

		bg.setOffset(0.0f, 0.0f);

	}
	private void drawWindow() {
		float lw;
		float rw;
		if (width < cs.getWidth() * 2) {
			int diff = (int) (width - cs.getWidth() * 2);
			if (diff % 2 == 0) {
				lw = cs.getWidth() + (diff / 2);
				rw = cs.getWidth() + (diff / 2);
			} else {
				lw = cs.getWidth() + (diff / 2) + 1;
				rw = cs.getWidth() + (diff / 2);
			}
		} else {
			lw = (cs.getWidth());
			rw = (cs.getWidth());
		}
		updateSprites();
		
		startStandardDraw();
		
		topLeft.setColor(color);
		topLeft.setPos(0, 0, 0);
		topLeft.setHeight((int) Math.min(cs.getHeight(), getHeight() / 2));
		topLeft.setWidth((int) lw);

		topRight.setColor(color);
		topRight.setPos((int) (getWidth() - rw), 0, 0);
		topRight.setWidth((int) rw);
		topRight.setHeight((int) Math.min(cs.getHeight(), getHeight() / 2));
		topLeft.drawRaw();
		topRight.drawRaw();

		bottomLeft.setColor(color);
		bottomLeft.setPos(0, getHeight() - topLeft.getHeight(), 0);
		bottomLeft.setWidth((int) lw);
		bottomLeft.setHeight((int) Math.min(cs.getHeight(), getHeight() / 2));
		bottomLeft.drawRaw();

		bottomRight.setColor(color);
		bottomRight.setPos((int) (getWidth() - rw), getHeight() - topLeft.getHeight(), 0);
		bottomRight.setWidth((int) rw);
		bottomRight.setHeight((int) Math.min(cs.getHeight(), getHeight() / 2));
		bottomRight.drawRaw();

		bg.setColor(color);
		bg.setPos((int) lw, topLeft.getHeight(), 0);
		bg.setWidth((int) Math.max(0, (getWidth() - (lw + rw))));
		bg.setHeight((int) Math.max(0, (getHeight() - (topRight.getHeight() + bottomRight.getHeight()))));
		bg.drawRaw();

		left.setColor(color);
		left.setWidth((int) lw);
		left.setPos(0, topLeft.getHeight(), 0);
		left.setHeight((int) Math.max(0, (getHeight() - (topLeft.getHeight() + bottomLeft.getHeight()))));
		left.drawRaw();

		right.setColor(color);
		right.setWidth((int) rw);
		right.setPos((int) (getWidth() - rw), topRight.getHeight(), 0);
		right.setHeight((int) Math.max(0, (getHeight() - (topRight.getHeight() + bottomRight.getHeight()))));
		right.drawRaw();

		top.setColor(color);
		top.setPos((int) lw, 0, 0);
		top.setHeight((int) topLeft.getHeight());
		top.setWidth((int) Math.max(0, (getWidth() - (lw + rw))));
		top.drawRaw();

		bottom.setColor(color);
		bottom.setPos((int) lw, getHeight() - bottomLeft.getHeight(), 0);
		bottom.setHeight((int) bottomLeft.getHeight());
		bottom.setWidth((int) Math.max(0, (getWidth() - (lw + rw))));
		bottom.drawRaw();
		
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
