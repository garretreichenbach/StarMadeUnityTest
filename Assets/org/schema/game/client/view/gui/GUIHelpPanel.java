package org.schema.game.client.view.gui;

import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIHelpPanel extends GUIAnchor {

	private final String title;

	private ObjectArrayList<String> keysS = new ObjectArrayList<String>();
	private ObjectArrayList<String> valuesS = new ObjectArrayList<String>();
	private ObjectArrayList<GUITextOverlay> keys = new ObjectArrayList<GUITextOverlay>();
	private ObjectArrayList<GUITextOverlay> values = new ObjectArrayList<GUITextOverlay>();

	private int valueSpacing;

	private int rows;

	private int columnDist;

	private String originalTitle;

	public static final Object2ObjectOpenHashMap<String, String> translations = new Object2ObjectOpenHashMap<String, String>();
	
	
	private static String getTranslation(String s){
		String t = translations.get(s);
		if(t != null){
			return t;
		}else{
			return s;
		}
	}
	
	public GUIHelpPanel(InputState state, Node node) {
		super(state);
		this.originalTitle = node.getNodeName();
		this.title = getTranslation(originalTitle);
		assert (node.getAttributes().getNamedItem("width") != null) : node.getNodeName() + ": " + node.getAttributes();
		this.width = Integer.parseInt(node.getAttributes().getNamedItem("width").getNodeValue());
		this.height = Integer.parseInt(node.getAttributes().getNamedItem("height").getNodeValue());
		this.valueSpacing = Integer.parseInt(node.getAttributes().getNamedItem("valueSpacing").getNodeValue());
		this.rows = Integer.parseInt(node.getAttributes().getNamedItem("rows").getNodeValue());
		this.columnDist = Integer.parseInt(node.getAttributes().getNamedItem("columnDist").getNodeValue());
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				String key = StringTools.cpt(getTranslation(child.getNodeName()));
				keysS.add(key);
				valuesS.add(child.getTextContent());
			}
		}
		update(state);
	}

	public void update(InputState state) {
		detachAll();
		keys.clear();
		values.clear();

		assert (keysS.size() == valuesS.size());
		for (int i = 0; i < keysS.size(); ++i) {
			String value = KeyboardMappings.formatText(valuesS.get(i));
			GUITextOverlay k = new GUITextOverlay(FontSize.SMALL_14, state);
			GUITextOverlay v = new GUITextOverlay(FontSize.SMALL_14, state);
			k.setTextSimple(keysS.get(i) + ":");
			v.setTextSimple(value);
			keys.add(k);
			values.add(v);
		}

		GUIColoredRectangle r = new GUIColoredRectangle(state, (int)this.width, (int)this.height, new Vector4f(0.25f, 0.16f, 0.8f, 0.75f));
		r.rounded = 4;
		attach(r);

		int ySpacing = 15;
		for (int i = 0; i < keys.size(); i++) {
			GUITextOverlay key = keys.get(i);
			GUITextOverlay value = values.get(i);

			int column = i / rows;
			int row = i % rows;

			int x = column * columnDist;
			int y = row * ySpacing;

			key.setPos(x, y, 0);
			value.setPos(x + valueSpacing, y, 0);
			r.attach(key);
			r.attach(value);
		}
		GUITextOverlay tit = new GUITextOverlay(FontSize.SMALL_14, state);
		tit.setTextSimple(title.equals("General") ? title + " (hide by pressing '" + KeyboardMappings.HELP_SCREEN.getKeyChar() + "' or in game settings)" : title);
		tit.getPos().y = -15;
		r.attach(tit);
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	public String getOriginalTitle() {
		return originalTitle;
	}

}
