package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Talk extends AnimationStructSet {

	public final TalkSalute talkSalute = new TalkSalute();

	@Override
	public void checkAnimations(String def) {
		if (!talkSalute.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: talkSalute");
			}
			talkSalute.parse(null, def, this);
		}
		children.add(talkSalute);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("salute")) {
				talkSalute.parse(node, def, this);
			}
		}

	}

}