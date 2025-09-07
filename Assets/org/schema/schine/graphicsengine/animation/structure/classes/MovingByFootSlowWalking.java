package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class MovingByFootSlowWalking extends AnimationStructSet {

	public final MovingByFootSlowWalkingNorth movingByFootSlowWalkingNorth = new MovingByFootSlowWalkingNorth();
	public final MovingByFootSlowWalkingSouth movingByFootSlowWalkingSouth = new MovingByFootSlowWalkingSouth();
	public final MovingByFootSlowWalkingWest movingByFootSlowWalkingWest = new MovingByFootSlowWalkingWest();
	public final MovingByFootSlowWalkingEast movingByFootSlowWalkingEast = new MovingByFootSlowWalkingEast();
	public final MovingByFootSlowWalkingNorthEast movingByFootSlowWalkingNorthEast = new MovingByFootSlowWalkingNorthEast();
	public final MovingByFootSlowWalkingNorthWest movingByFootSlowWalkingNorthWest = new MovingByFootSlowWalkingNorthWest();
	public final MovingByFootSlowWalkingSouthWest movingByFootSlowWalkingSouthWest = new MovingByFootSlowWalkingSouthWest();
	public final MovingByFootSlowWalkingSouthEast movingByFootSlowWalkingSouthEast = new MovingByFootSlowWalkingSouthEast();

	@Override
	public void checkAnimations(String def) {
		if (!movingByFootSlowWalkingNorth.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootSlowWalkingNorth");
			}
			movingByFootSlowWalkingNorth.parse(null, def, this);
		}
		children.add(movingByFootSlowWalkingNorth);

		if (!movingByFootSlowWalkingSouth.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootSlowWalkingSouth");
			}
			movingByFootSlowWalkingSouth.parse(null, def, this);
		}
		children.add(movingByFootSlowWalkingSouth);

		if (!movingByFootSlowWalkingWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootSlowWalkingWest");
			}
			movingByFootSlowWalkingWest.parse(null, def, this);
		}
		children.add(movingByFootSlowWalkingWest);

		if (!movingByFootSlowWalkingEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootSlowWalkingEast");
			}
			movingByFootSlowWalkingEast.parse(null, def, this);
		}
		children.add(movingByFootSlowWalkingEast);

		if (!movingByFootSlowWalkingNorthEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootSlowWalkingNorthEast");
			}
			movingByFootSlowWalkingNorthEast.parse(null, def, this);
		}
		children.add(movingByFootSlowWalkingNorthEast);

		if (!movingByFootSlowWalkingNorthWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootSlowWalkingNorthWest");
			}
			movingByFootSlowWalkingNorthWest.parse(null, def, this);
		}
		children.add(movingByFootSlowWalkingNorthWest);

		if (!movingByFootSlowWalkingSouthWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootSlowWalkingSouthWest");
			}
			movingByFootSlowWalkingSouthWest.parse(null, def, this);
		}
		children.add(movingByFootSlowWalkingSouthWest);

		if (!movingByFootSlowWalkingSouthEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootSlowWalkingSouthEast");
			}
			movingByFootSlowWalkingSouthEast.parse(null, def, this);
		}
		children.add(movingByFootSlowWalkingSouthEast);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("north")) {
				movingByFootSlowWalkingNorth.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("south")) {
				movingByFootSlowWalkingSouth.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("west")) {
				movingByFootSlowWalkingWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("east")) {
				movingByFootSlowWalkingEast.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("northeast")) {
				movingByFootSlowWalkingNorthEast.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("northwest")) {
				movingByFootSlowWalkingNorthWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("southwest")) {
				movingByFootSlowWalkingSouthWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("southeast")) {
				movingByFootSlowWalkingSouthEast.parse(node, def, this);
			}
		}

	}

}