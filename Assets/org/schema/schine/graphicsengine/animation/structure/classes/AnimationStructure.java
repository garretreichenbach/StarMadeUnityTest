package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class AnimationStructure extends AnimationStructSet {

	public final Attacking attacking = new Attacking();
	public final Death death = new Death();
	public final Sitting sitting = new Sitting();
	public final Hit hit = new Hit();
	public final Dancing dancing = new Dancing();
	public final Idling idling = new Idling();
	public final Salutes salutes = new Salutes();
	public final Talk talk = new Talk();
	public final Moving moving = new Moving();
	public final Helmet helmet = new Helmet();
	public final UpperBody upperBody = new UpperBody();

	@Override
	public void checkAnimations(String def) {
		if (!attacking.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: attacking");
			}
			attacking.parse(null, def, this);
		}
		children.add(attacking);

		if (!death.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: death");
			}
			death.parse(null, def, this);
		}
		children.add(death);

		if (!sitting.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: sitting");
			}
			sitting.parse(null, def, this);
		}
		children.add(sitting);

		if (!hit.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: hit");
			}
			hit.parse(null, def, this);
		}
		children.add(hit);

		if (!dancing.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: dancing");
			}
			dancing.parse(null, def, this);
		}
		children.add(dancing);

		if (!idling.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: idling");
			}
			idling.parse(null, def, this);
		}
		children.add(idling);

		if (!salutes.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: salutes");
			}
			salutes.parse(null, def, this);
		}
		children.add(salutes);

		if (!talk.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: talk");
			}
			talk.parse(null, def, this);
		}
		children.add(talk);

		if (!moving.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: moving");
			}
			moving.parse(null, def, this);
		}
		children.add(moving);

		if (!helmet.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: helmet");
			}
			helmet.parse(null, def, this);
		}
		children.add(helmet);

		if (!upperBody.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: upperBody");
			}
			upperBody.parse(null, def, this);
		}
		children.add(upperBody);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("attacking")) {
				attacking.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("death")) {
				death.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("sitting")) {
				sitting.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("hit")) {
				hit.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("dancing")) {
				dancing.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("idling")) {
				idling.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("salutes")) {
				salutes.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("talk")) {
				talk.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("moving")) {
				moving.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("helmet")) {
				helmet.parse(node, def, this);
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("upperbody")) {
				upperBody.parse(node, def, this);
			}
		}

	}

}