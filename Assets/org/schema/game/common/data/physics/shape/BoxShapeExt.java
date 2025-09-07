package org.schema.game.common.data.physics.shape;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.ScalarUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

public class BoxShapeExt extends BoxShape {

	Vector4f pl = new Vector4f();
	Vector3f he = new Vector3f();
	Vector3f he1 = new Vector3f();
	Vector3f im = new Vector3f();
	private Vector3f marginV = new Vector3f();
	private Vector3f d = new Vector3f();

	public BoxShapeExt(Vector3f boxHalfExtents) {
		super(boxHalfExtents);
	}

	@Override
	public Vector3f getHalfExtentsWithMargin(Vector3f out) {
		Vector3f halfExtents = getHalfExtentsWithoutMargin(out);
		Vector3f margin = he;
		margin.set(getMargin(), getMargin(), getMargin());
		halfExtents.add(margin);
		return out;
	}

	@Override
	public Vector3f getHalfExtentsWithoutMargin(Vector3f out) {
		out.set(implicitShapeDimensions); // changed in Bullet 2.63: assume the scaling and margin are included
		return out;
	}

	@Override
	public BroadphaseNativeType getShapeType() {
		return BroadphaseNativeType.BOX_SHAPE_PROXYTYPE;
	}

	@Override
	public Vector3f localGetSupportingVertex(Vector3f vec, Vector3f out) {
		Vector3f halfExtents = getHalfExtentsWithoutMargin(out);

		float margin = getMargin();
		halfExtents.x += margin;
		halfExtents.y += margin;
		halfExtents.z += margin;

		out.set(
				ScalarUtil.fsel(vec.x, halfExtents.x, -halfExtents.x),
				ScalarUtil.fsel(vec.y, halfExtents.y, -halfExtents.y),
				ScalarUtil.fsel(vec.z, halfExtents.z, -halfExtents.z));
		return out;
	}

