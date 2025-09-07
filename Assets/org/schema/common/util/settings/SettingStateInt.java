package org.schema.common.util.settings;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Locale;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.DropDownCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SettingStateInt extends SettingState{
	private int value;
	private int min = Integer.MIN_VALUE;
	private int max = Integer.MAX_VALUE;
	private int[] possibilities;
	public int step = 1;
	
	public SettingStateInt(int value) {
		this.value = value;
	}
	public SettingStateInt(int value, int[] possibilities) {
		this.possibilities = possibilities;
		this.value = value;
	}
	public SettingStateInt(int value, int min, int max) {
		this.min = min;
		this.max = max;
		this.value = value;
	}
	public int getInt() {
		return value;
	}
	public void setInt(int v) {
		value = Math.min(max, Math.max(min, v));
		onChanged();
	}
	@Override
	public SettingStateType getType() {
		return SettingStateType.INT;
	}
	@Override
	public Node writeXML(Document doc, Node baseNode) {
		{
			Element createElement = baseNode.getOwnerDocument().createElement("Value");
			createElement.setTextContent(Integer.toString(value));
			baseNode.appendChild(createElement);
		}
		{
			Element createElement = baseNode.getOwnerDocument().createElement("Min");
			createElement.setTextContent(Integer.toString(min));
			baseNode.appendChild(createElement);
		}
		{
			Element createElement = baseNode.getOwnerDocument().createElement("Max");
			createElement.setTextContent(Integer.toString(max));
			baseNode.appendChild(createElement);
		}
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
						value = Integer.parseInt(item.getTextContent());
					}
					if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("min")) {
						min = Integer.parseInt(item.getTextContent());
					}
					if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("max")) {
						max = Integer.parseInt(item.getTextContent());
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
		
		b.writeInt(value);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		//type already read
		int before = value;
		value = b.readInt();
		if(value != before) {
			onChanged();
		}
	}
	@Override
	public void setValue(SettingState settingState) {
		value = settingState.getInt();
		onChanged();
	}
	@Override
	public String toString() {
		return value+" : "+getType().attName;
	}
	public int[] getPossibilities() {
		return possibilities;
	}
	@Override
	public void setValueByObject(Object value) {
		this.value = (Integer)value;
	}
	@Override
	public String getAsString() {
		return String.valueOf(value);
	}

	@Override
	public boolean setFromString(String string) {
		string = string.trim();
		if(string.length() == 0) {
			return false;
		}
		try {
			value = Integer.parseInt(string);
			onChanged();
			return true;
		}catch(NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
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
		for (final int e : possibilities) {
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
				int inv = (int) element.getContent().getUserPointer();
				setInt(inv);
			}
		}, elements); 
		if(selIndex >= 0){
			t.setSelectedIndex(selIndex);
		}
		t.dependend = dependent;
		return t;
	}
	@Override
	public void next() {
		if(possibilities != null) {
			nextPossibility();
		}else {
			setInt(Math.min(value+step, max));
		}
	}
	@Override
	public void previous() {
		if(possibilities != null) {
			previousPossibility();
		}else {
			setInt(Math.max(value-step, min));
		}
	}
	
	@Override
	public boolean isOn() {
		return value != 0;
	}
	public int indexOf(int val) {
		if(possibilities != null) {
			for (int i = 0; i < possibilities.length; i++) {
				if(possibilities[i] == (val)) {
					return i;
				}
			}
		}
		return -1;
	}
	public void nextPossibility() {
		int ord = indexOf(value);
		if(ord != -1) {
			int next = (ord+1)%possibilities.length;
			setInt(possibilities[next]);
		}
	}

	public void previousPossibility() {
		int ord = indexOf(value);
		if(ord != -1) {
			int prev = ord == 0 ? possibilities.length-1 : ord-1;
			setInt(possibilities[prev]);
		}
	}
}
