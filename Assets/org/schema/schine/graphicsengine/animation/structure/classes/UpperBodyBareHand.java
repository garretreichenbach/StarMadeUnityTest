package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class UpperBodyBareHand extends AnimationStructSet {

	public final UpperBodyBareHandMelee upperBodyBareHandMelee = new UpperBodyBareHandMelee();

	@Override
	public void checkAnimations(String def) {
		if (!upperBodyBareHandMelee.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyBareHandMelee");
			}
			upperBodyBareHandMelee.parse(null, def, this);
		}
		children.add(upperBodyBareHandMelee);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("meelee")) {
				upperBodyBareHandMelee.parse(node, def, this);
			}
		}

	}

}