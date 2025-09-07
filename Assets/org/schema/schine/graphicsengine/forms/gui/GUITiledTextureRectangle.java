package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.input.InputState;

public class GUITiledTextureRectangle extends GUIColoredRectangle {

	private Texture texture;
	private int tiling;

	public GUITiledTextureRectangle(InputState state, int width, int height, Texture t, int tiling) {
		super(state, width, height, new Vector4f(1, 1, 1, 1));
		this.texture = t;
		this.tiling = tiling;
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
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());

		assert (generated);
		GL11.glCallList(diaplayListIndex);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glPopMatrix();

		drawSuper();

	}

	@Override
	protected void generateDisplayList() {
		// create one display list
		if (diaplayListIndex != 0) {
			GL11.glDeleteLists(this.diaplayListIndex, 1);
		}
		this.diaplayListIndex = GL11.glGenLists(1);
		// compile the display list, store a triangle in it
		GL11.glNewList(diaplayListIndex, GL11.GL_COMPILE);

		float mExtra = 0.5f - (1f / tiling) / 2;
		float maxWT = getWidth() / tiling + mExtra;
		float maxHT = getHeight() / tiling + mExtra;

		if (rounded == 0) {
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(mExtra, mExtra);
			GL11.glVertex2f(0, 0);

			GL11.glTexCoord2f(mExtra, maxHT);
			GL11.glVertex2f(0, getHeight());

			GL11.glTexCoord2f(maxWT, maxHT);
			GL11.glVertex2f(getWidth(), getHeight());

			GL11.glTexCoord2f(maxWT, mExtra);
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

}
