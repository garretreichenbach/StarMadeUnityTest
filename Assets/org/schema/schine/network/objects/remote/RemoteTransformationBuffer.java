package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

import com.bulletphysics.linearmath.Transform;

public class RemoteTransformationBuffer extends RemoteBuffer<RemoteTransformation> {

	private static final int CONTROL_BATCH = 32;

	public RemoteTransformationBuffer(NetworkObject synchOn) {
		super(RemoteTransformation.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			RemoteTransformation instance = new RemoteTransformation(new Transform(), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		int batchSize = Math.min(CONTROL_BATCH, get().size());

		buffer.writeInt(batchSize);

		int size = 0;
		//			int elementCount = get().size();
		for (int i = 0; i < batchSize; i++) {
			RemoteTransformation remoteField = get().remove(0);
			size++;
			size += remoteField.toByteStream(buffer);
			remoteField.setChanged(false);
		}
		keepChanged = !get().isEmpty();

		return size + ByteUtil.SIZEOF_INT;

	}

	@Override
	protected void cacheConstructor() {
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

}
