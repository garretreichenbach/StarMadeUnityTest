package org.schema.game.network.objects;

import org.schema.game.common.data.blockeffects.config.EffectConfigNetworkObjectInterface;
import org.schema.game.network.objects.remote.RemoteItemBuffer;
import org.schema.game.network.objects.remote.RemoteRuleStateChangeBuffer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteByte;
import org.schema.schine.network.objects.remote.RemoteIntBuffer;
import org.schema.schine.network.objects.remote.RemoteIntPrimitive;
import org.schema.schine.network.objects.remote.RemoteShortBuffer;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.objects.remote.RemoteVector3i;

public class NetworkSector extends NetworkObject implements EffectConfigNetworkObjectInterface, NTRuleInterface {

	public RemoteBoolean active = new RemoteBoolean(this);
	public RemoteVector3i pos = new RemoteVector3i(this);
	public RemoteItemBuffer itemBuffer = new RemoteItemBuffer(this);
	public RemoteByte type = new RemoteByte(this);
	public RemoteIntPrimitive mode = new RemoteIntPrimitive(0, this);
	public RemoteShortBuffer effectAddBuffer = new RemoteShortBuffer(this, 64);
	public RemoteShortBuffer effectRemoveBuffer = new RemoteShortBuffer(this, 64);
	public RemoteBuffer<RemoteString> ruleIndividualAddRemoveBuffer = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	
	public RemoteRuleStateChangeBuffer ruleChangeBuffer = new RemoteRuleStateChangeBuffer(this);

	public RemoteIntBuffer ruleStateRequestBuffer = new RemoteIntBuffer(this);
	public NetworkSector(StateInterface state) {
		super(state);
	}

	@Override
	public void onDelete(StateInterface stateI) {
	}

	@Override
	public void onInit(StateInterface stateI) {
	}

	@Override
	public RemoteShortBuffer getEffectAddBuffer() {
		return effectAddBuffer;
	}
	@Override
	public RemoteShortBuffer getEffectRemoveBuffer() {
		return effectRemoveBuffer;
	}
	@Override
	public RemoteRuleStateChangeBuffer getRuleStateChangeBuffer() {
		return ruleChangeBuffer;
	}

	@Override
	public RemoteIntBuffer getRuleStateRequestBuffer() {
		return ruleStateRequestBuffer;
	}

	@Override
	public RemoteBuffer<RemoteString> getRuleIndividualAddRemoveBuffer() {
		return ruleIndividualAddRemoveBuffer;
	}
}
