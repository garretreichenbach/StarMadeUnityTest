package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Moving extends AnimationStructSet {

	public final MovingJumping movingJumping = new MovingJumping();
	public final MovingFalling movingFalling = new MovingFalling();
	public final MovingLanding movingLanding = new MovingLanding();
	public final MovingNoGravity movingNoGravity = new MovingNoGravity();
	public final MovingByFoot movingByFoot = new MovingByFoot();

	@Override
	public void checkAnimations(String def) {
		if (!movingJumping.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingJumping");
			}
			movingJumping.parse(null, def, this);
		}
		children.add(movingJumping);

		if (!movingFalling.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingFalling");
			}
			movingFalling.parse(null, def, this);
		}
		children.add(movingFalling);

		if (!movingLanding.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingLanding");
			}
			movingLanding.parse(null, def, this);
		}
		children.add(movingLanding);

		if (!movingNoGravity.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravity");
			}
			movingNoGravity.parse(null, def, this);
		}
		children.add(movingNoGravity);

		if (!movingByFoot.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFoot");
			}
			movingByFoot.parse(null, def, this);
		}
		children.add(movingByFoot);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("jumping")) {
				movingJumping.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("falling")) {
				movingFalling.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("landing")) {
				movingLanding.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("nogravity")) {
				movingNoGravity.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("byfoot")) {
				movingByFoot.parse(node, def, this);
			}
		}

	}

}