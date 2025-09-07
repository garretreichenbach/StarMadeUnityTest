package org.schema.game.client.data;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.schema.common.util.settings.SettingState;
import org.schema.common.util.settings.SettingStateBoolean;
import org.schema.common.util.settings.SettingStateFloat;
import org.schema.common.util.settings.SettingStateInt;
import org.schema.common.util.settings.Settings;
import org.schema.schine.network.common.NetworkSettings;

public enum GameClientSettings implements Settings{
	
	
	CLIENT_TRAFFIC_CLASS(new SettingStateBoolean(true)), 
	NETWORK_DELAY(new SettingStateInt(0, 0, Integer.MAX_VALUE)), 
	SENDING_QUEUE_SIZE(new SettingStateInt(1024, 0, Integer.MAX_VALUE)),
	SOCKET_RECEIVE_BUFFER_SIZE(new SettingStateInt(NetworkSettings.DEFAULT_BUFFER_SIZE, 0, Integer.MAX_VALUE)),  
	SOCKET_SEND_BUFFER_SIZE(new SettingStateInt(NetworkSettings.DEFAULT_BUFFER_SIZE, 0, Integer.MAX_VALUE)), 
	
	CLIENT_CONNECT_TIMEOUT_MS(new SettingStateInt(10000, 0, Integer.MAX_VALUE)),
	LOGIN_SERVER_CONNECT_HANDOFF_TIMEOUT_MS(new SettingStateInt(20000, 0, Integer.MAX_VALUE)),
	
	
	;
	public final SettingState s;

	private GameClientSettings(SettingState s) {
		this.s = s;
	}

	public boolean isOn() {
		assert(s instanceof SettingStateBoolean):this;
		return s.isOn();
	}

	public int getInt() {
		assert(s instanceof SettingStateInt):this;
		return s.getInt();
	}

	public float getFloat() {
		assert(s instanceof SettingStateFloat):this;
		return s.getFloat();
	}

	public void setOn(boolean on) {
		assert(s instanceof SettingStateBoolean):this;
		s.setOn(on);
	}

	public void setInt(int v) {
		assert(s instanceof SettingStateInt):this;
		s.setInt(v);
	}

	public void setFloat(float v) {
		assert(s instanceof SettingStateFloat):this;
		s.setFloat(v);
	}

	public String toString() {
		return this.name()+"->"+s.toString();
	}

	@Override
	public String getString() {
		return s.getString();
	}

	@Override
	public void setString(String v) {
		s.setString(v);
	}
	public static SettingState[] getValues() {
		SettingState[] s = new SettingState[values().length];
		
		for(int i = 0; i < values().length; i++) {
			s[i] = values()[i].s;
		}
		
		return s;
	}

	public static String[] getNames() {
		String[] s = new String[values().length];
		
		for(int i = 0; i < values().length; i++) {
			s[i] = values()[i].name();
		}
		
		return s;
	}
	public static void save(boolean overwrite) throws IOException {
		Settings.write(Settings.getSettingsPath()+getPath(), getValues(), getNames(), overwrite);
	}
	public static void load() throws IOException {
		Settings.read(Settings.getSettingsPath()+getPath(), getValues(), getNames());
	}
	static{
		try {
			try {
			load();
			}catch(FileNotFoundException e) {
				
			}
			save(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String getPath() {
		return "gameclient.xml";
	}

	@Override
	public Object getObject() {
		return s.getObject();
	}

	@Override
	public void setObject(Object o) {
		s.setObject(o);
	}
	
}
