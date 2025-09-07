package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.player.playermessage.PlayerMessage;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemotePlayerMessageBuffer extends RemoteBuffer<RemotePlayerMessage> {

	public RemotePlayerMessageBuffer(NetworkObject synchOn) {
		super(RemotePlayerMessage.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {

			PlayerMessage instance = PlayerMessage.decode(buffer);
			RemotePlayerMessage r = new RemotePlayerMessage(instance, onServer);
			getReceiveBuffer().add(r);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		synchronized (get()) {
			//add size of collection
			buffer.writeInt(get().size());
			size += ByteUtil.SIZEOF_INT;

			for (RemotePlayerMessage remoteField : get()) {
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

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteBuffer#add(org.schema.schine.network.objects.remote.Streamable)
	 */
	@Override
	public boolean add(RemotePlayerMessage e) {
		return super.add(e);
	}

}
