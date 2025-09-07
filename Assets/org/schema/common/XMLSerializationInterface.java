package org.schema.common;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface XMLSerializationInterface {
	public void parseXML(Node node);
	public Node writeXML(Document doc, Node parent);
}
