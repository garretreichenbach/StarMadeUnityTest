package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteLongPrimitiveArray implements StreamableArray<Long[]>, Streamable<Long[]> {

	private final boolean onServer;
	protected boolean keepChanged;
	private boolean changed;
	private NetworkChangeObserver observer;
	private boolean forcedClientSending;
	private long[] array;

	public RemoteLongPrimitiveArray(int size, boolean onServer) {
		this(size, false, onServer);
	}

	public RemoteLongPrimitiveArray(int size, boolean bool, boolean onServer) {
		array = new long[size];
		//		this.synchObject = synchOn;
		this.onServer = onServer;
		changed = bool;
	}

	public RemoteLongPrimitiveArray(int size, boolean bool, NetworkObject synchOn) {
		this(size, bool, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteLongPrimitiveArray(int size, NetworkObject synchOn) {
		this(size, false, synchOn);
	}

	@Override
	public int arrayLength() {
		return array.length;
	}

	@Override
	public void cleanAtRelease() {
		
	}

	@Override
	public int byteLength() {
		return 4;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		for (int i = 0; i < array.length; i++) {
			set(i, stream.readLong(), forcedClientSending);
		}
	}

	/**
	 * This method is pretty wastefull! Use getintArray() instead
	 */
	@Override
	public Long[] get() {
		System.err.println("WARNING. USE PRECAHDED VERSION OF GET ARRAY");
		Long[] ar = new Long[array.length];
		for (int i = 0; i < ar.length; i++) {
			ar[i] = array[i];
		}
		return ar;
	}

	@Override
	public void set(Long[] value) {
		for (int i = 0; i < value.length; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	@Override
	public void set(Long[] value, boolean forcedClientSending) {
		for (int i = 0; i < value.length; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		for (int i = 0; i < array.length; i++) {
			stream.writeLong(array[i]);
		}
		return 4;
	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	public long[] getLongArray() {
		return array;
	}

	@Override
	public boolean hasChanged() {
		return changed;
	}

	@Override
	public boolean initialSynchUpdateOnly() {
				return false;
	}

	@Override
	public boolean keepChanged() {
		return keepChanged;
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public void setObserver(NetworkChangeObserver arg0) {
		this.observer = arg0;
	}

	public void set(int index, long value) {
		set(index, value, forcedClientSending);
	}

	public void set(int index, long value, boolean forced) {
		if (onServer || forced) {
			//set changed if value has changed. leave changed if already has been changed
			changed = hasChanged() || value != this.array[index];
		}
		this.array[index] = value;

		if (hasChanged() && observer != null) {
			//received buffer entries dont have buffers
			observer.update(this);
		}

	}

	public void set(long[] value) {
		set(value, forcedClientSending);
	}

	public void set(long[] value, boolean forcedClientSending) {
		for (int i = 0; i < value.length; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

}
