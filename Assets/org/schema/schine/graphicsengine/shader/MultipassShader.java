/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>MultipassShader</H2>
 * <H3>org.schema.schine.graphicsengine.shader</H3>
 * MultipassShader.java
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
package org.schema.schine.graphicsengine.shader;

import java.util.ArrayList;

import org.schema.schine.graphicsengine.core.GlUtil;

/**
 * The Class MultipassShader.
 */
public class MultipassShader {

	/**
	 * The shaderable.
	 */
	private Shaderable shaderable;

	/**
	 * The shaders.
	 */
	private ArrayList<Shader> shaders;

	/**
	 * The drawing.
	 */
	private boolean drawing;

	/**
	 * Instantiates a new multipass shader.
	 */
	public MultipassShader() {
		shaders = new ArrayList<Shader>();
	}

	/**
	 * Draw all shaders.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @ the error diolog exception
	 */
	public void drawAllShaders() {
		drawing = true;
		GlUtil.glPushMatrix();
		GlUtil.printGlErrorCritical();
		//		System.err.println("[Multipass] "+shaderable+": "+getShaders());
		for (int i = 0; i < shaders.size(); i++) {
			Shader shader = shaders.get(i);
			//			System.err.println("[Multipass] shader "+i+" "+getShaders());
			shader.setShaderInterface(shaderable);
			//			System.err.println("now loading "+shader);
			shader.load();
			GlUtil.printGlErrorCritical();
			//			getShaderable().draw(gl ,);
			GlUtil.printGlErrorCritical();
			shader.unload();
		}

		GlUtil.glPopMatrix();
		drawing = false;
	}

	/**
	 * Gets the shaderable.
	 *
	 * @return the shaderable
	 */
	public Shaderable getShaderable() {
		return shaderable;
	}

	/**
	 * Sets the shaderable.
	 *
	 * @param shaderable the shaderable to set
	 */
	public void setShaderable(Shaderable shaderable) {
		this.shaderable = shaderable;
	}

	/**
	 * Gets the shaders.
	 *
	 * @return the shaders
	 */
	public ArrayList<Shader> getShaders() {
		return shaders;
	}

	/**
	 * Sets the shaders.
	 *
	 * @param shaders the shaders to set
	 */
	public void setShaders(ArrayList<Shader> shaders) {
		this.shaders = shaders;
	}

	/**
	 * Checks if is drawing.
	 *
	 * @return the drawing
	 */
	public boolean isDrawing() {
		return drawing;
	}

	/**
	 * Sets the drawing.
	 *
	 * @param drawing the drawing to set
	 */
	public void setDrawing(boolean drawing) {
		this.drawing = drawing;
	}

}
