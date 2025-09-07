package org.schema.game.common.data.blockeffects.config;

import api.listener.events.register.ConfigGroupRegisterEvent;
import api.listener.events.register.RegisterConfigGroupsEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.XMLTools;
import org.schema.common.util.data.DataUtil;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.ClientStatics;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.network.objects.remote.RemoteEffectConfigGroup;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerExecutionJob;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigPool {
	
	public String hash = "";
	public static final String CONFIG_FILENAME = "EffectConfig.xml";
	
	public final static String configPathTemplate = "." + File.separator + "data" + File.separator + "config" + File.separator + "customConfigTemplate" + File.separator + "customEffectConfigTemplate.xml";
	public final static String configPathHOWTO = "." + File.separator + "data" + File.separator + "config" + File.separator + "customConfigTemplate" + File.separator + "CustomEffectConfigHowto.txt";

	
	public ConfigPool(){
		
	}
	public File getPath(boolean server){
		File f;
		if(server){
			f = new File(GameServerState.DATABASE_PATH+CONFIG_FILENAME);
			if(!f.exists()){
				f = new File(DataUtil.dataPath+File.separator+"config"+File.separator+CONFIG_FILENAME);
			}
		}else{
			
			f = new File(ClientStatics.ENTITY_DATABASE_PATH+CONFIG_FILENAME);
			if(!f.exists()){
				f = new File(DataUtil.dataPath+File.separator+"config"+File.separator+CONFIG_FILENAME);
			}
		}
		return f;
	}
	public void readConfigFromFile(File f) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException, IllegalArgumentException, IllegalAccessException {
		
		
		if(!f.exists()){
			System.err.println("[CONFIGPOOL] no effect config found: "+f.getAbsolutePath());
			throw new FileNotFoundException("Not found " +f.getAbsolutePath());
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new BufferedInputStream(new FileInputStream(f), 4096));
		boolean needsHashCalc = false;
		Document merge = null;
		File customVoidElementManager = new FileExt(GameResourceLoader.CUSTOM_EFFECT_CONFIG_PATH + "customEffectConfig.xml");
		if (customVoidElementManager.exists()) {
			merge = XMLTools.loadXML(customVoidElementManager);
			System.err.println("[SERVER] Custom effect config found");
			XMLTools.mergeDocumentOnAttrib(doc, merge, "id");
			//XMLTools.writeDocument(new FileExt("EffectConfigMergeResult.xml"), doc);
			//f = new FileExt("EffectConfigMergeResult.xml");
			needsHashCalc = true;
		}
		
		

		
		NodeList childs = doc.getDocumentElement().getChildNodes();
		
		String hash = doc.getDocumentElement().getAttribute("hash");
		if(!needsHashCalc && hash != null){
			this.hash = hash;
		}
		
		for(int i = 0; i < childs.getLength(); i++){
			Node item = childs.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE){
				ConfigGroup g = new ConfigGroup();
				g.parse(item);
				add(g);
				//INSERTED CODE
				ConfigGroupRegisterEvent event = new ConfigGroupRegisterEvent(this, g);
				StarLoader.fireEvent(event, true);
				///
			}
		}


		if(needsHashCalc) {
			this.hash = calculateHash();
		}
		//INSERTED CODE
		registerModGroups();
		///
	}

	public final Short2ObjectMap<ConfigGroup> ntMap = new Short2ObjectOpenHashMap<ConfigGroup>();
	public final Map<String, ConfigGroup> poolMapLowerCase = new Object2ObjectOpenHashMap<String, ConfigGroup>();
	public final List<ConfigGroup> pool = new ObjectArrayList<ConfigGroup>();
	private String hashRequestedByClient;
	
	
	public void clientReceive(ClientChannel clientChannel){
		assert(!clientChannel.isOnServer());
		receiveConfigGroups(clientChannel);
		ObjectArrayList<RemoteString> r = clientChannel.getNetworkObject().effectConfigSig.getReceiveBuffer();
		for(RemoteString s : r){
			if(clientChannel.isOnServer()){
				
			}else{
				hash = s.get();
				System.err.println("[CONFIGPOOL] CLIENT RECEIVED HASH: "+hash);
			}
		}
	}
	public String calculateHash(){
		List<ConfigGroup> sorted = new ObjectArrayList<ConfigGroup>(pool);
		Collections.sort(sorted);
		long hash = 0;
		for(int i = 0; i < sorted.size(); i++){
			hash += DataUtil.primes[i%DataUtil.primes.length]*sorted.get(i).getHash();
		}
		return String.valueOf(hash);
	}
	public void remove(ConfigGroup g){
		assert(g != null);
		pool.remove(g);
		poolMapLowerCase.remove(g.id.toLowerCase(Locale.ENGLISH));
		ntMap.remove(g.ntId);
	}
	public void add(ConfigGroup g){
		assert(g != null);
		pool.add(g);
		poolMapLowerCase.put(g.id.toLowerCase(Locale.ENGLISH), g);
		assert(g.ntId > 0);
		ntMap.put(g.ntId, g);
	}
	public void clear() {
		pool.clear();
		poolMapLowerCase.clear();
		ntMap.clear();	
	}
	public void write(File file) throws Exception{

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			org.w3c.dom.Element root = doc.createElement("Config");

			
//			root.appendChild(recipeRoot);

			doc.appendChild(root);
			doc.setXmlVersion("1.0");
			
			
			this.hash = calculateHash();
			for(ConfigGroup g : pool){
				Node write = g.write(doc);
				root.appendChild(write);
			}
			root.setAttribute("hash", this.hash);

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
		
	}

	public void updateToNetworkObject(ClientChannel clientChannel){
		
	}
	public void requestConfig(ClientChannel clientChannel){
		clientChannel.getNetworkObject().effectConfigSig.add(new RemoteString(hash, clientChannel.getNetworkObject()));
	}
	public void checkRequestReceived(final ClientChannel clientChannel){
		assert(clientChannel.isOnServer());
		ObjectArrayList<RemoteString> r = clientChannel.getNetworkObject().effectConfigSig.getReceiveBuffer();
		for(RemoteString s : r){
			if(s.get().trim().isEmpty() || !hash.equals(s.get())){
				//client has wrong config. send real one
				ServerExecutionJob j = state -> {
					clientChannel.getNetworkObject().effectConfigSig.add(new RemoteString(hash, clientChannel.isOnServer()));
					sendAllConfigGroups(clientChannel);
					return true;
				};
				((GameServerState)clientChannel.getState()).getServerExecutionJobs().enqueue(j);
			}
		}
		
	}

	private void sendConfigGroup(ClientChannel clientChannel, ConfigGroup e) {
		clientChannel.getNetworkObject().effectConfigGroupBuffer.add(new RemoteEffectConfigGroup(e, clientChannel.isOnServer()));
	}
	private void sendAllConfigGroups(ClientChannel clientChannel) {
		for(ConfigGroup f  : pool){
			sendConfigGroup(clientChannel, f);
		}
	}
	private void receiveConfigGroups(ClientChannel clientChannel) {
		ObjectArrayList<RemoteEffectConfigGroup> r = clientChannel.getNetworkObject().effectConfigGroupBuffer.getReceiveBuffer();
		if(r.size() > 0){
			System.err.println("[CONFIGPOOL] received new config groups: "+r.size());
			clear();
			for(RemoteEffectConfigGroup e : r){
				ConfigGroup configGroup = e.get();
				add(configGroup);
				//INSERTED CODE
				ConfigGroupRegisterEvent event = new ConfigGroupRegisterEvent(this, configGroup);
				StarLoader.fireEvent(event, false);
				///
			}
			//INSERTED CODE
			registerModGroups();
			///
		}
	}
	//INSERTED CODE
	private void registerModGroups(){
		RegisterConfigGroupsEvent event = new RegisterConfigGroupsEvent(this);
		StarLoader.fireEvent(event, false);
		ObjectArrayFIFOQueue<ConfigGroup> modConfigGroups = event.getModConfigGroups();
		while (!modConfigGroups.isEmpty()) {
			this.add(modConfigGroups.dequeue());
		}
	}
	///

	public void checkClientRequest(ClientChannel clientChannel) {
		if(hashRequestedByClient == null || !hashRequestedByClient.equals(hash)){
			requestConfig(clientChannel);
			hashRequestedByClient = hash;
		}
	}
}
