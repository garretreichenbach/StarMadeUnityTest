package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Streamable<E> extends NetworkChangeObservable {
	public int byteLength();

	public void cleanAtRelease();

	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException;

	public E get();

	public void set(E value);

	public void set(E value, boolean forcedClientSending);

	public int toByteStream(DataOutputStream stream) throws IOException;

}
