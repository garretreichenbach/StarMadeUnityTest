package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public abstract class RemoteGenericSerializationBuffer<E extends SerializationInterface> extends RemoteBuffer<RemoteGenericSerialization<E>> {

	public RemoteGenericSerializationBuffer(Class<RemoteGenericSerialization<E>> clazz, NetworkObject synchOn) {
		super(clazz, synchOn);
	}

	public abstract E getInstance();

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			RemoteGenericSerialization<E> instance = new RemoteGenericSerialization<E>(getInstance(), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		synchronized (get()) {
			//add size of collection
			buffer.writeInt(get().size());
			size += ByteUtil.SIZEOF_INT;

			for (RemoteGenericSerialization<E> remoteField : get()) {
				size += remoteField.toByteStream(buffer);
			}

			get().clear();

		}
		return size;

	}

	@Override
	protected void cacheConstructor() {
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

}
