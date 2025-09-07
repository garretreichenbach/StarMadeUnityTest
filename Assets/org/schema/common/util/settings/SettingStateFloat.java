package org.schema.common.util.settings;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector.LabelPosition;
import org.schema.schine.input.InputState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Locale;

public class SettingStateFloat extends SettingState {
	private final boolean slider;
	public float step = 0.1f;
	private float value;
	private float min = Float.NEGATIVE_INFINITY;
	private float max = Float.POSITIVE_INFINITY;
	private float[] possibilities;

	public SettingStateFloat(int value) {
		this(value, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	public SettingStateFloat(float value, float min, float max) {
		this(value, min, max, false);
	}

	public SettingStateFloat(float value, float min, float max, boolean slider) {
		this.slider = slider;
		this.min = min;
		this.max = max;
		setFloat(value);
	}

	public SettingStateFloat(float value, float[] possibilities) {
		setFloat(value);
		this.possibilities = possibilities;
		slider = false;
	}

	public float getMin() {
		return min;
	}

	public float getMax() {
		return max;
	}

	public float[] getPossibilities() {
		return possibilities;
	}

	public float getFloat() {
		return value;
	}

	public void setFloat(float v) {
		value = Math.min(max, Math.max(min, v));
		onChanged();
	}

	@Override
	public SettingStateType getType() {
		return SettingStateType.FLOAT;
	}

	@Override
	public Node writeXML(Document doc, Node baseNode) {
		{
			Element createElement = baseNode.getOwnerDocument().createElement("Value");
			createElement.setTextContent(Float.toString(value));
			baseNode.appendChild(createElement);
		}
		{
			Element createElement = baseNode.getOwnerDocument().createElement("Min");
			createElement.setTextContent(Float.toString(min));
			baseNode.appendChild(createElement);
		}
		{
			Element createElement = baseNode.getOwnerDocument().createElement("Max");
			createElement.setTextContent(Float.toString(max));
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
						value = Float.parseFloat(item.getTextContent());
					}
					if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("min")) {
						min = Float.parseFloat(item.getTextContent());
					}
					if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("max")) {
						max = Float.parseFloat(item.getTextContent());
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
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(getType().ordinal());

		b.writeFloat(value);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		//type already read
		float before = value;
		value = b.readFloat();
		if(value != before) {
			onChanged();
		}
	}

	@Override
	public void setValue(SettingState settingState) {
		value = settingState.getFloat();
		onChanged();
	}

	@Override
	public String toString() {
		return value + " : " + getType().attName;
	}

	public boolean isSlider() {
		return slider;
	}

	@Override
	public void setValueByObject(Object value) {
		this.value = (Float) value;
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
			value = Float.parseFloat(string);
			onChanged();
			return true;
		} catch(NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText) {
		if(possibilities == null) {
			if(min > Float.NEGATIVE_INFINITY && max < Float.POSITIVE_INFINITY) {
				GUIScrollSettingSelector scrollSetting = new GUIScrollSettingSelector(state, GUIScrollablePanel.SCROLLABLE_HORIZONTAL, 50, FontSize.MEDIUM_15) {
					@Override
					public boolean isVerticalActive() {
						return false;
					}


					@Override
					public void settingChanged(Object setting) {
////						System.err.println("SETTINGS :"+getName()+": "+setting+"; "+getRes().getCurrentValue());
//						if(setting instanceof Integer){
//							getRes().change((Integer)setting);
//						}
//						if(setting instanceof Float){
//							getRes().change(((Float)setting).intValue());
//						}
//						super.settingChanged(setting);
//						getRes().change(getRes().getCurrentValue());
					}

					@Override
					public boolean showLabel() {
						return true;
					}

					@Override
					public void resetScrollValue() {
						setSettingX(value);
					}

					@Override
					protected void incSetting() {
						next();
						settingChanged(value);
					}

					@Override
					protected float getSettingY() {
						return 0;
					}

					@Override
					protected void setSettingY(float value) {
					}

					@Override
					protected float getSettingX() {
						return value;
					}

					@Override
					protected void setSettingX(float value) {
						SettingStateFloat.this.setFloat(value);
						settingChanged(SettingStateFloat.this.value);
					}

					@Override
					public float getMaxY() {
						return 0;
					}

					@Override
					public float getMaxX() {
						return max;
					}

					@Override
					protected void decSetting() {
						previous();
						settingChanged(null);
					}

					@Override
					public float getMinX() {
						return min;
					}

					@Override
					public float getMinY() {
						return 0;
					}
				};
				scrollSetting.setNameLabel(new Object() {
					@Override
					public String toString() {
						return String.format("%.2f", value);
					}

				});
				scrollSetting.labelPosition = LabelPosition.RIGHT;
				scrollSetting.dep = dependent;
				scrollSetting.widthMod = -10;
				scrollSetting.posMoxX = 5;
				return scrollSetting;
			}


			return getGUIElementTextBar(state, dependent, deactText);
		}
		int width = UIScale.getUIScale().scale(100);
		int heigth = UIScale.getUIScale().h;
		int i = 0;
		GUIElement[] elements = new GUIElement[possibilities.length];
		int selIndex = -1;
		for(final float e : possibilities) {
			GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, state);
			final Vector3i pos = new Vector3i();
			o.setTextSimple(new Object() {
				@Override
				public String toString() {
					return String.valueOf(e);
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
				float inv = (float) element.getContent().getUserPointer();
				setFloat(inv);
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
		setFloat(Math.min(value + step, max));
	}

	@Override
	public void previous() {
		if(possibilities != null) {
			previousPossibility();
		} else {
			setFloat(Math.max(value - step, min));
		}
	}

	public int indexOf(float val) {
		if(possibilities != null) {
			for(int i = 0; i < possibilities.length; i++) {
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
			int next = (ord + 1) % possibilities.length;
			setFloat(possibilities[next]);
		}
	}

	public void previousPossibility() {
		int ord = indexOf(value);
		if(ord != -1) {
			int prev = ord == 0 ? possibilities.length - 1 : ord - 1;
			setFloat(possibilities[prev]);
		}
	}
}
