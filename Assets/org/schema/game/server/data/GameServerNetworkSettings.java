package org.schema.game.server.data;
import org.schema.common.util.settings.SettingStateInt;
import org.schema.common.util.settings.SettingStateString;
import org.schema.schine.network.server.ServerSettingsInterface;
public class GameServerNetworkSettings implements ServerSettingsInterface {
	@Override
	public SettingStateInt getDelaySetting() {
		return (SettingStateInt) GameServerSettings.NETWORK_DELAY.s;
	}

	@Override
	public SettingStateInt getSendingQueueSizeSetting() {
		return (SettingStateInt) GameServerSettings.SENDING_QUEUE_SIZE.s;
	}

	

	@Override
	public SettingStateInt getSocketReceiveBufferSize() {
		return (SettingStateInt) GameServerSettings.SOCKET_RECEIVE_BUFFER_SIZE.s;
	}

	@Override
	public SettingStateInt getSocketSendBufferSize() {
		return (SettingStateInt) GameServerSettings.SOCKET_SEND_BUFFER_SIZE.s;
	}

	@Override
	public SettingStateString getAcceptingIP() {
		return (SettingStateString) GameServerSettings.LISTENING_ON_NT_INTERFACE.s;
	}
	public SettingStateString getSpamProtectException() {
		return (SettingStateString) GameServerSettings.SPAM_PROTECTION_EXCEPTIONS.s;
	}
	@Override
	public SettingStateInt getSpamProtectionAttempts() {
		return (SettingStateInt) GameServerSettings.SPAM_PROTECTION_ATTEMPTS.s;
	}

	@Override
	public SettingStateInt getSpamProtectionAttemptTimeoutMs() {
		return (SettingStateInt) GameServerSettings.SPAM_PROTECTION_ATTEMPT_TIMEOUT_MS.s;
	}
	@Override
	public SettingStateInt getClientTimeout() {
		return (SettingStateInt) GameServerSettings.CLIENT_TIMEOUT.s;
	}
}
