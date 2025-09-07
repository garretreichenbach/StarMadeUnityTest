package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class SittingBlockNoFloor extends AnimationStructSet {

	public final SittingBlockNoFloorIdle sittingBlockNoFloorIdle = new SittingBlockNoFloorIdle();

	@Override
	public void checkAnimations(String def) {
		if (!sittingBlockNoFloorIdle.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingBlockNoFloorIdle");
			}
			sittingBlockNoFloorIdle.parse(null, def, this);
		}
		children.add(sittingBlockNoFloorIdle);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("idle")) {
				sittingBlockNoFloorIdle.parse(node, def, this);
			}
		}

	}

}