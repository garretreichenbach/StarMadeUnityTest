package org.schema.common.util.linAlg;

import javax.vecmath.Vector3f;

public class SphereTools {
	public static boolean lineSphereIntersect(Vector3f from, Vector3f to, Vector3f sphereOrigin, float radius) {
		// The line passes through p1 and p2:
		Vector3f p1 = from;
		Vector3f p2 = to;

		// Sphere center is p3, radius is r:
		Vector3f p3 = sphereOrigin;
		float r = radius;

		float x1 = p1.x;
		float y1 = p1.y;
		float z1 = p1.z;
		float x2 = p2.x;
		float y2 = p2.y;
		float z2 = p2.z;
		float x3 = p3.x;
		float y3 = p3.y;
		float z3 = p3.z;

		float dx = x2 - x1;
		float dy = y2 - y1;
		float dz = z2 - z1;

		float a = dx * dx + dy * dy + dz * dz;
		float b = 2.0f * (dx * (x1 - x3) + dy * (y1 - y3) + dz * (z1 - z3));
		float c = x3 * x3 + y3 * y3 + z3 * z3 + x1 * x1 + y1 * y1 + z1 * z1 - 2.0f * (x3 * x1 + y3 * y1 + z3 * z1)
				- r * r;

		float test = b * b - 4.0f * a * c;
		return test >= 0.0;

		// USE THIS IF YOU NEED A HITPOINT
		// if (test >= 0.0) {
		// // Hit (according to Treebeard, "a fine hit").
		//
		//
		// float u = (-b - FastMath.carmackSqrt(test)) / (2.0 * a);
		// vec3 hitp = p1 + u * (p2 - p1);
		// // Now use hitp.
		// }
	}
	public static boolean raySphereIntersect(Vector3f rayOrigin, Vector3f rayDirection, Vector3f sphereOrigin,
			float sphereRadius, Vector3f QTmp) {
		
		QTmp.set(sphereOrigin);
		QTmp.sub(rayOrigin);

		float c = QTmp.length();
		float v = QTmp.dot(rayDirection);
		float d = sphereRadius * sphereRadius - (c * c - v * v);

		return d >= 0.0;

	}
}
