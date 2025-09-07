package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.NetworkTransformation;

public class RemotePhysicsTransformBuffer extends RemoteBuffer<RemotePhysicsTransform> {

	public RemotePhysicsTransformBuffer(NetworkObject synchOn) {
		super(RemotePhysicsTransform.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			RemotePhysicsTransform instance = new RemotePhysicsTransform(new NetworkTransformation(), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		//add size of collection
		buffer.writeInt(get().size());
		size += ByteUtil.SIZEOF_INT;

		for (RemotePhysicsTransform remoteField : get()) {
			size += remoteField.toByteStream(buffer);
		}

		get().clear();
		return size;

	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}
}
