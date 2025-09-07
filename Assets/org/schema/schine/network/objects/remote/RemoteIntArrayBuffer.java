package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.pool.ArrayBufferPool;

public class RemoteIntArrayBuffer extends RemoteBuffer<RemoteIntArray> {

	private final int arraySize;
	private ArrayBufferPool<RemoteIntArray> pool;

	public RemoteIntArrayBuffer(final int arraySize, boolean synchOn) {
		super(RemoteIntArray.class, synchOn);

		this.arraySize = arraySize;
		cacheConstructor();
	}

	public RemoteIntArrayBuffer(final int arraySize, NetworkObject synchOn) {
		super(RemoteIntArray.class, synchOn);

		this.arraySize = arraySize;
		cacheConstructor();
	}

	@Override
	public int byteLength() {
		//		int i = 0;
		//		for(E e : get()){
		//			i += e.byteLength();
		//		}
		return ByteUtil.SIZEOF_INT; // all fields plus size
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		int collectionSize = buffer.readInt();
		//
		//		if(collectionSize > 0){
		//			System.err.println("RECEIVED: ADDING "+collectionSize+" Arrays");
		for (int n = 0; n < collectionSize; n++) {
			RemoteIntArray instance = pool.get(onServer);
			assert (((StreamableArray<?>) instance).arrayLength() == arraySize) : ((Streamable<?>) instance).byteLength() + " / " + arraySize;
			instance.fromByteStream(buffer, updateSenderStateId);
			receiveBuffer.add(instance);
		}
		//        	System.err.println("added remote array field to list:"+getReceiveBuffer());

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		//add size of collection
		//		buffer.writeInt(get().size());
		int batchSize = Math.min(MAX_BATCH, get().size());
		buffer.writeInt(batchSize);

		int size = 0;
		if (!get().isEmpty()) {
			//			System.err.println("SEND: ADDING "+get().size()+" Arrays");

			for (int i = 0; i < batchSize; i++) {
				Streamable<?> first = get().remove(0);
				//			for(RemoteField<?> remoteField : get()){
				size += first.toByteStream(buffer);
				first.setChanged(false);
			}
		}

		keepChanged = !get().isEmpty();
		//		get().clear();

		return size + 1;// +  ByteUtil.SIZEOF_INT;

	}

	@Override
	protected void cacheConstructor() {
		pool = ArrayBufferPool.get(clazz, arraySize);
	}

	@Override
	public void clearReceiveBuffer() {
		for (int i = 0; i < getReceiveBuffer().size(); i++) {
			pool.release(getReceiveBuffer().get(i));
		}
		getReceiveBuffer().clear();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteBuffer#add(org.schema.schine.network.objects.remote.RemoteField)
	 */
	@Override
	public boolean add(RemoteIntArray e) {
		assert (e.getIntArray().length == arraySize) : "Invalid Array Size: " + e.getIntArray().length + " != " + arraySize;
		return super.add(e);
	}

	/**
	 * @return the arraySize
	 */
	public int getArraySize() {
		return arraySize;
	}

}
