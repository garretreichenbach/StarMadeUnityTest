/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>PathSegment</H2>
 * <H3>org.schema.schine.ai.aStar</H3>
 * PathSegment.java
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
package org.schema.schine.ai.aStar;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3fTools;


// TODO: Auto-generated Javadoc

/**
 * The Class PathSegment.
 */
public class PathSegment {

	/**
	 * The start.
	 */
	public Vector3f start;

	/**
	 * The end.
	 */
	public Vector3f end;

	/**
	 * The start pixel.
	 */
	public Vector3f startPixel;

	/**
	 * The end pixel.
	 */
	public Vector3f endPixel;

	/**
	 * The dir.
	 */
	public Vector3f dir;

	/**
	 * The length.
	 */
	public float length;

	/**
	 * Instantiates a new path segment.
	 *
	 * @param start the start
	 * @param end   the end
	 */
	public PathSegment(Vector3f start, Vector3f end, int pixelMult) {
		this.start = new Vector3f(start);
		this.end = new Vector3f(end);

		this.startPixel = new Vector3f(
				start.x * pixelMult + pixelMult / 2,
				start.y * pixelMult,
				start.z * pixelMult + pixelMult / 2);

		this.endPixel = new Vector3f(
				end.x * pixelMult + pixelMult / 2,
				end.y * pixelMult,
				end.z * pixelMult + pixelMult / 2);

		dir = Vector3fTools.sub(endPixel, startPixel);
		length = dir.length();
	}

	/**
	 * Gets the end field.
	 *
	 * @param map the map
	 * @return the end field
	 */
	public Field getEndField(Map map) {
		return map.getField((int) end.x, (int) end.z);
	}

	/**
	 * Gets the start field.
	 *
	 * @param map the map
	 * @return the start field
	 */
	public Field getStartField(Map map) {
		return map.getField((int) start.x, (int) start.z);
	}

	/**
	 * Reset direction.
	 */
	public void resetDirection() {
		dir = Vector3fTools.sub(endPixel, startPixel);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[(" + start.x + "," + start.z + ")" + "-" + "(" + end.x + ","
				+ end.z + ")]";
	}
}
