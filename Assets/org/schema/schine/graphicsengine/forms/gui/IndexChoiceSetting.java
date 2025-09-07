package org.schema.schine.graphicsengine.forms.gui;

import java.util.List;

import org.schema.schine.graphicsengine.core.settings.EngineSettingsChangeListener;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class IndexChoiceSetting implements SettingsInterface{

	
	private int choices;
	private final List<EngineSettingsChangeListener> listeners = new ObjectArrayList<EngineSettingsChangeListener>();
	public IndexChoiceSetting(int choices) {
		this.choices = choices;
	}
	
	public abstract String getNameAt(int i);
	
	public void next() {
		setInt((getCurrentValue() + 1)%choices);
		
	}
	public void previous() {
		setInt((getCurrentValue() - 1) < 0 ? (choices-1) : (getCurrentValue()-1));
	}
	
	public abstract int getCurrentValue();
	public String getAsString() {
		return getNameAt(getCurrentValue());
	}
	@Override
	public boolean isOn() {
		throw new RuntimeException("invalid");
	}

	@Override
	public int getInt() {
		return getCurrentValue();
	}

	@Override
	public float getFloat() {
		throw new RuntimeException("invalid");
	}

	@Override
	public void setOn(boolean on) {
		throw new RuntimeException("invalid");
	}

	@Override
	public void setInt(int v) {
		onSelectedValue(v);
		
		for(EngineSettingsChangeListener l : listeners ) {
			l.onSettingChanged(this);
		}
	}

	public abstract void onSelectedValue(int v);

	@Override
	public void setFloat(float v) {
		throw new RuntimeException("invalid");		
	}

	@Override
	public String getString() {
		throw new RuntimeException("invalid");
	}

	@Override
	public void setString(String v) {
		throw new RuntimeException("invalid");		
	}

	@Override
	public Object getObject() {
		throw new RuntimeException("invalid");
	}

	@Override
	public void setObject(Object o) {
		throw new RuntimeException("invalid");
	}

	@Override
	public String name() {
		return "SETTING_NAME_UNDEFINED";
	}

	@Override
	public void addChangeListener(EngineSettingsChangeListener c) {
		listeners .add(c);
	}

	@Override
	public void removeChangeListener(EngineSettingsChangeListener c) {
		listeners.remove(c);
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
