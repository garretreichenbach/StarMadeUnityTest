package org.schema.schine.sound.controller;

import org.schema.common.XMLSerializationInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AudioPlaySettings implements XMLSerializationInterface{

	private static final String PRIM_TAG = "PrimaryRange";
	private static final String SEC_TAG = "SecondaryRange";

	private float primaryRange = 1;
	private float secondaryRange = 1;

	@Override
	public void parseXML(Node node) {
		NodeList cn = node.getChildNodes();
		for(int i = 0; i < cn.getLength(); i++) {
			Node item = cn.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				switch(item.getNodeName()) {
					case PRIM_TAG -> primaryRange = Float.parseFloat(item.getTextContent());
					case SEC_TAG -> secondaryRange = Float.parseFloat(item.getTextContent());
				}
			}
		}
	}

	@Override
	public Node writeXML(Document doc, Node parent) {
		Element a = doc.createElement(PRIM_TAG);
		a.setTextContent(String.valueOf(primaryRange));
		parent.appendChild(a);
		
		Element b = doc.createElement(SEC_TAG);
		b.setTextContent(String.valueOf(secondaryRange));
		parent.appendChild(b);
		
		return parent;
	}

	public void setPrimaryRange(float rangePrim) {
		this.primaryRange = rangePrim;		
	}

	public void setSecondaryRange(float rangeSec) {
		this.secondaryRange = rangeSec;
		
	}

}
