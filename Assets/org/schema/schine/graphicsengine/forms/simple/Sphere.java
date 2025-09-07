package org.schema.schine.graphicsengine.forms.simple;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;

public class Sphere {

	final static int space = 10;

	final static int VertexCount = ((90 / space) * (360 / space) * 4);

	public static void createSphere(float radius, float x, float y, float z,
	                                float tCoordMult, Vector3f[] vertices, Vector2f[] texCoords) {

		int n;

		float a;

		float b;

		n = 0;

		for (b = 0; b <= 90 - space; b += space) {

			for (a = 0; a <= 360 - space; a += space) {

				vertices[n].x = radius * FastMath.sin((a) / 180 * FastMath.PI)
						* FastMath.sin((b) / 180 * FastMath.PI) - x;

				vertices[n].y = radius * FastMath.cos((a) / 180 * FastMath.PI)
						* FastMath.sin((b) / 180 * FastMath.PI) + y;

				vertices[n].z = radius * FastMath.cos((b) / 180 * FastMath.PI)
						- z;

				texCoords[n].y = (2 * b) / 360 * tCoordMult;

				texCoords[n].x = (a) / 360 * tCoordMult;

				n++;

				vertices[n].x = radius * FastMath.sin((a) / 180 * FastMath.PI)
						* FastMath.sin((b + space) / 180 * FastMath.PI) - x;

				vertices[n].y = radius * FastMath.cos((a) / 180 * FastMath.PI)
						* FastMath.sin((b + space) / 180 * FastMath.PI) + y;

				vertices[n].z = radius
						* FastMath.cos((b + space) / 180 * FastMath.PI) - z;

				texCoords[n].y = (2 * (b + space)) / 360 * tCoordMult;

				texCoords[n].x = (a) / 360 * tCoordMult;

				n++;

				vertices[n].x = radius
						* FastMath.sin((a + space) / 180 * FastMath.PI)
						* FastMath.sin((b) / 180 * FastMath.PI) - x;

				vertices[n].y = radius
						* FastMath.cos((a + space) / 180 * FastMath.PI)
						* FastMath.sin((b) / 180 * FastMath.PI) + y;

				vertices[n].z = radius * FastMath.cos((b) / 180 * FastMath.PI)
						- z;

				texCoords[n].y = (2 * b) / 360 * tCoordMult;

				texCoords[n].x = (a + space) / 360 * tCoordMult;

				n++;

				vertices[n].x = radius
						* FastMath.sin((a + space) / 180 * FastMath.PI)
						* FastMath.sin((b + space) / 180 * FastMath.PI) - x;

				vertices[n].y = radius
						* FastMath.cos((a + space) / 180 * FastMath.PI)
						* FastMath.sin((b + space) / 180 * FastMath.PI) + y;

				vertices[n].z = radius
						* FastMath.cos((b + space) / 180 * FastMath.PI) - z;

				texCoords[n].y = (2 * (b + space)) / 360 * tCoordMult;

				texCoords[n].x = (a + space) / 360 * tCoordMult;

				n++;

			}

		}
	}

	public static void draw(Vector3f[] vertices, Vector2f[] texCoords) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		for (int b = 0; b < VertexCount; b++) {
			GL11.glTexCoord2f(texCoords[b].x, texCoords[b].y);
			GL11.glVertex3f(vertices[b].x, vertices[b].y, -vertices[b].z);
		}
		for (int b = 0; b < VertexCount; b++) {
			GL11.glTexCoord2f(texCoords[b].x, -texCoords[b].y);
			GL11.glVertex3f(vertices[b].x, vertices[b].y, vertices[b].z);
		}
		GL11.glEnd();
	}

	public static Vector2f[] initTexCoords() {
		Vector2f[] t = new Vector2f[VertexCount];
		for (int i = 0; i < t.length; i++) {
			t[i] = new Vector2f();
		}
		return t;
	}

	public static Vector3f[] initVertices() {
		Vector3f[] verts = new Vector3f[VertexCount];
		for (int i = 0; i < verts.length; i++) {
			verts[i] = new Vector3f();
		}
		return verts;
	}
}
