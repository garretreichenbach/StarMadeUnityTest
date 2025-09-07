package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.missile.updates.MissileUpdate;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteMissileUpdateBuffer extends RemoteBuffer<RemoteMissileUpdate> {

	//	private Constructor<RemoteMissileUpdate> constructor;
	//	private static Constructor<RemoteMissileUpdate> staticConstructor;

	public RemoteMissileUpdateBuffer(NetworkObject synchOn) {
		super(RemoteMissileUpdate.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		long ts = buffer.readLong();
		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {

			MissileUpdate instance = MissileUpdate.decodeMissile(buffer);
			RemoteMissileUpdate r = new RemoteMissileUpdate(instance, onServer);
			instance.timeStampServerSentToClient = ts;
			getReceiveBuffer().add(r);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeLong(System.currentTimeMillis());
		
		int size = 0;
		synchronized (get()) {
			//add size of collection
			buffer.writeInt(get().size());
			size += ByteUtil.SIZEOF_INT;

			for (RemoteMissileUpdate remoteField : get()) {
				size += remoteField.toByteStream(buffer);
			}

			get().clear();

		}
		return size;

	}

	@Override
	protected void cacheConstructor() {
		//		try {
		//			if(staticConstructor == null){
		//				staticConstructor = RemoteCatalogEntry.class.getConstructor(RemoteMissileUpdate.class, boolean.class );
		//			}
		//			constructor = staticConstructor;
		//		} catch (SecurityException e) {
		//			System.err.println("CLASS "+clazz);
		//			e.printStackTrace();
		//
		//			assert(false);
		//		} catch (NoSuchMethodException e) {
		//			System.err.println("CLASS "+clazz);
		//			e.printStackTrace();
		//			assert(false);
		//		}
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteBuffer#add(org.schema.schine.network.objects.remote.Streamable)
	 */
	@Override
	public boolean add(RemoteMissileUpdate e) {
		return super.add(e);
	}

}
