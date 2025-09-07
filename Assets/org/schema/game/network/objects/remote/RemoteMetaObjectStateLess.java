package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteMetaObjectStateLess extends RemoteField<MetaObject> {

	public RemoteMetaObjectStateLess(MetaObject entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteMetaObjectStateLess(MetaObject entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		set(MetaObjectManager.deserializeStatic(stream));
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		MetaObjectManager.serialize(buffer, get());
		return byteLength();
	}
}