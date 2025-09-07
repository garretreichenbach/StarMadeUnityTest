package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class UpperBodyGun extends AnimationStructSet {

	public final UpperBodyGunIdle upperBodyGunIdle = new UpperBodyGunIdle();
	public final UpperBodyGunIdleIn upperBodyGunIdleIn = new UpperBodyGunIdleIn();
	public final UpperBodyGunDraw upperBodyGunDraw = new UpperBodyGunDraw();
	public final UpperBodyGunAway upperBodyGunAway = new UpperBodyGunAway();
	public final UpperBodyGunFire upperBodyGunFire = new UpperBodyGunFire();
	public final UpperBodyGunFireHeavy upperBodyGunFireHeavy = new UpperBodyGunFireHeavy();
	public final UpperBodyGunMelee upperBodyGunMelee = new UpperBodyGunMelee();

	@Override
	public void checkAnimations(String def) {
		if (!upperBodyGunIdle.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyGunIdle");
			}
			upperBodyGunIdle.parse(null, def, this);
		}
		children.add(upperBodyGunIdle);

		if (!upperBodyGunIdleIn.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyGunIdleIn");
			}
			upperBodyGunIdleIn.parse(null, def, this);
		}
		children.add(upperBodyGunIdleIn);

		if (!upperBodyGunDraw.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyGunDraw");
			}
			upperBodyGunDraw.parse(null, def, this);
		}
		children.add(upperBodyGunDraw);

		if (!upperBodyGunAway.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyGunAway");
			}
			upperBodyGunAway.parse(null, def, this);
		}
		children.add(upperBodyGunAway);

		if (!upperBodyGunFire.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyGunFire");
			}
			upperBodyGunFire.parse(null, def, this);
		}
		children.add(upperBodyGunFire);

		if (!upperBodyGunFireHeavy.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyGunFireHeavy");
			}
			upperBodyGunFireHeavy.parse(null, def, this);
		}
		children.add(upperBodyGunFireHeavy);

		if (!upperBodyGunMelee.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyGunMelee");
			}
			upperBodyGunMelee.parse(null, def, this);
		}
		children.add(upperBodyGunMelee);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("idle")) {
				upperBodyGunIdle.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("idlein")) {
				upperBodyGunIdleIn.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("draw")) {
				upperBodyGunDraw.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("away")) {
				upperBodyGunAway.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("fire")) {
				upperBodyGunFire.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("fireheavy")) {
				upperBodyGunFireHeavy.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("meelee")) {
				upperBodyGunMelee.parse(node, def, this);
			}
		}

	}

}