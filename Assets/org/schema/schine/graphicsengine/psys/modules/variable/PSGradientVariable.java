package org.schema.schine.graphicsengine.psys.modules.variable;

import java.awt.Color;
import java.util.Locale;
import java.util.Map.Entry;

import javax.vecmath.Vector4f;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.floats.Float2ObjectRBTreeMap;

public abstract class PSGradientVariable implements PSVariable<Vector4f> {
	public Float2ObjectRBTreeMap<Color> color = new Float2ObjectRBTreeMap<Color>();

	public Vector4f c = new Vector4f();

	public PSGradientVariable() {
		super();
		init();
	}

	private static void parseColor(Node cp, PSGradientVariable p) {
		float percent = Float.parseFloat(cp.getAttributes().getNamedItem("percent").getNodeValue());
		NodeList childNodes = cp.getChildNodes();
		int red = 0;
		int green = 0;
		int blue = 0;
		int alpha = 0;
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {

				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("red")) {
					red = Integer.parseInt(item.getTextContent());
				}
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("green")) {
					green = Integer.parseInt(item.getTextContent());
				}
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("blue")) {
					blue = Integer.parseInt(item.getTextContent());
				}
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("alpha")) {
					alpha = Integer.parseInt(item.getTextContent());
				}

			}
		}
		p.color.put(percent, new Color(red, green, blue, alpha));
	}

	public void init() {
		color.put(0, Color.RED);
		color.put(1, Color.BLUE);
	}

	@Override
	public Vector4f get(float lifetime) {
		if (color.containsKey(lifetime)) {
			Color col = color.get(lifetime);
			c.set(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, col.getAlpha() / 255f);
			return c;
		}

		float fkA = 0;
		float fkB = 1;
		for (it.unimi.dsi.fastutil.floats.Float2ObjectMap.Entry<Color> a : color.float2ObjectEntrySet()) {
			if (a.getFloatKey() >= lifetime) {
				fkB = a.getFloatKey();
				break;
			} else {
				fkA = a.getFloatKey();
			}
		}

		Color beginColor = color.get(fkA);
		Color endColor = color.get(fkB);

		float dist = fkB - fkA;

		if (dist == 0.0f) {
			c.set(beginColor.getRed() / 255f, beginColor.getGreen() / 255f, beginColor.getBlue() / 255f, beginColor.getAlpha() / 255f);
			return c;
		}

		float percent = lifetime / dist;

		float red = beginColor.getRed()
				+ percent * (endColor.getRed() - beginColor.getRed());
		float green = beginColor.getGreen()
				+ percent * (endColor.getGreen() - beginColor.getGreen());
		float blue = beginColor.getBlue()
				+ percent * (endColor.getBlue() - beginColor.getBlue());
		float alpha = beginColor.getAlpha()
				+ percent * (endColor.getAlpha() - beginColor.getAlpha());

		c.set(red / 255f, green / 255f, blue / 255f, alpha / 255f);
		return c;

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.variable.PSVariable#set(org.schema.schine.graphicsengine.psys.modules.variable.PSVariable)
	 */
	@Override
	public void set(PSVariable<Vector4f> var) {
		color.clear();
		color.putAll(((PSGradientVariable) var).color);
	}

	@Override
	public void appendXML(Object m, Element element) {
		Element root = element.getOwnerDocument().createElement("Gradient");
		for (Entry<Float, Color> p : color.entrySet()) {
			Element point = element.getOwnerDocument().createElement("Color");
			point.setAttribute("percent", String.valueOf(p.getKey()));
			Element r = element.getOwnerDocument().createElement("red");
			Element g = element.getOwnerDocument().createElement("green");
			Element b = element.getOwnerDocument().createElement("blue");
			Element a = element.getOwnerDocument().createElement("alpha");
			root.appendChild(point);
			point.appendChild(r);
			point.appendChild(g);
			point.appendChild(b);
			point.appendChild(a);

			r.setTextContent(String.valueOf(p.getValue().getRed()));
			g.setTextContent(String.valueOf(p.getValue().getGreen()));
			b.setTextContent(String.valueOf(p.getValue().getBlue()));
			a.setTextContent(String.valueOf(p.getValue().getAlpha()));

		}
		element.appendChild(root);

	}

	@Override
	public void parse(Node r) {
		color.clear();
		NodeList childNodes = r.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("gradient")) {

					parseGradient(item, this);
				}

			}
		}

		if (color.size() < 2) {
			throw new IllegalArgumentException("Color gradient cannot be made from less then 2 colors");
		}
	}

	public abstract String getName();

	private void parseGradient(Node r, PSGradientVariable psGradientVariable) {
		NodeList childNodes = r.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("color")) {
					parseColor(item, this);
				}

			}
		}
	}
}
