package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.IOException;

import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteMetaObjectRequestAwnserBuffer extends RemoteMetaObjectBuffer {

	public RemoteMetaObjectRequestAwnserBuffer(MetaObjectManager man,
	                                           NetworkObject synchOn) {
		super(man, synchOn);
	}

	@Override
	protected void handleMetaObjectInputStream(DataInputStream buffer) throws IOException {
		man.deserialize(buffer);
	}

}
