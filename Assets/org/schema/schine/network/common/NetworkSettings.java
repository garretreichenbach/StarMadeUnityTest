package org.schema.schine.network.common;

import org.schema.common.util.settings.SettingStateInt;

public interface NetworkSettings {

	public static final int DEFAULT_BUFFER_SIZE = 1024 * 512;
	
	public SettingStateInt getDelaySetting();
	public SettingStateInt getSendingQueueSizeSetting();
	public SettingStateInt getSocketReceiveBufferSize();
	public SettingStateInt getSocketSendBufferSize();

}
