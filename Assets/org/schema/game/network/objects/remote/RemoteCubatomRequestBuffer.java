package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

import org.schema.game.common.data.cubatoms.CubatomRequest;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteCubatomRequestBuffer extends RemoteBuffer<RemoteCubatomRequest> {

	private static Constructor<RemoteCubatomRequest> staticConstructor;

	static {
		try {
			if (staticConstructor == null) {
				staticConstructor = RemoteCubatomRequest.class.getConstructor(CubatomRequest.class, boolean.class);
			}
		} catch (SecurityException e) {
			e.printStackTrace();

			assert (false);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			assert (false);
		}
	}

	public RemoteCubatomRequestBuffer(NetworkObject synchOn) {
		super(RemoteCubatomRequest.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			CubatomRequest r = new CubatomRequest();
			RemoteCubatomRequest instance = new RemoteCubatomRequest(r, onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		buffer.writeInt(get().size());

		for (RemoteCubatomRequest remoteField : get()) {
			remoteField.toByteStream(buffer);
		}

		get().clear();

		return 1;

	}

	@Override
	protected void cacheConstructor() {
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

}
