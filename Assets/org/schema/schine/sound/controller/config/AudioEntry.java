package org.schema.schine.sound.controller.config;

import java.util.Locale;

import org.schema.common.XMLSerializationInterface;
import org.schema.schine.sound.controller.AudioParam;
import org.schema.schine.sound.controller.AudioTagGroup;
import org.schema.schine.sound.controller.assignment.AudioAssignmentID;
import org.schema.schine.sound.controller.assignment.AudioAssignmentID.AudioAssignmentType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AudioEntry implements XMLSerializationInterface{
	public static final String TAG_VERSION = "Version";
	
	public static final String TAG_MAIN_EVENTS = "Events";
	public static final String TAG_ENTRY = "Entry";
	public static final String TAG_MAIN_ASSIGNMENTS = "Assignments";
	public static final String TAG_MAIN_ASSETS = "Assets";
	
	public static final String TAG_ID = "Id";
	public static final String TAG_TAGS = "Tags";
	public static final String TAG_TAG = "Tag";
	public static final String TAG_NAME = "Name";
	public static final String TAG_PARAM = "Param";
	public static final String TAG_REMOTE = "IsRemote";
	public static final String TAG_ARGUMENT = "HasArgument";

	public static final String TAG_ASSIGNMENT = "Assignment";
	public static final String TAG_ASSET = "Asset";
	
	
	public int id;
	public AudioTagGroup tags = new AudioTagGroup();
	public String name = "";
	public AudioParam audioParam = AudioParam.ONE_TIME;
	public boolean argumentPresent;
	public boolean remote;
	
	public AudioAssignmentID assignmnetID;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		return ((AudioEntry) obj).id == id;
	}
	@Override
	public void parseXML(Node node) {
		NodeList rCNodes = node.getChildNodes();
		for(int i = 0; i < rCNodes.getLength(); i++) {
			Node mainItem = rCNodes.item(i);
			if(mainItem.getNodeType() == Element.ELEMENT_NODE) {
				
				switch(mainItem.getNodeName()) {
				case TAG_ID: id = Integer.parseInt(mainItem.getTextContent()); break;
				case TAG_TAGS: tags.parseXML(mainItem); break;
				case TAG_NAME: name = mainItem.getTextContent(); break;
				case TAG_PARAM: audioParam = AudioParam.valueOf(mainItem.getTextContent().toUpperCase(Locale.ENGLISH)); break;
				case TAG_REMOTE: remote = Boolean.parseBoolean(mainItem.getTextContent()); break;
				case TAG_ARGUMENT: argumentPresent = Boolean.parseBoolean(mainItem.getTextContent()); break;
				case TAG_ASSIGNMENT: 
					try {
						assignmnetID = AudioAssignmentID.parseXMLStatic((Element) mainItem); 
					}catch(RuntimeException e) {
						 System.err.println("Parse Error happened in EVENT ID "+id);
						throw e;
					}
					break;
				}
				
			}
		}
	}
	@Override
	public Node writeXML(Document doc, Node parent) {
		
		Element main = doc.createElement(TAG_ENTRY);
		
		Element versionTag = doc.createElement(TAG_VERSION);
		Element idTag = doc.createElement(TAG_ID);
		Element tagsTag = tags.writeXML(doc, parent);
		Element nameTag = doc.createElement(TAG_NAME);
		Element paramTag = doc.createElement(TAG_PARAM);
		Element remoteTag = doc.createElement(TAG_REMOTE);
		Element argumentTag = doc.createElement(TAG_ARGUMENT);
		Element outputTag = (Element) (assignmnetID == null ?  doc.createElement(TAG_ASSIGNMENT) : assignmnetID.writeXML(doc, main));
		
		
		idTag.setTextContent(String.valueOf(id));
		versionTag.setTextContent(String.valueOf(AudioConfiguration.VERSION));
		nameTag.setTextContent(name);
		paramTag.setTextContent(audioParam.name());
		remoteTag.setTextContent(String.valueOf(remote));
		argumentTag.setTextContent(String.valueOf(argumentPresent));
		
		
		main.appendChild(idTag);
		main.appendChild(versionTag);
		main.appendChild(tagsTag);
		main.appendChild(nameTag);
		main.appendChild(paramTag);
		main.appendChild(remoteTag);
		main.appendChild(argumentTag);
		
		main.appendChild(outputTag);
		
		
		parent.appendChild(main);
		return main;
	}
	@Override
	public String toString() {
		return "AudioEntry [id=" + id + ", tags=" + tags + ", name=" + name + ", audioParam=" + audioParam
				+ ", argumentPresent=" + argumentPresent + ", remote=" + remote + ", audioOutput=" + assignmnetID + "]";
	}
	public boolean isShowInList(
			boolean withAudio, 
			boolean withoutAudio, 
			boolean tagAudio, 
			boolean manual, 
			boolean remote,
			boolean nonRemote) {
		
		
		
		assert(this.assignmnetID != null);
		if(withoutAudio && this.assignmnetID.getType() == AudioAssignmentType.NONE) {
			if(nonRemote && !this.remote) {
				return true;
			}
			else if(remote && this.remote) {
				return true;
			}else {
				return false;
			}
			
		}else if(!withoutAudio && this.assignmnetID.getType() != AudioAssignmentType.NONE) {
			return false;
		}
		if(withAudio && this.assignmnetID.getType() != AudioAssignmentType.NONE) {
			if(manual && this.assignmnetID.getType() == AudioAssignmentType.MANUAL) {
				if(nonRemote && !this.remote) {
					return true;
				}
				else if(remote && this.remote) {
					return true;
				}else {
					return false;
				}
			}
			if(tagAudio && this.assignmnetID.getType() == AudioAssignmentType.TAG) {
				if(nonRemote && !this.remote) {
					return true;
				}
				else if(remote && this.remote) {
					return true;
				}else {
					return false;
				}
			}
		}
		
		
		
		
		
		
		return true;
		
		
		
	}
	
}
