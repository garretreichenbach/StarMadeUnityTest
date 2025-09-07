/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>TerrainMeshOptimizer</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * TerrainMeshOptimizer.java
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

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.forms.Mesh.Face;

/**
 * The Class TerrainMeshOptimizer.
 */
public class TerrainMeshOptimizer {

	/**
	 * The Constant TERRAIN_OPTIMIZE_HEIGHT_THRESHOLD.
	 */
	private static final float TERRAIN_OPTIMIZE_HEIGHT_THRESHOLD = 1;

	/**
	 * The Constant TERRAIN_OPTIMIZE_MIN_QUAD_LENGTH.
	 */
	private static final int TERRAIN_OPTIMIZE_MIN_QUAD_LENGTH = 8;

	/**
	 * The Constant TERRAIN_OPTIMIZE_MAX_AFFECTED.
	 */
	private static final int TERRAIN_OPTIMIZE_MAX_AFFECTED = 1;
	/**
	 * The m.
	 */
	private OldTerrain m;
	/**
	 * The w.
	 */
	private int w;
	/**
	 * The h.
	 */
	private int h;
	/**
	 * The sub quads.
	 */
	private Vector3f[][] subQuads;

	/**
	 * Instantiates a new heightField mesh optimizer.
	 *
	 * @param m the m
	 * @param w the w
	 * @param h the h
	 */
	public TerrainMeshOptimizer(OldTerrain m, int w, int h) {
		this.m = m;
		this.w = w;
		this.h = h;
	}

	/**
	 * Optimize.
	 *
	 * @param m        the m
	 * @param w        the w
	 * @param h        the h
	 * @param subQuads the sub quads
	 */
	public static void optimize(OldTerrain m, int w, int h, Vector3f[][] subQuads, float terOffSetX, float terOffSetY, float maxWidth, float maxHeight) {
		ArrayList<Vector3f> newVerts = new ArrayList<Vector3f>();
		ArrayList<Vector3f> newTexCoords = new ArrayList<Vector3f>();
		ArrayList<Face> newFaces = new ArrayList<Face>();

		//		 Vector3f[] verts = new Vector3f[m.vertices.length];
		//		 for(int i = 0; i < verts.length; i++){
		//			 verts[i] = m.vertices[i/2];
		//		 }
		optimizeQuad(m, 0, 0, w, h, w, h, 0, m.vertices, newVerts, newTexCoords, newFaces, subQuads);
		m.vertCount = newVerts.size();
		m.faceCount = newFaces.size();

		m.vertices = new Vector3f[m.vertCount];
		m.normals = new Vector3f[m.vertCount];
		m.texCoords = new Vector3f[m.vertCount];
		m.faces = new Face[m.faceCount];

		float facX = w / maxWidth;
		float facY = h / maxHeight;
		float texXStart = (terOffSetX) * facX;
		float texYStart = (terOffSetY) * facY;
		System.err.println("factor: " + facX + ", " + facY + "   -start:   " + texXStart + ", " + texYStart + "   -off:   " + terOffSetX + ", " + terOffSetY);
		for (int i = 0; i < m.vertCount; i++) {
			m.vertices[i] = newVerts.get(i);
			m.normals[i] = new Vector3f();
			Vector3f t = new Vector3f(newTexCoords.get(i));
			t.set(t.x * facX, t.y * facY, 0);
			t.set(t.x + texXStart, t.y + texYStart, 0);

			m.texCoords[i] = t;
		}
		for (int i = 0; i < m.faceCount; i++) {
			m.faces[i] = newFaces.get(i);
		}
	}

