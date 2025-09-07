package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class HitSmall extends AnimationStructSet {

	public final HitSmallFloating hitSmallFloating = new HitSmallFloating();
	public final HitSmallGravity hitSmallGravity = new HitSmallGravity();

	@Override
	public void checkAnimations(String def) {
		if (!hitSmallFloating.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: hitSmallFloating");
			}
			hitSmallFloating.parse(null, def, this);
		}
		children.add(hitSmallFloating);

		if (!hitSmallGravity.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: hitSmallGravity");
			}
			hitSmallGravity.parse(null, def, this);
		}
		children.add(hitSmallGravity);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floating")) {
				hitSmallFloating.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("gravity")) {
				hitSmallGravity.parse(node, def, this);
			}
		}

	}

}