package org.schema.schine.network.objects.remote;

import org.schema.common.util.linAlg.Vector4i;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteVector4i extends RemoteIntPrimitiveArray {

	public RemoteVector4i(boolean synchOn) {
		super(4, synchOn);
	}

	public RemoteVector4i(NetworkObject synchOn) {
		super(4, synchOn);
	}

	public RemoteVector4i(Vector4i v, boolean synchOn) {
		super(4, synchOn);
		set(v);

	}

	public RemoteVector4i(Vector4i v, NetworkObject synchOn) {
		super(4, synchOn);
		set(v);

	}

	public Vector4i getVector() {
		return getVector(new Vector4i());
	}

	public Vector4i getVector(Vector4i out) {
		out.set(super.getIntArray()[0], super.getIntArray()[1], super.getIntArray()[2], super.getIntArray()[3]);
		return out;
	}

	public void set(Vector4i vector4i) {
		super.set(0, vector4i.x);
		super.set(1, vector4i.y);
		super.set(2, vector4i.z);
		super.set(3, vector4i.w);
	}

	@Override
	public String toString() {
		return "(r" + getVector() + ")";
	}

}
