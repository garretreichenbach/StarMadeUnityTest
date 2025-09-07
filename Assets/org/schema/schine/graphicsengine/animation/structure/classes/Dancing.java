package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Dancing extends AnimationStructSet {

	public final DancingGravity dancingGravity = new DancingGravity();

	@Override
	public void checkAnimations(String def) {
		if (!dancingGravity.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: dancingGravity");
			}
			dancingGravity.parse(null, def, this);
		}
		children.add(dancingGravity);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("gravity")) {
				dancingGravity.parse(node, def, this);
			}
		}

	}

}