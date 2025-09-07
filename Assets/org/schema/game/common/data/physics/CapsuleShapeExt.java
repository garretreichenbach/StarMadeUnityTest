package org.schema.game.common.data.physics;

import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;
import org.schema.common.FastMath;
import org.schema.game.common.data.physics.shape.GameShape;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class CapsuleShapeExt extends CapsuleShape implements GameShape {

	private final Vector3f tmp = new Vector3f();
	private final Vector3f halfExtents = new Vector3f();
	private final Vector3f extent = new Vector3f();
	private final Matrix3f abs_b = new Matrix3f();

	private final Vector3f aabbMinCache = new Vector3f();
	private final Vector3f aabbMaxCache = new Vector3f();
	private final Transform tCache = new Transform();
	private final Vector3f vec = new Vector3f();
	private final Vector3f vtx = new Vector3f();
	private final Vector3f tmp1 = new Vector3f();
	private final Vector3f tmp2 = new Vector3f();
	private final Vector3f pos = new Vector3f();
	int cachePointer;
	private float lastRadiusA;
	private float lastRadiusB;
	private final Vector3f[] cacheFrom = {new Vector3f(), new Vector3f()};
	private final Vector3f[] cacheTo = {new Vector3f(), new Vector3f()};
	private final SimpleTransformableSendableObject<?> obj;
	public CapsuleShapeExt(SimpleTransformableSendableObject<?> obj, float radius, float height) {
		super(radius, height);
		this.obj = obj;
	}

	@Override
	public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec0, Vector3f out) {
		if (getRadius() == lastRadiusB) {
			if (cacheFrom[0].equals(vec0)) {
				return cacheTo[0];
			}
			if (cacheFrom[1].equals(vec0)) {
				return cacheTo[1];
			}
		} else {
			cacheFrom[0].set(0, 0, 0);
			cacheFrom[1].set(0, 0, 0);
		}
		lastRadiusB = getRadius();
		Vector3f supVec = out;
		supVec.set(0.0f, 0.0f, 0.0f);

		float maxDot = -1.0e30f;

		vec.set(vec0);
		float lenSqr = vec.lengthSquared();
		if (lenSqr < 0.0001f) {
			vec.set(1.0f, 0.0f, 0.0f);
		} else {
			float rlen = FastMath.carmackInvSqrt(lenSqr);//1f / (float) FastMath.carmackSqrt(lenSqr);
			vec.scale(rlen);
		}

		float newDot;

		float radius = getRadius();

		{
			pos.set(0.0f, 0.0f, 0.0f);
			VectorUtil.setCoord(pos, getUpAxis(), getHalfHeight());

			VectorUtil.mul(tmp1, vec, localScaling);
			tmp1.scale(radius);
			tmp2.scale(getMargin(), vec);
			vtx.add(pos, tmp1);
			vtx.sub(tmp2);
			newDot = vec.dot(vtx);
			if (newDot > maxDot) {
				maxDot = newDot;
				supVec.set(vtx);
			}
		}
		{
			pos.set(0.0f, 0.0f, 0.0f);
			VectorUtil.setCoord(pos, getUpAxis(), -getHalfHeight());

			VectorUtil.mul(tmp1, vec, localScaling);
			tmp1.scale(radius);
			tmp2.scale(getMargin(), vec);
			vtx.add(pos, tmp1);
			vtx.sub(tmp2);
			newDot = vec.dot(vtx);
			if (newDot > maxDot) {
				maxDot = newDot;
				supVec.set(vtx);
			}
		}
		cacheFrom[cachePointer].set(vec0);
		cacheTo[cachePointer].set(out);
		cachePointer = (cachePointer + 1) % cacheFrom.length;
		return out;
	}

	@Override
	public void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax) {

		if (!t.equals(tCache) || getRadius() != lastRadiusA) {
			lastRadiusA = getRadius();
			halfExtents.set(getRadius(), getRadius(), getRadius());
			VectorUtil.setCoord(halfExtents, upAxis, getRadius() + getHalfHeight());

			halfExtents.x += getMargin();
			halfExtents.y += getMargin();
			halfExtents.z += getMargin();

			abs_b.set(t.basis);
			MatrixUtil.absolute(abs_b);

			Vector3f center = t.origin;

			abs_b.getRow(0, tmp);
			extent.x = tmp.dot(halfExtents);
			abs_b.getRow(1, tmp);
			extent.y = tmp.dot(halfExtents);
			abs_b.getRow(2, tmp);
			extent.z = tmp.dot(halfExtents);

			aabbMin.sub(center, extent);
			aabbMax.add(center, extent);
			aabbMinCache.set(aabbMin);
			aabbMaxCache.set(aabbMax);
			tCache.set(t);
		} else {
			aabbMin.set(aabbMinCache);
			aabbMax.set(aabbMaxCache);
		}
	}

	@Override
	public SimpleTransformableSendableObject getSimpleTransformableSendableObject() {
		return obj;
	}

}
