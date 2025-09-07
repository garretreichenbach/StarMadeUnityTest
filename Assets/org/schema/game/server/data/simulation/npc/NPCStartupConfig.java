package org.schema.game.server.data.simulation.npc;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.schema.common.config.ConfigParserException;
import org.schema.common.util.linAlg.Vector3i;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class NPCStartupConfig {
	public static final byte XML_VERSION = 0;
	public static final String dataPath = "./data/npcFactions/npcSpawnConfig.xml";
	public static final String customPath = "./customNPCConfig/npcSpawnConfig.xml";
	
	
	
	public final ObjectArrayList<NPCFactionSpawn> spawns = new ObjectArrayList<NPCFactionSpawn>();
	
	
	private String getTag() {
		return "Config";
	}
	
	public static void main(String[] args){
		NPCStartupConfig s = new NPCStartupConfig();
		
		s.add("Trading Guild", "A Faction", false, 9, new Vector3i(0,0,0), "Trading Guild");
		s.add("Outcasts Alpha", "A Faction", true, 33, null, "Outcasts");
		s.add("Outcasts Beta", "A Faction", true, 6, null, "Outcasts");
		s.add("Outcasts Gamma", "A Faction", true, 8, null, "Outcasts");
		s.add("Scavengers Alpha", "A Faction", true, 44, null, "Scavengers");
		s.add("Scavengers Beta", "A Faction", true, 22, null, "Scavengers");
		s.add("Scavengers Gamma", "A Faction", true, 12, null, "Scavengers");
		
		
		writeDocument(new File(dataPath), s);
	}
	
	public static NPCFactionSpawn createSpawn(String name, String desc, boolean randomSpawn, int initialGrowth, Vector3i fixedSpawn, String ... possiblePresets){
		NPCFactionSpawn p = new NPCFactionSpawn();
		p.randomSpawn = randomSpawn;
		p.initialGrowth = initialGrowth;
		p.fixedSpawnSystem = fixedSpawn;
		p.description = desc.trim();
		StringBuffer sx = new StringBuffer();
		
		for(int i = 0; i < possiblePresets.length; i++){
			sx.append(possiblePresets[i].trim());
			if(i < possiblePresets.length-1){
				sx.append(", ");
			}
		}
		
		p.possiblePresets = sx.toString();
		p.name = name.trim();
		
		
		return p;
	}
	public NPCFactionSpawn add(String name, String desc, boolean randomSpawn, int initialGrowth, Vector3i fixedSpawn, String ... possiblePresets){
		NPCFactionSpawn p = createSpawn(name, desc, randomSpawn, initialGrowth, fixedSpawn, possiblePresets);
		
		spawns.add(p);
		
		return p;
	}
	public void create(Document config) throws DOMException, IllegalArgumentException, IllegalAccessException{
		Element root = config.createElement("NPCConfig");
		config.appendChild(root);
		
		
		Element verNode = config.createElement("Version");
		verNode.setTextContent(String.valueOf(XML_VERSION));
		
		Comment comment = config
				.createComment("autocreated");
		verNode.appendChild(comment);
		
		
		root.appendChild(verNode);

		Element configNode = config.createElement("Config"); 
		root.appendChild(configNode);
		
		
		
		for(NPCFactionSpawn s : spawns){
			Element fE = config.createElement("Faction");
			
			s.append(config, fE);
			
			
			configNode.appendChild(fE);
			
		}
		
	}
	public void parse(Document config) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
		org.w3c.dom.Element root = config.getDocumentElement();
		NodeList childNodesTop = root.getChildNodes();

		Field[] fields = getClass().getDeclaredFields();

		boolean foundTop = false;
		ObjectOpenHashSet<Field> loaded = new ObjectOpenHashSet<Field>();
		byte version = 0;
		for (int j = 0; j < childNodesTop.getLength(); j++) {
			Node itemTop = childNodesTop.item(j);

			if (itemTop.getNodeType() == Node.ELEMENT_NODE && itemTop.getNodeName().toLowerCase(Locale.ENGLISH).equals("version")) {
				
				try{
					version = Byte.parseByte(itemTop.getTextContent().trim());
				}catch(Exception e){
					e.printStackTrace();
					throw new ConfigParserException("malformed version in xml", e);
				}
				
			}else if (itemTop.getNodeType() == Node.ELEMENT_NODE && itemTop.getNodeName().toLowerCase(Locale.ENGLISH).equals(getTag().toLowerCase(Locale.ENGLISH))) {
				
				NodeList childNodesIn = itemTop.getChildNodes();
				for (int k = 0; k < childNodesIn.getLength(); k++) {
					Node item = childNodesIn.item(k);
					if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().toLowerCase(Locale.ENGLISH).equals("faction")){
						NPCFactionSpawn p = new NPCFactionSpawn();
						p.parse(item);
						spawns.add(p);
					}
				}
				
				
			}
		}
	}
	public static File writeDocument(File file, NPCStartupConfig config) {
		try {
			// ///////////////////////////
			// Creating an empty XML Document

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			
			config.create(doc);
			
			doc.setXmlVersion("1.0");

			// create a comment and put it in the root element

			// ///////////////
			// Output the XML

			// set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			
			// create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(file);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			// String xmlString = sw.toString();

			// print xml
			// System.out.println("Here's the xml:\n\n" + xmlString);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
