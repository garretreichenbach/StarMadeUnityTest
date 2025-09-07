package org.schema.game.network.objects;

import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteVector4i;

public interface NetworkDoorInterface {
	public RemoteBuffer<RemoteVector4i> getDoorActivate();
}
