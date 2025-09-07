package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public abstract class RemoteMetaObjectBuffer extends RemoteBuffer<RemoteMetaObject> {

	protected final MetaObjectManager man;

	public RemoteMetaObjectBuffer(MetaObjectManager man, NetworkObject synchOn) {
		super(RemoteMetaObject.class, synchOn);
		this.man = man;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			handleMetaObjectInputStream(buffer);
		}
		//no receive buffer needed. objects are autoinserted in map (should be synched)

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		synchronized (get()) {
			//add size of collection
			buffer.writeInt(get().size());
			size += ByteUtil.SIZEOF_INT;

			for (RemoteMetaObject remoteField : get()) {
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

	protected abstract void handleMetaObjectInputStream(DataInputStream buffer) throws IOException;

}
