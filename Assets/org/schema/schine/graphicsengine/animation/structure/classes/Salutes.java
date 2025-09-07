package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.Locale;

import org.w3c.dom.Node;

public class Salutes extends AnimationStructSet {

	public final SalutesSalute salutesSalute = new SalutesSalute();

	@Override
	public void checkAnimations(String def) {
		if (!salutesSalute.parsed) {
			if (AnimationStructSet.DEBUG) {
				System.err.println("[PARSER] not parsed: salutesSalute");
			}
			salutesSalute.parse(null, def, this);
		}
		children.add(salutesSalute);

	}

	@Override
	public void parseAnimation(Node node, String def) {
		if (node != null) {

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals("salute")) {
				salutesSalute.parse(node, def, this);
			}
		}

	}

}