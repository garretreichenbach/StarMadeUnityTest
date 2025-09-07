package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

public class GUIToolTip extends GUIElement {

	public static GUIToolTipGraphics tooltipGraphics;
	private static int lastId;
	private static int idGen;
	private final Vector3f absPos = new Vector3f();
	private final Vector3f color = new Vector3f(0, 0.6f, 0);
	boolean firstDraw = true;
	private final GUIElement toolTipParent;
	private boolean mouseOver = true;
	private float alpha = -1;
	private boolean forced;
	private Object text = "";
	private int id;

	public Object getText() {
		return text;
	}

	public GUIToolTip(InputState state, Object text, GUIElement on) {
		super(state);
		setText(text);
		if (tooltipGraphics == null || tooltipGraphics.getState() != state) {
			if(tooltipGraphics != null){
				tooltipGraphics.cleanUp();
			}
			tooltipGraphics = new GUIToolTipGraphics(getState());
		}
		id = ++idGen;
		assert(on != null);
		this.toolTipParent = on;
	}

	@Override
	public void cleanUp() {
	}
	public void onNotDrawTooltip(){
		if (GUIToolTip.lastId == this.id ) {
			tooltipGraphics.setToolTipText("");
		}
	}
	public boolean isDrawableTooltip(){
		return !mouseOver || toolTipParent.isInside() || forced;
	}
	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		
		if (isDrawableTooltip()) {
			GlUtil.glPushMatrix();
			if (mouseOver) {
				setPos((int)Math.min(GLFrame.getWidth() - getWidth(), Mouse.getX()), Mouse.getY() - tooltipGraphics.getHeight(), 0);
			}
			
			transform();
			
			if (alpha != -1) {

			}
			GUIToolTip.lastId = this.id;
			absPos.set(Controller.modelviewMatrix.m30, Controller.modelviewMatrix.m31, Controller.modelviewMatrix.m32);
			tooltipGraphics.setToolTipText(text);
			tooltipGraphics.draw();

			
			for (AbstractSceneNode e : getChilds()) {
				e.draw();
			}
			GlUtil.glPopMatrix();
		} else {
			onNotDrawTooltip();
		}

	}

	@Override
	public void onInit() {

		firstDraw = false;
	}

	@Override
	protected void doOrientation() {
	}

	@Override
	public float getHeight() {
		return tooltipGraphics.getHeight();
	}

	@Override
	public float getWidth() {
		return tooltipGraphics.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public void drawForced() {
		this.forced = true;
		draw();
		this.forced = false;
	}

	/**
	 * @return the color
	 */
	public Vector3f getColor() {
		return color;
	}

	/**
	 * @return the mouseOver
	 */
	public boolean isMouseOver() {
		return mouseOver;
	}

	/**
	 * @param mouseOver the mouseOver to set
	 */
	public void setMouseOver(boolean mouseOver) {
		this.mouseOver = mouseOver;
	}

	public void setAlpha(float f) {
		this.alpha = f;
	}

	public void setText(final Object text) {
		assert(text != null && text.toString().length() < 2000):"Abnormal tooltip: "+text;
		this.text = text;
	}

}
