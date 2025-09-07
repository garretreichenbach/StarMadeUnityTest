package org.schema.schine.sound.controller.assignment;

import org.schema.common.XMLSerializationInterface;
import org.schema.schine.sound.controller.AudioPlaySettings;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.controller.asset.AudioAssetManager;
import org.schema.schine.sound.controller.assignment.AudioAssignmentID.AudioAssignmentType;
import org.schema.schine.sound.controller.mixer.AudioMixer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public interface AudioAssignment extends XMLSerializationInterface{
	
	public AudioMixer getMixer();
	public AudioPlaySettings getSettings();
	public AudioAssignmentType getType();
	public boolean hasSound();
	public static AudioAssignment parseXMLStatic(Node from) {
		NodeList cn = from.getChildNodes();
		for(int x = 0; x < cn.getLength(); x++) {
			Node it = cn.item(x);
			if(it.getNodeType() == Node.ELEMENT_NODE && it.getNodeName().equals("Type")) {
				AudioAssignmentType type = AudioAssignmentType.valueOf(it.getTextContent());
				if(type == null) {
					throw new RuntimeException("unknown type "+it.getTextContent());
				}
				
				if(type == AudioAssignmentType.MANUAL) {
					AudioAssignmentManual m = new AudioAssignmentManual();
					m.parseXML(from);
					return m;
				}else if(type == AudioAssignmentType.TAG) {
					AudioAssignmentTags m = new AudioAssignmentTags();
					m.parseXML(from);
					return m;
				}
			}
		}
		throw new RuntimeException("Audio Assignment missing 'Type' Tag");
	}
	public AudioAsset getAssetPrimary();
	public AudioAsset getAssetSecondary();
	public void setPrimaryAsset(AudioAsset a);
	public void setSecondaryAsset(AudioAsset a);
	public void setAudioMixer(AudioMixer mixer);
	public void resolveLoadedAsset(AudioAssetManager man);
}
