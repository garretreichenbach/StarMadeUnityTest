package org.schema.schine.network.objects.remote;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteVector3i extends RemoteIntPrimitiveArray {

	public RemoteVector3i(boolean synchOn) {
		super(3, synchOn);
	}

	public RemoteVector3i(NetworkObject synchOn) {
		super(3, synchOn);
	}

	public RemoteVector3i(Vector3i v, boolean synchOn) {
		super(3, synchOn);
		set(v);
	}

	public RemoteVector3i(Vector3i v, NetworkObject synchOn) {
		super(3, synchOn);
		set(v);
	}

	public Vector3i getVector() {
		return getVector(new Vector3i());
	}

	public Vector3i getVector(Vector3i out) {
		out.set(super.getIntArray()[0], super.getIntArray()[1], super.getIntArray()[2]);
		return out;
	}

	public boolean equalsVector(Vector3i to) {
		return super.getIntArray()[0] == to.x && super.getIntArray()[1] == to.y && super.getIntArray()[2] == to.z;
	}

	public void set(Vector3i vector3i) {
		super.set(0, vector3i.x);
		super.set(1, vector3i.y);
		super.set(2, vector3i.z);
	}

	public void set(Vector3i vector3i, boolean forced) {
		super.set(0, vector3i.x, forced);
		super.set(1, vector3i.y, forced);
		super.set(2, vector3i.z, forced);
	}

	@Override
	public String toString() {
		return "(r" + getVector() + ")";
	}

}
