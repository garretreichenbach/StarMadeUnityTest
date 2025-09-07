package org.schema.game.network.objects;

import org.schema.game.network.objects.remote.RemoteRuleStateChangeBuffer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteIntBuffer;
import org.schema.schine.network.objects.remote.RemoteString;

public class NetworkFaction extends NetworkEntity implements NTRuleInterface{
	public NetworkFaction(StateInterface state) {
		super(state);
	}

	public RemoteRuleStateChangeBuffer ruleChangeBuffer = new RemoteRuleStateChangeBuffer(this);
	public RemoteIntBuffer ruleStateRequestBuffer = new RemoteIntBuffer(this);
	public RemoteBuffer<RemoteString> ruleIndividualAddRemoveBuffer = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	
	@Override
	public RemoteRuleStateChangeBuffer getRuleStateChangeBuffer() {
		return ruleChangeBuffer;
	}

	@Override
	public RemoteIntBuffer getRuleStateRequestBuffer() {
		//used for factions
		return ruleStateRequestBuffer;
	}

	@Override
	public RemoteBuffer<RemoteString> getRuleIndividualAddRemoveBuffer() {
		//used for factions
		return ruleIndividualAddRemoveBuffer;
	}

	@Override
	public void onDelete(StateInterface stateI) {
		
	}

	@Override
	public void onInit(StateInterface stateI) {
		
	}
}
