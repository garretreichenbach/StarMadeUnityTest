package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteRuleStateChangeBuffer extends RemoteBuffer<RemoteRuleStateChange> {


	public RemoteRuleStateChangeBuffer(NetworkObject synchOn) {
		super(RemoteRuleStateChange.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {

			RemoteRuleStateChange r = new RemoteRuleStateChange(new RuleStateChange(), onServer);
			r.fromByteStream(buffer, updateSenderStateId);
			assert(r.get().ruleIdNT != Integer.MIN_VALUE);
			getReceiveBuffer().add(r);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		synchronized (get()) {
			//add size of collection
			buffer.writeInt(get().size());
			size += ByteUtil.SIZEOF_INT;

			for (RemoteRuleStateChange remoteField : get()) {
				size += remoteField.toByteStream(buffer);
			}

			get().clear();

		}
		return size;

	}

	@Override
	protected void cacheConstructor() {
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}


}
