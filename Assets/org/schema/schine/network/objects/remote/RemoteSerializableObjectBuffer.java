package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteSerializableObjectBuffer<E extends RemoteSerializable> extends RemoteBufferClassless<RemoteSerializableObject<E>> {

	private int maxBatch = Integer.MAX_VALUE;
	private RemoteSerializableFactory<E> instantiator;

	public RemoteSerializableObjectBuffer(NetworkObject synchOn, RemoteSerializableFactory<E> inst) {
		super(synchOn);
		this.instantiator = inst;
	}

	public RemoteSerializableObjectBuffer(NetworkObject synchOn, RemoteSerializableFactory<E> inst, int maxBatch) {
		this(synchOn, inst);
		this.maxBatch = maxBatch;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			E r = instantiator.instantiate();
			RemoteSerializableObject<E> instance = new RemoteSerializableObject<E>(r, onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		int batchSize = Math.min(maxBatch, get().size());

		buffer.writeInt(batchSize);

		int size = 0;
		//			int elementCount = get().size();
		for (int i = 0; i < batchSize; i++) {
			Streamable<?> remoteField = get().remove(0);

			size += remoteField.toByteStream(buffer);

			remoteField.setChanged(false);
		}
		keepChanged = !get().isEmpty();

		return size + ByteUtil.SIZEOF_INT;

	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

}
