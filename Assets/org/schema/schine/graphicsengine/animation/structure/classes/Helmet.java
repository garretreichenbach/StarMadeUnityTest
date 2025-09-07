package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Helmet extends AnimationStructSet {

	public final HelmetOn helmetOn = new HelmetOn();
	public final HelmetOff helmetOff = new HelmetOff();

	@Override
	public void checkAnimations(String def) {
		if (!helmetOn.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: helmetOn");
			}
			helmetOn.parse(null, def, this);
		}
		children.add(helmetOn);

		if (!helmetOff.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: helmetOff");
			}
			helmetOff.parse(null, def, this);
		}
		children.add(helmetOff);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("on")) {
				helmetOn.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("off")) {
				helmetOff.parse(node, def, this);
			}
		}

	}

}