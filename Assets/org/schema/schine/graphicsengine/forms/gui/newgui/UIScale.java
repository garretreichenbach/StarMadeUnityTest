package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.common.XMLTools;
import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

public enum UIScale {

	S_100("Scale100"),
	S_150("Scale150"),
	S_200("Scale200");

	public static UIScale getUIScale() {
		return (UIScale) EngineSettings.UI_SCALE.getObject();
	}
	
	public static float getScalef() {
		return switch(EngineSettings.UI_SCALE.getObject()) {
			case S_100 -> 1.0f;
			case S_150 -> 1.5f;
			case S_200 -> 2.0f;
			default -> 1.0f;
		};
	}

	@ScalingValue(tag = "DefaultHeight")
	public int h = 24;

	@ScalingValue(tag = "DefaultInset")
	public int inset = 4;

	@ScalingValue(tag = "smallButtonOffsetPX")
	public int smallButtonOffsetPX = 4;

	@ScalingValue(tag = "smallButtonHeight")
	public int smallButtonHeight = 24;

	@ScalingValue(tag = "smallinset")
	public int smallinset = 2;

	@ScalingValue(tag = "ICON_SIZE")
	public int ICON_SIZE = 64;

	@ScalingValue(tag = "ICON_START_POINT")
	public int ICON_START_POINT = 8;

	@ScalingValue(tag = "W_inset")
	public int W_inset = 29;

	@ScalingValue(tag = "W_xInnerOffset")
	public int W_xInnerOffset = 8;

	@ScalingValue(tag = "W_yInnerOffset")
	public int W_yInnerOffset = 8;

	@ScalingValue(tag = "W_innerHeightSubstraction")
	public int W_innerHeightSubstraction = 60;

	@ScalingValue(tag = "W_innerHeightSubstraction_textarea")
	public int W_innerHeightSubstraction_textarea = 68;

	@ScalingValue(tag = "W_innerHeightSubstraction_noPadding")
	public int W_innerHeightSubstraction_noPadding = 24;

	@ScalingValue(tag = "W_innerWidthSubstraction")
	public int W_innerWidthSubstraction = 32;

	@ScalingValue(tag = "W_topDist")
	public int W_topDist = 20;

	@ScalingValue(tag = "W_innerWidthSubstraction_shipInfoPanel")
	public int W_innerWidthSubstraction_shipInfoPanel = 14;

	@ScalingValue(tag = "W_xInnerOffet_shipInfoPanel")
	public int W_xInnerOffet_shipInfoPanel = 4;

	@ScalingValue(tag = "W_TAB_MIN_WIDTH")
	public int W_TAB_MIN_WIDTH = 96; //int tabSize = FastMath.round(((getTabContentWidth() - UIScale.getUIScale().W_TAB_MIN_WIDTH) / getTabCount()));

	@ScalingValue(tag = "W_TAB_HEIGHT")
	public int W_TAB_HEIGHT = 32;

	@ScalingValue(tag = "W_MinWidth")
	public int W_MinWidth = 128;

	@ScalingValue(tag = "W_MinHeight")
	public int W_MinHeight = 128;

	@ScalingValue(tag = "W_DIALOG_CROSS_X")
	public int W_DIALOG_CROSS_X = -36;

	@ScalingValue(tag = "W_DIALOG_CROSS_Y")
	public int W_DIALOG_CROSS_Y = -16;

	@ScalingValue(tag = "P_BUTTON_PANE_HEIGHT")
	public int P_BUTTON_PANE_HEIGHT = 28;

	@ScalingValue(tag = "P_BUTTON_PANE_HEIGHT")
	public int P_SMALL_PANE_HEIGHT = 24;

	@ScalingValue(tag = "MAIN_WINDOW_INNER_FB_INSET")
	public int MAIN_WINDOW_INNER_FB_INSET = 29;

	@ScalingValue(tag = "MAIN_WINDOW_TABS_HEIGHT")
	public int MAIN_WINDOW_TABS_HEIGHT = 29;

	@ScalingValue(tag = "MAIN_WINDOW_INNER_BG_INSET")
	public int MAIN_WINDOW_INNER_BG_INSET = 24;

	@ScalingValue(tag = "MAIN_WINDOW_xInnerOffset")
	public int MAIN_WINDOW_xInnerOffset = 8;

	@ScalingValue(tag = "MAIN_WINDOW_yInnerOffset")
	public int MAIN_WINDOW_yInnerOffset = 8;

	@ScalingValue(tag = "MAIN_WINDOW_TABBED_INNER_WIDTH_DIST")
	public int MAIN_WINDOW_TABBED_INNER_WIDTH_DIST = 32;

	@ScalingValue(tag = "MAIN_WINDOW_TABBED_INNER_HEIGHT_DIST")
	public int MAIN_WINDOW_TABBED_INNER_HEIGHT_DIST = 32;

	@ScalingValue(tag = "TABBED_WINDOW_cornerDistanceBottomY")
	public int TABBED_WINDOW_cornerDistanceBottomY = 8;

	@ScalingValue(tag = "MAIN_WINDOW_TABBED_getInnerOffsetX")
	public int MAIN_WINDOW_TABBED_getInnerOffsetX = 8;

	@ScalingValue(tag = "MAIN_WINDOW_TABBED_getInnerOffsetY")
	public int MAIN_WINDOW_TABBED_getInnerOffsetY = 8;

	@ScalingValue(tag = "MAIN_WINDOW_CROSS_X")
	public int MAIN_WINDOW_CROSS_X = -58;

