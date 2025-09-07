package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface RemoteSerializable {
	public void serialize(DataOutputStream stream) throws IOException;

	public void deserialize(DataInputStream stream) throws IOException;
}
