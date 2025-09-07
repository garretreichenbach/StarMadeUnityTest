package org.schema.game.client.controller.manager.ingame;

import java.io.IOException;
import java.util.Collections;

import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.GeneralBlockStyle;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class BlockSyleSubSlotController {
	
	public final Int2ObjectOpenHashMap<GeneralBlockStyle> full;
	
	
	public BlockSyleSubSlotController() {
		super();
		this.full = getFull();
	}
	public short getBlockType(short type, int subSlot){
		return getSelectedStack(type)[subSlot];
	}
	public short[] getSelectedStack(short type){
		ObjectArrayList<GeneralBlockStyle> l = getPermanentList(type);
		ElementInformation info = ElementKeyMap.getInfo(type);
		
		short[] lst = new short[l.size()+1];
		
		for(int i = 0; i < l.size(); i++){
			if(l.get(i).getId() == 0){
				lst[i] = type;
			}else{
				for(short s : info.blocktypeIds){
					
					int oId = GeneralBlockStyle.getId(s);
					if(oId == l.get(i).getId()){
						lst[i] = s;
						break;
					}
				}
			}
			assert(lst[i] != 0):l.get(i);
		}
		lst[lst.length-1] = -1; //wildcard for the radial menu
		return lst;
	}
	public ObjectArrayList<GeneralBlockStyle> getPermanentList(short type){
		ObjectArrayList<GeneralBlockStyle> set = new ObjectArrayList<GeneralBlockStyle>(); 
		getSet(type, full, set);
		
		for(int i = 0; i < set.size(); i++){
			
			assert(full.get(set.get(i).getId()) != null):set.get(i)+" -> "+full;
			
			if(!full.get(set.get(i).getId()).permanent){
				set.remove(i);
				i--;
			}
		}
		Collections.sort(set);
		return set;
	}
	
	
	
	public static Int2ObjectOpenHashMap<GeneralBlockStyle> getFull(){
		int index = 0;
		Int2ObjectOpenHashMap<GeneralBlockStyle> g = new Int2ObjectOpenHashMap<GeneralBlockStyle>();
		for(BlockStyle s : BlockStyle.values()){
			GeneralBlockStyle b = new GeneralBlockStyle();
			b.blockStyle = s;
			b.index = index++;
			g.put(b.getId(), b);
		}
		for(int i = 0; i < 4; i++){
			GeneralBlockStyle b = new GeneralBlockStyle();
			b.blockStyle = BlockStyle.NORMAL;
			b.slab = i;
			if(!g.containsKey(b.getId())){
				b.index = index++;
				g.put(b.getId(), b);
			}
		}
		
		for(int i = 1; i < 20; i++){
			for(BlockStyle bs : BlockStyle.values()){
				GeneralBlockStyle b = new GeneralBlockStyle();
				b.blockStyle = bs;
				b.slab = 0;
				b.wildcard = i;
				if(!g.containsKey(b.getId())){
					b.index = index++;
					g.put(b.getId(), b);
				}
			}
		}
		g.get(0).permanent = true;
		
		System.err.println("LLL "+g);
		return g;
	}
	
	
	public static ObjectArrayList<GeneralBlockStyle> getSet(short type, Int2ObjectOpenHashMap<GeneralBlockStyle> full, ObjectArrayList<GeneralBlockStyle> out){
		ElementInformation info = ElementKeyMap.getInfo(type);
		
		out.clear();
		out.add(full.get(0));
		for(short s : info.blocktypeIds){
			GeneralBlockStyle gb = full.get(GeneralBlockStyle.getId(s));
			assert(gb != null):info+" -> "+ElementKeyMap.toString(s)+" ID "+GeneralBlockStyle.getId(s)+"; WildCardIndex: "+ElementKeyMap.getInfo(type).wildcardIndex+" ; ;; "+full;
			out.add(gb);
		}
		
		return out;
	}
	public void save() {
		StringBuffer b = new StringBuffer();
		ObjectIterator<GeneralBlockStyle> it = full.values().iterator();
		while(it.hasNext()){
			GeneralBlockStyle next = it.next();
			b.append(next.getId()+", "+next.permanent+", "+next.index);
			if(it.hasNext()){
				b.append("; ");
			}
		}
		EngineSettings.BLOCK_STYLE_PRESET.setString(b.toString());
		try {
			EngineSettings.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void load() {
		try{
			String l = EngineSettings.BLOCK_STYLE_PRESET.getString().trim();
			if(l.length() > 0){
				String[] split = l.split(";");
				
				for(String sss : split){
					
					String[] sp = sss.split(",");
					
					String idStr = sp[0];
					String act = sp[1];
					String indexStr = sp[2];
					
					int id = Integer.parseInt(idStr.trim());
					boolean perm = Boolean.parseBoolean(act.trim());
					int index = Integer.parseInt(indexStr.trim());
					GeneralBlockStyle generalBlockStyle = full.get(id);
					if(generalBlockStyle != null){
						generalBlockStyle.index = index;
						generalBlockStyle.permanent = perm;
					}
				}
			}
		}catch(Exception r){
			r.printStackTrace();
		}
	}
	public boolean isPerm(short type) {
		return full.get(GeneralBlockStyle.getId(type)).permanent;
	}
	public void switchPerm(short type) {
		GeneralBlockStyle g = full.get(GeneralBlockStyle.getId(type)); 
		g.permanent = !g.permanent;
		
		
		int sourceReference = ElementKeyMap.getInfo(type).getSourceReference();
		if(sourceReference == 0){
			sourceReference = type; //this is the source block
		}
		if(sourceReference > 0 && getSelectedStack((short)sourceReference).length <= 1){
			//turn on default
			full.get(GeneralBlockStyle.getId((short)sourceReference)).permanent = true; 
		}
		save();
		
	}
	
}
