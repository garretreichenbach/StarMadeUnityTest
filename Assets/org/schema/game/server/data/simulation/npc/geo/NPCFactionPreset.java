package org.schema.game.server.data.simulation.npc.geo;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.common.util.FileUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class NPCFactionPreset {
	public String factionPresetName;
	public BluePrintController blueprintController;
	public Document doc;
	public File confFile;

	public static boolean importPreset(String dirSource, String dirDest) {
		
		
		System.err.println("[SERVER][NPC] importing default preset: "+dirSource+" -> "+dirDest);
		
		File dirSourceF = new File(dirSource);
		File dirDestF = new File(dirDest);
		File[] sourceFiles = dirSourceF.listFiles();
		
		
		boolean hasBlueprints = false;
		boolean hasconfig = false;
		
		for(File source : sourceFiles){
			if(source.getName().toLowerCase(Locale.ENGLISH).equals("blueprints.zip")){
				try {
					assert(dirDest.endsWith(File.separator));
					String bbPath = dirDest+ "blueprints";
					File bb = new File(bbPath);
					bb.mkdirs();
					FileUtil.extract(source, bbPath);
					hasBlueprints = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(source.getName().toLowerCase(Locale.ENGLISH).equals("npcconfig.xml")){
				File d = new File(dirDest, "npcConfig.xml");
				try {
					FileUtil.copyFile(source, d);
					hasconfig = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		if(!hasBlueprints || !hasconfig){
			System.err.println("Exception: could not import NPC faction preset "+dirSourceF.getAbsolutePath()+"; hasBlueprints: "+hasBlueprints+"; hasConfig: "+hasconfig);
			try {
				FileUtil.deleteRecursive(dirDestF);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	public static NPCFactionPreset readFromDirOnlyConf(String path) throws IOException, SAXException, ParserConfigurationException {
		assert(path.endsWith(File.separator));
		File dir = new File(path);
		File[] listFiles = dir.listFiles();
		NPCFactionPreset p = new NPCFactionPreset();
		p.factionPresetName = dir.getName().toLowerCase(Locale.ENGLISH);
		File conf = new File(dir, "npcConfig.xml");
		if(!conf.exists() || conf.isDirectory()){
			throw new IOException("no config file (npcConfig.xml) in "+dir.getAbsolutePath());
		}
		p.parseNPCConfigPreset(conf);
		
		return p;
	}
	public static NPCFactionPreset readFromDir(String path) throws IOException, SAXException, ParserConfigurationException {
		assert(path.endsWith(File.separator));
		File dir = new File(path);
		File[] listFiles = dir.listFiles();
		NPCFactionPreset p = new NPCFactionPreset();
		p.factionPresetName = dir.getName().toLowerCase(Locale.ENGLISH);
		File conf = new File(dir, "npcConfig.xml");
		if(!conf.exists() || conf.isDirectory()){
			throw new IOException("no config file (npcConfig.xml) in "+dir.getAbsolutePath());
		}
		p.parseNPCConfigPreset(conf);
		
		File bbDir = new File(dir, "blueprints");
		if(!bbDir.exists() || !bbDir.isDirectory()){
			throw new IOException("no blueprints dir in "+dir.getAbsolutePath());
		}
		p.blueprintController =  new BluePrintController(path+"blueprints", GameServerState.SEGMENT_DATA_BLUEPRINT_PATH);
		
		if(p.blueprintController.readBluePrints().isEmpty()){
			throw new IOException("no valid blueprints found in blueprints dir of "+dir.getAbsolutePath());
		}
		
		for(BlueprintEntry e : p.blueprintController.readBluePrints()){
			if(e.getHeaderVersion() < 3 || !e.hadCargoByteRead()){
				try {
					throw new NPCBlueprintVersionException("WARNING for NPC preset "+dir.getName()+": Old blueprint version detected: "+e.getName()+"; Please load the blueprint and resave it for NPC use!");
				} catch (NPCBlueprintVersionException e1) {
					e1.printStackTrace();
				}
			}else{
				if(e.getClassification() == BlueprintClassification.CARGO && e.getTotalCapacity() == 0){
					try {
						throw new NPCBlueprintVersionException("WARNING for NPC preset "+dir.getName()+": Cargo ship has no capacity although it has CARGO classification: "+e.getName()+"; Please add some cargo to it for NPC use!");
					} catch (NPCBlueprintVersionException e1) {
						e1.printStackTrace();
					}
				}
//				System.err.println("BLUEPRINT OK: "+e.getName()+": Capacity: "+e.getTotalCapacity());
			}
		}
		return p;
	}

	private void parseNPCConfigPreset(File conf) throws SAXException, IOException, ParserConfigurationException {
		this.confFile = conf;
		System.err.println("[SERVER][NPCFACTIONS] Using Faction Config File "+conf.getAbsolutePath());
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	    DocumentBuilder db = dbf.newDocumentBuilder();
	    doc = db.parse(conf);
	}
}
