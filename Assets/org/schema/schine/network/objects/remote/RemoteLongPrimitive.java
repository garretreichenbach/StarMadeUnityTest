package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteLongPrimitive implements Streamable<Long> {

	private final boolean onServer;
	protected boolean keepChanged;
	private boolean changed;
	private NetworkChangeObserver observer;
	private long value;
	private boolean forcedClientSending;

	public RemoteLongPrimitive(long e, boolean onServer) {
		this(e, false, onServer);
	}

	public RemoteLongPrimitive(long e, boolean bool, boolean onServer) {
		value = e;
		//		this.synchObject = synchOn;
		this.onServer = onServer;
		changed = bool;
	}

	public RemoteLongPrimitive(long e, boolean bool, NetworkObject synchOn) {
		this(e, bool, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteLongPrimitive(long e, NetworkObject synchOn) {
		this(e, false, synchOn);
	}

	@Override
	public int byteLength() {
		return 8;
	}

	@Override
	public void cleanAtRelease() {
		
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		set(stream.readLong());
	}

	@Override
	public Long get() {
		return value;
	}

	@Override
	public void set(Long value) {
		set(value.longValue());
	}

	@Override
	public void set(Long value, boolean forcedClientSending) {
		set(value);

	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		stream.writeLong(value);
		return 8;
	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	public long getLong() {
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

	public void set(long value) {
		set(value, forcedClientSending);
	}

	public void set(long value, boolean forced) {
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
