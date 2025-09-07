/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>TextForm</H2>
 * <H3>org.schema.schine.graphicsengine.font</H3>
 * TextForm.java
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
package org.schema.schine.graphicsengine.font;

import java.awt.Dimension;
import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.SceneNode;
import org.schema.schine.graphicsengine.texture.Texture;

/**
 * The Class TextForm.
 */
public class TextForm extends SceneNode implements FontRenderer {

	// Character dimension
	/**
	 * The Constant TEXTURE_WIDTH.
	 */
	public final static int TEXTURE_WIDTH = 256;
	// contain the font picture
	/**
	 * The Constant TEXTURE_HEIGHT.
	 */
	public final static int TEXTURE_HEIGHT = 256;
	// Character offset in the font texture
	/**
	 * The Constant NB_CHAR_WIDTH.
	 */
	public final static int NB_CHAR_WIDTH = 16;
	// TextureNew dimension
	/**
	 * The Constant NB_CHAR_HEIGHT.
	 */
	public final static int NB_CHAR_HEIGHT = 14;
	/**
	 * The Constant CHAR_SIZE.
	 */
	public final static Dimension CHAR_SIZE = new Dimension(TEXTURE_WIDTH
			/ NB_CHAR_WIDTH, TEXTURE_HEIGHT / NB_CHAR_HEIGHT);
	// Number of character in the texture
	private static final Vector2f CHAR_SCALE = new Vector2f(3, 5);
	/**
	 * The CHA r_ offset.
	 */
	private final static int CHAR_OFFSET = 32;
	// Character dimension
	/**
	 * The tex.
	 */
	private static Texture tex;

	// Display Lists
	/**
	 * The FONT.
	 */
	private static int FONT = 0;
	/**
	 * The local pos.
	 */
	public Vector3f localPos = new Vector3f();
	/**
	 * The text.
	 */
	private ArrayList<String> text = new ArrayList<String>();

	/**
	 * The billboard.
	 */
	private boolean billboard = false;

	/**
	 * The height.
	 */
	private float height;

	/**
	 * The width.
	 */
	private float width;
	private boolean flip;

	/**
	 * Instantiates a new text form.
	 *
	 * @param text2  the text
	 * @param width  the width
	 * @param height the height
	 */
	public TextForm(List<String> text, float width, float height) {
		this.text.addAll(text);
		this.width = width;
		this.height = height;
	}

