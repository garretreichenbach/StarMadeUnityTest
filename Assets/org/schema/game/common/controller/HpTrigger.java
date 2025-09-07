package org.schema.game.common.controller;

import java.util.Arrays;
import java.util.Locale;

import org.schema.common.config.ConfigParserException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HpTrigger {
	public float amount = -1;
	public HpTriggerType type;

	public static HpTrigger parse(Node n) throws ConfigParserException {

		HpTrigger t = new HpTrigger();

		NodeList childNodes = n.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);

			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("type")) {
					t.type = HpTriggerType.valueOf(child.getTextContent().toUpperCase(Locale.ENGLISH));
					if (t.type == null) {
						throw new ConfigParserException(child.getParentNode().getNodeName() + " -> " + child.getNodeName() + "; trigger type not found '" + child.getTextContent().toUpperCase(Locale.ENGLISH) + "'. Must be one of " + Arrays.toString(HpTriggerType.values()));
					}
				} else if (child.getNodeName().toLowerCase(Locale.ENGLISH).equals("amount")) {
					t.amount = Float.parseFloat(child.getTextContent());
				} else {
					throw new ConfigParserException(child.getParentNode().getNodeName() + " -> " + child.getNodeName() + "; invalid node: " + child.getNodeName() + ". Must be either: <Type> or <Amount>");
				}
			}

		}

		if (t.type == null) {
			throw new ConfigParserException(n.getParentNode().getNodeName() + " -> " + n.getNodeName() + "; trigger type not found in <Type>");
		}
		if (t.type.needsAmount && t.amount < 0) {
			throw new ConfigParserException(n.getParentNode().getNodeName() + " -> " + n.getNodeName() + "; trigger amount must be a positive value in <Amount>");
		}

		return t;
	}

	public enum HpTriggerType {
		POWER(true),
		SHIELD(true),
		THRUST(true),
		CONTROL_LOSS(false),
		OVERHEATING(false);

		private boolean needsAmount;

		private HpTriggerType(boolean needsAmount) {
			this.needsAmount = needsAmount;
		}

		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return name();
		}

	}

}