	@ScalingValue(tag = "MAIN_WINDOW_CROSS_Y")
	public int MAIN_WINDOW_CROSS_Y = -8;

	@ScalingValue(tag = "MAIN_WINDOW_STRIP_0_X")
	public int MAIN_WINDOW_STRIP_0_X = 119;

	@ScalingValue(tag = "MAIN_WINDOW_STRIP_1_X")
	public int MAIN_WINDOW_STRIP_1_X = 4;

	@ScalingValue(tag = "MAIN_WINDOW_STRIP_W")
	public int MAIN_WINDOW_STRIP_W = 5;

	@ScalingValue(tag = "SLIDER_W")
	public int SLIDER_W = 24;
	@ScalingValue(tag = "SLIDER_distanceAfterButton")
	public int SLIDER_distanceAfterButton = 4;
	@ScalingValue(tag = "SLIDER_SPRITE_SIZE")
	public int SLIDER_SPRITE_SIZE = 16;

	@ScalingValue(tag = "TABLE_defaultColumnsHeight")
	public int TABLE_defaultColumnsHeight = 28;
	@ScalingValue(tag = "TABLE_HEADER_HEIGHT")
	public int TABLE_HEADER_HEIGHT = 24;
	@ScalingValue(tag = "TABLE_FOOTER_HEIGHT")
	public int TABLE_FOOTER_HEIGHT = 24;

	public final String name;

	public static String UI_SCALE_PATH = DataUtil.dataPath + "config" + File.separator + "gui" + File.separator + "UIScale.xml";

	UIScale(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		switch(this) {
			case S_100 -> {
				return "100%";
			}
			case S_150 -> {
				return "150%";
			}
			case S_200 -> {
				return "200%";
			}
		}
		return name;
	}

	public int scale(int i) {
		return switch(this) {
			case S_100 -> i;
			case S_150 -> i + i / 2;
			case S_200 -> i + i;
		};
//		throw new RuntimeException("Invalid Scale "+this);
	}

	private static final String sep = "/";

	public String getGuiPath() {
		return switch(this) {
			case S_100 -> "gui" + sep + "100" + sep;
			case S_150 -> "gui" + sep + "150" + sep;
			case S_200 -> "gui" + sep + "200" + sep;
		};

	}

	public static void write() throws IOException {
		Document doc;
		try {
			doc = XMLTools.loadXML(new File(UI_SCALE_PATH));

			Element root = doc.createElement("UIScale");
			doc.appendChild(root);
			for(UIScale s : values()) {
				Element cd = doc.createElement(s.name);
				s.write(doc, cd);
				root.appendChild(cd);
			}

			XMLTools.writeDocument(new File(UI_SCALE_PATH), doc);
		} catch(ParserConfigurationException e) {
			throw new IOException(e);
		} catch(TransformerException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(Document d, Element n) throws ParserConfigurationException {
		Field[] fs = getClass().getFields();
		for(Field f : fs) {
			f.setAccessible(true);
			ScalingValue a = f.getAnnotation(ScalingValue.class);
			if(a != null) {
				Element elem = d.createElement(a.tag().toLowerCase(Locale.ENGLISH));
				if(f.getType() == Integer.TYPE) {
					try {
						elem.setTextContent(String.valueOf(f.getInt(this)));
					} catch(DOMException | IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				} else {
					throw new RuntimeException("unsupported type");
				}
				n.appendChild(elem);
			}
		}

	}

	public static void load() throws SAXException, IOException, ParserConfigurationException {
		Document doc = XMLTools.loadXML(new File(UI_SCALE_PATH));

		NodeList cn = doc.getDocumentElement().getChildNodes();
		for(int i = 0; i < cn.getLength(); i++) {
			Node item = cn.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				boolean found = false;
				for(UIScale s : values()) {
					if(s.name.toLowerCase(Locale.ENGLISH).equals(item.getNodeName().toLowerCase(Locale.ENGLISH))) {
						s.load((Element) item);
						found = true;
						break;
					}
				}
				if(!found) {
					throw new RuntimeException("No Enum found for node: '" + item.getNodeName() + "'  (case insensitive)");
				}
			}
		}

	}

	public void load(Element rt) {
		Field[] fs = getClass().getFields();
		for(Field f : fs) {
			f.setAccessible(true);
			ScalingValue a = f.getAnnotation(ScalingValue.class);
			if(a != null) {

				NodeList cn = rt.getChildNodes();
				boolean found = false;
				for(int i = 0; i < cn.getLength(); i++) {
					Node item = cn.item(i);
					if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().toLowerCase(Locale.ENGLISH).equals(a.tag().toLowerCase(Locale.ENGLISH))) {

						if(f.getType() == Integer.TYPE) {
							try {
								f.setInt(this, Integer.parseInt(item.getTextContent().trim()));
							} catch(DOMException | IllegalArgumentException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						} else {
							throw new RuntimeException("unsupported type");
						}
						found = true;
						break;
					}
				}
				if(!found) {
					throw new RuntimeException("no field found for element '" + a.tag() + "' (case insensitive)");
				}

			}
		}
	}

	public static void main(String[] atrgs) throws IOException {
		for(UIScale s : values()) {
			Field[] fs = s.getClass().getFields();
			for(Field f : fs) {
				f.setAccessible(true);
				if(f.getType() == Integer.TYPE) {
					try {
						f.setInt(s, s.scale(f.getInt(s)));
					} catch(IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		//write default
		write();
	}
}
