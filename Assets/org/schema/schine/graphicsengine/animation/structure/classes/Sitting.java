package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Sitting extends AnimationStructSet {

	public final SittingBlock sittingBlock = new SittingBlock();
	public final SittingWedge sittingWedge = new SittingWedge();

	@Override
	public void checkAnimations(String def) {
		if (!sittingBlock.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingBlock");
			}
			sittingBlock.parse(null, def, this);
		}
		children.add(sittingBlock);

		if (!sittingWedge.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sittingWedge");
			}
			sittingWedge.parse(null, def, this);
		}
		children.add(sittingWedge);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("block")) {
				sittingBlock.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("wedge")) {
				sittingWedge.parse(node, def, this);
			}
		}

	}

}