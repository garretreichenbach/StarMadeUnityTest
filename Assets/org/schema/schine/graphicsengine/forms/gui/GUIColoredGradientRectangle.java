package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.input.InputState;

public class GUIColoredGradientRectangle extends GUIAnchor {

	public Vector4f gradient;
	public float rounded;
	//	private static FloatBuffer buffer = MemoryUtil.memAllocFloat(4*3);
	protected int diaplayListIndex;
	protected boolean generated;
	private Vector4f color;
	private Vector4f lastColor = new Vector4f();
	private Vector4f lastGradient = new Vector4f();

	public GUIColoredGradientRectangle(InputState state, float width, float height, Vector4f color) {
		super(state, width, height);
		this.color = color;
		this.gradient = new Vector4f(color);
		this.lastColor.set(color);
		this.lastGradient.set(color);
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
		GL11.glCallList(diaplayListIndex);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);

		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glPopMatrix();

		drawSuper();

		if (!lastGradient.equals(gradient)) {
			generated = false;
		}
		GlUtil.glColor4f(0.3f, 0.3f, 0.3f, 0.3f);
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

		if (rounded == 0) {
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(0, 0);
			GL11.glVertex2f(0, getHeight());

			GlUtil.glColor4f(gradient.x, gradient.y, gradient.z, gradient.w);
			GL11.glVertex2f(getWidth(), getHeight());
			GL11.glVertex2f(getWidth(), 0);

		} else {
			GL11.glBegin(GL11.GL_POLYGON);
			GL11.glVertex2f(0, rounded);

			GL11.glVertex2f(0, getHeight() - rounded);
			GL11.glVertex2f(rounded, getHeight());

			GL11.glVertex2f(getWidth() - rounded, getHeight());
			GL11.glVertex2f(getWidth(), getHeight() - rounded);

			GlUtil.glColor4f(gradient.x, gradient.y, gradient.z, gradient.w);

			GL11.glVertex2f(getWidth(), rounded);

			GL11.glVertex2f(getWidth() - rounded, 0);

			GL11.glVertex2f(rounded, 0);
			GL11.glVertex2f(rounded, rounded);

		}
		GL11.glEnd();
		GL11.glEndList();
		this.lastColor.set(color);
		this.lastGradient.set(gradient);
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
