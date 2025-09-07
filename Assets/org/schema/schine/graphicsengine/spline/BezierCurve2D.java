package org.schema.schine.graphicsengine.spline;

import javax.vecmath.Vector2f;

public class BezierCurve2D {
	private static float coeff(int n, int k) {
		return (float) (fact(n) / (fact(k) * fact(n - k)));
	}

	private static double fact(int n) {
		double val = 1.0;
		for (int i = 2; i <= n; i++)
			val *= i;
		return val;
	}

	public static Vector2f interpolate(float t, Vector2f... ps) {
		float x = 0.0f;
		float y = 0.0f;

		final int n = ps.length - 1;

		for (int k = 0; k <= n; k++) {
			float m = pow(1.0f - t, n - k) * coeff(n, k) * pow(t, k);

			x += ps[k].x * m;
			y += ps[k].y * m;
		}

		return new Vector2f(x, y);
	}

	public static Vector2f interpolate4(float t, Vector2f p1, Vector2f p2, Vector2f p3, Vector2f p4) {
		float invT = 1.0f - t;

		float m1 = pow3(invT) * 1.0f * pow0(t);
		float m2 = pow2(invT) * 3.0f * pow1(t);
		float m3 = pow1(invT) * 3.0f * pow2(t);
		float m4 = pow0(invT) * 1.0f * pow3(t);

		float x = 0.0f;
		float y = 0.0f;

		x += p1.x * m1;
		y += p1.y * m1;

		x += p2.x * m2;
		y += p2.y * m2;

		x += p3.x * m3;
		y += p3.y * m3;

		x += p4.x * m4;
		y += p4.y * m4;

		return new Vector2f(x, y);
	}

	private static float pow(float t, int times) {
		float tt = 1.0f;
		for (int i = 0; i < times; i++)
			tt *= t;
		return tt;
	}

	private static float pow0(float t) {
		return 1.0f;
	}

	private static float pow1(float t) {
		return t;
	}

	private static float pow2(float t) {
		return t * t;
	}

	private static float pow3(float t) {
		return t * t * t;
	}
}
