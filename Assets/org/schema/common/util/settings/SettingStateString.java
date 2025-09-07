package org.schema.common.util.settings;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
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

public class SettingStateString extends SettingState{
	private final String[] possibilities;
	private String value;
	
	public SettingStateString(String value) {
		this(value, null);
	}
	public SettingStateString(String value, String[] possibilities) {
		this.value = value;
		this.possibilities = possibilities;
	}
	public String getString() {
		return value;
	}
	public void setString(String v) {
		assert v != null;
		value = v;
		onChanged();
	}
	
	@Override
	public SettingStateType getType() {
		return SettingStateType.STRING;
	}

	@Override
	public Node writeXML(Document doc, Node baseNode) {
		Element createElement = baseNode.getOwnerDocument().createElement("Value");
		createElement.setTextContent(value);
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
						value = item.getTextContent();
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
		
		b.writeUTF(value);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		//type already read
		String before = value;
		value = b.readUTF();
		if(!value.equals(before)) {
			onChanged();
		}
	}
	@Override
	public void setValue(SettingState settingState) {
		value = settingState.getString();
		onChanged();
	}
	@Override
	public String toString() {
		return value+" : "+getType().attName;
	}
	@Override
	public void setValueByObject(Object value) {
		this.value = (String)value;
	}
	@Override
	public String getAsString() {
		return value;
	}
	@Override
	public boolean setFromString(String string) {
		value = string;
		onChanged();
		return true;
	}
	@Override
	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText) {
		if(possibilities == null) {
			return getGUIElementTextBar(state, dependent, deactText);
		}
		int width = UIScale.getUIScale().scale(100);
		int heigth = UIScale.getUIScale().h;
		int i = 0;
		GUIElement[] elements = new GUIElement[possibilities.length];
		int selIndex = -1;
		for (final String e : possibilities) {
			GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, state);
			final Vector3i pos = new Vector3i();
			o.setTextSimple(new Object() {
				@Override
				public String toString() {
					return String.valueOf(e);
				}
			});
			if(e == value){
				selIndex = i;
			}
			GUIAnchor a = new GUIAnchor(state, width, heigth) {
				@Override
				public void draw() {
					setWidth(dependent.getWidth());
					super.draw();
				}
			};
			o.getPos().x = UIScale.getUIScale().inset;
			o.getPos().y = UIScale.getUIScale().inset;
			a.attach(o);
			a.setUserPointer(e);
			elements[i] = a;
			i++;
		}

		GUIDropDownList t = new GUIDropDownList(state, width, heigth, 400, new DropDownCallback() {
			private boolean first = true;

			@Override
			public void onSelectionChanged(GUIListElement element) {
				if (first) {
					first = false;
					return;
				}
				String inv = (String) element.getContent().getUserPointer();
				setString(inv);
			}
		}, elements); 
		if(selIndex >= 0){
			t.setSelectedIndex(selIndex);
		}
		t.dependend = dependent;
		return t;
	}
	public int indexOf(String val) {
		if(possibilities != null) {
			for (int i = 0; i < possibilities.length; i++) {
				if(possibilities[i].equals(val)) {
					return i;
				}
			}
		}
		return -1;
	}
	@Override
	public void next() {
		int ord = indexOf(value);
		if(ord != -1) {
			int next = (ord+1)%possibilities.length;
			setString(possibilities[next]);
		}
	}

	@Override
	public void previous() {
		int ord = indexOf(value);
		if(ord != -1) {
			int prev = ord == 0 ? possibilities.length-1 : ord-1;
			setString(possibilities[prev]);
		}
	}
}
