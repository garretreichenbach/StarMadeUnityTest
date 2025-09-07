package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.network.objects.valueUpdate.ValueUpdate;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteValueUpdateBuffer extends RemoteBuffer<RemoteValueUpdate> {

	private static final int CONTROL_BATCH = 32;
	public RemoteValueUpdateBuffer(NetworkObject synchOn, ManagerContainer<?> man) {
		super(RemoteValueUpdate.class, synchOn);
	}

	@Override
	public boolean add(RemoteValueUpdate e) {
		assert(e.get().checkOnAdd());
		return super.add(e);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			byte type = buffer.readByte();
			ValueUpdate inst = ValueUpdate.getInstance(type);
			RemoteValueUpdate instance = new RemoteValueUpdate(inst, onServer);

			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection

		int batchSize = Math.min(CONTROL_BATCH, get().size());

		buffer.writeInt(batchSize);

		int size = 0;
		//			int elementCount = get().size();
		for (int i = 0; i < batchSize; i++) {
			RemoteValueUpdate remoteField = get().remove(0);
			byte t = (byte) remoteField.get().getType().ordinal();
			buffer.writeByte(t);
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