	@Override
	public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec, Vector3f out) {
		Vector3f halfExtents = getHalfExtentsWithoutMargin(out);

		out.set(
				ScalarUtil.fsel(vec.x, halfExtents.x, -halfExtents.x),
				ScalarUtil.fsel(vec.y, halfExtents.y, -halfExtents.y),
				ScalarUtil.fsel(vec.z, halfExtents.z, -halfExtents.z));
		return out;
	}

	@Override
	public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3f[] vectors, Vector3f[] supportVerticesOut, int numVectors) {
		Vector3f halfExtents = getHalfExtentsWithoutMargin(he);

		for (int i = 0; i < numVectors; i++) {
			Vector3f vec = vectors[i];
			supportVerticesOut[i].set(ScalarUtil.fsel(vec.x, halfExtents.x, -halfExtents.x),
					ScalarUtil.fsel(vec.y, halfExtents.y, -halfExtents.y),
					ScalarUtil.fsel(vec.z, halfExtents.z, -halfExtents.z));
		}
	}

	@Override
	public void setMargin(float margin) {
		// correct the implicitShapeDimensions for the margin
		Vector3f oldMargin = he;
		oldMargin.set(getMargin(), getMargin(), getMargin());
		Vector3f implicitShapeDimensionsWithMargin = im;
		implicitShapeDimensionsWithMargin.add(implicitShapeDimensions, oldMargin);

		super.setMargin(margin);
		Vector3f newMargin = he1;
		newMargin.set(getMargin(), getMargin(), getMargin());
		implicitShapeDimensions.sub(implicitShapeDimensionsWithMargin, newMargin);
	}

	@Override
	public void setLocalScaling(Vector3f scaling) {
		Vector3f oldMargin = he;
		oldMargin.set(getMargin(), getMargin(), getMargin());
		Vector3f implicitShapeDimensionsWithMargin = im;
		implicitShapeDimensionsWithMargin.add(implicitShapeDimensions, oldMargin);
		Vector3f unScaledImplicitShapeDimensionsWithMargin = he1;
		VectorUtil.div(unScaledImplicitShapeDimensionsWithMargin, implicitShapeDimensionsWithMargin, localScaling);

		super.setLocalScaling(scaling);

		VectorUtil.mul(implicitShapeDimensions, unScaledImplicitShapeDimensionsWithMargin, localScaling);
		implicitShapeDimensions.sub(oldMargin);
	}

	@Override
	public void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax) {
		AabbUtil2.transformAabb(getHalfExtentsWithoutMargin(he), getMargin(), t, aabbMin, aabbMax);
	}

	@Override
	public void calculateLocalInertia(float mass, Vector3f inertia) {
		//btScalar margin = btScalar(0.);
		Vector3f halfExtents = getHalfExtentsWithMargin(he);

		float lx = 2f * halfExtents.x;
		float ly = 2f * halfExtents.y;
		float lz = 2f * halfExtents.z;

		inertia.set(mass / 12f * (ly * ly + lz * lz),
				mass / 12f * (lx * lx + lz * lz),
				mass / 12f * (lx * lx + ly * ly));
	}

	@Override
	public void getPlane(Vector3f planeNormal, Vector3f planeSupport, int i) {
		// this plane might not be aligned...
		Vector4f plane = pl;
		getPlaneEquation(plane, i);
		planeNormal.set(plane.x, plane.y, plane.z);
		Vector3f tmp = he1;
		tmp.negate(planeNormal);
		localGetSupportingVertex(tmp, planeSupport);
	}

	@Override
	public int getNumPlanes() {
		return 6;
	}

	@Override
	public int getNumVertices() {
		return 8;
	}

	@Override
	public int getNumEdges() {
		return 12;
	}

	@Override
	public void getVertex(int i, Vector3f vtx) {
		Vector3f halfExtents = getHalfExtentsWithoutMargin(he);

		vtx.set(halfExtents.x * (1 - (i & 1)) - halfExtents.x * (i & 1),
				halfExtents.y * (1 - ((i & 2) >> 1)) - halfExtents.y * ((i & 2) >> 1),
				halfExtents.z * (1 - ((i & 4) >> 2)) - halfExtents.z * ((i & 4) >> 2));
	}

	@Override
	public void getPlaneEquation(Vector4f plane, int i) {
		Vector3f halfExtents = getHalfExtentsWithoutMargin(he);

		switch (i) {
			case 0:
				plane.set(1f, 0f, 0f, -halfExtents.x);
				break;
			case 1:
				plane.set(-1f, 0f, 0f, -halfExtents.x);
				break;
			case 2:
				plane.set(0f, 1f, 0f, -halfExtents.y);
				break;
			case 3:
				plane.set(0f, -1f, 0f, -halfExtents.y);
				break;
			case 4:
				plane.set(0f, 0f, 1f, -halfExtents.z);
				break;
			case 5:
				plane.set(0f, 0f, -1f, -halfExtents.z);
				break;
			default:
				assert (false);
		}
	}

	@Override
	public void getEdge(int i, Vector3f pa, Vector3f pb) {
		int edgeVert0 = 0;
		int edgeVert1 = 0;

		switch (i) {
			case 0:
				edgeVert0 = 0;
				edgeVert1 = 1;
				break;
			case 1:
				edgeVert0 = 0;
				edgeVert1 = 2;
				break;
			case 2:
				edgeVert0 = 1;
				edgeVert1 = 3;

				break;
			case 3:
				edgeVert0 = 2;
				edgeVert1 = 3;
				break;
			case 4:
				edgeVert0 = 0;
				edgeVert1 = 4;
				break;
			case 5:
				edgeVert0 = 1;
				edgeVert1 = 5;

				break;
			case 6:
				edgeVert0 = 2;
				edgeVert1 = 6;
				break;
			case 7:
				edgeVert0 = 3;
				edgeVert1 = 7;
				break;
			case 8:
				edgeVert0 = 4;
				edgeVert1 = 5;
				break;
			case 9:
				edgeVert0 = 4;
				edgeVert1 = 6;
				break;
			case 10:
				edgeVert0 = 5;
				edgeVert1 = 7;
				break;
			case 11:
				edgeVert0 = 6;
				edgeVert1 = 7;
				break;
			default:
				assert (false);
		}

		getVertex(edgeVert0, pa);
		getVertex(edgeVert1, pb);
	}

	@Override
	public boolean isInside(Vector3f pt, float tolerance) {
		Vector3f halfExtents = getHalfExtentsWithoutMargin(he);

		//btScalar minDist = 2*tolerance;

		boolean result =
				(pt.x <= (halfExtents.x + tolerance)) &&
						(pt.x >= (-halfExtents.x - tolerance)) &&
						(pt.y <= (halfExtents.y + tolerance)) &&
						(pt.y >= (-halfExtents.y - tolerance)) &&
						(pt.z <= (halfExtents.z + tolerance)) &&
						(pt.z >= (-halfExtents.z - tolerance));

		return result;
	}

	@Override
	public String getName() {
		return "Box";
	}

	@Override
	public int getNumPreferredPenetrationDirections() {
		return 6;
	}

	@Override
	public void getPreferredPenetrationDirection(int index, Vector3f penetrationVector) {
		switch (index) {
			case 0:
				penetrationVector.set(1f, 0f, 0f);
				break;
			case 1:
				penetrationVector.set(-1f, 0f, 0f);
				break;
			case 2:
				penetrationVector.set(0f, 1f, 0f);
				break;
			case 3:
				penetrationVector.set(0f, -1f, 0f);
				break;
			case 4:
				penetrationVector.set(0f, 0f, 1f);
				break;
			case 5:
				penetrationVector.set(0f, 0f, -1f);
				break;
			default:
				assert (false);
		}
	}

	public void setDimFromBB(Vector3f min, Vector3f max) {
		marginV.set(getMargin(), getMargin(), getMargin());
		d.sub(max, min);

		VectorUtil.mul(implicitShapeDimensions, d, localScaling);
		implicitShapeDimensions.sub(marginV);
	}

	public void setHalfSize(Vector3f hs) {
		implicitShapeDimensions.set(hs);

	}

}
