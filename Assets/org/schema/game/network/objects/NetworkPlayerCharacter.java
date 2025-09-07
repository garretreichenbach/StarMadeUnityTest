package org.schema.game.network.objects;

import org.schema.game.network.objects.remote.RemoteCharacterBlockActivationBuffer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteString;

public abstract class NetworkPlayerCharacter extends NetworkEntity {

	public RemoteString uniqueId = new RemoteString(this);
	public RemoteBoolean hit = new RemoteBoolean(false, this);

	public RemoteCharacterBlockActivationBuffer blockActivationsWithReaction = new RemoteCharacterBlockActivationBuffer(this);

	public NetworkPlayerCharacter(StateInterface state) {
		super(state);
	}

	@Override
	public void onDelete(StateInterface stateI) {
		
	}

	@Override
	public void onInit(StateInterface stateI) {
		
	}

}
