package org.schema.game.common.controller;

import java.util.Comparator;

import org.schema.common.config.ConfigParserException;
import org.w3c.dom.Node;

public class HpCondition implements Comparable<HpCondition>, Comparator<HpCondition> {
	public float hpPercent;

	public HpTrigger trigger;

	public static HpCondition parse(Node child) throws ConfigParserException {

		HpCondition c = new HpCondition();
		try {
			c.hpPercent = Float.parseFloat(child.getAttributes().getNamedItem("conditionhp").getNodeValue());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigParserException(child.getParentNode().getNodeName() + " -> " + child.getNodeName() + "; must have valid attribute 'conditionhp'");
		}

		c.trigger = HpTrigger.parse(child);

		return c;
	}

	@Override
	public int compare(HpCondition o1, HpCondition o2) {
		return o1.compareTo(o2);
	}

	@Override
	public int compareTo(HpCondition o) {
		return Float.compare(hpPercent, o.hpPercent);
	}

}
