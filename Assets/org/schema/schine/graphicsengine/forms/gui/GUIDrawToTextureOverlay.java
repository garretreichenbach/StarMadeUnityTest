/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>GUIDrawToTextureOverlay</H2>
 * <H3>org.schema.schine.graphicsengine.forms.gui</H3>
 * GUIDrawToTextureOverlay.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright © 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.graphicsengine.forms.gui;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLU;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.texture.TextureLoader;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.client.ClientStateInterface;

/**
 * The Class GUIDrawToTextureOverlay.
 */
public abstract class GUIDrawToTextureOverlay extends GUIOverlay {

	/**
	 * The tex width.
	 */
	protected int texWidth = 256;
	/**
	 * The tex height.
	 */
	protected int texHeight = 256;
	/**
	 * The firstdraw.
	 */
	private boolean firstdraw = true;

	/**
	 * Instantiates a new gUI draw to texture overlay.
	 *
	 * @param width  the width
	 * @param height the height
	 * @param canvas the canvas
	 */
	public GUIDrawToTextureOverlay(int width, int height, ClientState state) {
		super(new Sprite(width, height), state);
		this.texWidth = width;
		this.texHeight = height;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		if (firstdraw) {
			onInit();
		}
		sprite.setBlend(true);
		super.draw();
		sprite.setBlend(false);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
		firstdraw = false;
		sprite.getMaterial().setTexture(TextureLoader.getEmptyTexture(texWidth, texHeight));
		sprite.setHeight(texHeight);
		sprite.setWidth(texWidth);
		sprite.onInit();
	}

	//	static int[] viewport = new int[4];
	@Override
	public void updateGUI(ClientStateInterface state) {
		if (firstdraw) {
			onInit();
		}

		GL11.glViewport(0, 0, texWidth, texHeight);

		GlUtil.glPushMatrix();
		GlUtil.glLoadIdentity();
		;
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glPushMatrix(); // push projection maxtrix

		GlUtil.glLoadIdentity(); // load id
		GLU.gluOrtho2D(0, texWidth, 0, texHeight);
		GL11.glClearColor(0.4f, 0.4f, 0.4f, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		drawOverlayTexture(state);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		// System.err.println(sprite.getTexture().getTextureId());
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, sprite.getMaterial().getTexture().getTextureId());
		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 0, 0, texWidth,
				texHeight, 0);

		GlUtil.glPopMatrix();// pop projection matrix
		// Back to the projection we were using before
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW); // Best switch back to modelview
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glColor4f(1, 1, 1, 1f);
		GlUtil.glPopMatrix();
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glViewport(Controller.viewport.get(0), Controller.viewport.get(1), Controller.viewport.get(2), Controller.viewport.get(3));
	}

	/**
	 * Draw overlay texture.
	 *
	 * @param gl    the gl
	 * @param glu   the glu
	 * @param state the state
	 */
	public abstract void drawOverlayTexture(ClientStateInterface state);

}
