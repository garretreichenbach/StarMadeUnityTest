package org.schema.schine.sound.controller.assignment;

import org.schema.schine.sound.controller.assignment.AudioAssignmentID.AudioAssignmentType;
import org.schema.schine.sound.controller.config.AudioEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AudioAssignmentManual extends AudioAssignmentImpl{
	public int manualId = -1;
	@Override
	public void parseXML(Node node) {
		NodeList assNode = node.getChildNodes();
		for(int i = 0;i < assNode.getLength(); i++) {
			Node item = assNode.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				switch(item.getNodeName()) {
					case "Type": assert(getType() == AudioAssignmentType.valueOf(item.getTextContent())):getType()+"; "+AudioAssignmentType.valueOf(item.getTextContent()); break; 
					case "ManualId": manualId = Integer.parseInt(item.getTextContent()); break; 
					case "Settings": getSettings().parseXML(item); break; 
					case "Mixer": parseMixer(item.getTextContent()); break;
					case PRIMARY: parseAsset(item, 0); break;
					case SECONDARY: parseAsset(item, 1); break;
					default: break;
				}
			}
		}
	}

	

	@Override
	public Node writeXML(Document doc, Node root) {
		Element parent = doc.createElement(AudioEntry.TAG_ASSIGNMENT);
		root.appendChild(parent);
		
		Element t = doc.createElement("Type");
		t.setTextContent(getType().name());
		
		Element e = doc.createElement("ManualId");
		e.setTextContent(String.valueOf(manualId));
		
		Element m = doc.createElement("Mixer");
		m.setTextContent(getMixer().getName());
		
		Element set = doc.createElement("Settings");
		getSettings().writeXML(doc, set);
		
		
		parent.appendChild(t);
		parent.appendChild(e);
		parent.appendChild(set);
		parent.appendChild(m);
		
		writeAssets(doc, parent);
		
		return root;
	}

	@Override
	public AudioAssignmentType getType() {
		return AudioAssignmentType.MANUAL;
	}


}
