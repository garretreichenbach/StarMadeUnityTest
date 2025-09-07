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

public class SettingStateEnum<E extends Enum<?>> extends SettingState {
	private E value;
	private E[] values;

	public SettingStateEnum(E b, E[] values) {
		this.value = b;
		assert (value != null);
		this.values = values;
	}

	@Override
	public SettingStateType getType() {
		return SettingStateType.ENUM;
	}

	@Override
	public Node writeXML(Document doc, Node baseNode) {

		Element createElement = baseNode.getOwnerDocument().createElement("Value");
		if(value == null) {
			throw new NullPointerException("VALUE NULL");
		}
		createElement.setTextContent(value.name());

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
//						setFromString(item.getTextContent());
						//dont trigger the onChanged event
						for(E e : values) {
							if(e.name().toLowerCase(Locale.ENGLISH).equals(item.getTextContent().toLowerCase(Locale.ENGLISH))) value = e;
						}
						if(value == null) {
							throw new SettingsParseException("Parse failed in node name: " + item.getTextContent() + "; " + node.getNodeName() + " -> " + node.getParentNode().getNodeName() + "; ");
						}
					}
				}
			}
		} catch(NumberFormatException e) {
			Node namedItem = node.getParentNode().getAttributes().getNamedItem("name");
			String name = "n/a";
			if(namedItem != null) {
				name = namedItem.getNodeValue();
			}
			throw new SettingsParseException("Parse failed in node name: " + name + "; " + node.getNodeName() + " -> " + node.getParentNode().getNodeName() + "; ", e);
		}
		assert value != null : "value cannot be null at this time. Should have thrown exception";
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(getType().ordinal());
		b.writeShort(value.ordinal());
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		//type already read
		E before = value;
		value = values[b.readShort()];

		if(!value.equals(before)) {
			onChanged();
		}
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(SettingState settingState) {

		value = (E) settingState.getObject();
		assert (value != null) : "Cannot set null";
		onChanged();
	}

	@Override
	public String toString() {
		return value + " : " + getType().attName;
	}

	public E getObject() {
		return value;
	}

	public void setObject(E o) {
		assert (o != null) : "Cannot set null";
		value = o;
		onChanged();
	}

	@Override
	public void setValueByObject(Object value) {
		this.value = (E) value;
	}

	@Override
	public String getAsString() {
		return value.name().replaceAll("_", " ");
	}

	@Override
	public boolean setFromString(String string) {
		for(E e : values) {
			if(e.name().toLowerCase(Locale.ENGLISH).equals(string.toLowerCase(Locale.ENGLISH))) {
				value = e;
				onChanged();
				return true;
			}
		}
		assert (false) : "Cannot set null";
		return false;
	}

	@Override
	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText) {

		int width = UIScale.getUIScale().scale(100);
		int heigth = UIScale.getUIScale().h;

		int i = 0;
		GUIElement[] elements = new GUIElement[values.length];
		int selIndex = -1;
		for(final E e : values) {
			GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, state);
			final Vector3i pos = new Vector3i();
			o.setTextSimple(new Object() {
				@Override
				public String toString() {
					return e.toString().replaceAll("_", " ");
				}
			});
			if(e == value) {
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
				if(first) {
					first = false;
					return;
				}
				E inv = (E) element.getContent().getUserPointer();
				setObject(inv);
			}
		}, elements);
		if(selIndex >= 0) {
			t.setSelectedIndex(selIndex);
		}
		t.dependend = dependent;
		return t;
	}

	@Override
	public void next() {
		int next = (value.ordinal() + 1) % values.length;
		setObject(values[next]);
	}

	@Override
	public void previous() {
		int prev = value.ordinal() == 0 ? values.length - 1 : value.ordinal() - 1;
		setObject(values[prev]);
	}

	public E[] getValues() {
		return values;
	}
}
