package org.schema.game.common.controller.rules.rules.conditions;

import org.schema.common.XMLSerializationInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FactionRange implements XMLSerializationInterface {
	public int from;
	public int to;
	
	public boolean isInRange(int factionId) {
		return factionId >= from && factionId <= to;
	}

	@Override
	public void parseXML(Node node) {
		String t = node.getTextContent();
		if(t.contains(",")) {
			String[] s = t.split(",");
			from = Integer.parseInt(s[0].trim());
			to = Integer.parseInt(s[1].trim());
		}else {
			from = Integer.parseInt(t.trim());
			to = from;
		}
	}

	@Override
	public Node writeXML(Document doc, Node parent) {
		parent.setTextContent(from+", "+to);
		return parent;
	}

	@Override
	public String toString() {
		return "["+from+", "+to+"]";
	}
	
}
