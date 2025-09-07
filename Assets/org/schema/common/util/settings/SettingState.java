package org.schema.common.util.settings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.Version;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.input.InputState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public abstract class SettingState implements SettingsSerializable{
	
	
	private final List<SettingChangedListener> settingChangeListeners = new ObjectArrayList<>();
	
	
	
	public void addListener(SettingChangedListener a) {
		settingChangeListeners.add(a);
	}
	public void removeListener(SettingChangedListener a) {
		settingChangeListeners.remove(a);
	}
	
	protected void onChanged() {
		for(SettingChangedListener l : settingChangeListeners) {
			l.onSettingChanged(this);
		}
	}

    public long getLong() {
		throw new RuntimeException("Invalid");
    }
	public Object getObject(){
		throw new RuntimeException("Invalid");
	}
	public void setObject(Object o){
		throw new RuntimeException("Invalid");
	}

	public static interface SettingStateFac{
		public SettingState inst(SettingStateValueFac valueFac);
	}
	public static interface SettingStateValueFac{
		public SettingState inst();
	}
	
	public static enum SettingStateType{
		BOOLEAN("boolean", (SettingStateValueFac o) -> new SettingStateBoolean(false)),
		FLOAT("float", (SettingStateValueFac o) -> new SettingStateFloat(0)),
		LONG("long", (SettingStateValueFac o) -> new SettingStateLong(0)),
		INT("int", (SettingStateValueFac o) -> new SettingStateInt(0)),
		STRING("string", (SettingStateValueFac o) -> new SettingStateString("")),
		VERSION("version", (SettingStateValueFac o) -> new SettingStateVersion(new Version())), 
		ENUM("enum", (SettingStateValueFac o) -> o.inst()),  
		;

		public final String attName;
		public final SettingStateFac fac;

		private SettingStateType(String name, SettingStateFac fac) {
			this.attName = name;
			this.fac = fac;
		}
		public static SettingState fromName(Settings setting, String name) throws SettingsParseException {
			for(SettingStateType t : values()) {
				if(name.toLowerCase(Locale.ENGLISH).equals(t.attName.toLowerCase(Locale.ENGLISH))) {
					return t.fac.inst(Settings.stateFac.get(setting));
				}
			}
			throw new SettingsParseException("Setting type not found: "+name);
		}
	}
	
	public static SettingState deserializeStatic(Settings setting, DataInput b) throws IOException {
		SettingState s = SettingStateType.values()[ b.readByte()].fac.inst(Settings.stateFac.get(setting));
		s.deserialize(b, 0, true);
		return s;
	}
	
	public boolean isOn() {
		throw new RuntimeException("Invalid");
	}
	public int getInt() {
		throw new RuntimeException("Invalid");
	}
	public float getFloat() {
		throw new RuntimeException("Invalid");
	}
	public void setOn(boolean on) {
		throw new RuntimeException("Invalid");
	}
	public void setInt(int v) {
		throw new RuntimeException("Invalid");
	}
	public void setFloat(float v) {
		throw new RuntimeException("Invalid");
	}
	public String getString() {
		throw new RuntimeException("Invalid");
	}
	public void setString(String v) {
		throw new RuntimeException("Invalid");		
	}
	public Version getVersion() {
		throw new RuntimeException("Invalid");
	}
	public void setVersion(Version v) {
		throw new RuntimeException("Invalid");		
	}
	public Object getValue(){
		return null;
	}
	public abstract SettingStateType getType();
	
	@Override
	public Node writeXML(Document doc, Node root) {
		throw new RuntimeException("Not implemented");
	}

	public void addToNode(String settingName, Node root) {
		Element createElement = root.getOwnerDocument().createElement("Setting");
		createElement.setAttribute("name", settingName.toLowerCase(Locale.ENGLISH));
		createElement.setAttribute("type", getType().attName.toLowerCase(Locale.ENGLISH));
		writeXML(root.getOwnerDocument(), createElement);
		root.appendChild(createElement);
	}
	
	public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, String deactText) {
		
		GUIActivatableTextBar t = new GUIActivatableTextBar(state, FontSize.MEDIUM_15, deactText, dependent, new TextCallback() {
			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
			}
			
			@Override
			public void onFailedTextCheck(String msg) {
			}
			
			@Override
			public void newLine() {
				
			}
			
			@Override
			public String handleAutoComplete(String s, TextCallback callback,
					String prefix) throws PrefixNotFoundException {
				return null;
			}
			
			@Override
			public String[] getCommandPrefixes() {
				return null;
			}
		}, t1 -> {
			try {
				setFromString(t1);
			}catch(NumberFormatException e) {
				e.printStackTrace();
			}
			return t1;
		}){

			@Override
			protected void onBecomingInactive() {
				//revert to last valid state
				setText(getAsString());
			}
			
		};
		t.setDeleteOnEnter(false);
		t.setText(getAsString());
		return t;
			
	}
	
	public GUIElement getGUIElement(InputState state, GUIElement dependent) {
		return getGUIElement(state, dependent, "SETTING");
	}
	public abstract GUIElement getGUIElement(InputState state, final GUIElement dependent, String deactText);
		
	public static SettingState fromSettingsNode(Settings setting, Node node) throws SettingsParseException{
		Node namedItem = node.getAttributes().getNamedItem("type");
		if(namedItem != null) {
			SettingState s = SettingStateType.fromName(setting, namedItem.getNodeValue());
			s.parseXML(node);
			return s;
		}else {
			throw new SettingsParseException("Setting had no type attribute");
		}
	}
	public boolean isSlider() {
		return false;
	}
	
	
	public abstract void setValue(SettingState settingState);
	public abstract void setValueByObject(Object value);
	public abstract String getAsString();
	public abstract boolean setFromString(String string);
	public void addPossibilityOption(Object o) {
		throw new RuntimeException("Invalid");	
	}
	public void setLong(long l) {
		throw new RuntimeException("invalid");
	}
	public abstract void next();
	public abstract void previous();
}
