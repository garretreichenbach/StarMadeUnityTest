package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.input.InputState;

public class GUITintScreenElement extends GUIElement {

	private final Vector4f color = new Vector4f(1, 1, 1, 0.5f);
	private int diaplayListIndex;
	private boolean generated;
	private int border = -1;

	public GUITintScreenElement(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		GlUtil.glColor4f(color.x, color.y, color.z, color.w);
		assert (generated);
		GL11.glCallList(diaplayListIndex);

		GlUtil.glColor4f(1, 1, 1, 1);
	}

	@Override
	public void onInit() {
		generateDisplayList();

	}

	@Override
	protected void doOrientation() {
		
	}

	@Override
	public float getHeight() {
		return GLFrame.getHeight();
	}

	@Override
	public float getWidth() {
		return GLFrame.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	private void generateDisplayList() {
		// create one display list

		this.diaplayListIndex = GL11.glGenLists(1);
		// compile the display list, store a triangle in it
		GL11.glNewList(diaplayListIndex, GL11.GL_COMPILE);

		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glBegin(GL11.GL_QUADS);

		if (border >= 0) {

			GL11.glVertex2f(0, 0);
			GL11.glVertex2f(0, GLFrame.getHeight());
			GL11.glVertex2f(border, GLFrame.getHeight());
			GL11.glVertex2f(border, 0);

			GL11.glVertex2f(GLFrame.getWidth() - border, 0);
			GL11.glVertex2f(GLFrame.getWidth() - border, GLFrame.getHeight());
			GL11.glVertex2f(GLFrame.getWidth(), GLFrame.getHeight());
			GL11.glVertex2f(GLFrame.getWidth(), 0);

			GL11.glVertex2f(border, 0);
			GL11.glVertex2f(border, border);
			GL11.glVertex2f(GLFrame.getWidth() - border, border);
			GL11.glVertex2f(GLFrame.getWidth() - border, 0);

			GL11.glVertex2f(border, GLFrame.getHeight() - border);
			GL11.glVertex2f(border, GLFrame.getHeight());
			GL11.glVertex2f(GLFrame.getWidth() - border, GLFrame.getHeight());
			GL11.glVertex2f(GLFrame.getWidth() - border, GLFrame.getHeight() - border);

		} else {

			// If there's no border set, do a fullscreen tint
			GL11.glVertex2f(0, 0);
			GL11.glVertex2f(0, GLFrame.getHeight());
			GL11.glVertex2f(GLFrame.getWidth(), GLFrame.getHeight());
			GL11.glVertex2f(GLFrame.getWidth(), 0);

		}

		GL11.glEnd();
		GlUtil.glDisable(GL11.GL_BLEND);

		GL11.glEndList();

		generated = true;
	}

	/**
	 * @return the color
	 */
	public Vector4f getColor() {
		return color;
	}

	public void setBorder(int border) {
		this.border = border;
	}

}
