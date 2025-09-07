package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.util.ObjectArrayList;

public class ConvexHullShapeExt extends ConvexHullShape {


	public ConvexHullShapeExt(ObjectArrayList<Vector3f> points) {
		super(points);
	}
	private final Vector3f tmp0 = new Vector3f();
	private final Vector3f tmp1 = new Vector3f();
	private final Vector3f tmp2 = new Vector3f();
	@Override
	public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec0, Vector3f out) {
		Vector3f supVec = out;
		supVec.set(0f, 0f, 0f);
		float newDot, maxDot = -1e30f;

		Vector3f vec = tmp0;
		vec.set(vec0);
		float lenSqr = vec.lengthSquared();
		if (lenSqr < 0.0001f) {
			vec.set(1f, 0f, 0f);
		}
		else {
			float rlen = FastMath.carmackInvSqrt(lenSqr);
			vec.scale(rlen);
		}


		Vector3f vtx = tmp1;
		final int size = getPoints().size();
		for (int i = 0; i < size; i++) {
//			VectorUtil.mul(vtx, getPoints().getQuick(i), localScaling);
			vtx.set(getPoints().getQuick(i));
			newDot = vec.dot(vtx);
			if (newDot > maxDot) {
				maxDot = newDot;
				supVec.set(vtx);
			}
		}
		return out;
	}
	@Override
	public Vector3f localGetSupportingVertex(Vector3f vec, Vector3f out) {
		Vector3f supVertex = localGetSupportingVertexWithoutMargin(vec, out);

		if (getMargin() != 0f) {
			Vector3f vecnorm = tmp2;
			vecnorm.set(vec);
			if (vecnorm.lengthSquared() < (BulletGlobals.FLT_EPSILON * BulletGlobals.FLT_EPSILON)) {
				vecnorm.set(-1f, -1f, -1f);
			}
			FastMath.normalizeCarmack(vecnorm);
			supVertex.scaleAdd(getMargin(), vecnorm, supVertex);
		}
		return out;
	}
}
