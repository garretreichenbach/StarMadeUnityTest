/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Line</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * Line.java
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
package org.schema.schine.graphicsengine.forms;

import javax.vecmath.AxisAngle4f;

import org.schema.schine.graphicsengine.core.settings.EngineSettings;

/**
 * The Class Line.
 */
public class Line extends Geometry {

	@Override
	public void cleanUp() {

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		if (EngineSettings.G_DEBUG_LINE_DRAWING_ACTIVATED.isOn()) {
			//TODO draw line
			System.err.println("TODO: no line drawing yet implemented");
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
	}

	@Override
	public AbstractSceneNode clone() {

		return null;
	}

	/**
	 * Gets the axis.
	 *
	 * @return the axis
	 */
	public AxisAngle4f getAxis() {
		return null;
	}

}
