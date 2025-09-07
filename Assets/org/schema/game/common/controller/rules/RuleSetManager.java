package org.schema.game.common.controller.rules;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.SerializationInterface;
import org.schema.common.XMLSerializationInterface;
import org.schema.common.XMLTools;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.RuleParserException;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.game.common.util.DataUtil;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.remote.RemoteRuleSetManager;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.Sendable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class RuleSetManager implements SerializationInterface, XMLSerializationInterface{
	
	public static final String rulesPath = "."+File.separator+"rules.xml";
	public static final String defaultRulesPath = DataUtil.dataPath+"config"+File.separator+"defaultRules.xml";

	
	private final static byte VERSION = 0;

	private Object2ObjectOpenHashMap<String, RuleSet> ruleSets = new Object2ObjectOpenHashMap<String, RuleSet>();
	

	public final Object2ObjectOpenHashMap<String, Rule> ruleUIDlkMap = new Object2ObjectOpenHashMap<String, Rule>();
	public final Int2ObjectOpenHashMap<Rule> ruleIdMap = new Int2ObjectOpenHashMap<Rule>();
	
	
	public GameStateInterface state;
	
	
	public RuleSetManager(){
		this.state = null;
	}
	public RuleSetManager(GameStateInterface state){
		this.state = state;
	}

	
	
//	private final Object2LongOpenHashMap<RuleEntityContainer> triggers = new Object2LongOpenHashMap<RuleEntityContainer>(); 
	
	
	
	
	public class RuleCol{
		private Object2ObjectOpenHashMap<TopLevelType, List<Rule>> ruleByType = new Object2ObjectOpenHashMap<TopLevelType, List<Rule>>(); 
		public void generate() {
			ruleByType.clear();
			for(Rule r : ruleIdMap.values()) {
				TopLevelType t = r.getEntityType();
				List<Rule> list = ruleByType.get(t);
				if(list == null) {
					list = new ObjectArrayList<Rule>();
					ruleByType.put(t, list);
				}
				list.add(r);
			}
		}
	}
	private final RuleCol col = new RuleCol();
	public boolean includePropertiesInSendAndSaveOnServer;
	public boolean receivedInitialOnClient;
	public RulePropertyContainer receivedFullRuleChange;
//	public void trigger(RuleEntityContainer o, long triggerType) {
//		long k = triggers.getLong(o);
//		k |= triggerType;
//		triggers.put(o, k);
//	}
//	public void update(SendableGameState gameState, Timer timer) {
//		final StateInterface state = gameState.getState();
//		
//		if(!triggers.isEmpty()) {
//			FastEntrySet<RuleEntityContainer> es = triggers.object2LongEntrySet();
//			for(Entry<RuleEntityContainer> e : es) {
//				RuleEntityContainer s = e.getKey();
//				long trigger = e.getLongValue();
//				checkRules(s, trigger);
//				
//			}
//		}
//	}
//	public void checkRulesAllTriggers(RuleEntityContainer s) {
//		checkRules(s, SegmentControllerCondition.TRIGGER_ON_ALL);
//	}
//	public void checkRules(RuleEntityContainer s, long trigger) {
//		List<Rule> list = s.getRuleEntityManager().getActiveRules();
//		TopLevelType topLevelType = s.getTopLevelType();
//
//		for(Rule r : list) {
//			r.process(s, topLevelType, trigger);
//		}
//	}
	/**
	 * creates an exact clone of this manager with the same ids and every. Usefull for editing on without changing actual data.
	 * @param cloneFrom
	 * @throws IOException
	 */
	public RuleSetManager(RuleSetManager cloneFrom) throws IOException {
		this();
		FastByteArrayOutputStream fbo = new FastByteArrayOutputStream(1024*1024);
		DataOutputStream sb = new DataOutputStream(fbo);
		cloneFrom.serialize(sb, true);
//		cloneFrom.properties.serialize(sb, true);
//		
		DataInputStream in = new DataInputStream(new FastByteArrayInputStream(fbo.array, 0, (int)fbo.position()));
		deserialize(in, 0, true);
//		properties.deserialize(in, 0, true);
	}
	
	public void loadFromDisk() throws IOException {
		loadFromDisk(rulesPath);
	}
	public void loadFromDisk(String path) throws IOException {
		loadRulesFromDisk(path);
	}
	public void writeToDisk() throws IOException {
		writeToDisk(rulesPath);
	}
	public void writeToDisk(File f) throws IOException {
		writeRules(f, ruleSets.values());
	}
	public void writeToDisk(String path) throws IOException {
		writeRules(path);
	}
	public static void writeRules(String path, Collection<RuleSet> ruleSets) throws IOException{
		writeRules(new File(path), ruleSets);
	}
	public static void writeRules(File f, Collection<RuleSet> ruleSets) throws IOException  {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		}
		Document doc = dBuilder.newDocument();
		
		assert(ruleSets != null);
		assert(doc != null);
		doc.appendChild(writeXML(doc, ruleSets));

		try {
			XMLTools.writeDocument(f, doc);
		} catch(ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch(TransformerException e) {
			throw new RuntimeException(e);
		}
	}
	private void writeRules(String path) throws IOException {
		writeRules(path, ruleSets.values());
	}
	public void loadRulesFromDisk(String path) throws IOException {
		File f = new File(path);
		if(!f.exists()) {
			File defaultRules = new File(defaultRulesPath);
			if(defaultRules.exists()) {
				FileUtil.copyFile(defaultRules, f);
			}
		}
		loadFromDisk(f);
	}
	public void loadFromDisk(File f) throws IOException {
		
		if(f.exists()) {
			loadRulesFromDisk(f);
		}else {
			System.err.println("[RULES] no rules read from disk: "+f.getCanonicalPath());
		}
		createCollections();
	}


	private void loadRulesFromDisk(File f) throws IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();

			BufferedInputStream bui = new BufferedInputStream(new FileInputStream(f), 4096);
			Document doc = dBuilder.parse(bui);
			bui.close();

			parseXML(doc.getDocumentElement());
		} catch (ParserConfigurationException | SAXException e) {
			throw new IOException(e);
		}
	}
	public void createCollections() {
		ruleUIDlkMap.clear();
		ruleIdMap.clear();
		for(RuleSet s : ruleSets.values()) {
			addRules(s);
		}
		
		col.generate();
	}
	
	public RuleSet removeRuleSet(String uid) {
		RuleSet s = ruleSets.remove(uid);
		if(s != null) {
			removeRules(s);
		}
		//redo collections in case the ruleset added had any rules in it
		createCollections();
		return s;
	}
	private void removeRules(RuleSet s) {
		for(Rule r : s) {
			ruleUIDlkMap.remove(r.getUniqueIdentifier().toLowerCase(Locale.ENGLISH));
			ruleIdMap.remove(r.getRuleId());
			r.ruleSetUID = "undefined";
		}
	}


	public void addRuleSet(RuleSet s) {
		
		for(Rule r : s) {
			String nm = r.getUniqueIdentifier();
			int i = 0;
			String tNm = nm;
			while(ruleUIDlkMap.containsKey(tNm.toLowerCase(Locale.ENGLISH))) {
				tNm = nm+i;
				i++;
			}
			r.setUniqueIdentifier(tNm);
		}
		ruleSets.put(s.uniqueIdentifier, s);
		addRules(s);
		//redo collections in case the ruleset added had any rules in it
		createCollections();
	}
	private void addRules(RuleSet s) {
		for(Rule r : s) {
			ruleUIDlkMap.put(r.getUniqueIdentifier().toLowerCase(Locale.ENGLISH), r);
			ruleIdMap.put(r.getRuleId(), r);
			r.setRuleSet(s);
		}
	}
	@Override
	public void parseXML(Node node) {
		
		if(node.getAttributes().getNamedItem("version") == null) {
			throw new RuleParserException("missing version attribute on ruleset");
		}
		
		final byte version = Byte.parseByte(node.getAttributes().getNamedItem("version").getNodeValue());

		
		ruleSets.clear();
		
		NodeList childNodes = node.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				
				if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("ruleset")) {
					RuleSet r = new RuleSet();
					r.parseXML(item);
					ruleSets.put(r.getUniqueIdentifier(), r);
				}else {
					throw new RuleParserException("Node name must be 'Rule'");
				}
			}
		}
	}
	public static Node writeXML(Document doc, Collection<RuleSet> ruleSets) {
		Node root = doc.createElement("RuleSets");
		
		Attr vAtt = doc.createAttribute("version");
		vAtt.setValue(String.valueOf(VERSION));
		root.getAttributes().setNamedItem(vAtt);
		assert(ruleSets.size() > 0);
		for(RuleSet c : ruleSets) {
			Node n = c.writeXML(doc, root);
			root.appendChild(n);
		}
		
		return root;
	}
	@Override
	public Node writeXML(Document doc, Node parent) {
		return writeXML(doc, ruleSets.values());
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		if(getProperties() == null) {
			System.err.println("[RULES][WARNING] tried to submit properties but properties were null");
			includePropertiesInSendAndSaveOnServer = false;
		}
		b.writeBoolean(includePropertiesInSendAndSaveOnServer);
		b.writeInt(ruleSets.size());
		for(RuleSet c : ruleSets.values()) {
			c.serialize(b, isOnServer);
		}
		if(includePropertiesInSendAndSaveOnServer) {
			System.err.println("[RULESET] serializing all Rules and properties.");
			getProperties().serialize(b, isOnServer);
			includePropertiesInSendAndSaveOnServer = false;
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		includePropertiesInSendAndSaveOnServer = b.readBoolean();
		final int size = b.readInt();
		for(int i = 0; i < size; i++) {
			RuleSet r = new RuleSet();
			r.deserialize(b, updateSenderStateId, isOnServer);
			ruleSets.put(r.getUniqueIdentifier(), r);
		}
		createCollections();
		
		if(includePropertiesInSendAndSaveOnServer) {
			RulePropertyContainer c = new RulePropertyContainer(this);
			c.deserialize(b, updateSenderStateId, isOnServer);
			receivedFullRuleChange = c;
			includePropertiesInSendAndSaveOnServer = false;
		}
		
	}

	public void initializeOnServer() throws IOException {
		loadFromDisk();
	}


	public Collection<RuleSet> getRuleSets() {
		return ruleSets.values();
	}
	public RuleSet getRuleSetByUID(String receivedRuleSetUID) {
		RuleSet ruleSet = ruleSets.get(receivedRuleSetUID);
		if(ruleSet == null) {
			System.err.println("RuleSet by UID not found: '"+receivedRuleSetUID+"'; available: "+ruleSets);
		}
		return ruleSet;
	}
	public void trigger(SegmentController c, long trigger) {
		c.getRuleEntityManager().trigger(trigger);
	}
	public List<RuleSet> getGlobalRules(byte subType, List<RuleSet> out) {
		return getProperties().getGlobalRules(subType, out);
	}
	public RulePropertyContainer getProperties() {
		if(state == null) {
			assert(state != null):"No Properties";
			return null;
		}
		assert(state.getGameState() != null);
		return state.getGameState().getRuleProperties();
	}
	public void updateToNetworkObject(NetworkGameState o) {
	}
	public void updateToFullNetworkObject(NetworkGameState o) {
		includePropertiesInSendAndSaveOnServer = true;
		o.ruleSetManagerBuffer.add(new RemoteRuleSetManager(this, o));
	}
	public void initFromNetworkObject(NetworkGameState o) {
	}


	public void flagChanged(StateInterface state) {
		synchronized(state) {
			for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if(s instanceof RuleEntityContainer) {
					//globals might have changed so we need to flag for update
					((RuleEntityContainer)s).getRuleEntityManager().flagRuleChanged();
				}
			}
		}
	}
	public void setState(GameStateInterface state) {
		this.state = state;
	}
	public boolean containtsName(RuleSet r) {
		return ruleSets.containsKey(r.uniqueIdentifier);
	}


	
}