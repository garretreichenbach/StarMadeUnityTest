package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteFloatPrimitiveArray implements StreamableArray<Float[]>, Streamable<Float[]> {

	private final boolean onServer;
	protected boolean keepChanged;
	private boolean changed;
	private NetworkChangeObserver observer;
	private boolean forcedClientSending;
	private float[] array;

	public RemoteFloatPrimitiveArray(int size, boolean onServer) {
		this(size, false, onServer);
	}

	public RemoteFloatPrimitiveArray(int size, boolean bool, boolean onServer) {
		array = new float[size];
		//		this.synchObject = synchOn;
		this.onServer = onServer;
		changed = bool;
	}

	public RemoteFloatPrimitiveArray(int size, boolean bool, NetworkObject synchOn) {
		this(size, bool, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteFloatPrimitiveArray(int size, NetworkObject synchOn) {
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
			set(i, stream.readFloat(), forcedClientSending);
		}
	}

	/**
	 * This method is pretty wastefull! Use getintArray() instead
	 */
	@Override
	public Float[] get() {
		System.err.println("WARNING. USE PRECAHDED VERSION OF GET ARRAY");
		Float[] ar = new Float[array.length];
		for (int i = 0; i < ar.length; i++) {
			ar[i] = array[i];
		}
		return ar;
	}

	@Override
	public void set(Float[] value) {
		for (int i = 0; i < value.length; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	@Override
	public void set(Float[] value, boolean forcedClientSending) {
		for (int i = 0; i < value.length; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		for (int i = 0; i < array.length; i++) {
			stream.writeFloat(array[i]);
		}
		return 4;
	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	public float[] getFloatArray() {
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

	public void set(float[] value) {
		set(value, forcedClientSending);
	}

	public void set(float[] value, boolean forcedClientSending) {
		for (int i = 0; i < value.length; i++) {
			set(i, value[i], forcedClientSending);
		}
	}

	public void set(int index, float value) {
		set(index, value, forcedClientSending);
	}

	public void set(int index, float value, boolean forced) {
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
