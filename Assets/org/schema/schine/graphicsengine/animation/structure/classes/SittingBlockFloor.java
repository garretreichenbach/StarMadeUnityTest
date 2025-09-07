package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class SittingBlockFloor extends AnimationStructSet {

	public final SittingBlockFloorIdle sittingBlockFloorIdle = new SittingBlockFloorIdle();

	@Override
	public void checkAnimations(String def) {
		if (!sittingBlockFloorIdle.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingBlockFloorIdle");
			}
			sittingBlockFloorIdle.parse(null, def, this);
		}
		children.add(sittingBlockFloorIdle);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("idle")) {
				sittingBlockFloorIdle.parse(node, def, this);
			}
		}

	}

}