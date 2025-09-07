package org.schema.schine.graphicsengine.forms;

import java.nio.FloatBuffer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.forms.simple.Box;

/**
 * Utility class to draw a cube. Some examples use the GLUT function glutWireCube(). This class can
 * be used to replace these calls.
 *
 * @author Ciardhubh
 */
public class Cube {

	// Cube data from example 2-16
	private static final float[][] vertices = {
			{-0.5f, -0.5f, -0.5f}, // 0
			{0.5f, -0.5f, -0.5f},
			{0.5f, 0.5f, -0.5f},
			{-0.5f, 0.5f, -0.5f}, // 3
			{-0.5f, -0.5f, 0.5f}, // 4
			{0.5f, -0.5f, 0.5f},
			{0.5f, 0.5f, 0.5f},
			{-0.5f, 0.5f, 0.5f} // 7
	};
	private static final float[][] normals = {
			{0, 0, -1},
			{0, 0, 1},
			{0, -1, 0},
			{0, 1, 0},
			{-1, 0, 0},
			{1, 0, 0}
	};
	private static final byte[][] indices = {
			{0, 3, 2, 1},
			{4, 5, 6, 7},
			{0, 1, 5, 4},
			{3, 7, 6, 2},
			{0, 4, 7, 3},
			{1, 2, 6, 5}
	};

	private static final Vector3f[][] verts = Box.init();
	private static final Vector2f[][] texCoords = new Vector2f[6][4];

	public static void cube(FloatBuffer buffer, float size, Vector3f pos) {
		// Draw all six sides of the cube.
		for (int i = 0; i < 6; i++) {
			// Draw all four vertices of the current side.
			for (int m = 0; m < 4; m++) {
				float[] temp = vertices[indices[i][m]];
				buffer.put(pos.x + temp[0] * size);
				buffer.put(pos.y + temp[1] * size);
				buffer.put(pos.z + temp[2] * size);
			}
		}
	}

	public static void cubeWithIndex(FloatBuffer buffer, float size, Vector3f pos) {
		// Draw all six sides of the cube.
		for (int i = 0; i < 6; i++) {
			// Draw all four vertices of the current side.
			for (int m = 0; m < 4; m++) {
				float[] temp = vertices[indices[i][m]];
				buffer.put(pos.x + temp[0] * size);
				buffer.put(pos.y + temp[1] * size);
				buffer.put(pos.z + temp[2] * size);
				buffer.put(i * 6 + 4);
			}
		}
	}

	/**
	 * Draws a solid cube with the current set transformations. The cube's size can be adjusted.
	 * <p/>
	 * Size should be positive. Behaviour for negative values is undefined. The parameter size
	 * defines the resulting cube's edge length, e.g. a size of 1.0f means that the cube will have
	 * an edge length of 1.0f.
	 *
	 * @param size Length of the cube's edges.
	 */
	public static void solidCube(final float size) {
		// Draw all six sides of the cube.
		for (int i = 0; i < 6; i++) {
			GL11.glBegin(GL11.GL_QUADS);
			// Draw all four vertices of the current side.
			for (int m = 0; m < 4; m++) {
				float[] temp = vertices[indices[i][m]];
				GL11.glNormal3f(normals[i][0], normals[i][1], normals[i][2]);
				GL11.glVertex3f(temp[0] * size, temp[1] * size, temp[2] * size);
			}
			GL11.glEnd();
		}
	}

	public static void solidCube(final Vector3f min, Vector3f max) {
		Vector3f[][] box = Box.getVertices(
				min,
				max, verts);
		Box.getTexCoords(1, texCoords);
		GL11.glBegin(GL11.GL_QUADS);
		for (int i = 0; i < box.length; i++) {
			for (int k = box[i].length - 1; k >= 0; k--) {
				//					GL11.glNormal3f(normals[i/6][0], normals[i/6][1], normals[i/6][2]);
				GL11.glTexCoord2f(texCoords[i][k].x, texCoords[i][k].y);
				GL11.glVertex3f(box[i][k].x, box[i][k].y, box[i][k].z);
			}
		}
		GL11.glEnd();

	}

	/**
	 * Draws a wireframe cube with the current set transformations. The cube's size can be adjusted.
	 * <p/>
	 * Size should be positive. Behaviour for negative values is undefined. The parameter size
	 * defines the resulting cube's edge length, e.g. a size of 1.0f means that the cube will have
	 * an edge length of 1.0f.
	 *
	 * @param size Length of the cube's edges.
	 */
	public static void wireCube(final float size) {
		// Draw all six sides of the cube.
		for (int i = 0; i < 6; i++) {
			GL11.glBegin(GL11.GL_LINE_LOOP);
			// Draw all four vertices of the current side.
			for (int m = 0; m < 4; m++) {
				float[] temp = vertices[indices[i][m]];
				GL11.glNormal3f(normals[i][0], normals[i][1], normals[i][2]);
				GL11.glVertex3f(temp[0] * size, temp[1] * size, temp[2] * size);
			}
			GL11.glEnd();
		}
	}
}
