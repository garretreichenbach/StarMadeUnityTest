package org.schema.game.common.data.blockeffects.config;

import org.schema.schine.network.objects.remote.RemoteShortBuffer;

public interface EffectConfigNetworkObjectInterface {
	public RemoteShortBuffer getEffectAddBuffer();
	public RemoteShortBuffer getEffectRemoveBuffer();
}
