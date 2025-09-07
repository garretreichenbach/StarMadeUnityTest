package org.schema.schine.graphicsengine.forms.gui;

import org.schema.common.util.settings.Settings;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsChangeListener;
import org.schema.schine.input.InputState;

public interface SettingsInterface extends Settings{


	public void addChangeListener(EngineSettingsChangeListener c);
	public void removeChangeListener(EngineSettingsChangeListener c);
	public void next();
	public void previous();
	public String getAsString();
	
	/**
	 * 
	 * @return either this instance or a temp instance of the setting to be used in options (setting without applying)
	 */
	public SettingsInterface getSettingsForGUI();
	
	
	public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, String deactText);
	public GUIElement getGUIElement(InputState state, GUIElement dependent);
	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText);
}
