package org.schema.game.common.controller.rules;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.schema.common.SerializationInterface;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.remote.RemoteRuleRuleProperty;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RulePropertyContainer implements TagSerializable, SerializationInterface{

	
	public byte VERSION = 0;
	private final Object2ObjectOpenHashMap<String, RuleProperty> ruleUIDlkMap = new Object2ObjectOpenHashMap<String, RuleProperty>();
	
	public static String getPropertiesPath() {
		return GameServerState.ENTITY_DATABASE_PATH+File.separator+"ruleproperties.tag";
	}
	public final RuleSetManager manager;
	public GameStateInterface state;
	private boolean changed;
	private final ObjectArrayFIFOQueue<RuleProperty> receivedRuleProperties = new ObjectArrayFIFOQueue<RuleProperty>();
	public RulePropertyContainer(RuleSetManager man) {
		this.manager = man;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(ruleUIDlkMap.size());
		for(RuleProperty l : ruleUIDlkMap.values()) {
			l.serialize(b, isOnServer);
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		final int size = b.readInt();
		
		for(int i = 0; i < size; i++) {
			RuleProperty p = new RuleProperty();
			p.deserialize(b, updateSenderStateId, isOnServer);
			RuleSet ruleSet = manager.getRuleSetByUID(p.receivedRuleSetUID);
			
			if(ruleSet != null) {
				p.ruleSet = ruleSet;
				ruleUIDlkMap.put(p.ruleSet.uniqueIdentifier.toLowerCase(Locale.ENGLISH), p);
			}else {
				try {
					throw new Exception("Received property RuleSet not found: "+p.receivedRuleSetUID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] s = tag.getStruct();
		byte version = s[0].getByte();
		Tag[] conts = s[1].getStruct();
		for(int i = 0; i < conts.length-1; i++) {
			RuleProperty p = new RuleProperty();
			p.fromTagStructure(conts[i]);
			
			RuleSet ruleSet = manager.getRuleSetByUID(p.receivedRuleSetUID);
			
			if(ruleSet != null) {
				p.ruleSet = ruleSet;
				ruleUIDlkMap.put(p.ruleSet.uniqueIdentifier.toLowerCase(Locale.ENGLISH), p);
			}else {
				try {
					throw new Exception("Received property RuleSet not found: "+p.receivedRuleSetUID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag[] t = new Tag[ruleUIDlkMap.size()+1];
		int i = 0;
		for(RuleProperty p : ruleUIDlkMap.values()) {
			t[i] = p.toTagStructure();
			i++;
		}
		t[i] = FinishTag.INST;
		
		return new Tag(Type.STRUCT, null, new Tag[] {
			new Tag(Type.BYTE, null, VERSION),
			new Tag(Type.STRUCT, null, t),
			FinishTag.INST,
		});
	}
	public void loadFromDisk(String propertiesPath) throws IOException {
		File f = new File(propertiesPath);
		if(f.exists()) {
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(f));
			Tag tag = Tag.readFrom(is, true, false);
			fromTagStructure(tag);
		}else {
			System.err.println("[RULES][PROPERTIES] no properties read. File doesn't exist: "+f.getCanonicalPath()+"; Will be created on save");
		}
	}
	public void saveToDisk(String propertiesPath) throws IOException {
		File f = new File(propertiesPath);
		if(f.exists()) {
			f.delete();
		}
		BufferedOutputStream is = new BufferedOutputStream(new FileOutputStream(f));
		toTagStructure().writeTo(is, true);
	}
	public RuleProperty getProperty(RuleSet o) {
		assert(o.uniqueIdentifier != null);
		assert(ruleUIDlkMap != null);
		RuleProperty ruleProperty = ruleUIDlkMap.get(o.uniqueIdentifier.toLowerCase(Locale.ENGLISH));
		if(ruleProperty == null) {
			ruleProperty = new RuleProperty(o);
			ruleUIDlkMap.put(o.uniqueIdentifier.toLowerCase(Locale.ENGLISH), ruleProperty);
		}
		return ruleProperty;
	}
	public byte getSubType(RuleSet o) {
		RuleProperty property = getProperty(o);
		return property.subType;
	}
	public boolean isGlobal(RuleSet o) {
		RuleProperty property = getProperty(o);
		return property.global;
	}
	public void setGlobal(RuleSet o, boolean global, boolean send) {
		RuleProperty property = getProperty(o);
		property.global = global;
//		System.err.println("SET GLOBAL FOR "+o.uniqueIdentifier+" -> "+global);
		if(send) {
			assert(state != null);
			assert(state.getGameState().getNetworkObject().rulePropertyBuffer != null);
			state.getGameState().getNetworkObject().rulePropertyBuffer.add(new RemoteRuleRuleProperty(property, state.getGameState().isOnServer()));
		}
			
	}
	public List<RuleSet> getGlobalRules(byte subType, List<RuleSet> out) {
//		System.err.println("GET GLOBAL RULES "+ruleUIDlkMap+"; "+subType);
		for(RuleProperty p : ruleUIDlkMap.values()) {
			if(p.global && (p.subType == RuleProperty.ALL_SUBTYPES || p.subType == subType)) {
				assert(p.ruleSet != null):p.receivedRuleSetUID;
				out.add(p.ruleSet);
			}
		}
		return out;
	}
	public void setSubAllTypes(RuleSet o, boolean send) {
		setSubTypes(o, RuleProperty.ALL_SUBTYPES, send);
	}
	public void setSubTypes(RuleSet o, byte subType, boolean send) {
		RuleProperty property = getProperty(o);
		property.subType = subType;
		if(send) {
			state.getGameState().getNetworkObject().rulePropertyBuffer.add(new RemoteRuleRuleProperty(property, state.getGameState().isOnServer()));
		}
		
	}
	public void initializeOnServer() {
		try {
			loadFromDisk(getPropertiesPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void update() {
		
		while(!receivedRuleProperties.isEmpty()) {
			RuleProperty p = receivedRuleProperties.dequeue();
			p.ruleSet = manager.getRuleSetByUID(p.receivedRuleSetUID);
			if(p.ruleSet != null) {
				ruleUIDlkMap.put(p.ruleSet.uniqueIdentifier.toLowerCase(Locale.ENGLISH), p);
				if(state.getGameState().isOnServer()) {
					//delegate to clients
					state.getGameState().getNetworkObject().rulePropertyBuffer.add(new RemoteRuleRuleProperty(p, state.getGameState().isOnServer()));
				}
				changed = true;
			}
		}
		
		if(changed) {
			manager.flagChanged((StateInterface) state);
			if(state.getGameState().isOnServer()){
				try {
					saveToDisk(getPropertiesPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			changed = false;
		}
	}
	
	public void updateFromNetworkObject(NetworkGameState o) {
		ObjectArrayList<RemoteRuleRuleProperty> r = o.rulePropertyBuffer.getReceiveBuffer();
		for(int i = 0; i < r.size(); i++) {
			receivedRuleProperties.enqueue(r.get(i).get());
		}
	}
	public void initFromNetworkObject(NetworkGameState o) {
		updateFromNetworkObject(o);
	}
	public void sendAll(NetworkGameState o) {
		for(RuleProperty p : ruleUIDlkMap.values()) {
			o.rulePropertyBuffer.add(new RemoteRuleRuleProperty(p, state.getGameState().isOnServer()));
		}
	}
}
