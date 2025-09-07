package org.schema.game.common.data.physics.shape;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.Transform;

public class ModifiedAABBSphereShape extends SphereShape {

	Vector3f extent = new Vector3f();
	private float ext;

	public ModifiedAABBSphereShape(float radius, float ext) {
		super(radius);
		this.ext = ext;
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.shapes.SphereShape#getAabb(com.bulletphysics.linearmath.Transform, javax.vecmath.Vector3f, javax.vecmath.Vector3f)
	 */
	@Override
	public void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax) {
		super.getAabb(t, aabbMin, aabbMax);
		Vector3f center = t.origin;
		extent.set(getMargin() + ext, getMargin() + ext, getMargin() + ext);
		aabbMin.sub(center, extent);
		aabbMax.add(center, extent);
	}

}
