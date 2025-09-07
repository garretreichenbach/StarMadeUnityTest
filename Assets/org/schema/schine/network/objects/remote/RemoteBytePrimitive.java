package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteBytePrimitive implements Streamable<Byte> {

	private final boolean onServer;
	protected boolean keepChanged;
	private boolean changed;
	private NetworkChangeObserver observer;
	private byte value;
	private boolean forcedClientSending;

	public RemoteBytePrimitive(byte e, boolean onServer) {
		this(e, false, onServer);
	}

	public RemoteBytePrimitive(byte e, boolean bool, boolean onServer) {
		value = e;
		//		this.synchObject = synchOn;
		this.onServer = onServer;
		changed = bool;
	}

	public RemoteBytePrimitive(byte e, boolean bool, NetworkObject synchOn) {
		this(e, bool, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteBytePrimitive(byte e, NetworkObject synchOn) {
		this(e, false, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void cleanAtRelease() {
		
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		set(stream.readByte());
	}

	@Override
	public Byte get() {
		return value;
	}

	@Override
	public void set(Byte value) {
		set(value.byteValue());
	}

	@Override
	public void set(Byte value, boolean forcedClientSending) {
		set(value);

	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		stream.writeByte(value);
		return 1;
	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	public byte getByte() {
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

	public void set(byte value) {
		set(value, forcedClientSending);
	}

	public void set(byte value, boolean forced) {
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
