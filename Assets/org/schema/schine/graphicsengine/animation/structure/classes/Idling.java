package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Idling extends AnimationStructSet {

	public final IdlingFloating idlingFloating = new IdlingFloating();
	public final IdlingGravity idlingGravity = new IdlingGravity();

	@Override
	public void checkAnimations(String def) {
		if (!idlingFloating.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: idlingFloating");
			}
			idlingFloating.parse(null, def, this);
		}
		children.add(idlingFloating);

		if (!idlingGravity.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: idlingGravity");
			}
			idlingGravity.parse(null, def, this);
		}
		children.add(idlingGravity);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floating")) {
				idlingFloating.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("gravity")) {
				idlingGravity.parse(node, def, this);
			}
		}

	}

}