package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class MovingFalling extends AnimationStructSet {

	public final MovingFallingStandard movingFallingStandard = new MovingFallingStandard();
	public final MovingFallingLedge movingFallingLedge = new MovingFallingLedge();

	@Override
	public void checkAnimations(String def) {
		if (!movingFallingStandard.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingFallingStandard");
			}
			movingFallingStandard.parse(null, def, this);
		}
		children.add(movingFallingStandard);

		if (!movingFallingLedge.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingFallingLedge");
			}
			movingFallingLedge.parse(null, def, this);
		}
		children.add(movingFallingLedge);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("standard")) {
				movingFallingStandard.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("ledge")) {
				movingFallingLedge.parse(node, def, this);
			}
		}

	}

}