package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class AttackingMelee extends AnimationStructSet {

	public final AttackingMeleeFloating attackingMeleeFloating = new AttackingMeleeFloating();
	public final AttackingMeleeGravity attackingMeleeGravity = new AttackingMeleeGravity();

	@Override
	public void checkAnimations(String def) {
		if (!attackingMeleeFloating.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: attackingMeleeFloating");
			}
			attackingMeleeFloating.parse(null, def, this);
		}
		children.add(attackingMeleeFloating);

		if (!attackingMeleeGravity.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: attackingMeleeGravity");
			}
			attackingMeleeGravity.parse(null, def, this);
		}
		children.add(attackingMeleeGravity);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floating")) {
				attackingMeleeFloating.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("gravity")) {
				attackingMeleeGravity.parse(node, def, this);
			}
		}

	}

}