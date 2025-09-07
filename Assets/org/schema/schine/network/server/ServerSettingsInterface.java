package org.schema.schine.network.server;

import org.schema.common.util.settings.SettingStateInt;
import org.schema.common.util.settings.SettingStateString;
import org.schema.schine.network.common.NetworkSettings;

public interface ServerSettingsInterface extends NetworkSettings{
	public SettingStateString getSpamProtectException();
	public SettingStateString getAcceptingIP();
	public SettingStateInt getSpamProtectionAttempts();
	public SettingStateInt getSpamProtectionAttemptTimeoutMs();
	public SettingStateInt getClientTimeout();

}
