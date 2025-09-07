/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>BoundingBox</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * BoundingBox.java
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

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.debug.DebugGeometry;
import org.schema.schine.graphicsengine.forms.simple.Box;

import com.bulletphysics.linearmath.Transform;

/**
 * The Class BoundingBox.
 */
public class DebugBox extends DebugGeometry {

	private static Vector3f[][] verts = Box.init();
	private Vector4f color = new Vector4f(1, 1, 1, 1);
	private Transform t;
	private Vector3f start;
	private Vector3f end;

	/**
	 * Color is for debugging BB only
	 *
	 * @param m1
	 * @param m2
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public DebugBox(Vector3f start, Vector3f end, Transform t, float r, float g, float b, float a) {
		color.set(r, g, b, a);
		this.t = t;
		this.start = start;
		this.end = end;
	}

	public static void draw(Vector3f start, Vector3f end, Vector4f color, Transform t, Vector3f[][] box) {

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL12.GL_TEXTURE_3D);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_TEXTURE_1D);
		//		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glColor4f(color.x, color.y, color.z, color.w);

		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(t);
		GL11.glBegin(GL11.GL_QUADS);
		for (int i = 0; i < box.length; i++) {
			for (int k = 0; k < box[i].length; k++) {
				GL11.glVertex3f(box[i][k].x, box[i][k].y, box[i][k].z);
			}
		}
		GL11.glEnd();
		GlUtil.glPopMatrix();
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glDisable(GL11.GL_BLEND);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	public void draw() {
		Vector3f[][] box = Box.getVertices(
				start,
				end, verts);
		draw(start, end, color, t, box);
	}

	public Vector4f getColor() {
		return color;
	}

	@Override
	public void setColor(Vector4f color) {
		this.color = color;
	}

}
