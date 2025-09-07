package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class MovingLanding extends AnimationStructSet {

	public final MovingLandingShortFall movingLandingShortFall = new MovingLandingShortFall();
	public final MovingLandingMiddleFall movingLandingMiddleFall = new MovingLandingMiddleFall();
	public final MovingLandingLongFall movingLandingLongFall = new MovingLandingLongFall();

	@Override
	public void checkAnimations(String def) {
		if (!movingLandingShortFall.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingLandingShortFall");
			}
			movingLandingShortFall.parse(null, def, this);
		}
		children.add(movingLandingShortFall);

		if (!movingLandingMiddleFall.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingLandingMiddleFall");
			}
			movingLandingMiddleFall.parse(null, def, this);
		}
		children.add(movingLandingMiddleFall);

		if (!movingLandingLongFall.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingLandingLongFall");
			}
			movingLandingLongFall.parse(null, def, this);
		}
		children.add(movingLandingLongFall);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("shortfall")) {
				movingLandingShortFall.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("middlefall")) {
				movingLandingMiddleFall.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("longfall")) {
				movingLandingLongFall.parse(node, def, this);
			}
		}

	}

}