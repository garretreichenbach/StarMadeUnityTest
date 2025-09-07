package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteIntPrimitive implements Streamable<Integer> {

	private final boolean onServer;
	protected boolean keepChanged;
	private boolean changed;
	private NetworkChangeObserver observer;
	private int value;
	private boolean forcedClientSending;

	public RemoteIntPrimitive(int e, boolean onServer) {
		this(e, false, onServer);
	}

	public RemoteIntPrimitive(int e, boolean bool, boolean onServer) {
		value = e;
		//		this.synchObject = synchOn;
		this.onServer = onServer;
		changed = bool;
	}

	public RemoteIntPrimitive(int e, boolean bool, NetworkObject synchOn) {
		this(e, bool, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteIntPrimitive(int e, NetworkObject synchOn) {
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
		set(stream.readInt());
	}

	@Override
	public Integer get() {
		return value;
	}

	@Override
	public void set(Integer value) {
		set(value.intValue());
	}

	@Override
	public void set(Integer value, boolean forcedClientSending) {
		set(value);

	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		stream.writeInt(value);
		return 4;
	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	public int getInt() {
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

	public void set(int value) {
		set(value, forcedClientSending);
	}

	public void set(int value, boolean forced) {
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
