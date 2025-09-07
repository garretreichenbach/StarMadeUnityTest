package org.schema.schine.network.objects.remote;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteVector3b extends RemoteByteArray {

	public RemoteVector3b(boolean synchOn) {
		super(3, synchOn);
	}

	public RemoteVector3b(NetworkObject synchOn) {
		super(3, synchOn);
	}

	public Vector3b getVector() {
		return getVector(new Vector3b());
	}

	public Vector3b getVector(Vector3b out) {
		out.set(super.get()[0].get(), super.get()[1].get(), super.get()[2].get());
		return out;
	}

	public void set(byte x, byte y, byte z) {
		super.set(0, x);
		super.set(1, y);
		super.set(2, z);

	}

	public void set(Vector3b vector3b) {
		super.set(0, vector3b.x);
		super.set(1, vector3b.y);
		super.set(2, vector3b.z);
	}

	@Override
	public String toString() {
		return "(r" + getVector(new Vector3b()) + ")";
	}

}
