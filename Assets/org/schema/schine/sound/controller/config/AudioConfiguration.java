package org.schema.schine.sound.controller.config;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.schema.common.XMLSerializationInterface;
import org.schema.common.XMLTools;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.AudioTagGroup;
import org.schema.schine.sound.controller.asset.AudioAssetManager;
import org.schema.schine.sound.controller.assignment.AudioAssignment;
import org.schema.schine.sound.controller.assignment.AudioAssignmentID.AudioAssignmentType;
import org.schema.schine.sound.controller.assignment.AudioAssignmentManual;
import org.schema.schine.sound.controller.assignment.AudioAssignmentTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class AudioConfiguration implements XMLSerializationInterface {
	public static final int VERSION = 1;
	public static final String AUDIO_FILE_PATH = "./data/config/AudioConfig.xml"; 
	
	public final Int2ObjectMap<AudioEntry> entries = new Int2ObjectOpenHashMap<AudioEntry>();
	public final Int2ObjectMap<AudioAssignmentManual> manualAudioAssignmnets = new Int2ObjectOpenHashMap<>();
	public final Object2ObjectMap<AudioTagGroup, AudioAssignmentTags> tagAudioAssignmnets = new Object2ObjectOpenHashMap<>();
	public final AudioAssetManager assetManager = new AudioAssetManager();
	
	
	public void save() throws ParserConfigurationException, IOException, TransformerException {
		save(this);
	}
	public static void save(AudioConfiguration c) throws ParserConfigurationException, IOException, TransformerException {
		// ///////////////////////////
		// Creating an empty XML Document

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		
		Element root = doc.createElement("AudioConfig");
		
		doc.appendChild(root);
		
		c.writeXML(doc, root);
		
		File f = new File(AUDIO_FILE_PATH);
		//create incrementing backups when saving
		if(f.exists()) {
			(new File("./backupConfig/")).mkdir();
			File backUp;
			int i = 0;
			do {
				backUp = new File("./backupConfig/"+f.getName()+".backup"+i+".xml");
				i++;
			}while(backUp.exists());
			
			FileUtil.copyFile(f, backUp);
			f.delete();
		}
		
		XMLTools.writeDocument(f, doc);
		
		System.out.println("[AUDIOCONFIG][WRITE] Audio Config written to "+f.getCanonicalPath());
	}
	
	public static AudioConfiguration load() throws SAXException, IOException, ParserConfigurationException {
		
		
		AudioConfiguration c = new AudioConfiguration();
		File fileEvents = new File(AUDIO_FILE_PATH);
		if(fileEvents.exists()) {
			c.load(fileEvents);
		}
		return c;
	}

	private void load(File file) throws IOException {
		Document doc = XMLTools.loadXML(file);
		Element root = doc.getDocumentElement();
		parseXML(root);
		
		
		
	}
	public void resolveAllEvents(AudioController c) {
		for(AudioEntry e : entries.values()) {
			e.assignmnetID.resolveAssignment(e.id, e.tags, c);
		}
	}
	@Override
	public void parseXML(Node node) {
		parseAssets(node);
		parseAssignments(node);
		parseEvents(node);
	}
	private void parseAssignments(Node node) {
		NodeList childNodes = node.getChildNodes();
		
		for(int x = 0; x < childNodes.getLength(); x++) {
			Node rItem = childNodes.item(x);
			if(rItem.getNodeType() == Node.ELEMENT_NODE && rItem.getNodeName().equals(AudioEntry.TAG_MAIN_ASSIGNMENTS)) {
				
				for(int i = 0; i < rItem.getChildNodes().getLength(); i++) {
					Node item = rItem.getChildNodes().item(i);
					
					if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals(AudioEntry.TAG_ASSIGNMENT)) {
						AudioAssignment as = AudioAssignment.parseXMLStatic(item);
						if(as.getType() == AudioAssignmentType.MANUAL) {
							AudioAssignmentManual m = (AudioAssignmentManual)as;
							manualAudioAssignmnets.put(m.manualId, m);
						}else if(as.getType() == AudioAssignmentType.TAG) {
							AudioAssignmentTags m = (AudioAssignmentTags)as;
							assert(m.group.size() > 0);
							tagAudioAssignmnets.put(m.group, m);
						}else {
							throw new RuntimeException("unknown audio assignment type "+as);
						}
					}
				}
			}
		}
		System.err.println("[AUDIO] parsed assignments: TAGS: "+tagAudioAssignmnets.size()+"; MANUAL: "+manualAudioAssignmnets.size());
	}
	private void parseEvents(Node node) {
		NodeList childNodes = node.getChildNodes();
		
		for(int x = 0; x < childNodes.getLength(); x++) {
			Node rItem = childNodes.item(x);
			if(rItem.getNodeType() == Node.ELEMENT_NODE && rItem.getNodeName().equals(AudioEntry.TAG_MAIN_EVENTS)) {
				for(int i = 0; i < rItem.getChildNodes().getLength(); i++) {
					Node item = rItem.getChildNodes().item(i);
					if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals(AudioEntry.TAG_ENTRY)) {
						AudioEntry e = new AudioEntry();
						e.parseXML(item);
						assert(e.id != 0):e;
						if(entries.putIfAbsent(e.id, e) != null) {
							throw new RuntimeException("Invalid Config. Duplicate entry for id "+e.id+": \n"+e+"\n----------\n"+entries.get(e.id));
						}
					}
				}
			}
		}
	}
	private void parseAssets(Node node) {
		NodeList childNodes = node.getChildNodes();
		
		for(int x = 0; x < childNodes.getLength(); x++) {
			Node rItem = childNodes.item(x);
			if(rItem.getNodeType() == Node.ELEMENT_NODE && rItem.getNodeName().equals(AudioEntry.TAG_MAIN_ASSETS)) {
				assetManager.parseXML(rItem);
			}
		}
	}
	@Override
	public Node writeXML(Document doc, Node rt) {
		System.err.println("[AUDIOCONFIG][WRITE] WRITING AUDIO ENTRIES: "+entries.size());
		{
			Element parent = doc.createElement(AudioEntry.TAG_MAIN_EVENTS);
			for(AudioEntry a : entries.values()) {
				a.writeXML(doc, parent);
			}
			rt.appendChild(parent);
		}
		{
			Element parent = doc.createElement(AudioEntry.TAG_MAIN_ASSIGNMENTS);
			for(AudioAssignment a : tagAudioAssignmnets.values()) {
				
				a.writeXML(doc, parent);				
			}
			for(AudioAssignment a : manualAudioAssignmnets.values()) {
				a.writeXML(doc, parent);
			}
			rt.appendChild(parent);
		}
		{
			Element parent = doc.createElement(AudioEntry.TAG_MAIN_ASSETS);
			rt.appendChild(parent);
			assetManager.writeXML(doc, parent);
		}
		
		return rt;
	}

	public String printEntries() {
		StringBuffer b = new StringBuffer();
		for(AudioEntry e : entries.values()) {
			b.append(e.id+" -> "+e+"\n");
		}
		return b.toString();
	}

	public AudioAssignmentManual getAssignmentManual(int eventId, AudioTagGroup tags) {
		AudioAssignmentManual audioAssignment = manualAudioAssignmnets.get(eventId);
		if(audioAssignment == null) {
			audioAssignment = new AudioAssignmentManual();
			audioAssignment.manualId = eventId;
			manualAudioAssignmnets.put(eventId, audioAssignment);
		}
		return audioAssignment;
	}

	public AudioAssignmentTags getAssignmentTags(int eventId, AudioTagGroup tags) {
		AudioAssignmentTags audioAssignment = tagAudioAssignmnets.get(tags);
		
		if(audioAssignment == null) {
			System.err.println("NO ASSIGNMENT FOUND FOR "+tags+"; "+tagAudioAssignmnets.size());
			audioAssignment = new AudioAssignmentTags();
			audioAssignment.group = new AudioTagGroup(tags);
			tagAudioAssignmnets.put(tags, audioAssignment);
		}
		return audioAssignment;
	}
	public void sortAssets() {
		assetManager.sort();
	}
}
