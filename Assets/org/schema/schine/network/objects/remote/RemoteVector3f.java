package org.schema.schine.network.objects.remote;

import javax.vecmath.Vector3f;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteVector3f extends RemoteFloatPrimitiveArray {

	public RemoteVector3f(boolean synchOn) {
		super(3, synchOn);
	}

	public RemoteVector3f(boolean synchOn, Vector3f v) {
		this(synchOn);
		set(v);
	}

	public RemoteVector3f(NetworkObject synchOn) {
		super(3, synchOn);
	}

	public RemoteVector3f(NetworkObject synchOn, Vector3f v) {
		this(synchOn);
		set(v);
	}

	public Vector3f getVector() {
		return getVector(new Vector3f());
	}

	public Vector3f getVector(Vector3f out) {
		out.set(super.getFloatArray()[0], super.getFloatArray()[1], super.getFloatArray()[2]);
		return out;
	}

	public void set(float x, float y, float z) {
		super.set(0, x);
		super.set(1, y);
		super.set(2, z);
	}

	public void set(Vector3f vector3f) {
		super.set(0, vector3f.x);
		super.set(1, vector3f.y);
		super.set(2, vector3f.z);
	}

	@Override
	public String toString() {
		return "(r" + getVector() + ")";
	}

}
