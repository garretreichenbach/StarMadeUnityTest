package org.schema.schine.network.objects.remote;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteVector4f extends RemoteFloatPrimitiveArray {

	public RemoteVector4f(boolean synchOn) {
		super(4, synchOn);
	}

	public RemoteVector4f(NetworkObject synchOn) {
		super(4, synchOn);
	}

	public RemoteVector4f(Vector4f v, boolean synchOn) {
		super(4, synchOn);
		set(v);

	}

	public RemoteVector4f(Vector4f v, NetworkObject synchOn) {
		super(4, synchOn);
		set(v);

	}

	public RemoteVector4f(Vector3f v, float w, boolean synchOn) {
		super(4, synchOn);
		set(v.x, v.y, v.z, w);

	}

	public RemoteVector4f(Vector3f v, float w, NetworkObject synchOn) {
		super(4, synchOn);
		set(v.x, v.y, v.z, w);

	}

	public Vector4f getVector() {
		return getVector(new Vector4f());
	}
	public float getX(){
		return getFloatArray()[0];
	}
	public float getY(){
		return getFloatArray()[1];
	}
	public float getZ(){
		return getFloatArray()[2];
	}
	public float getW(){
		return getFloatArray()[3];
	}
	public Quat4f getVector(Quat4f out) {
		out.set(super.getFloatArray()[0], super.getFloatArray()[1], super.getFloatArray()[2], super.getFloatArray()[3]);
		return out;
	}

	public Vector4f getVector(Vector4f out) {
		out.set(super.getFloatArray()[0], super.getFloatArray()[1], super.getFloatArray()[2], super.getFloatArray()[3]);
		return out;
	}

	public void set(Vector4f vector4f) {
		super.set(0, vector4f.x);
		super.set(1, vector4f.y);
		super.set(2, vector4f.z);
		super.set(3, vector4f.w);
	}

	public void set(Vector4f vector4f, boolean force) {
		super.set(0, vector4f.x, force);
		super.set(1, vector4f.y, force);
		super.set(2, vector4f.z, force);
		super.set(3, vector4f.w, force);
	}

	@Override
	public String toString() {
		return "(r" + getVector() + ")";
	}

	public void set(float x, float y, float z, float w) {
		super.set(0, x);
		super.set(1, y);
		super.set(2, z);
		super.set(3, w);
	}

	public void set(float x, float y, float z, float w, boolean force) {
		super.set(0, x, force);
		super.set(1, y, force);
		super.set(2, z, force);
		super.set(3, w, force);
	}

	public void set(Vector3f n, float w) {
		super.set(0, n.x);
		super.set(1, n.y);
		super.set(2, n.z);
		super.set(3, w);
	}

	public void set(Quat4f q) {
		super.set(0, q.x);
		super.set(1, q.y);
		super.set(2, q.z);
		super.set(3, q.w);
	}

}