	private static void createFont() {
		// generate 256 display lists
		tex = Controller.getResLoader().getSprite("fontset-standard").getMaterial().getTexture();

		FONT = GL11.glGenLists(256);

		int width = CHAR_SIZE.width;
		int height = CHAR_SIZE.height;

		// create all the list begining with the 32 (all the previous does not
		// contained characters)
		for (char i = CHAR_OFFSET; i < 256; i++) {
			GL11.glNewList(FONT + i, GL11.GL_COMPILE);
			// Position of the character on the grid
			int line = (i - CHAR_OFFSET) / 16;
			int row = (i - CHAR_OFFSET) % 16;
			Point topLeft = new Point(row * width, (1 + line) * height);
			float tHeight = tex.getHeight();
			float tWidth = tex.getWidth();
			// Draw the character and translate
			GL11.glBegin(GL11.GL_QUADS);

			GL11.glTexCoord2f(topLeft.x / tWidth, topLeft.y / tHeight);
			GL11.glVertex2f(0, 0);

			GL11.glTexCoord2f((topLeft.x + width) / tWidth, topLeft.y / tHeight);
			GL11.glVertex2f(CHAR_SCALE.x, 0);

			GL11.glTexCoord2f((topLeft.x + width) / tWidth,
					(topLeft.y - height + 1) / tHeight);
			GL11.glVertex2f(CHAR_SCALE.x, CHAR_SCALE.y);

			GL11.glTexCoord2f(topLeft.x / tWidth, (topLeft.y - height + 1)
					/ tHeight);
			GL11.glVertex2f(0, CHAR_SCALE.y);

			// GL11.glTexCoord2f(topLeft.x/256.f, topLeft.y/256.f);
			// GL11.glVertex2f(0, charSize.height);
			// GL11.glTexCoord2f((topLeft.x+width)/256.f, topLeft.y/256.f);
			// GL11.glVertex2f(charSize.width, charSize.height);
			//GL11.glTexCoord2f((topLeft.x+width)/256.f,(topLeft.y-height)/256.f);
			// GL11.glVertex2f(charSize.width, 0);
			// GL11.glTexCoord2f(topLeft.x/256.f, (topLeft.y-height)/256.f);
			// GL11.glVertex2f(0, 0);

			GL11.glEnd();
			GL11.glTranslatef(CHAR_SCALE.x, 0.0f, 0.0f);
			GL11.glEndList();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		drawTextAt(text, getPos().x, getPos().y, getPos().z);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {

		if (FONT == 0) {
			createFont();
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.font.FontRenderer#drawText(java.lang.String, javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void drawText(List<String> text) {
		drawText(text, FontAlign.BotLeft);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.font.FontRenderer#drawText(java.lang.String, org.schema.schine.graphicsengine.font.FontRenderer.FontAlign, javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void drawText(List<String> text, FontAlign align) {
		drawTextAt(text, align, 0, height, 0);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.font.FontRenderer#drawTextAt(java.lang.String, float, float, float, javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void drawTextAt(List<String> text, float x, float y, float z) {
		drawTextAt(text, FontAlign.BotLeft, x, y, z);

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.font.FontRenderer#drawTextAt(java.lang.String, org.schema.schine.graphicsengine.font.FontRenderer.FontAlign, float, float, float, javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void drawTextAt(List<String> text2, FontAlign align, float x, float y,
	                       float z) {
		drawTextAt(text2, align, x, y, z, this.width, this.height);
	}

	/**
	 * Draw text at.
	 *
	 * @param text   the text
	 * @param align  the align
	 * @param x      the x
	 * @param y      the y
	 * @param z      the z
	 * @param width  the width
	 * @param height the height
	 * @param gl     the gl
	 * @param glu    the glu
	 */
	private void drawTextAt(List<String> text, FontAlign align, float x, float y,
	                        float z, float width, float height) {

		if (FONT == 0 || tex == null) {
			onInit();
		}

		// activate texture
		tex.attach(0);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);

		// activate the blending
		GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		GlUtil.glEnable(GL11.GL_BLEND);

		// Text alignment
		float posX = x, posY = y, posZ = z;

		// Draw the text
		GL11.glListBase(FONT);
		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);

		int yOffset = 0;
		for (String s : text) {
			switch(align) {
				case TopRight -> {
					posX -= width * s.length();
					posY -= height;
				}
				case Right -> {
					posX -= width * s.length();
					posY -= height / 2;
				}
				case BotRight -> posX -= width * s.length();
				case TopCenter -> {
					posX -= width * s.length() / 2;
					posY -= height;
				}
				case Center -> {
					posX -= width * s.length() / 2;
					posY -= height / 2;
				}
				case BotCenter -> posX -= width * s.length() / 2;
				case TopLeft -> posY -= height;
				case Left -> posY -= height / 2;
				// case BotLeft:
				// break;
				default -> {
				}
			}
			GlUtil.glPushMatrix();
			{
				GL11.glTranslatef(posX, posY, posZ);
				// System.err.println("text: "+posX+", "+posY+", "+posZ);
				if (billboard) {
					GlUtil.glLoadMatrix(getBillboardSphericalBeginMatrix());
				}

				if (flip) {
					GlUtil.scaleModelview(1, -1, 1);
				}
				//				System.err.println("println "+s);
				GlUtil.scaleModelview(localPos.x, localPos.y + yOffset, localPos.z);

				GL11.glScalef(getScale().x * width, getScale().y * height, getScale().z);
				byte[] bytes = s.getBytes();

				ByteBuffer b = GlUtil.getDynamicByteBuffer(bytes.length, 0);
				b.put(bytes);

				GL11.glCallLists(b);
				yOffset += height + 4 * getScale().y;
			}
			GlUtil.glPopMatrix();
		}

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	}

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Sets the height.
	 *
	 * @param height the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	public ArrayList<String> getText() {
		return text;
	}

	public void setText(ArrayList<String> text) {
		this.text = text;
	}

	/**
	 * @return the width
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * Called by init(). This method is used to create our font as a display list. This list gets stored in the int base and
	 * is then created. This is not just one list btw, it is 256 lists for 256 characters. We create a list for each character.
	 *
	 * @param x    where to place the string on the x axis
	 * @param y    where to place the string on the y axis
	 * @param text the string to display
	 * @param set  are we using the first character set or the second...
	 * @param gl   send the gl object to this method for use.
	 */
	public void glPrint() {

		//    	GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if (!GL11.glIsEnabled(GL11.GL_BLEND)) {
			GlUtil.glEnable(GL11.GL_BLEND);
		}
		if (FONT == 0) {
			onInit();
		}
		tex.attach(0);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		boolean textureActive = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		if (!textureActive) {
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		}
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);    // Select The Projection Matrix
		GlUtil.glPushMatrix();            // Store The Projection Matrix
		GlUtil.glLoadIdentity();
		GL11.glOrtho(0, GLFrame.getWidth(), 0, GLFrame.getHeight() + 15, -1, 1);
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);

		GlUtil.glPushMatrix();
		int yOffset = 0;
		for (String s : text) {
			GlUtil.glPushMatrix();
			GlUtil.glLoadIdentity();
			GL11.glTranslated(localPos.x, GLFrame.getHeight() - height * getScale().y - localPos.y - yOffset, 0);
			GL11.glScaled(getScale().x, getScale().y, 1);
			GL11.glListBase(FONT);
			byte[] bytes = s.getBytes();

			ByteBuffer b = GlUtil.getDynamicByteBuffer(bytes.length, 0);
			b.put(bytes);
			b.rewind();
			GL11.glCallLists(b);
			yOffset += height + 4.7f * getScale().y;
			GlUtil.glPopMatrix();
		}

		GlUtil.glMatrixMode(GL11.GL_PROJECTION);    // Select The Projection Matrix
		GlUtil.glPopMatrix();    // Restore The Old Projection Matrix
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPopMatrix();
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);

		GlUtil.glDisable(GL11.GL_BLEND);
	}

	/**
	 * Checks if is billboard.
	 *
	 * @return true, if is billboard
	 */
	public boolean isBillboard() {
		return billboard;
	}

	/**
	 * Sets the billboard.
	 *
	 * @param billboard the new billboard
	 */
	public void setBillboard(boolean billboard) {
		this.billboard = billboard;
	}

	/**
	 * @return the flip
	 */
	public boolean isFlip() {
		return flip;
	}

	/**
	 * @param flip the flip to set
	 */
	public void setFlip(boolean flip) {
		this.flip = flip;
	}

}
