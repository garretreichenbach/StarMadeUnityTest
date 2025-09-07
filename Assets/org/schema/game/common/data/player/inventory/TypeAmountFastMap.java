package org.schema.game.common.data.player.inventory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class TypeAmountFastMap implements SerializationInterface{
	private final int[] am = new int[ElementKeyMap.highestType+1];
	private final Short2IntOpenHashMap extra = new Short2IntOpenHashMap();
	private final ShortArrayList indices = new ShortArrayList();
	
	
	public void fromTagStructure(Tag struct){
		
		Tag[] tags = (Tag[])struct.getValue();
		
		for(int i = 0; i < tags.length-1; i++){
			Tag typeAmount[] = (Tag[])tags[i].getValue();
			put((Short)typeAmount[0].getValue(), (Integer)typeAmount[1].getValue());
		}
	}
	public Tag toTagStructure(){
		Tag[] filMap = new Tag[size() + 1];
		filMap[filMap.length - 1] = FinishTag.INST;
		int i = 0;
		
		
		for (short index : indices) {
			
			short type = index;
			int amount = am[index];
			
			filMap[i] = new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.SHORT, null, type), new Tag(Type.INT, null, amount), FinishTag.INST});
			i++;
		}	
		
		for(Entry<Short, Integer> e : extra.entrySet()){
			short type = e.getKey();
			int amount = e.getValue();
			
			filMap[i] = new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.SHORT, null, type), new Tag(Type.INT, null, amount), FinishTag.INST});
			i++;
		}
		
		return new Tag(Type.STRUCT, null, filMap);
	}
	
	public boolean containsKey(short arg0) {
		if(!ElementKeyMap.isValidType(arg0)){
			return extra.containsKey(arg0);
		}
		return am[arg0] > 0;
	}
	
	
	
	public int get(short arg0) {
		if(!ElementKeyMap.isValidType(arg0)){
			return extra.get(arg0);
		}
		return am[arg0];
	}
	
	public int put(short arg0, int arg1) {
		if(!ElementKeyMap.isValidType(arg0)){
			if(arg0 < 0){
				return extra.put(arg0, arg1);
			}
			return 0;
		}
		if(arg1 <= 0){
			return remove(arg0);
		}
		int r = am[arg0];
		am[arg0] = arg1;
		if(r == 0 && arg1 > 0){
			indices.add(arg0);
		}
		
		return r;
	}
	
	public int remove(short arg0) {
		if(!ElementKeyMap.isValidType(arg0)){
			return extra.remove(arg0);
		}
		int r = am[arg0];
		if(r > 0){
			am[arg0] = 0;
			int indexOf = indices.indexOf(arg0);
			if(indexOf >= 0){
				indices.removeShort(indexOf);
			}
		}
		return r;
	}
	
	public void clear() {
		Arrays.fill(am, 0);
		indices.clear();
		extra.clear();
	}
	
	public boolean containsKey(Object arg0) {
		if(!ElementKeyMap.isValidType((Short)arg0)){
			return extra.containsKey(arg0);
		}
		return am[(Short)arg0] > 0;
	}
	
	public Integer getObj(Object arg0) {
		if(!ElementKeyMap.isValidType((Short)arg0)){
			return extra.get(arg0);
		}
		return am[(Short)arg0];
	}
	
	public Integer putInt(Short arg0, Integer arg1) {
		if(!ElementKeyMap.isValidType(arg0)){
			if(arg0 < 0){
				return extra.put(arg0, arg1);
			}
			return 0;
		}
		if(arg1 <= 0){
			return remove(arg0.shortValue());
		}
		int r = am[arg0];
		am[arg0] = arg1;
		if(r == 0 && arg1 > 0){
			indices.add(arg0.shortValue());
		}
		return r;
	}
	
	public Integer removeObj(Object arg0) {
		if(!ElementKeyMap.isValidType((Short)arg0)){
			return extra.remove(arg0);
		}
		int r = am[(Short)arg0];
		if(r > 0){
			am[(Short)arg0] = 0;
			int indexOf = indices.indexOf(arg0);
			if(indexOf >= 0){
				indices.removeShort(indexOf);
			}
		}
		return r;
	}
	
	public int size() {
		return indices.size() + extra.size();
	}
	
	public boolean isEmpty() {
		return indices.isEmpty() && extra.isEmpty();
	}
	public ShortList getTypes() {
		return indices;
	}
	public Short2IntOpenHashMap getMapInstance() {
		Short2IntOpenHashMap map = new Short2IntOpenHashMap(size());
		for(short s : indices){
			map.put(s, am[s]);
		}
		map.putAll(extra);
		return map;
	}
	public void putMap(Short2IntOpenHashMap map) {
		for(Entry<Short, Integer> e : map.entrySet()){
			put(e.getKey(), e.getValue());
		}
	}
	public Short2IntOpenHashMap getExtra() {
		return extra;
	}

	public void handleLoop(TypeAmountLoopHandle handle){
//		System.err.println("HANDLE LOOP: "+indices);
		final int size = indices.size();
		for(int i = 0; i < size; i++){
			short type = indices.getShort(i);
			int amount = am[type];
			handle.handle(type, amount);
		}
		ObjectSet<it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry> entrySet = extra.short2IntEntrySet();
		
		for(it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry e : entrySet){
			handle.handle(e.getShortKey(), e.getIntValue());
		}
	}
	@Override
	public void serialize(final DataOutput b, boolean isOnServer) throws IOException {
		b.writeShort(size());
		handleLoop((type, amount) -> {
			try {
				b.writeShort(type);
				b.writeInt(amount);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		short filterCount = b.readShort();
		if (filterCount > 0) {
			for (int i = 0; i < filterCount; i++) {
				put(b.readShort(), b.readInt());
			}
		}			
	}
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("[");
		for(int i = 0; i < indices.size(); i++) {
			b.append(ElementKeyMap.toString(indices.getShort(i))+"->"+am[indices.getShort(i)]);
			if(i < indices.size()-1) {
				b.append(", ");
			}
		}
		b.append("]");
		return b.toString();
	}
	
	
}
