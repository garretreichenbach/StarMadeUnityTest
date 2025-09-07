package org.schema.common.util.settings;

import org.schema.common.util.settings.SettingState.SettingStateType;

public interface SettingOptionFactory<E extends SettingsSerializable> {
	public E inst();
	public SettingStateType getType();
}
