package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class SittingWedgeNoFloor extends AnimationStructSet {

	public final SittingWedgeNoFloorIdle sittingWedgeNoFloorIdle = new SittingWedgeNoFloorIdle();

	@Override
	public void checkAnimations(String def) {
		if (!sittingWedgeNoFloorIdle.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingWedgeNoFloorIdle");
			}
			sittingWedgeNoFloorIdle.parse(null, def, this);
		}
		children.add(sittingWedgeNoFloorIdle);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("idle")) {
				sittingWedgeNoFloorIdle.parse(node, def, this);
			}
		}

	}

}