/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>TextureNew</H2>
 * <H3>org.schema.schine.graphicsengine.texture</H3>
 * TextureNew.java
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
package org.schema.schine.graphicsengine.texture;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;

/**
 * The Class TextureNew.
 */
public class Texture {

	private final String name;
	/**
	 * The height.
	 */
	protected int height;
	/**
	 * The width.
	 */
	protected int width;
	/**
	 * The height.
	 */
	private int textureHeight;
	/**
	 * The width.
	 */
	private int textureWidth;
	/**
	 * The on texture index.
	 */
	private int onTextureIndex = 0;
	/**
	 * The must flip.
	 */
	private boolean mustFlip;
	private int target;
	private int textureId;
	private int originalWidth;
	private int originalHeight;

	/**
	 * Instantiates a new texture.
	 *
	 * @param resourceName
	 * @param width        the width
	 * @param height       the height
	 * @param gl           the gl
	 * @param glu          the glu
	 */
	public Texture(int target, int textureID, String resourceName) {
		this.textureId = textureID;
		this.target = target;
		this.name = resourceName;
	}

	/**
	 * Retrieve a URL resource from the jar. If the resource is not found, then
	 * the local disk is also checked.
	 *
	 * @param filename Complete filename, including parent path
	 * @return a URL object if resource is found, otherwise null.
	 */
	public final static URL getResource(final String filename) {
		// Try to load resource from jar
		URL url = ClassLoader.getSystemResource(filename);
		// If not found in jar, then load from disk
		if (url == null) {
			try {
				url = new URL("file", "localhost", filename);
			} catch (Exception urlException) {
			} // ignore
		}
		return url;
	}

	/**
	 * Gets the empty texture.
	 *
	 * @param gl     the gl
	 * @param width  the width
	 * @param height the height
	 * @return the empty texture
	 */
	public static int getTextureFromBuffer(int width, int height, ByteBuffer data) {
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		// Create An Empty TextureNew
		// Create Storage Space For TextureNew Data (128x128x4)
		data.limit(data.capacity());

		IntBuffer txtIdBuffer = GlUtil.getIntBuffer1();
		GL11.glGenTextures(txtIdBuffer); // Create 1 TextureNew
		int texId = txtIdBuffer.get(0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texId); // Bind The TextureNew

		// Build TextureNew Using Information In data
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, data);

		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		return texId; // Return The TextureNew ID
	}

	/**
	 * Attach.
	 *
	 * @param gl     the gl
	 * @param filter the filter
	 */
	public void attach(int filter) {
		if (filter > 2 || filter < 0) {
			throw new IllegalArgumentException(
					"GL: TextureNew filter cannot be applied " + filter);
		}
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		// joglTexture.enable();
		// joglTexture.bind();

		// if(path != null)
		// System.err.println("attached path "+path[0]+" filter "+tex[filter]);
	}

	/**
	 * Bind on shader.
	 *
	 * @param gl             the gl
	 * @param glu            the glu
	 * @param gltextureindex the gltextureindex
	 * @param textureindex   the textureindex
	 * @param shaderTexture  the shader texture
	 * @param shaderprogram  the shaderprogram
	 */
	public void bindOnShader(int gltextureindex,
	                         int textureindex, String shaderTexture, Shader shader) {
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glActiveTexture(gltextureindex);
		//		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_WRAP_R, GL11.GL_REPEAT);
		onTextureIndex = gltextureindex;
		// if (g_maxAnisotrophy > 1.0f)
		// glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY,
		// g_maxAnisotrophy);
		GlUtil.updateShaderInt(shader, shaderTexture, textureindex);
		// GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
	}

	/**
	 * Clean up.
	 *
	 * @param gl the gl
	 */
	public void cleanUp() {
		GL11.glDeleteTextures(textureId);
	}

	/**
	 * Detach.
	 *
	 * @param gl the gl
	 */
	public void detach() {
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
	}

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the height.
	 *
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Gets the on texture index.
	 *
	 * @return the on texture index
	 */
	public int getOnTextureIndex() {
		return onTextureIndex;
	}

	/**
	 * @return the originalHeight
	 */
	public int getOriginalHeight() {
		return originalHeight;
	}

	/**
	 * @param originalHeight the originalHeight to set
	 */
	public void setOriginalHeight(int originalHeight) {
		this.originalHeight = originalHeight;
	}

	/**
	 * @return the originalWidth
	 */
	public int getOriginalWidth() {
		return originalWidth;
	}

	/**
	 * @param originalWidth the originalWidth to set
	 */
	public void setOriginalWidth(int originalWidth) {
		this.originalWidth = originalWidth;
	}

	/**
	 * @return the target
	 */
	public int getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(int target) {
		this.target = target;
	}

	/**
	 * @return the textureHeight
	 */
	public int getTextureHeight() {
		return textureHeight;
	}

	/**
	 * @param textureHeight the textureHeight to set
	 */
	public void setTextureHeight(int textureHeight) {
		this.textureHeight = textureHeight;
	}

	/**
	 * @return the textureId
	 */
	public int getTextureId() {
		return textureId;
	}

	/**
	 * @param textureId the textureId to set
	 */
	public void setTextureId(int textureId) {
		this.textureId = textureId;
	}

	/**
	 * @return the textureWidth
	 */
	public int getTextureWidth() {
		return textureWidth;
	}

	/**
	 * @param textureWidth the textureWidth to set
	 */
	public void setTextureWidth(int textureWidth) {
		this.textureWidth = textureWidth;
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width.
	 *
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Checks if is must flip.
	 *
	 * @return true, if is must flip
	 */
	public boolean isMustFlip() {
		return mustFlip;
	}

	/**
	 * Sets the must flip.
	 *
	 * @param mustFlip the mustFlip to set
	 */
	public void setMustFlip(boolean mustFlip) {
		this.mustFlip = mustFlip;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
				return "texture[" + name + "(" + textureId + ")(" + width + "x" + height + ")]";
	}

	/**
	 * Unbind from index.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 */
	public void unbindFromIndex() {
		if (onTextureIndex > 0) {
			;
			//System.err.println(onTextureIndex+" "+Arrays.toString(getPath()));
			GlUtil.glActiveTexture(onTextureIndex);
			;
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			;
			// GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			onTextureIndex = 0;
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			;
		}
	}

	public void updateTexture(BufferedImage b) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}