	/**
	 * Optimize quad.
	 *
	 * @param m            the m
	 * @param x            the x
	 * @param y            the y
	 * @param quadW        the ex
	 * @param quadH        the ey
	 * @param w            the w
	 * @param h            the h
	 * @param lvl          the lvl
	 * @param quad         the quad
	 * @param newVerts     the new verts
	 * @param newTexCoords the new tex coords
	 * @param newFaces     the new faces
	 * @param subQuads     the sub quads
	 */
	private static void optimizeQuad(OldTerrain m, float x, float y, float quadW, float quadH, int w, int h, int lvl, Vector3f[] quad, ArrayList<Vector3f> newVerts, ArrayList<Vector3f> newTexCoords, ArrayList<Face> newFaces, Vector3f[][] subQuads) {
	    /*
         * with 4*4 its a=0, b=3, c=12, d=15
		 * 0  1  2  3
		 * 4  5  6  7
		 * 8  9  10 11
		 * 12 13 14 15
		 *
		 */

		//get the average height
		float avg = 0;
		int s = 0;
		int yMin = Math.max((int) FastMath.floor(y) - 1, 0);
		int xMin = Math.max((int) FastMath.floor(x) - 1, 0);
		for (int i = yMin; i < Math.min(h, yMin + FastMath.ceil(quadH) + 1); i++) {
			for (int j = xMin; j < Math.min(h, xMin + FastMath.ceil(quadW) + 1); j++) {
				avg += subQuads[i][j].y;
				s++;
			}
		}
		if (s == 0) {
			throw new ArithmeticException("zero at " + x + "," + y + "," + quadW + ", " + quadH);
		}
		avg /= s;
		//check, if a vertices in the quad differ strongly from the avg
		//		System.err.println("avg: "+avg);
		int affected = 0;
		boolean optimizeFurther = false;

		for (int i = yMin; i < Math.min(h, yMin + FastMath.ceil(quadH) + 1); i++) {
			for (int j = xMin; j < Math.min(h, xMin + FastMath.ceil(quadW) + 1); j++) {
				if (FastMath.abs(subQuads[i][j].y - avg) > TERRAIN_OPTIMIZE_HEIGHT_THRESHOLD) {

					affected++;
				}
				if (affected > TERRAIN_OPTIMIZE_MAX_AFFECTED) {
					optimizeFurther = true;
					break;
				}
			}
			if (optimizeFurther) {
				break;
			}
		}
		if (optimizeFurther && FastMath.abs(x - quadW) >= TERRAIN_OPTIMIZE_MIN_QUAD_LENGTH && FastMath.abs(quadH - y) >= TERRAIN_OPTIMIZE_MIN_QUAD_LENGTH) {
			optimizeQuadTree(m, x, y, quadW, quadH, w, h, lvl, quad, newVerts, newTexCoords, newFaces, subQuads);
		} else {
			//this quad is optimal:
			//add id to the mesh (abc) (cbd)
			Face fA = new Face();
			Face fB = new Face();
			int i = newVerts.size();

			fA.m_normalIndex = new int[3];
			fA.m_texCoordsIndex = new int[3];
			fA.m_vertsIndex = new int[3];

			fB.m_normalIndex = new int[3];
			fB.m_texCoordsIndex = new int[3];
			fB.m_vertsIndex = new int[3];

			fA.m_vertsIndex[0] = i;            //a
			fA.m_vertsIndex[1] = i + 1;        //b
			fA.m_vertsIndex[2] = i + 2;        //c
			fA.m_normalIndex[0] = i;        //a
			fA.m_normalIndex[1] = i + 1;        //b
			fA.m_normalIndex[2] = i + 2;        //c
			fA.m_texCoordsIndex[0] = i;        //a
			fA.m_texCoordsIndex[1] = i + 1;    //b
			fA.m_texCoordsIndex[2] = i + 2;    //c

			fB.m_vertsIndex[0] = i + 2;        //c
			fB.m_vertsIndex[1] = i + 1;        //b
			fB.m_vertsIndex[2] = i + 3;        //d
			fB.m_normalIndex[0] = i + 2;        //a
			fB.m_normalIndex[1] = i + 1;        //b
			fB.m_normalIndex[2] = i + 3;        //c
			fB.m_texCoordsIndex[0] = i + 2;        //a
			fB.m_texCoordsIndex[1] = i + 1;    //b
			fB.m_texCoordsIndex[2] = i + 3;    //c

			float texFakX = 1f / ((float) (w) * OldTerrain.FIELD_SIZE * OldTerrain.scalefactor);
			float texFakY = 1f / ((float) (h) * OldTerrain.FIELD_SIZE * OldTerrain.scalefactor);

			Vector3f aV = new Vector3f(subQuads[(int) y][(int) x]);
			Vector3f bV = new Vector3f(subQuads[(int) y][Math.min(w - 1, (int) quadW)]);
			Vector3f cV = new Vector3f(subQuads[Math.min(h - 1, (int) quadH)][(int) x]);
			Vector3f dV = new Vector3f(subQuads[Math.min(h - 1, (int) quadH)][Math.min(w - 1, (int) quadW)]);

			newTexCoords.add(new Vector3f(aV.x * texFakX, aV.z * texFakY, 0));
			newTexCoords.add(new Vector3f(bV.x * texFakX, bV.z * texFakY, 0));
			newTexCoords.add(new Vector3f(cV.x * texFakX, cV.z * texFakY, 0));
			newTexCoords.add(new Vector3f(dV.x * texFakX, dV.z * texFakY, 0));

			newVerts.add(new Vector3f(subQuads[(int) y][(int) x]));
			newVerts.add(new Vector3f(subQuads[(int) y][Math.min(w - 1, (int) quadW)]));
			newVerts.add(new Vector3f(subQuads[Math.min(h - 1, (int) quadH)][(int) x]));
			newVerts.add(new Vector3f(subQuads[Math.min(h - 1, (int) quadH)][Math.min(w - 1, (int) quadW)]));

			newFaces.add(fA);
			newFaces.add(fB);
		}
	}

