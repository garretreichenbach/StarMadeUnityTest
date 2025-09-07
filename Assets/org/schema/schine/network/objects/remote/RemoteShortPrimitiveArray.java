package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteShortPrimitiveArray implements StreamableArray<Short[]>, Streamable<Short[]> {

	private final boolean onServer;
	private final int size;
	protected boolean keepChanged;
	private boolean changed;
	private NetworkChangeObserver observer;
	private boolean forcedClientSending;
	private short[] array;

	public RemoteShortPrimitiveArray(int size, boolean onServer) {
		this(size, false, onServer);

	}

	public RemoteShortPrimitiveArray(int size, boolean bool, boolean onServer) {
		array = new short[size];
		//		this.synchObject = synchOn;
		this.size = size;
		this.onServer = onServer;
		changed = bool;
	}

	public RemoteShortPrimitiveArray(int size, boolean bool, NetworkObject synchOn) {
		this(size, bool, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteShortPrimitiveArray(int size, NetworkObject synchOn) {
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
		return 2;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		for (int i = 0; i < size; i++) {
			set(i, stream.readShort(), forcedClientSending);
		}
	}

	/**
	 * This method is pretty wastefull! Use getintArray() instead
	 */
	@Override
	public Short[] get() {
		System.err.println("WARNING. USE PRECAHDED VERSION OF GET ARRAY");
		Short[] ar = new Short[size];
		for (int i = 0; i < size; i++) {
			ar[i] = array[i];
		}
		return ar;
	}

	@Override
	public void set(Short[] value) {
		assert (value.length == size);
		for (int i = 0; i < size; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	@Override
	public void set(Short[] value, boolean forcedClientSending) {
		assert (value.length == size);
		for (int i = 0; i < size; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		for (int i = 0; i < size; i++) {
			stream.writeShort(array[i]);
		}
		return 4;
	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	public short[] getIntArray() {
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

	public void set(short[] value) {
		set(value, forcedClientSending);
	}

	public void set(short[] value, boolean forcedClientSending) {
		assert (value.length == size);
		for (int i = 0; i < size; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	public void set(int index, short value) {
		assert (index < size) : index + "; " + size;
		set(index, value, forcedClientSending);
	}

	public void set(int index, short value, boolean forced) {
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
