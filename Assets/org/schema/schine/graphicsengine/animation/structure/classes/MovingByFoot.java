package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class MovingByFoot extends AnimationStructSet {

	public final MovingByFootCrawling movingByFootCrawling = new MovingByFootCrawling();
	public final MovingByFootSlowWalking movingByFootSlowWalking = new MovingByFootSlowWalking();
	public final MovingByFootWalking movingByFootWalking = new MovingByFootWalking();
	public final MovingByFootRunning movingByFootRunning = new MovingByFootRunning();

	@Override
	public void checkAnimations(String def) {
		if (!movingByFootCrawling.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootCrawling");
			}
			movingByFootCrawling.parse(null, def, this);
		}
		children.add(movingByFootCrawling);

		if (!movingByFootSlowWalking.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootSlowWalking");
			}
			movingByFootSlowWalking.parse(null, def, this);
		}
		children.add(movingByFootSlowWalking);

		if (!movingByFootWalking.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootWalking");
			}
			movingByFootWalking.parse(null, def, this);
		}
		children.add(movingByFootWalking);

		if (!movingByFootRunning.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingByFootRunning");
			}
			movingByFootRunning.parse(null, def, this);
		}
		children.add(movingByFootRunning);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("crawling")) {
				movingByFootCrawling.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("slowwalking")) {
				movingByFootSlowWalking.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("walking")) {
				movingByFootWalking.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("running")) {
				movingByFootRunning.parse(node, def, this);
			}
		}

	}

}