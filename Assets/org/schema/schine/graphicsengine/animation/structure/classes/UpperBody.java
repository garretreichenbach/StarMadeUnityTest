package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class UpperBody extends AnimationStructSet {

	public final UpperBodyGun upperBodyGun = new UpperBodyGun();
	public final UpperBodyFabricator upperBodyFabricator = new UpperBodyFabricator();
	public final UpperBodyBareHand upperBodyBareHand = new UpperBodyBareHand();

	@Override
	public void checkAnimations(String def) {
		if (!upperBodyGun.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyGun");
			}
			upperBodyGun.parse(null, def, this);
		}
		children.add(upperBodyGun);

		if (!upperBodyFabricator.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyFabricator");
			}
			upperBodyFabricator.parse(null, def, this);
		}
		children.add(upperBodyFabricator);

		if (!upperBodyBareHand.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyBareHand");
			}
			upperBodyBareHand.parse(null, def, this);
		}
		children.add(upperBodyBareHand);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("gun")) {
				upperBodyGun.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("fabricator")) {
				upperBodyFabricator.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("barehand")) {
				upperBodyBareHand.parse(node, def, this);
			}
		}

	}

}