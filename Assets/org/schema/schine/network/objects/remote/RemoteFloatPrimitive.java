package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteFloatPrimitive implements Streamable<Float> {

	private final boolean onServer;
	protected boolean keepChanged;
	private boolean changed;
	private NetworkChangeObserver observer;
	private float value;
	private boolean forcedClientSending;

	public RemoteFloatPrimitive(float e, boolean onServer) {
		this(e, false, onServer);
	}

	public RemoteFloatPrimitive(float e, boolean bool, boolean onServer) {
		value = e;
		//		this.synchObject = synchOn;
		this.onServer = onServer;
		changed = bool;
	}

	public RemoteFloatPrimitive(float e, boolean bool, NetworkObject synchOn) {
		this(e, bool, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteFloatPrimitive(float e, NetworkObject synchOn) {
		this(e, false, synchOn);
	}

	@Override
	public int byteLength() {
		return 4;
	}

	@Override
	public void cleanAtRelease() {
		
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		set(stream.readFloat());
	}

	@Override
	public Float get() {
		return value;
	}

	@Override
	public void set(Float value) {
		set(value.floatValue());
	}

	@Override
	public void set(Float value, boolean forcedClientSending) {
		set(value);

	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		stream.writeFloat(value);
		return 4;
	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	public float getFloat() {
		return value;
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

	public void set(float value) {
		set(value, forcedClientSending);
	}

	public void set(float value, boolean forced) {
		if (onServer || forced) {
			//set changed if value has changed. leave changed if already has been changed
			changed = hasChanged() || value != this.value;
		}
		this.value = value;

		if (hasChanged() && observer != null) {
			//received buffer entries dont have buffers
			observer.update(this);
		}

	}

}
