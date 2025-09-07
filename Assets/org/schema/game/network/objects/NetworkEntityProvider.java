package org.schema.game.network.objects;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.NetworkTransformation;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteIntPrimitive;
import org.schema.schine.network.objects.remote.RemotePhysicsTransform;

public class NetworkEntityProvider extends NetworkObject {

	public RemoteIntPrimitive clientId = new RemoteIntPrimitive(-777777, this);
	public RemoteBoolean connectionReady = new RemoteBoolean(false, this);
	public RemotePhysicsTransform transformationBuffer = new RemotePhysicsTransform(new NetworkTransformation(), this);
	
	public NetworkEntityProvider(StateInterface state) {
		super(state);
	}

	
	

	@Override
	public void onDelete(StateInterface state) {
	}

	@Override
	public void onInit(StateInterface state) {
	}


}
