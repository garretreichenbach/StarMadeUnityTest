package org.schema.common.util.linAlg;

import javax.vecmath.Vector2f;

public class Vector2fTools {

	public static Vector2f intersectsFromInside(float x, float y, float width, float height, Vector2f from, Vector2f to) {

		{
			Vector2f a = new Vector2f(x, y);
			Vector2f b = new Vector2f(x, y + height);

			Vector2f intersects = intersects(from, to, a, b);
			if (intersects != null) {
				return intersects;
			}
		}
		{
			Vector2f a = new Vector2f(x, y);
			Vector2f b = new Vector2f(x + width, y);

			Vector2f intersects = intersects(from, to, a, b);
			if (intersects != null) {
				return intersects;
			}
		}
		{
			Vector2f a = new Vector2f(x + width, y);
			Vector2f b = new Vector2f(x + width, y + height);

			Vector2f intersects = intersects(from, to, a, b);
			if (intersects != null) {
				return intersects;
			}
		}
		{
			Vector2f a = new Vector2f(x + width, y + height);
			Vector2f b = new Vector2f(x, y + height);

			Vector2f intersects = intersects(from, to, a, b);
			if (intersects != null) {
				return intersects;
			}
		}

		return null;
	}

	public static Vector2f intersects(Vector2f a1, Vector2f a2, Vector2f b1, Vector2f b2) {
		Vector2f b = sub(a2, a1);
		Vector2f d = sub(b2, b1);
		float bDotDPerp = b.x * d.y - b.y * d.x;

		// if b dot d == 0, it means the lines are parallel so have infinite intersection points
		if (bDotDPerp == 0)
			return null;

		Vector2f c = sub(b1, a1);
		float t = (c.x * d.y - c.y * d.x) / bDotDPerp;
		if (t < 0 || t > 1) {
			return null;
		}

		float u = (c.x * b.y - c.y * b.x) / bDotDPerp;
		if (u < 0 || u > 1) {
			return null;
		}

		b.scale(t);
		a1.add(b);

		return a1;

//        return a1 + t * b;
	}

	public static Vector2f sub(Vector2f a1, Vector2f a2) {
		Vector2f v = new Vector2f();
		v.sub(a1, a2);
		return v;
	}
}
