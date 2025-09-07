package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class MovingJumping extends AnimationStructSet {

	public final MovingJumpingJumpUp movingJumpingJumpUp = new MovingJumpingJumpUp();
	public final MovingJumpingJumpDown movingJumpingJumpDown = new MovingJumpingJumpDown();

	@Override
	public void checkAnimations(String def) {
		if (!movingJumpingJumpUp.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingJumpingJumpUp");
			}
			movingJumpingJumpUp.parse(null, def, this);
		}
		children.add(movingJumpingJumpUp);

		if (!movingJumpingJumpDown.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingJumpingJumpDown");
			}
			movingJumpingJumpDown.parse(null, def, this);
		}
		children.add(movingJumpingJumpDown);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("jumpup")) {
				movingJumpingJumpUp.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("jumpdown")) {
				movingJumpingJumpDown.parse(node, def, this);
			}
		}

	}

}