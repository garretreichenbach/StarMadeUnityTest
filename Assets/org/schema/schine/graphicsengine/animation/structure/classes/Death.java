package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Death extends AnimationStructSet {

	public final DeathFloating deathFloating = new DeathFloating();
	public final DeathGravity deathGravity = new DeathGravity();

	@Override
	public void checkAnimations(String def) {
		if (!deathFloating.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: deathFloating");
			}
			deathFloating.parse(null, def, this);
		}
		children.add(deathFloating);

		if (!deathGravity.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: deathGravity");
			}
			deathGravity.parse(null, def, this);
		}
		children.add(deathGravity);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floating")) {
				deathFloating.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("gravity")) {
				deathGravity.parse(node, def, this);
			}
		}

	}

}