package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteIntPrimitiveArray implements StreamableArray<Integer[]>, Streamable<Integer[]> {

	private final boolean onServer;
	private final int size;
	protected boolean keepChanged;
	private boolean changed;
	private NetworkChangeObserver observer;
	private boolean forcedClientSending;
	private int[] array;

	public RemoteIntPrimitiveArray(int size, boolean onServer) {
		this(size, false, onServer);

	}

	public RemoteIntPrimitiveArray(int size, boolean bool, boolean onServer) {
		array = new int[size];
		//		this.synchObject = synchOn;
		this.size = size;
		this.onServer = onServer;
		changed = bool;
	}

	public RemoteIntPrimitiveArray(int size, boolean bool, NetworkObject synchOn) {
		this(size, bool, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteIntPrimitiveArray(int size, NetworkObject synchOn) {
		this(size, false, synchOn);
	}

	@Override
	public int arrayLength() {
		return size;
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
		for (int i = 0; i < size; i++) {
			set(i, stream.readInt(), forcedClientSending);
		}
	}

	/**
	 * This method is pretty wastefull! Use getintArray() instead
	 */
	@Override
	public Integer[] get() {
		System.err.println("WARNING. USE PRECAHDED VERSION OF GET ARRAY");
		Integer[] ar = new Integer[size];
		for (int i = 0; i < size; i++) {
			ar[i] = array[i];
		}
		return ar;
	}

	@Override
	public void set(Integer[] value) {
		assert (value.length == size);
		for (int i = 0; i < size; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	@Override
	public void set(Integer[] value, boolean forcedClientSending) {
		assert (value.length == size);
		for (int i = 0; i < size; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		for (int i = 0; i < size; i++) {
			stream.writeInt(array[i]);
		}
		return 4;
	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	public int[] getIntArray() {
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

	public void set(int[] value) {
		set(value, forcedClientSending);
	}

	public void set(int[] value, boolean forcedClientSending) {
		assert (value.length == size);
		for (int i = 0; i < size; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	public void set(int index, int value) {
		assert (index < size) : index + "; " + size;
		set(index, value, forcedClientSending);
	}

	public void set(int index, int value, boolean forced) {
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

}
