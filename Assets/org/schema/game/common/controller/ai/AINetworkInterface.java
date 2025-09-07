package org.schema.game.common.controller.ai;

import org.schema.schine.network.objects.remote.RemoteArrayBuffer;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.objects.remote.RemoteStringArray;

public interface AINetworkInterface {
	public RemoteArrayBuffer<RemoteStringArray> getAiSettingsModification();

	public RemoteString getDebugState();
}
