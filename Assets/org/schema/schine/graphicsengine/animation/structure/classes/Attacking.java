package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Attacking extends AnimationStructSet {

	public final AttackingMelee attackingMelee = new AttackingMelee();

	@Override
	public void checkAnimations(String def) {
		if (!attackingMelee.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: attackingMelee");
			}
			attackingMelee.parse(null, def, this);
		}
		children.add(attackingMelee);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("meelee")) {
				attackingMelee.parse(node, def, this);
			}
		}

	}

}