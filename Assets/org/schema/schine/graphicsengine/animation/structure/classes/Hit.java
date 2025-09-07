package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Hit extends AnimationStructSet {

	public final HitSmall hitSmall = new HitSmall();

	@Override
	public void checkAnimations(String def) {
		if (!hitSmall.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: hitSmall");
			}
			hitSmall.parse(null, def, this);
		}
		children.add(hitSmall);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("small")) {
				hitSmall.parse(node, def, this);
			}
		}

	}

}