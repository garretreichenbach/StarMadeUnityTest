package org.schema.game.client.view.gui.options;

import org.schema.common.util.StringTools;
import org.schema.schine.common.JoystickAxisMapping;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsChangeListener;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.SettingsInterface;
import org.schema.schine.input.InputState;
import org.schema.schine.input.JoystickMappingFile;

public class JoystickSettingInterface implements SettingsInterface, EngineSettingsChangeListener {

	public int assignedTo = -1;
	JoystickAxisMapping map;
	private JoystickMappingFile file;
	private InputState clientState;
	public JoystickSettingInterface(JoystickMappingFile file, JoystickAxisMapping map, int value, InputState clientState) {
		this.map = map;
		this.assignedTo = value;
		this.file = file;
		this.clientState = clientState;
		this.addChangeListener(this);
	}

	public void switchSetting() throws StateParameterNotFoundException {
		if (!JoystickMappingFile.ok()) {
			return;
		}
		if (assignedTo >= JoystickMappingFile.getAxesCount() - 1) {
			assignedTo = -1;
		} else {
			assignedTo++;
		}
	}

	public Object getCurrentState() {
		if (!JoystickMappingFile.ok()) {
			return "invalid joysick";
		}
		if (assignedTo >= JoystickMappingFile.getAxesCount()) {
			assignedTo = JoystickMappingFile.getAxesCount() - 1;
		}

		return new Object() {
			@Override
			public String toString() {
				if (assignedTo >= JoystickMappingFile.getAxesCount()) {
					assignedTo = -1;
				}
				return assignedTo == -1 ? "NONE" : JoystickMappingFile.getAxisName(assignedTo) + " (" +
						StringTools.formatPointZero(file.getAxis(map))
						+ ")";
			}
		};
	}

	public void switchSettingBack() throws StateParameterNotFoundException {
		if (!JoystickMappingFile.ok()) {
			return;
		}
		if (assignedTo < 0) {
			assignedTo = JoystickMappingFile.getAxesCount() - 1;
		} else {
			assignedTo--;
		}
	}



	@Override
	public void addChangeListener(EngineSettingsChangeListener c) {
				
	}

	@Override
	public void removeChangeListener(EngineSettingsChangeListener c) {
				
	}

	@Override
	public boolean isOn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getInt() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFloat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setOn(boolean on) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInt(int v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFloat(float v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setString(String v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setObject(Object o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onSettingChanged(SettingsInterface setting) {
		if (!JoystickMappingFile.ok()) {
			return;
		}
		file.setAxis(map, assignedTo);		
	}

	@Override
	public void next() {
		try {
			switchSetting();
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void previous() {
		try {
			switchSettingBack();
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public String getAsString() {
		return file.getAxisName(assignedTo);
	}

	@Override
	public SettingsInterface getSettingsForGUI() {
		return this;
	}

	@Override
	public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, String deactText) {
		throw new RuntimeException();
	}

	@Override
	public GUIElement getGUIElement(InputState state, GUIElement dependent) {
		throw new RuntimeException();
	}

	@Override
	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText) {
		throw new RuntimeException();
	}

}
