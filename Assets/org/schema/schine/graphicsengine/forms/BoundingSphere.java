package org.schema.schine.graphicsengine.forms;

import com.bulletphysics.linearmath.AabbUtil2;
import org.schema.common.util.linAlg.Vector3fTools;

import javax.vecmath.Vector3f;

public class BoundingSphere {
	public float radius;
	public Vector3f center;

	public BoundingSphere(float radius, Vector3f center) {
		this.radius = radius;
		this.center = center;
	}

	public BoundingSphere() {
		this(0, new Vector3f());
	}

	public void setFrom(BoundingBox boundingBox) {
		radius = Math.max(boundingBox.max.length(), boundingBox.min.length());
		center = new Vector3f();
	}

	public boolean intersects(BoundingSphere other) {
		return AabbUtil2.testSphereAgainstSphere(other.radius, other.center, radius, center);
	}

	public boolean isInside(Vector3f point) {
		return Vector3fTools.distance(point, center) < radius;
	}

	public boolean isInside(float x, float y, float z) {
		return Vector3fTools.distance(x, y, z, center.x, center.y, center.z) < radius;
	}
}
