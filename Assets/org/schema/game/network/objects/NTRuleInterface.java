package org.schema.game.network.objects;

import org.schema.game.network.objects.remote.RemoteRuleStateChangeBuffer;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteIntBuffer;
import org.schema.schine.network.objects.remote.RemoteString;

public interface NTRuleInterface {
	public RemoteRuleStateChangeBuffer getRuleStateChangeBuffer();
	public RemoteIntBuffer getRuleStateRequestBuffer();
	public RemoteBuffer<RemoteString> getRuleIndividualAddRemoveBuffer();
}
