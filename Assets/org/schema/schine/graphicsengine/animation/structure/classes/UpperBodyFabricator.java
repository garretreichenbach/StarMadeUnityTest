package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class UpperBodyFabricator extends AnimationStructSet {

	public final UpperBodyFabricatorIdle upperBodyFabricatorIdle = new UpperBodyFabricatorIdle();
	public final UpperBodyFabricatorDraw upperBodyFabricatorDraw = new UpperBodyFabricatorDraw();
	public final UpperBodyFabricatorAway upperBodyFabricatorAway = new UpperBodyFabricatorAway();
	public final UpperBodyFabricatorFire upperBodyFabricatorFire = new UpperBodyFabricatorFire();
	public final UpperBodyFabricatorPumpAction upperBodyFabricatorPumpAction = new UpperBodyFabricatorPumpAction();

	@Override
	public void checkAnimations(String def) {
		if (!upperBodyFabricatorIdle.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyFabricatorIdle");
			}
			upperBodyFabricatorIdle.parse(null, def, this);
		}
		children.add(upperBodyFabricatorIdle);

		if (!upperBodyFabricatorDraw.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyFabricatorDraw");
			}
			upperBodyFabricatorDraw.parse(null, def, this);
		}
		children.add(upperBodyFabricatorDraw);

		if (!upperBodyFabricatorAway.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyFabricatorAway");
			}
			upperBodyFabricatorAway.parse(null, def, this);
		}
		children.add(upperBodyFabricatorAway);

		if (!upperBodyFabricatorFire.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyFabricatorFire");
			}
			upperBodyFabricatorFire.parse(null, def, this);
		}
		children.add(upperBodyFabricatorFire);

		if (!upperBodyFabricatorPumpAction.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBodyFabricatorPumpAction");
			}
			upperBodyFabricatorPumpAction.parse(null, def, this);
		}
		children.add(upperBodyFabricatorPumpAction);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("idle")) {
				upperBodyFabricatorIdle.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("draw")) {
				upperBodyFabricatorDraw.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("away")) {
				upperBodyFabricatorAway.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("fire")) {
				upperBodyFabricatorFire.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("pumpaction")) {
				upperBodyFabricatorPumpAction.parse(node, def, this);
			}
		}

	}

}