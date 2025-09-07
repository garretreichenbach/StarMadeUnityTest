/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>GUITextBox</H2>
 * <H3>org.schema.schine.graphicsengine.forms.gui</H3>
 * GUITextBox.java
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
package org.schema.schine.graphicsengine.forms.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.font.TextForm;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.client.ClientStateInterface;

/**
 * The Class GUITextBox.
 */
public class GUITextBox extends GUIDrawToTextureOverlay {

	/**
	 * The first.
	 */
	private boolean first;

	/**
	 * The text form.
	 */
	private TextForm textForm;

	/**
	 * The text.
	 */
	private List<String> text = new ArrayList<String>();

	/**
	 * Instantiates a new gUI text box.
	 *
	 * @param textWidth  the text width
	 * @param textHeight the text height
	 * @param width      the width
	 * @param height     the height
	 * @param canvas     the canvas
	 */
	public GUITextBox(float textWidth, float textHeight, int width, int height, ClientState state) {
		super(width, height, state);
		textForm = new TextForm(text, textWidth, textHeight);
	}

	@Override
	public void drawOverlayTexture(ClientStateInterface state) {

		if (first) {
			textForm.onInit();
		}

		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		//		GlUtil.glEnable(GL11.GL_BLEND);
		//		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glBegin(GL11.GL_QUADS);
		{
			//backgroundcolor
			GlUtil.glColor4f(0.2f, 0.2f, 0.2f, 0.0f);
			GL11.glVertex3f(0, 0, 0);
			GL11.glVertex3f(texWidth, 0, 0);
			GL11.glVertex3f(texWidth, texHeight, 0);
			GL11.glVertex3f(0, texHeight, 0);
		}
		GL11.glEnd();

		GlUtil.glColor4f(1.0f, 1.0f, 1.0f, 1f);
		textForm.setFlip(true);
		textForm.drawText(text);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);

	}

	public float getLifeTime() {
		return 5000;
	}

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public List<String> getText() {
		return text;
	}

	/**
	 * Sets the text.
	 *
	 * @param text the text to set
	 */
	public void setText(List<String> text) {
		this.text = text;
	}

}
