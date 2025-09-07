/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>VertexBoneWeight</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * VertexBoneWeight.java
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

/**
 * The Class VertexBoneWeight.
 */
public class VertexBoneWeight {

	/**
	 * The vertex index.
	 */
	public int vertexIndex;

	/**
	 * The weight.
	 */
	public float weight;

	/**
	 * The bone index.
	 */
	public int boneIndex;

	/**
	 * Instantiates a new vertex bone weight.
	 *
	 * @param vertexIndex the vertex index
	 * @param boneIndex   the bone index
	 * @param weigth      the weigth
	 */
	public VertexBoneWeight(int vertexIndex, int boneIndex, float weigth) {
		super();
		this.vertexIndex = vertexIndex;
		this.weight = weigth;
		this.boneIndex = boneIndex;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VertexBoneWeight [vertexIndex=" + vertexIndex + ", weight="
				+ weight + ", boneIndex=" + boneIndex + "]";
	}

}
