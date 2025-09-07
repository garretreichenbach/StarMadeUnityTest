package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class MovingByFootRunning extends AnimationStructSet {

	public final MovingByFootRunningNorth movingByFootRunningNorth = new MovingByFootRunningNorth();
	public final MovingByFootRunningSouth movingByFootRunningSouth = new MovingByFootRunningSouth();
	public final MovingByFootRunningWest movingByFootRunningWest = new MovingByFootRunningWest();
	public final MovingByFootRunningEast movingByFootRunningEast = new MovingByFootRunningEast();
	public final MovingByFootRunningNorthEast movingByFootRunningNorthEast = new MovingByFootRunningNorthEast();
	public final MovingByFootRunningNorthWest movingByFootRunningNorthWest = new MovingByFootRunningNorthWest();
	public final MovingByFootRunningSouthWest movingByFootRunningSouthWest = new MovingByFootRunningSouthWest();
	public final MovingByFootRunningSouthEast movingByFootRunningSouthEast = new MovingByFootRunningSouthEast();

	@Override
	public void checkAnimations(String def) {
		if (!movingByFootRunningNorth.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootRunningNorth");
			}
			movingByFootRunningNorth.parse(null, def, this);
		}
		children.add(movingByFootRunningNorth);

		if (!movingByFootRunningSouth.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootRunningSouth");
			}
			movingByFootRunningSouth.parse(null, def, this);
		}
		children.add(movingByFootRunningSouth);

		if (!movingByFootRunningWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootRunningWest");
			}
			movingByFootRunningWest.parse(null, def, this);
		}
		children.add(movingByFootRunningWest);

		if (!movingByFootRunningEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootRunningEast");
			}
			movingByFootRunningEast.parse(null, def, this);
		}
		children.add(movingByFootRunningEast);

		if (!movingByFootRunningNorthEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootRunningNorthEast");
			}
			movingByFootRunningNorthEast.parse(null, def, this);
		}
		children.add(movingByFootRunningNorthEast);

		if (!movingByFootRunningNorthWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootRunningNorthWest");
			}
			movingByFootRunningNorthWest.parse(null, def, this);
		}
		children.add(movingByFootRunningNorthWest);

		if (!movingByFootRunningSouthWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootRunningSouthWest");
			}
			movingByFootRunningSouthWest.parse(null, def, this);
		}
		children.add(movingByFootRunningSouthWest);

		if (!movingByFootRunningSouthEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootRunningSouthEast");
			}
			movingByFootRunningSouthEast.parse(null, def, this);
		}
		children.add(movingByFootRunningSouthEast);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("north")) {
				movingByFootRunningNorth.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("south")) {
				movingByFootRunningSouth.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("west")) {
				movingByFootRunningWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("east")) {
				movingByFootRunningEast.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("northeast")) {
				movingByFootRunningNorthEast.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("northwest")) {
				movingByFootRunningNorthWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("southwest")) {
				movingByFootRunningSouthWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("southeast")) {
				movingByFootRunningSouthEast.parse(node, def, this);
			}
		}

	}

}