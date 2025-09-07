package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteReactorTree extends RemoteField<ReactorTree> {
	public RemoteReactorTree(ReactorTree entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteReactorTree(ReactorTree entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().deserialize(stream, updateSenderStateId, onServer);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		get().serialize(buffer, onServer);

		return 1;
	}

}
