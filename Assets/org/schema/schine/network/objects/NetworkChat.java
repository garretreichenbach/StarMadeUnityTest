package org.schema.schine.network.objects;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteArrayBuffer;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteInteger;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.objects.remote.RemoteStringArray;

public class NetworkChat extends NetworkObject {
	public RemoteBuffer<RemoteString> chatLogBuffer = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	public RemoteBuffer<RemoteString> chatServerLogBuffer = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	public RemoteInteger owner = new RemoteInteger(-1, this);
	public RemoteArrayBuffer<RemoteStringArray> chatWisperBuffer = new RemoteArrayBuffer<RemoteStringArray>(3, RemoteStringArray.class, this);

	public NetworkChat(StateInterface state) {
		super(state);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDelete(StateInterface stateI) {

	}

	@Override
	public void onInit(StateInterface stateI) {

	}
}
