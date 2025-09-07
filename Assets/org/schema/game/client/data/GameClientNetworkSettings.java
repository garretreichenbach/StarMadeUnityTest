package org.schema.game.client.data;

import org.schema.common.util.settings.SettingStateInt;
import org.schema.schine.network.client.ClientSettingsInterface;

public class GameClientNetworkSettings implements ClientSettingsInterface{
	@Override
	public SettingStateInt getDelaySetting() {
		return (SettingStateInt) GameClientSettings.NETWORK_DELAY.s;
	}

	@Override
	public SettingStateInt getSendingQueueSizeSetting() {
		return (SettingStateInt) GameClientSettings.SENDING_QUEUE_SIZE.s;
	}

	

	@Override
	public SettingStateInt getSocketReceiveBufferSize() {
		return (SettingStateInt) GameClientSettings.SOCKET_RECEIVE_BUFFER_SIZE.s;
	}

	@Override
	public SettingStateInt getSocketSendBufferSize() {
		return (SettingStateInt) GameClientSettings.SOCKET_SEND_BUFFER_SIZE.s;
	}

}
