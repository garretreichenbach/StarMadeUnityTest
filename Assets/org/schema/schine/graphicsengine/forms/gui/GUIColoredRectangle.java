package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.input.InputState;

public class GUIColoredRectangle extends GUIColoredAnchor implements TooltipProviderCallback{

	public float rounded;
	//	private static FloatBuffer buffer = MemoryUtil.memAllocFloat(4*3);
	protected int diaplayListIndex;
	protected boolean generated;
	private Vector4f color;
	private GUIResizableElement dependend;
	private GUIToolTip toolTip;

	public GUIColoredRectangle(InputState state, int width, int height, Vector4f color) {
		this(state, width, height, null, color);
	}

	public GUIColoredRectangle(InputState state, int width, int height, GUIResizableElement dependent, Vector4f color) {
		super(state, width, height);
		this.color = color;
		this.dependend = dependent;
	}

	protected void drawRect() {
		GlUtil.glColor4f(color.x, color.y, color.z, color.w);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glCallList(diaplayListIndex);

		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {

		if (dependend != null) {
			setWidth(dependend.getWidth());
			setHeight(dependend.getHeight());
		}

		if (!generated) {
			generateDisplayList();
		}
		GlUtil.glPushMatrix();
		transform();
		if (color.w > 0) {

			assert (generated);
			if (isRenderable()) {
				doDrawRect();
			}
		}

		GlUtil.glPopMatrix();

		drawSuper();

	}

	@Override
	public void cleanUp() {
		if(generated){
			if (diaplayListIndex != 0) {
				GL11.glDeleteLists(this.diaplayListIndex, 1);
			}
		}
		super.cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		generateDisplayList();
	}

	@Override
	public void setHeight(float height) {
		if (this.height != height) {
			generated = false;
		}
		this.height = height;

	}

	@Override
	public void setWidth(float width) {
		if (this.width != width) {
			generated = false;
		}

		this.width = width;

	}

	protected void doDrawRect() {
		drawRect();
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
			GL11.glVertex2f(getWidth(), getHeight());
			GL11.glVertex2f(getWidth(), 0);

		} else {
			GL11.glBegin(GL11.GL_POLYGON);
			GL11.glVertex2f(0, rounded);
			GL11.glVertex2f(0, getHeight() - rounded);
			GL11.glVertex2f(rounded, getHeight());
			GL11.glVertex2f(getWidth() - rounded, getHeight());
			GL11.glVertex2f(getWidth(), getHeight() - rounded);
			GL11.glVertex2f(getWidth(), rounded);
			GL11.glVertex2f(getWidth() - rounded, 0);
			GL11.glVertex2f(rounded, 0);
			GL11.glVertex2f(rounded, rounded);

		}
		GL11.glEnd();
		GL11.glEndList();

		generated = true;
	}

	/**
	 * @return the color
	 */
	@Override
	public Vector4f getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	@Override
	public void setColor(Vector4f color) {
		this.color = color;
	}

	@Override
	public void setWidth(int width) {
		if (this.width != width) {
			generated = false;
		}

		this.width = width;

	}

	@Override
	public void setHeight(int height) {
		if (this.height != height) {
			generated = false;
		}
		this.height = height;

	}

	@Override
	public GUIToolTip getToolTip() {
		return toolTip;
	}

	@Override
	public void setToolTip(GUIToolTip toolTip) {
		this.toolTip = toolTip;
		if(getCallback() == null){
			//set default callback
			setCallback(new GUICallback() {
				@Override
				public boolean isOccluded() {
					return !isActive();
				}
				
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
				}
			});
		}
	}
}
