package org.schema.schine.graphicsengine.core.settings;

import java.io.IOException;

import org.schema.common.util.settings.SettingState;
import org.schema.common.util.settings.Settings;

public class SettingStateParseError extends IOException{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5947987226958559170L;

	public SettingStateParseError(String arg, SettingState state, Settings setting) {
		super("Cant set "+arg+" for "+state+" in "+setting);
	}
	public SettingStateParseError(String arg, SettingState state) {
		super("Cant set "+arg+" for "+state+" in unknown setting");
	}
	public SettingStateParseError() {
		super();
	}

	public SettingStateParseError(String message, Throwable cause) {
		super(message, cause);
	}

	public SettingStateParseError(String message) {
		super(message);
	}

	public SettingStateParseError(Throwable cause) {
		super(cause);
	}
	
}
