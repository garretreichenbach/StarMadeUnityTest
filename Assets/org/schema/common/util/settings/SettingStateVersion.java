package org.schema.common.util.settings;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Locale;

import org.schema.common.util.Version;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SettingStateVersion extends SettingState{
	private Version value;
	
	public SettingStateVersion(Version value) {
		this.value = value;
	}
	public Version getVersion() {
		return value;
	}
	public void setVersion(Version v) {
		value = v;
		onChanged();
	}
	
	@Override
	public SettingStateType getType() {
		return SettingStateType.VERSION;
	}

	@Override
	public Node writeXML(Document doc, Node baseNode) {
		Element createElement = baseNode.getOwnerDocument().createElement("Value");
		createElement.setTextContent(value.toString());
		baseNode.appendChild(createElement);
		return baseNode;
	}
	@Override
	public void parseXML(Node node)  {
		try {
			NodeList cn = node.getChildNodes();
			for(int i = 0; i < cn.getLength(); i++) {
				Node item = cn.item(i);
				if(item.getNodeType() == Node.ELEMENT_NODE) {
					if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("value")) {
						try {
							value = Version.parseFrom(item.getTextContent());
						}catch(NumberFormatException e) {
							
							System.err.println("ERROR PARSING: "+item.getTextContent());
							throw e;
						}
					}
				}
			}
		}catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {
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
		
		value.serialize(b, isOnServer);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		//type already read
		Version before = value;
		
		value = new Version();
		value.deserialize(b, updateSenderStateId, isOnServer);
		
		if(!value.equals(before)) {
			onChanged();
		}
		
	}
	@Override
	public void setValue(SettingState settingState) {
		value = settingState.getVersion();
		onChanged();
	}
	@Override
	public String toString() {
		return value+" : "+getType().attName;
	}
	@Override
	public void setValueByObject(Object value) {
		this.value = (Version)value;
	}
	@Override
	public String getAsString() {
		return value.toString();
	}
	@Override
	public boolean setFromString(String string) {
		try {
			value.parse(string);
			onChanged();
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText) {
		throw new RuntimeException("No gui representation of this setting");
	}
	@Override
	public void next() {
		throw new RuntimeException("invalid");
	}
	@Override
	public void previous() {
		throw new RuntimeException("invalid");
	}
}