	/**
	 * Optimize quad tree.
	 *
	 * @param m            the m
	 * @param x            the x
	 * @param y            the y
	 * @param quadW        the ex
	 * @param quadH        the ey
	 * @param w            the w
	 * @param h            the h
	 * @param lvl          the lvl
	 * @param quad         the quad
	 * @param newVerts     the new verts
	 * @param newTexCoords the new tex coords
	 * @param newFaces     the new faces
	 * @param subQuads     the sub quads
	 */
	private static void optimizeQuadTree(OldTerrain m, float x, float y, float quadW, float quadH, int w, int h, int lvl, Vector3f[] quad, ArrayList<Vector3f> newVerts, ArrayList<Vector3f> newTexCoords, ArrayList<Face> newFaces, Vector3f[][] subQuads) {
		//do a quadric subdivision

		//		System.err.println("subdividing: "+x+","+y+","+ex+","+ey);
        /*
         * with 4*4
		 * 0  1 | 2  3
		 * 4  5 | 6  7
		 * ------------
		 * 8  9 | 10 11
		 * 12 13| 14 15
		 *
		 */
		//		int[] multiTexturePathPattern = getSubdivionQuadIndices(w, h);

		float width = FastMath.abs(quadW - x);
		float height = FastMath.abs(quadH - y);

		optimizeQuad(m, x, y, x + width / 2f, y + height / 2f, w, h, lvl + 1, quad, newVerts, newTexCoords, newFaces, subQuads);

		optimizeQuad(m, x + width / 2f, y, x + width, y + height / 2f, w, h, lvl + 1, quad, newVerts, newTexCoords, newFaces, subQuads);

		optimizeQuad(m, x, y + height / 2f, x + width / 2f, y + height, w, h, lvl + 1, quad, newVerts, newTexCoords, newFaces, subQuads);

		optimizeQuad(m, x + width / 2, y + height / 2, x + width, y + height, w, h, lvl + 1, quad, newVerts, newTexCoords, newFaces, subQuads);

	}

	/**
	 * Optimize.
	 */
	public void optimize(int terOffSetX, int terOffSetY, int maxWidth, int maxHeight) {
		this.subQuads = new Vector3f[w][h];
		int c = 0;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				subQuads[i][j] = m.getOrigVerts()[c];
				//					System.err.println(j+", "+i+" "+quad[c]);
				c++;
			}
		}
		optimize(m, w, h, subQuads, terOffSetX, terOffSetY, maxWidth, maxHeight);
	}
}
