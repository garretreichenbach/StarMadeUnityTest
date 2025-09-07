package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class MovingByFootCrawling extends AnimationStructSet {

	public final MovingByFootCrawlingNorth movingByFootCrawlingNorth = new MovingByFootCrawlingNorth();
	public final MovingByFootCrawlingSouth movingByFootCrawlingSouth = new MovingByFootCrawlingSouth();
	public final MovingByFootCrawlingWest movingByFootCrawlingWest = new MovingByFootCrawlingWest();
	public final MovingByFootCrawlingEast movingByFootCrawlingEast = new MovingByFootCrawlingEast();
	public final MovingByFootCrawlingNorthEast movingByFootCrawlingNorthEast = new MovingByFootCrawlingNorthEast();
	public final MovingByFootCrawlingNorthWest movingByFootCrawlingNorthWest = new MovingByFootCrawlingNorthWest();
	public final MovingByFootCrawlingSouthWest movingByFootCrawlingSouthWest = new MovingByFootCrawlingSouthWest();
	public final MovingByFootCrawlingSouthEast movingByFootCrawlingSouthEast = new MovingByFootCrawlingSouthEast();

	@Override
	public void checkAnimations(String def) {
		if (!movingByFootCrawlingNorth.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootCrawlingNorth");
			}
			movingByFootCrawlingNorth.parse(null, def, this);
		}
		children.add(movingByFootCrawlingNorth);

		if (!movingByFootCrawlingSouth.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootCrawlingSouth");
			}
			movingByFootCrawlingSouth.parse(null, def, this);
		}
		children.add(movingByFootCrawlingSouth);

		if (!movingByFootCrawlingWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootCrawlingWest");
			}
			movingByFootCrawlingWest.parse(null, def, this);
		}
		children.add(movingByFootCrawlingWest);

		if (!movingByFootCrawlingEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootCrawlingEast");
			}
			movingByFootCrawlingEast.parse(null, def, this);
		}
		children.add(movingByFootCrawlingEast);

		if (!movingByFootCrawlingNorthEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootCrawlingNorthEast");
			}
			movingByFootCrawlingNorthEast.parse(null, def, this);
		}
		children.add(movingByFootCrawlingNorthEast);

		if (!movingByFootCrawlingNorthWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootCrawlingNorthWest");
			}
			movingByFootCrawlingNorthWest.parse(null, def, this);
		}
		children.add(movingByFootCrawlingNorthWest);

		if (!movingByFootCrawlingSouthWest.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootCrawlingSouthWest");
			}
			movingByFootCrawlingSouthWest.parse(null, def, this);
		}
		children.add(movingByFootCrawlingSouthWest);

		if (!movingByFootCrawlingSouthEast.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootCrawlingSouthEast");
			}
			movingByFootCrawlingSouthEast.parse(null, def, this);
		}
		children.add(movingByFootCrawlingSouthEast);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("north")) {
				movingByFootCrawlingNorth.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("south")) {
				movingByFootCrawlingSouth.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("west")) {
				movingByFootCrawlingWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("east")) {
				movingByFootCrawlingEast.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("northeast")) {
				movingByFootCrawlingNorthEast.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("northwest")) {
				movingByFootCrawlingNorthWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("southwest")) {
				movingByFootCrawlingSouthWest.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("southeast")) {
				movingByFootCrawlingSouthEast.parse(node, def, this);
			}
		}

	}

}