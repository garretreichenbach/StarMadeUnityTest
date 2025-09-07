package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.input.InputState;

public class GUIColoredRectangleOutline extends GUIAnchor {

	//	private static FloatBuffer buffer = MemoryUtil.memAllocFloat(4*3);
	protected int diaplayListIndex;
	protected boolean generated;
	private Vector4f color;
	private int inset;

	public GUIColoredRectangleOutline(InputState state, float width, float height, int inset, Vector4f color) {
		super(state, width, height);
		this.color = color;
		this.inset = inset;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {

		if (!generated) {
			generateDisplayList();
		}
		GlUtil.glPushMatrix();
		transform();
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);

		GlUtil.glColor4f(color.x, color.y, color.z, color.w);
		assert (generated);
		if (isRenderable()) {
			GL11.glCallList(diaplayListIndex);
		}
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);

		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glPopMatrix();

		drawSuper();

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		generateDisplayList();
	}

	public void drawSuper() {
		super.draw();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		if (diaplayListIndex != 0) {
			GL11.glDeleteLists(this.diaplayListIndex, 1);
		}
	}

	protected void generateDisplayList() {
		// create one display list
		if (diaplayListIndex != 0) {
			GL11.glDeleteLists(this.diaplayListIndex, 1);
		}
		this.diaplayListIndex = GL11.glGenLists(1);
		// compile the display list, store a triangle in it
		GL11.glNewList(diaplayListIndex, GL11.GL_COMPILE);

		GL11.glBegin(GL11.GL_QUADS);

		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(0, inset);
		GL11.glVertex2f(getWidth(), inset);
		GL11.glVertex2f(getWidth(), 0);

		GL11.glVertex2f(0, getHeight() - inset);
		GL11.glVertex2f(0, getHeight());
		GL11.glVertex2f(getWidth(), getHeight());
		GL11.glVertex2f(getWidth(), getHeight() - inset);

		GL11.glVertex2f(0, inset);
		GL11.glVertex2f(0, getHeight() - inset);
		GL11.glVertex2f(inset, getHeight() - inset);
		GL11.glVertex2f(inset, inset);

		GL11.glVertex2f(getWidth() - inset, inset);
		GL11.glVertex2f(getWidth() - inset, getHeight() - inset);
		GL11.glVertex2f(getWidth(), getHeight() - inset);
		GL11.glVertex2f(getWidth(), inset);

		GL11.glEnd();

		GL11.glEndList();

		generated = true;
	}

	/**
	 * @return the color
	 */
	public Vector4f getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Vector4f color) {
		this.color = color;
	}

	/**
	 * @return the inset
	 */
	public int getInset() {
		return inset;
	}

	/**
	 * @param inset the inset to set
	 */
	public void setInset(int inset) {
		this.inset = inset;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
		generated = false;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
		generated = false;
	}
}
