package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class SittingWedge extends AnimationStructSet {

	public final SittingWedgeNoFloor sittingWedgeNoFloor = new SittingWedgeNoFloor();
	public final SittingWedgeFloor sittingWedgeFloor = new SittingWedgeFloor();

	@Override
	public void checkAnimations(String def) {
		if (!sittingWedgeNoFloor.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingWedgeNoFloor");
			}
			sittingWedgeNoFloor.parse(null, def, this);
		}
		children.add(sittingWedgeNoFloor);

		if (!sittingWedgeFloor.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingWedgeFloor");
			}
			sittingWedgeFloor.parse(null, def, this);
		}
		children.add(sittingWedgeFloor);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("nofloor")) {
				sittingWedgeNoFloor.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floor")) {
				sittingWedgeFloor.parse(node, def, this);
			}
		}

	}

}