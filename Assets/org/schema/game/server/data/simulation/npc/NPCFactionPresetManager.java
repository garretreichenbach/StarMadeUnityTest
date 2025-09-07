package org.schema.game.server.data.simulation.npc;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.schema.common.util.data.DataUtil;
import org.schema.game.server.data.simulation.npc.geo.NPCFactionPreset;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.LoadingScreen;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class NPCFactionPresetManager {
	public static String npcPathReal;
	public static final String npcPathPre = "."+File.separator+DataUtil.dataPath+"npcFactions"+File.separator;
	private static final byte TAG_VERSION = 0;
	
	private final List<NPCFactionPreset> npcPresets = new ObjectArrayList<NPCFactionPreset>();
	private final Object2IntOpenHashMap<String> takenNPCPresets = new Object2IntOpenHashMap<String>();
	
	
	public static void importPresets(String path){
		File pre = new File(npcPathPre);
		File[] dirPre = pre.listFiles();
		
		
		npcPathReal = path+File.separator+"npcFactions"+File.separator;
		File real = new File(npcPathReal);
		real.mkdir();
		File[] dirReal = real.listFiles(); 
		
		
		
		ObjectOpenHashSet<String> realSet = new ObjectOpenHashSet<String>();
		for(File r : dirReal){
			if(r.isDirectory()){
				realSet.add(r.getName().toLowerCase(Locale.ENGLISH));
			}
		}
		
		int imported = 0;
		for(File p : dirPre){
			if(p.isDirectory() && !realSet.contains(p.getName().toLowerCase(Locale.ENGLISH))){
				LoadingScreen.serverMessage = Lng.str("Preparing npc assets (takes a bit the first time)");
				boolean importPreset = NPCFactionPreset.importPreset(npcPathPre+p.getName()+File.separator, npcPathReal+p.getName()+File.separator);
				if(importPreset){
					imported++;
				}
			}
		}
	}
	public void readNpcPresets(){
		File real = new File(npcPathReal);
		File[] dirReal = real.listFiles(); 
		for(File r : dirReal){
			if(r.isDirectory()){
				NPCFactionPreset p;
				try {
					p = NPCFactionPreset.readFromDir(npcPathReal+r.getName()+File.separator);
					npcPresets.add(p);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			
			}
		}
		assert(npcPresets.size() > 0);
	}
	public NPCFactionPreset getRandomLeastUsedPreset(long universeSeed, int facSeed){
		assert(npcPresets != null);
		Random r = new Random(universeSeed+facSeed);
		
		int min = Integer.MAX_VALUE;
		
		for(NPCFactionPreset pre : npcPresets){
			assert(pre.factionPresetName.equals(pre.factionPresetName.toLowerCase(Locale.ENGLISH)));
			if(!takenNPCPresets.containsKey(pre.factionPresetName.toLowerCase(Locale.ENGLISH))){
				takenNPCPresets.put(pre.factionPresetName, 0);
			}
		}
		
		for(Entry<String, Integer> e : takenNPCPresets.entrySet()){
			min = Math.min(e.getValue(), min);
		}
		
		List<String> p = new ObjectArrayList<String>();
		for(Entry<String, Integer> e : takenNPCPresets.entrySet()){
			if(e.getValue().intValue() == min){
				p.add(e.getKey());
			}
		}
		assert(p.size() > 0):p+"; "+takenNPCPresets;
		
		String selected = p.get(r.nextInt(p.size()));
		
		for(NPCFactionPreset pre : npcPresets){
			if(pre.factionPresetName.equals(selected)){
				takenNPCPresets.addTo(pre.factionPresetName, 1);
				return pre;
			}
		}
		throw new IllegalArgumentException("No faction preset found");
	}
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[])tag.getValue();
		final byte version = (Byte) t[0].getValue();
		Tag.fromString2IntMapToTagStruct(t[1], takenNPCPresets);
	}
	

	public Tag toTagStructure() {
		
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.BYTE, null, TAG_VERSION),
			Tag.string2IntMapToTagStruct(takenNPCPresets),
			FinishTag.INST});
	}
	public List<NPCFactionPreset> getNpcPresets() {
		return npcPresets;
	}
}
