package org.schema.game.network.objects;

import org.schema.game.network.objects.remote.RemoteReactorBonusUpdateBuffer;
import org.schema.game.network.objects.remote.RemoteReactorPriorityQueueBuffer;
import org.schema.game.network.objects.remote.RemoteReactorSetBuffer;
import org.schema.game.network.objects.remote.RemoteReactorTreeBuffer;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteFloatBuffer;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteLongIntPair;
import org.schema.schine.network.objects.remote.RemoteLongPrimitive;
import org.schema.schine.network.objects.remote.RemoteShortBuffer;

public interface PowerInterfaceNetworkObject {
	public RemoteReactorPriorityQueueBuffer getReactorPrioQueueBuffer();
	public RemoteReactorSetBuffer getReactorSetBuffer();
	public RemoteReactorTreeBuffer getReactorTreeBuffer();
	public RemoteLongBuffer getConvertRequestBuffer();
	public RemoteLongBuffer getBootRequestBuffer();
	public RemoteFloatBuffer getReactorCooldownBuffer();
	public RemoteLongPrimitive getActiveReactor();
	public RemoteShortBuffer getRecalibrateRequestBuffer();
	public RemoteBuffer<RemoteLongIntPair> getReactorChangeBuffer();
	public RemoteFloatBuffer getEnergyStreamCooldownBuffer(); 
	public RemoteReactorBonusUpdateBuffer getReactorBonusMatrixUpdateBuffer(); 
}
