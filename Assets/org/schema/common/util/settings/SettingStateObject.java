package org.schema.common.util.settings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
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
import java.util.List;
import java.util.Locale;

public class SettingStateObject<E extends SettingsXMLValue> extends SettingState{
	private E value;
	private final List<E> possibilities = new ObjectArrayList<>();

	public SettingStateObject(E b, E[] possibilities) {
		this.value = b;
		for(int i = 0; i < possibilities.length; i++) {
			this.possibilities.add(possibilities[i]);
		}
	}


	@Override
	public SettingStateType getType() {
		return SettingStateType.ENUM;
	}

	@Override
	public Node writeXML(Document doc, Node baseNode) {
		
		Element createElement = baseNode.getOwnerDocument().createElement("Value");
		
		createElement.setTextContent(value.getStringID());		
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
						
						String stringId = node.getTextContent().toLowerCase(Locale.ENGLISH);
						
						setFromString(stringId);
						
						if(value == null) {
							throw new SettingsParseException("Parse failed in node name: "+item.getTextContent()+"; "+node.getNodeName()+" -> "+node.getParentNode().getNodeName()+"; ");
						}
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
		b.writeUTF(value.getStringID().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		//type already read
		String stringId = b.readUTF();
		
		for(E v : possibilities) {
			if(v.getStringID().toLowerCase(Locale.ENGLISH).equals(stringId)) {
				value = v;
				onChanged();
				break;
			}
		}
		
	}

	@Override
	public Object getValue() {
		return value;
	}


	@Override
	public void setValue(SettingState settingState) {
		value = (E) settingState.getValue();
		onChanged();
	}
	@Override
	public String toString() {
		return value+" : "+getType().attName;
	}


	public E[] getPossibilities() {
		return (E[]) possibilities.toArray();
	}

	public E getObject(){
		return value;
	}
	public void setObject(Object o){
		value = (E)o;
		onChanged();
	}
	public void setObject(E o){
		value = o;
		onChanged();
	}
	@Override
	public void setValueByObject(Object value) {
		this.value = (E)value;
	}
	@Override
	public String getAsString() {
		return value.getStringID();
	}

	@Override
	public boolean setFromString(String string) {
		for(E e : possibilities) {
			if(e.getStringID().trim().toLowerCase(Locale.ENGLISH).equals(string.trim().toLowerCase(Locale.ENGLISH))) {
				value = e;
				onChanged();
				return true;
			}
		}
		return false;
	}
	
	public void addPossibilityOption(Object o) {
		possibilities.add((E) o);
	}
	
	@Override
	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText) {
		int width = UIScale.getUIScale().scale(100);
		int heigth = UIScale.getUIScale().h;
		
		int i = 0;
		GUIElement[] elements = new GUIElement[possibilities.size()];
		int selIndex = -1;
		for (E e : possibilities) {
			GUITextOverlay o = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, state);
			Vector3i pos = new Vector3i();
			o.setTextSimple(new Object() {
				@Override
				public String toString() {
					return e.toString().replaceAll("_", " ");
				}
			});
			if(e.getStringID().equals(value.getStringID())){
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
				E inv = (E) element.getContent().getUserPointer();
				setObject(inv);
			}
		}, elements); 
		if(selIndex >= 0){
			t.setSelectedIndex(selIndex);
		}
		t.dependend = dependent;
		return t;
	}
	public int indexOf(E val) {
		for (int i = 0; i < possibilities.size(); i++) {
			if(possibilities.get(i) == val) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public void next() {
		int ord = indexOf(value);
		int next = (ord+1)%possibilities.size();
		setObject(possibilities.get(next));
	}

	@Override
	public void previous() {
		int ord = indexOf(value);
		int prev = ord == 0 ? possibilities.size()-1 : ord-1;
		setObject(possibilities.get(prev));		
	}
}
