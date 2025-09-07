package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteBooleanPrimitive implements Streamable<Boolean> {

	private final boolean onServer;
	protected boolean keepChanged;
	private boolean changed;
	private NetworkChangeObserver observer;
	private boolean value;
	private boolean forcedClientSending;

	public RemoteBooleanPrimitive(boolean onServer) {
		this(false, false, onServer);
	}

	public RemoteBooleanPrimitive(boolean e, boolean onServer) {
		this(e, false, onServer);
	}

	public RemoteBooleanPrimitive(boolean e, boolean changed, boolean onServer) {
		value = e;
		//		this.synchObject = synchOn;
		this.onServer = onServer;
		this.changed = changed;
	}

	public RemoteBooleanPrimitive(boolean e, boolean changed, NetworkObject synchOn) {
		this(e, changed, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteBooleanPrimitive(boolean e, NetworkObject synchOn) {
		this(e, false, synchOn);
	}

	public RemoteBooleanPrimitive(NetworkObject synchOn) {
		this(false, false, synchOn);
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
		set(stream.readBoolean());
	}

	@Override
	public Boolean get() {
		return value;
	}

	@Override
	public void set(Boolean value) {
		set(value.booleanValue());
	}

	@Override
	public void set(Boolean value, boolean forcedClientSending) {
		set(value);
	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		stream.writeBoolean(value);
		return 4;
	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	public boolean getBoolean() {
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

	public void set(boolean value) {
		set(value, forcedClientSending);
	}

	public void set(boolean value, boolean forced) {
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
