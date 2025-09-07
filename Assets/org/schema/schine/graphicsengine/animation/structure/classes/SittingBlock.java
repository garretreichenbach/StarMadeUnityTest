package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class SittingBlock extends AnimationStructSet {

	public final SittingBlockNoFloor sittingBlockNoFloor = new SittingBlockNoFloor();
	public final SittingBlockFloor sittingBlockFloor = new SittingBlockFloor();

	@Override
	public void checkAnimations(String def) {
		if (!sittingBlockNoFloor.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingBlockNoFloor");
			}
			sittingBlockNoFloor.parse(null, def, this);
		}
		children.add(sittingBlockNoFloor);

		if (!sittingBlockFloor.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingBlockFloor");
			}
			sittingBlockFloor.parse(null, def, this);
		}
		children.add(sittingBlockFloor);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("nofloor")) {
				sittingBlockNoFloor.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floor")) {
				sittingBlockFloor.parse(node, def, this);
			}
		}

	}

}