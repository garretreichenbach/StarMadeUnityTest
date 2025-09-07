package org.schema.schine.network.objects.remote;

import javax.vecmath.Matrix3f;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteMatrix3f extends RemoteFloatPrimitiveArray {

	public RemoteMatrix3f(boolean synchOn) {
		super(9, synchOn);
	}

	public RemoteMatrix3f(NetworkObject synchOn) {
		super(9, synchOn);
	}

	public RemoteMatrix3f(Matrix3f v, boolean synchOn) {
		super(9, synchOn);
		set(v);

	}

	public RemoteMatrix3f(Matrix3f v, NetworkObject synchOn) {
		super(9, synchOn);
		set(v);

	}


	public Matrix3f getMatrix() {
		return getMatrix(new Matrix3f());
	}

	public Matrix3f getMatrix(Matrix3f q) {
		
		q.m00 = super.getFloatArray()[0];
		q.m01 = super.getFloatArray()[1];
		q.m02 = super.getFloatArray()[2];
		q.m10 = super.getFloatArray()[3];
		q.m11 = super.getFloatArray()[4];
		q.m12 = super.getFloatArray()[5];
		q.m20 = super.getFloatArray()[6];
		q.m21 = super.getFloatArray()[7];
		q.m22 = super.getFloatArray()[8];
		
		return q;
	}


	public void set(Matrix3f q, boolean force) {
		super.set(0, q.m00, force);
		super.set(1, q.m01, force);
		super.set(2, q.m02, force);
		super.set(3, q.m10, force);
		super.set(4, q.m11, force);
		super.set(5, q.m12, force);
		super.set(6, q.m20, force);
		super.set(7, q.m21, force);
		super.set(8, q.m22, force);
	}

	@Override
	public String toString() {
		return "(r" + getMatrix() + ")";
	}


	public void set(Matrix3f q) {
		super.set(0, q.m00);
		super.set(1, q.m01);
		super.set(2, q.m02);
		super.set(3, q.m10);
		super.set(4, q.m11);
		super.set(5, q.m12);
		super.set(6, q.m20);
		super.set(7, q.m21);
		super.set(8, q.m22);
	}

	public boolean equalsMatrix(Matrix3f q) {
		return 
			q.m00 == super.getFloatArray()[0] &&
			q.m01 == super.getFloatArray()[1] &&
			q.m02 == super.getFloatArray()[2] &&
			q.m10 == super.getFloatArray()[3] &&
			q.m11 == super.getFloatArray()[4] &&
			q.m12 == super.getFloatArray()[5] &&
			q.m20 == super.getFloatArray()[6] &&
			q.m21 == super.getFloatArray()[7] &&
			q.m22 == super.getFloatArray()[8];
	}

}
