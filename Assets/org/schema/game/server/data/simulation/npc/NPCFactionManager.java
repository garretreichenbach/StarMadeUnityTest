package org.schema.game.server.data.simulation.npc;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.schema.common.config.ConfigParserException;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.database.SystemInDatabase;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.npc.geo.NPCFactionPreset;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NPCFactionManager implements DiskWritable{
	private static final byte TAG_VERSION = 0;
	private static final String TAG_FILE_NAME = "NPCFACTIONS";
	private final GameServerState state;

	
	private List<NPCFaction> factions = new ObjectArrayList<NPCFaction>();
	private final Galaxy galaxy;
	private long lastUpdate;
	
	public NPCFactionManager(GameServerState state, Galaxy galaxy) {
		this.state = state;
		this.galaxy = galaxy;
		
		this.lastUpdate = System.currentTimeMillis();
		if(!galaxy.galaxyPos.equals(0, 0, 0) || EngineSettings.SECRET.getString().toLowerCase(Locale.ENGLISH).contains("nonpc")){
			System.err.println("[SERVER][GALAXY] not creating factions for "+galaxy.galaxyPos);
			return;
		}
		for(Faction f : state.getFactionManager().getFactionCollection()){
			if(f instanceof NPCFaction){
				if(((NPCFaction)f).serverGalaxyPos.equals(galaxy.galaxyPos)){
					factions.add(((NPCFaction)f));
				}
			}
		}
		
//		if(EngineSettings.SECRET.getCurrentState().toString().contains("NPCSYS")){
			try {
				Tag t = Tag.readFrom(
						new BufferedInputStream(
								new FileInputStream(getFileNameTag())), true, false);
				
				fromTagStructure(t);
				
			} catch (FileNotFoundException e) {
				System.err.println("[SERVER][NPCFACTIONS] no previous file existed. Generating NPC factions freshly");
				
				try {
					if(factions.isEmpty()){
						NPCStartupConfig g = new NPCStartupConfig();
						 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					    DocumentBuilder db = dbf.newDocumentBuilder();
					    
					    File f = new File(NPCStartupConfig.customPath);
					    
					    if(!f.getParentFile().exists()){
					    	f.getParentFile().mkdirs();
					    	
					    	File howTo = new File(f.getParentFile(), "HOWTO.txt");
					    	
					    	BufferedWriter br = new BufferedWriter(new FileWriter(howTo));
					    	br.write("Copy the startupConfig and folders over from ./data/npcConfigs to use them as your default npc configurations");
					    	
					    	br.close();
					    }
					    
					    if(!f.exists()){
					    	f = new File(NPCStartupConfig.dataPath);
					    	System.err.println("[SERVER] USING DEFAULT NPC FACTION STARTUP CONFIG: "+f.getAbsolutePath());
					    }else{
					    	System.err.println("[SERVER] USING CUSTOM NPC FACTION STARTUP CONFIG: "+f.getAbsolutePath());
					    }
					    
					    Document doc = db.parse(f);
						g.parse(doc);
						
						assert(g.spawns.size() > 0):"No Spawns";
						
						for(NPCFactionSpawn spawn : g.spawns){
							try {
								add(galaxy, spawn);
							} catch (NPCSpawnException e1) {
								e1.printStackTrace();
							}
						}
					}
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (ConfigParserException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (SAXException e1) {
					e1.printStackTrace();
				} catch (ParserConfigurationException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
//		}
	}
	public void write() {
		
	}
	public static String getFileNameTag(Vector3i galaxyPos){
		return TAG_FILE_NAME+"_"+galaxyPos.x+"_"+galaxyPos.y+"_"+galaxyPos.z+".tag";
	}
	public void removeFaction(Faction f){
		factions.remove(f);
	}
	public void updateLocal(long time){
		
		if(time - lastUpdate > 3000){
			
//			assert(checkTradeNodes());
			for(int i = 0; i < factions.size(); i++){
				NPCFaction f = factions.get(i);
				f.getFleetManager().update(time);
				f.accumulatedTimeNPCFactionTurn += time - lastUpdate;
				if(f.accumulatedTimeNPCFactionTurn > f.getTimeBetweenFactionTurns()){
					System.err.println("[SERVER][NPC] scheduling faction turn for "+f);
					state.getFactionManager().scheduleTurn(f);
					f.accumulatedTimeNPCFactionTurn = 0;
				}
				
				f.getDiplomacy().update(time);
			}
			lastUpdate = time;
		}
	}
	
	public boolean checkTradeNodes(){
		for(NPCFaction f : factions){
			assert(f.getTradeNode() != null):f;
			if(f.getTradeNode()  == null){
				return false;
			}
		}
		return true;
	}
	private boolean isLimited(){
		return ServerConfig.NPC_FACTION_SPAWN_LIMIT.getInt() >= 0
				&& factions.size() >= ServerConfig.NPC_FACTION_SPAWN_LIMIT.getInt();
	}
	
	public void add(Galaxy galaxy, NPCFactionSpawn spawn) throws NPCSpawnException{
		if(isLimited()){
			throw new NPCSpawnException("Cannot spawn more faction. limited by server.cfg");
		}
		
		
		Random r = new Random(galaxy.getSeed()*getFactionManager().currentFactionIdCreator);
		
		Vector3i pos = null;
		
		
		Vector3i tmp = new Vector3i();

		if(!spawn.randomSpawn){
			SystemInDatabase system = state.getDatabaseIndex().getTableManager().getSystemTable().getSystem(spawn.fixedSpawnSystem);
			if(system == null || system.factionUID == 0 || system.factionUID == getFactionManager().currentFactionIdCreator){
				pos = new Vector3i(spawn.fixedSpawnSystem);
			}
		}
		
		while(pos == null){
			int rawPosIndex = galaxy.getStarIndices().getInt(r.nextInt(galaxy.getStarIndices().size()));
			galaxy.getNormalizedPosFromIndex(rawPosIndex, tmp);
			for(Faction other : factions){
				if(((NPCFaction)other).npcFactionHomeSystem.equals(tmp)){
					continue;
				}
			}
			SystemInDatabase system = state.getDatabaseIndex().getTableManager().getSystemTable().getSystem(tmp);
			
			if(system == null || system.factionUID == 0 || system.factionUID == getFactionManager().currentFactionIdCreator){
				pos = new Vector3i(tmp); 
			}
		}
		
		
		try{
			NPCFactionPreset preset;
			if(spawn.possiblePresets == null || spawn.possiblePresets.trim().length() == 0){
				preset = state.getFactionManager()
					.npcFactionPresetManager
					.getRandomLeastUsedPreset(galaxy.getSeed(), getFactionManager().currentFactionIdCreator);
			}else{
				List<NPCFactionPreset> npcPresets = state.getFactionManager()
				.npcFactionPresetManager.getNpcPresets();
				List<NPCFactionPreset> possible = new ObjectArrayList<NPCFactionPreset>();
				
				String[] l;
				if(spawn.possiblePresets.contains(",")){
					l = spawn.possiblePresets.split(",");
				}else{
					l = new String[]{spawn.possiblePresets};
				}
				
				for(int i = 0; i < npcPresets.size(); i++){
					NPCFactionPreset p = npcPresets.get(i);
					for(String s : l){
						if(p.factionPresetName.trim().toLowerCase(Locale.ENGLISH).equals(s.trim().toLowerCase(Locale.ENGLISH))){
							possible.add(p);
						}
					}
				}
				
				if(possible.isEmpty()){
					try{
						throw new Exception("[ERROR] Exception: No NPC Faction Preset found for "+spawn.name+"; But should be: "+spawn.possiblePresets+"; Picking random one from existing");
					}catch(Exception e){
						e.printStackTrace();
					}
					preset = state.getFactionManager()
							.npcFactionPresetManager
							.getRandomLeastUsedPreset(galaxy.getSeed(), getFactionManager().currentFactionIdCreator);
				}else{
					preset = possible.get(r.nextInt(possible.size()));
					System.err.println("[SERVER][NPCFACTION] picked preset "+preset.factionPresetName+" for Faction "+spawn.name);
				}
			}
			
			System.err.println("[SERVER][NPCFACTIONS] creating NPC faction. Created: "+factions.size()+"; position: "+pos);
			NPCFaction fac = new NPCFaction(state, getFactionManager().currentFactionIdCreator, 
					spawn.name.trim(), spawn.description.trim(), pos);
			fac.serverGalaxyPos = new Vector3i(galaxy.galaxyPos);
			fac.initializeWithState(state);
			fac.setConfig(new NPCFactionConfig());
			fac.getConfig().setPreset(preset);
			fac.initialize();
			
			fac.initialSpawn = spawn.initialGrowth;
			assert(preset.blueprintController != null);
			
			fac.onCreated();
			getFactionManager().addFaction(fac);
			factions.add(fac);
		}catch(Exception e){
			throw new NPCSpawnException(e);
		}
		getFactionManager().currentFactionIdCreator++;
	}
	
//	public boolean createNew(Galaxy galaxy) throws IllegalArgumentException, IllegalAccessException, ConfigParserException{
//		if(factions.size() < galaxy.getNpcNodes().size() && !isLimited()){
//			
//			
//			NPCFactionPreset preset = state.getFactionManager()
//					.npcFactionPresetManager.getRandomLeastUsedPreset(galaxy.getSeed(), currentFactionIdCreator);
//			
//			
//			Vector3i pos = galaxy.getNpcNodes().get(factions.size());
//			
//			System.err.println("[SERVER][NPCFACTIONS] creating NPC faction. Created: "+factions.size()+"; Nodes: "+galaxy.getNpcNodes().size()+"; position: "+pos);
//			NPCFaction fac = new NPCFaction(state, currentFactionIdCreator, "FAC-"+currentFactionIdCreator, "NPC Faction", pos);
//			fac.serverGalaxyPos = new Vector3i(galaxy.galaxyPos);
//			fac.initializeWithState(state);
//			fac.setConfig(new NPCFactionConfig());
//			fac.getConfig().setPreset(preset);
//			fac.initialize();
//			assert(preset.blueprintController != null);
//			
//			fac.onCreated();
//			getFactionManager().addFaction(fac);
//			factions.add(fac);
//			
//			currentFactionIdCreator++;
//			return true;
//		}else{
//			return false;
//		}
//	}
	
	@Override
	public String getUniqueIdentifier() {
		return null;
	}
	public FactionManager getFactionManager(){
		return state.getFactionManager();
	}
	@Override
	public boolean isVolatile() {
		return false;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[])tag.getValue();
		final byte version = (Byte) t[0].getValue();
	}
	

	@Override
	public Tag toTagStructure() {
		
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.BYTE, null, TAG_VERSION),
			FinishTag.INST});
	}
	public String getFileNameTag() {
		return getFileNameTag(galaxy.galaxyPos);
	}
	public void onSystemOwnershipChanged(int ownerFactionBef, int ownerFactionNow,
			Vector3i pos) {
//		Vector3i t = new Vector3i();
//		if(ownerFactionBef != ownerFactionNow){
//			{
//				Faction ownedBef = state.getFactionManager().getFaction(ownerFactionBef);
//				if(ownedBef != null){
//					for(int i = 0; i < 6; i++){
//						t.set(pos);
//						t.add(Element.DIRECTIONSi[i]);
//						boolean found = false;
//						for(NPCFaction f : factions){
//							NPCSystem system = f.structure.getSystem(t);
//							if(system != null){
//								if(ownedBef.isNPC()){
//									((NPCFaction)ownedBef).modCloseTerritoryFactions(f.getIdFaction(), -1);
//								}
//								f.modCloseTerritoryFactions(ownedBef.getIdFaction(), -1);
//								found = true;
//								break;
//							}
//						}
//						if(found){
//							break;
//						}
//					}
//				}
//			}
//			{
//				Faction ownedNow = state.getFactionManager().getFaction(ownerFactionBef);
//			
//				if(ownedNow != null){
//					for(int i = 0; i < 6; i++){
//						t.set(pos);
//						t.add(Element.DIRECTIONSi[i]);
//						boolean found = false;
//						for(NPCFaction f : factions){
//							NPCSystem system = f.structure.getSystem(t);
//							if(system != null){
//								if(ownedNow.isNPC()){
//									((NPCFaction)ownedNow).modCloseTerritoryFactions(f.getIdFaction(), 1);
//								}
//								f.modCloseTerritoryFactions(ownedNow.getIdFaction(), 1);
//							
//								found = true;
//								break;
//							}
//						}
//						if(found){
//							break;
//						}
//					}
//				}
//			}
//		}
	}
	
	
	
}
