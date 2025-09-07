package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class MovingByFootWalking extends AnimationStructSet {

	public final MovingByFootWalkingNorth movingByFootWalkingNorth = new MovingByFootWalkingNorth();
	public final MovingByFootWalkingSouth movingByFootWalkingSouth = new MovingByFootWalkingSouth();
	public final MovingByFootWalkingWest movingByFootWalkingWest = new MovingByFootWalkingWest();
	public final MovingByFootWalkingEast movingByFootWalkingEast = new MovingByFootWalkingEast();
	public final MovingByFootWalkingNorthEast movingByFootWalkingNorthEast = new MovingByFootWalkingNorthEast();
	public final MovingByFootWalkingNorthWest movingByFootWalkingNorthWest = new MovingByFootWalkingNorthWest();
	public final MovingByFootWalkingSouthWest movingByFootWalkingSouthWest = new MovingByFootWalkingSouthWest();
	public final MovingByFootWalkingSouthEast movingByFootWalkingSouthEast = new MovingByFootWalkingSouthEast();

	@Override
	public void checkAnimations(String def) {
		if (!movingByFootWalkingNorth.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootWalkingNorth");
			}
			movingByFootWalkingNorth.parse(null, def, this);
		}
		children.add(movingByFootWalkingNorth);

		if (!movingByFootWalkingSouth.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootWalkingSouth");
			}
			movingByFootWalkingSouth.parse(null, def, this);
		}
		children.add(movingByFootWalkingSouth);

		if (!movingByFootWalkingWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootWalkingWest");
			}
			movingByFootWalkingWest.parse(null, def, this);
		}
		children.add(movingByFootWalkingWest);

		if (!movingByFootWalkingEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootWalkingEast");
			}
			movingByFootWalkingEast.parse(null, def, this);
		}
		children.add(movingByFootWalkingEast);

		if (!movingByFootWalkingNorthEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootWalkingNorthEast");
			}
			movingByFootWalkingNorthEast.parse(null, def, this);
		}
		children.add(movingByFootWalkingNorthEast);

		if (!movingByFootWalkingNorthWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootWalkingNorthWest");
			}
			movingByFootWalkingNorthWest.parse(null, def, this);
		}
		children.add(movingByFootWalkingNorthWest);

		if (!movingByFootWalkingSouthWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootWalkingSouthWest");
			}
			movingByFootWalkingSouthWest.parse(null, def, this);
		}
		children.add(movingByFootWalkingSouthWest);

		if (!movingByFootWalkingSouthEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootWalkingSouthEast");
			}
			movingByFootWalkingSouthEast.parse(null, def, this);
		}
		children.add(movingByFootWalkingSouthEast);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("north")) {
				movingByFootWalkingNorth.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("south")) {
				movingByFootWalkingSouth.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("west")) {
				movingByFootWalkingWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("east")) {
				movingByFootWalkingEast.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("northeast")) {
				movingByFootWalkingNorthEast.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("northwest")) {
				movingByFootWalkingNorthWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("southwest")) {
				movingByFootWalkingSouthWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("southeast")) {
				movingByFootWalkingSouthEast.parse(node, def, this);
			}
		}

	}

}