package org.schema.schine.sound.controller;

import org.schema.common.XMLSerializationInterface;
import org.schema.schine.sound.controller.config.AudioEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;

public class AudioTagGroup extends ObjectAVLTreeSet<AudioTag> implements XMLSerializationInterface{
	private static final long serialVersionUID = 1L;
	public AudioTagGroup() {
	}
	public AudioTagGroup(AudioTagGroup tags) {
		super(tags);
	}

	public String getStringID() {
		StringBuilder b = new StringBuilder();
		int i =0;
		for(AudioTag t : this) {
			
			b.append(t.toString());
			if(i < size()-1) {
				b.append(",");
			}
			i++;
		}
		return b.toString();
	}

	@Override
	public void parseXML(Node node) {
		NodeList rCNodes = node.getChildNodes();
		for(int i = 0; i < rCNodes.getLength(); i++) {
			Node mainItem = rCNodes.item(i);
			if(mainItem.getNodeType() == Node.ELEMENT_NODE) {
				add(AudioTag.getFromText(mainItem.getTextContent()));
			}
		}
	}

	@Override
	public Element writeXML(Document doc, Node parent) {
		
		Element tagsTag = doc.createElement(AudioEntry.TAG_TAGS);
		
		for(AudioTag tag : this) {
			Element tt = doc.createElement(AudioEntry.TAG_TAG);
			tt.setTextContent(tag.toString());
			tagsTag.appendChild(tt);
		}
		return tagsTag;
	}
	
}
