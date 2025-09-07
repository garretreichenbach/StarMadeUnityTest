/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Geometry</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * Geometry.java
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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import javax.vecmath.Vector3f;

import org.w3c.dom.Document;

/**
 * The Class Geometry.
 */
public abstract class Geometry extends AbstractSceneNode {

	/**
	 * The vertices.
	 */
	public Vector3f vertices[];
	public FloatBuffer verticesBuffer; // Vertex Data
	/**
	 * The pivot.
	 */
	protected Vector3f pivot;
	/**
	 * The vert count.
	 */
	protected int vertCount;
	/**
	 * The mass.
	 */
	private float mass;
	private IntBuffer indexBuffer;
	/**
	 * The physics mats.
	 */
	private Vector<float[]> physicsMats = new Vector<float[]>();

	/**
	 * The physics matTmp.
	 */
	private float[] physicsMat;

	/**
	 * The user data.
	 */
	private Document userData;
	public String scenePath;
	public String sceneFile;

	public Geometry() {
		super();
		pivot = new Vector3f(0, 0, 0);
	}

	/**
	 * @return the indexBuffer
	 */
	public IntBuffer getIndexBuffer() {
		return indexBuffer;
	}

	/**
	 * @param indexBuffer the indexBuffer to set
	 */
	public void setIndexBuffer(IntBuffer indexBuffer) {
		this.indexBuffer = indexBuffer;
	}

	/**
	 * Gets the mass.
	 *
	 * @return the mass
	 */
	public float getMass() {
		return mass;
	}

	/**
	 * Sets the mass.
	 *
	 * @param mass the new mass
	 */
	public void setMass(float mass) {
		this.mass = mass;
	}

	/**
	 * Gets the physics matTmp.
	 *
	 * @return the physics matTmp
	 */
	public float[] getPhysicsMat() {
		return physicsMat;
	}

	/**
	 * Sets the physics matTmp.
	 *
	 * @param physicsMat the new physics matTmp
	 */
	public void setPhysicsMat(float[] physicsMat) {
		this.physicsMat = physicsMat;
	}

	/**
	 * Gets the physics mats.
	 *
	 * @return the physics mats
	 */
	public Vector<float[]> getPhysicsMats() {
		return physicsMats;
	}

	/**
	 * Sets the physics mats.
	 *
	 * @param physicsMats the new physics mats
	 */
	public void setPhysicsMats(Vector<float[]> physicsMats) {
		this.physicsMats = physicsMats;
	}

	/**
	 * Gets the pivot.
	 *
	 * @return the pivot
	 */
	public Vector3f getPivot() {
		return pivot;
	}

	/**
	 * Sets the pivot.
	 *
	 * @param pivot the new pivot
	 */
	public void setPivot(Vector3f pivot) {
		this.pivot = pivot;
	}

	/**
	 * Gets the user data.
	 *
	 * @return the user data
	 */
	public Document getUserData() {
		return userData;
	}

	/**
	 * Sets the user data.
	 *
	 * @param userData the new user data
	 */
	public void setUserData(Document userData) {
		this.userData = userData;
	}

	/**
	 * Gets the vert count.
	 *
	 * @return the vert count
	 */
	public int getVertCount() {
		return vertCount;
	}

	/**
	 * Sets the vert count.
	 *
	 * @param vertCount the new vert count
	 */
	public void setVertCount(int vertCount) {
		this.vertCount = vertCount;
	}

}
