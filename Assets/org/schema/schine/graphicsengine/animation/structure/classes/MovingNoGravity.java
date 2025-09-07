package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class MovingNoGravity extends AnimationStructSet {

	public final MovingNoGravityGravToNoGrav movingNoGravityGravToNoGrav = new MovingNoGravityGravToNoGrav();
	public final MovingNoGravityNoGravToGrav movingNoGravityNoGravToGrav = new MovingNoGravityNoGravToGrav();
	public final MovingNoGravityFloatMoveN movingNoGravityFloatMoveN = new MovingNoGravityFloatMoveN();
	public final MovingNoGravityFloatMoveS movingNoGravityFloatMoveS = new MovingNoGravityFloatMoveS();
	public final MovingNoGravityFloatMoveUp movingNoGravityFloatMoveUp = new MovingNoGravityFloatMoveUp();
	public final MovingNoGravityFloatMoveDown movingNoGravityFloatMoveDown = new MovingNoGravityFloatMoveDown();
	public final MovingNoGravityFloatRot movingNoGravityFloatRot = new MovingNoGravityFloatRot();
	public final MovingNoGravityFloatHit movingNoGravityFloatHit = new MovingNoGravityFloatHit();
	public final MovingNoGravityFloatDeath movingNoGravityFloatDeath = new MovingNoGravityFloatDeath();

	@Override
	public void checkAnimations(String def) {
		if (!movingNoGravityGravToNoGrav.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravityGravToNoGrav");
			}
			movingNoGravityGravToNoGrav.parse(null, def, this);
		}
		children.add(movingNoGravityGravToNoGrav);

		if (!movingNoGravityNoGravToGrav.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravityNoGravToGrav");
			}
			movingNoGravityNoGravToGrav.parse(null, def, this);
		}
		children.add(movingNoGravityNoGravToGrav);

		if (!movingNoGravityFloatMoveN.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravityFloatMoveN");
			}
			movingNoGravityFloatMoveN.parse(null, def, this);
		}
		children.add(movingNoGravityFloatMoveN);

		if (!movingNoGravityFloatMoveS.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravityFloatMoveS");
			}
			movingNoGravityFloatMoveS.parse(null, def, this);
		}
		children.add(movingNoGravityFloatMoveS);

		if (!movingNoGravityFloatMoveUp.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravityFloatMoveUp");
			}
			movingNoGravityFloatMoveUp.parse(null, def, this);
		}
		children.add(movingNoGravityFloatMoveUp);

		if (!movingNoGravityFloatMoveDown.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravityFloatMoveDown");
			}
			movingNoGravityFloatMoveDown.parse(null, def, this);
		}
		children.add(movingNoGravityFloatMoveDown);

		if (!movingNoGravityFloatRot.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravityFloatRot");
			}
			movingNoGravityFloatRot.parse(null, def, this);
		}
		children.add(movingNoGravityFloatRot);

		if (!movingNoGravityFloatHit.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravityFloatHit");
			}
			movingNoGravityFloatHit.parse(null, def, this);
		}
		children.add(movingNoGravityFloatHit);

		if (!movingNoGravityFloatDeath.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: movingNoGravityFloatDeath");
			}
			movingNoGravityFloatDeath.parse(null, def, this);
		}
		children.add(movingNoGravityFloatDeath);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("gravtonograv")) {
				movingNoGravityGravToNoGrav.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("nogravtograv")) {
				movingNoGravityNoGravToGrav.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floatmoven")) {
				movingNoGravityFloatMoveN.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floatmoves")) {
				movingNoGravityFloatMoveS.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floatmoveup")) {
				movingNoGravityFloatMoveUp.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floatmovedown")) {
				movingNoGravityFloatMoveDown.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floatrot")) {
				movingNoGravityFloatRot.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floathit")) {
				movingNoGravityFloatHit.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("floatdeath")) {
				movingNoGravityFloatDeath.parse(node, def, this);
			}
		}

	}

}