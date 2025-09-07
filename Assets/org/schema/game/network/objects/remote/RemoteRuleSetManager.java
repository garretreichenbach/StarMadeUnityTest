package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteRuleSetManager extends RemoteField<RuleSetManager> {
	public RemoteRuleSetManager(RuleSetManager entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteRuleSetManager(RuleSetManager entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().deserialize(stream, updateSenderStateId, onServer);
		
		System.err.println("[RULE][NT] rulesetmanager received on "+(onServer ? "SERVER" : "CLIENT")+" from sender id "+updateSenderStateId);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		get().serialize(buffer, onServer);
		
		return 1;
	}

	

	

}
