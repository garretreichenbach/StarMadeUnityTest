package org.schema.schine.sound.controller.assignment;

import org.schema.common.XMLSerializationInterface;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.AudioTagGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AudioAssignmentID implements XMLSerializationInterface{

	
	public enum AudioAssignmentType{
		NONE,
		MANUAL,
		TAG;
		public String getShortName(AudioAssignment assignment) {
			return this == NONE ? "N" : ((this == MANUAL ? "M" : "T") +(assignment.hasSound() ? "" : "X"));
		}
	}
	public AudioAssignmentType type = AudioAssignmentType.TAG ;
	
	private AudioAssignment assignment;
	
	@Override
	public void parseXML(final Node node) {
		NodeList assNode = node.getChildNodes();
		for(int i = 0;i < assNode.getLength(); i++) {
			Node item = assNode.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				switch(item.getNodeName()) {
					case "Type" -> this.type = AudioAssignmentType.valueOf(item.getTextContent());
				}
			}
		}
		if(type == null) {
			throw new RuntimeException("Invalid assignment type!");
		}
		
		//the assignment itself is read separately
	}
	
	@Override
	public Node writeXML(Document doc, Node parent) {
		Element root = doc.createElement("Assignment");
		
		
		
		Element typeTag = doc.createElement("Type");
		typeTag.setTextContent(type.name());
		root.appendChild(typeTag);
		
		//the assignment itself it written separately
		
		return root;
	}

	public static AudioAssignmentID parseXMLStatic(Element node) {
		AudioAssignmentID s = new AudioAssignmentID();
		s.parseXML(node);
		return s;
	}

	public void resolveAssignment(int eventId, AudioTagGroup tags, AudioController controller) {
		
		if(type == AudioAssignmentType.MANUAL) {
			assignment = controller.getConfig().getAssignmentManual(eventId, tags);
			assert(assignment != null);
		}else if(type == AudioAssignmentType.TAG) {
			assignment = controller.getConfig().getAssignmentTags(eventId, tags);
			assert(assignment != null);
		}else {
			//no assignment
			assignment = null;
		}
		if(assignment != null) {
			assignment.resolveLoadedAsset(controller.getConfig().assetManager);
		}
	}

	public AudioAssignmentType getType() {
		return type;
	}

	public AudioAssignment getAssignment() {
		return assignment;
	}

	public void setAssignment(AudioAssignment assignment) {
		this.assignment = assignment;
	}
	
}
