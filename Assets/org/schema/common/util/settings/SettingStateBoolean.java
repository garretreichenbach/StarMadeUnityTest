package org.schema.common.util.settings;

import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Locale;

public class SettingStateBoolean extends SettingState{
	private boolean value;

	public SettingStateBoolean(boolean b) {
		this.value = b;
	}

	@Override
	public boolean isOn() {
		return value;
	}

	@Override
	public void setOn(boolean on) {
		value = on;
		onChanged();
	}

	@Override
	public SettingStateType getType() {
		return SettingStateType.BOOLEAN;
	}

	@Override
	public Node writeXML(Document doc, Node baseNode) {
		Element createElement = baseNode.getOwnerDocument().createElement("Value");
		createElement.setTextContent(Boolean.toString(value));
		baseNode.appendChild(createElement);
		return baseNode;
	}

	@Override
	public void parseXML(Node node) throws SettingsParseException {
		try {
			NodeList cn = node.getChildNodes();
			for(int i = 0; i < cn.getLength(); i++) {
				Node item = cn.item(i);
				if(item.getNodeType() == Node.ELEMENT_NODE) {
					if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("value")) {
						value = Boolean.parseBoolean(item.getTextContent());
					}
				}
			}
		}catch(NumberFormatException e) {
			Node namedItem = node.getParentNode().getAttributes().getNamedItem("name");
			String name = "n/a";
			if(namedItem != null) {
				name = namedItem.getNodeValue();
			}
			throw new SettingsParseException("Parse failed in node name: "+name+"; "+node.getNodeName()+" -> "+node.getParentNode().getNodeName()+"; ", e);
		}
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(getType().ordinal());
		
		b.writeBoolean(value);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		//type already read
		boolean before = value;
		value = b.readBoolean();
		if(value != before) {
			onChanged();
		}
	}

	@Override
	public void setValue(SettingState settingState) {
		value = settingState.isOn();
		onChanged();
	}
	@Override
	public String toString() {
		return value+" : "+getType().attName;
	}

	@Override
	public void setValueByObject(Object value) {
		this.value = (Boolean)value;
	}

	@Override
	public String getAsString() {
		return String.valueOf(value);
	}

	@Override
	public boolean setFromString(String string) {
		try {
			value = Boolean.parseBoolean(string);
			onChanged();
			return true;
		}catch(NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText) {
		GUICheckBox c = new GUICheckBox(state) {
			
			@Override
			protected boolean isActivated() {
				return isOn();
			}
			
			@Override
			protected void deactivate() {
				setOn(false);
			}
			
			@Override
			protected void activate() {
				setOn(true);
			}
		};
		c.activeInterface = dependent::isActive;
		
		c.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		return c;
	}

	@Override
	public void next() {
		setOn(!value);
	}

	@Override
	public void previous() {
		setOn(!value);		
	}
	
}
