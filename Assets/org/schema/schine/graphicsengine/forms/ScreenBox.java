/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ScreenBox</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * ScreenBox.java
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
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
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
package org.schema.schine.graphicsengine.forms;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.font.TextForm;

/**
 * The Class ScreenBox.
 */
public class ScreenBox extends SceneNode {

	/**
	 * The color.
	 */
	public Vector4f color = new Vector4f(0.5f, 0.5f, 0.5f, 0.7f);
	/**
	 * The textPosX.
	 */
	public float textPosX, textPosY;
	/**
	 * The text.
	 */
	private TextForm text;
	private float boxWidth;
	private float boxHeight;

	/**
	 * Instantiates a new screen box.
	 *
	 * @param textPosX the textPosX
	 * @param textPosY the textPosY
	 * @param text     the text
	 */
	public ScreenBox(float texPosX, float texPosY, float boxWidth, float boxHeight, TextForm text) {
		super();
		this.textPosX = texPosX;
		this.textPosY = texPosY;
		this.boxWidth = boxWidth;
		this.boxHeight = boxHeight;
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {

		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);

		Sprite box = Controller.getResLoader().getSprite("box");
		box.setPos(getPos().x, getPos().y, getPos().z);
		box.setDepthTest(false);
		box.setBillboard(true);
		box.getScale().set(boxWidth, boxHeight, 1f);
		box.setTint(color);
		box.draw();

		text.getScale().set(1f, 1f, 1f);
		text.setPos(getPos().x, getPos().y, getPos().z);
		text.localPos.x = (-(textPosX / 2) + 9);
		text.localPos.y = (+textPosY / 2 - text.getHeight() - 3);
		text.setBillboard(true);
		text.draw();

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_LIGHTING);

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
	}

	public void drawOrthogonal() {
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		// save the current modelview matrix
		GlUtil.glPushMatrix();

		text.getScale().set(3.0f, 3.0f, 1);
		text.setPos(getPos().x, getPos().y, getPos().z);
		text.localPos.x = (0);
		text.localPos.y = (0);
		text.setBillboard(true);
		text.glPrint();
		GlUtil.glPopMatrix();

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_LIGHTING);

	}

	/**
	 * @return the text
	 */
	public TextForm getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(TextForm text) {
		this.text = text;
	}

}
