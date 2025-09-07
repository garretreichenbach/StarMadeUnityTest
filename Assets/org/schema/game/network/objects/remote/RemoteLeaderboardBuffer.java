package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.gamemode.battle.KillerEntity;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RemoteLeaderboardBuffer extends RemoteBuffer<RemoteLeaderboard> {

	//	private Constructor<RemoteMissileUpdate> constructor;
	//	private static Constructor<RemoteMissileUpdate> staticConstructor;

	public RemoteLeaderboardBuffer(NetworkObject synchOn) {
		super(RemoteLeaderboard.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			RemoteLeaderboard instance = new RemoteLeaderboard(new Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>>(), onServer);
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

			for (RemoteLeaderboard remoteField : get()) {
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
	public boolean add(RemoteLeaderboard e) {
		return super.add(e);
	}

}
