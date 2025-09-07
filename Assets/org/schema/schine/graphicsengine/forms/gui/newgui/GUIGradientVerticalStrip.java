package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.input.InputState;

public class GUIGradientVerticalStrip extends GUIAnchor {

	public Vector4f colorMid;
	//	private static FloatBuffer buffer = MemoryUtil.memAllocFloat(4*3);
	protected int diaplayListIndex;
	protected boolean generated;
	private Vector4f color;
	private Vector4f colorEnd;

	public GUIGradientVerticalStrip(InputState state, float width, float height, Vector4f colorStart, Vector4f colorMid, Vector4f colorEnd) {
		super(state, width, height);
		this.color = colorStart;
		this.colorMid = new Vector4f(colorMid);
		this.colorEnd = new Vector4f(colorEnd);
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

		GlUtil.glColor4fForced(1, 1, 1, 1);
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

	protected void generateDisplayList() {
		// create one display list
		if (diaplayListIndex != 0) {
			GL11.glDeleteLists(this.diaplayListIndex, 1);
		}
		this.diaplayListIndex = GL11.glGenLists(1);
		// compile the display list, store a triangle in it
		GL11.glNewList(diaplayListIndex, GL11.GL_COMPILE);

		GL11.glBegin(GL11.GL_QUADS);
		GlUtil.glColor4f(color.x, color.y, color.z, color.w);
		GL11.glVertex2f(getWidth(), 0);
		GL11.glVertex2f(0, 0);

		GlUtil.glColor4f(colorMid.x, colorMid.y, colorMid.z, colorMid.w);
		GL11.glVertex2f(0, getHeight() / 2);
		GL11.glVertex2f(getWidth(), getHeight() / 2);

		GL11.glVertex2f(getWidth(), getHeight() / 2);
		GL11.glVertex2f(0, getHeight() / 2);

		GlUtil.glColor4f(colorEnd.x, colorEnd.y, colorEnd.z, colorEnd.w);
		GL11.glVertex2f(0, getHeight());
		GL11.glVertex2f(getWidth(), getHeight());

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

