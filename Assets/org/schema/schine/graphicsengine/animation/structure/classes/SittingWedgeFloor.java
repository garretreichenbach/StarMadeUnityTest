package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class SittingWedgeFloor extends AnimationStructSet {

	public final SittingWedgeFloorIdle sittingWedgeFloorIdle = new SittingWedgeFloorIdle();

	@Override
	public void checkAnimations(String def) {
		if (!sittingWedgeFloorIdle.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingWedgeFloorIdle");
			}
			sittingWedgeFloorIdle.parse(null, def, this);
		}
		children.add(sittingWedgeFloorIdle);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("idle")) {
				sittingWedgeFloorIdle.parse(node, def, this);
			}
		}

	}

}