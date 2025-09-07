package org.schema.schine.graphicsengine.forms.simple;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class Box {

	public static Vector2f[][] getTexCoords() {
		Vector2f[][] out = new Vector2f[6][4];
		for (int i = 0; i < out.length; i++) {
			out[i][0] = new Vector2f(0, 0);
			out[i][1] = new Vector2f(1, 0);
			out[i][2] = new Vector2f(1, 1);
			out[i][3] = new Vector2f(0, 1);
		}
		return out;
	}

	public static Vector2f[][] getTexCoords(float mult, Vector2f[][] out) {
		for (int i = 0; i < out.length; i++) {
			out[i][0] = new Vector2f(0, 0);
			out[i][1] = new Vector2f(mult, 0);
			out[i][2] = new Vector2f(mult, mult);
			out[i][3] = new Vector2f(0, mult);
		}
		return out;
	}

	public static Vector3f[][] getVertices(Vector3f min, Vector3f max) {
		return getVertices(min, max, init());
	}

	public static Vector3f[][] getVertices(Vector3f min, Vector3f max, Vector3f[][] out) {
		assert (out != null);
		assert (out.length == 6) : out.length;
		assert (out[0].length == 4) : out[0].length;
		out[0][0].set(max.x, min.y, min.z);
		out[0][1].set(max.x, max.y, min.z);
		out[0][2].set(max.x, max.y, max.z);
		out[0][3].set(max.x, min.y, max.z);
		out[1][3].set(min.x, min.y, min.z);
		out[1][2].set(min.x, max.y, min.z);
		out[1][1].set(min.x, max.y, max.z);
		out[1][0].set(min.x, min.y, max.z);

		out[2][3].set(min.x, max.y, min.z);
		out[2][2].set(max.x, max.y, min.z);
		out[2][1].set(max.x, max.y, max.z);
		out[2][0].set(min.x, max.y, max.z);
		out[3][0].set(min.x, min.y, min.z);
		out[3][1].set(max.x, min.y, min.z);
		out[3][2].set(max.x, min.y, max.z);
		out[3][3].set(min.x, min.y, max.z);

		out[4][0].set(min.x, min.y, max.z);
		out[4][1].set(max.x, min.y, max.z);
		out[4][2].set(max.x, max.y, max.z);
		out[4][3].set(min.x, max.y, max.z);
		out[5][3].set(min.x, min.y, min.z);
		out[5][2].set(max.x, min.y, min.z);
		out[5][1].set(max.x, max.y, min.z);
		out[5][0].set(min.x, max.y, min.z);

		return out;
	}

	public static Vector3f[][] init() {
		Vector3f[][] b = new Vector3f[6][4];
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				b[i][j] = new Vector3f();
			}
		}
		return b;
	}

}
