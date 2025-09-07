package org.schema.game.common.data.blockeffects.config;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.StatusEffectApplyListener;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameterType;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;

import javax.vecmath.Vector3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigEntityManager extends GUIObservable{

	private final ShortOpenHashSet transientEffects = new ShortOpenHashSet();
	private final ShortOpenHashSet permanentEffects = new ShortOpenHashSet();


	private final List<ConfigProviderSource> transientEffectSources = new ObjectArrayList<ConfigProviderSource>();
	private boolean effectsChanged = true; 
	
	private final EffectAccumulator effectAccumulator = new EffectAccumulator();

	private final EffectEntityType entityType;

	private final long entityId;
	private final ConfigPoolProvider provider;
	
	private static final byte[] buffer = new byte[1024*128];

	public String entityName;
	
	public enum EffectEntityType{
		OTHER,
		STRUCTURE,
		SECTOR,
		SYSTEM
	}
	
	public ConfigEntityManager(long entityId, EffectEntityType t, ConfigPoolProvider provider){
		
		this.entityId = entityId;
		this.entityType = t;
		
		this.provider = provider;
		
	}
	public void saveToDatabase(GameServerState state){
		try {
			state.getDatabaseIndex().getTableManager().getEntityEffectTable().writeEffects(this, entityId, entityType);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void loadFromDatabase(GameServerState state){
		try {
			state.getDatabaseIndex().getTableManager().getEntityEffectTable().loadEffects(this, entityId, entityType);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ConfigPool getConfigPool(){
		return provider.getConfigPool();
	}
	
	
	
	public void updateToFullNetworkObject(EffectConfigNetworkObjectInterface e){
		for(short s : transientEffects){
			e.getEffectAddBuffer().add((short) -s);
		}
		for(short s : permanentEffects){
			e.getEffectAddBuffer().add(s);
		}
	}
	public void updateToNetworkObject(EffectConfigNetworkObjectInterface e){
		
	}
	public void updateFromNetworkObject(EffectConfigNetworkObjectInterface e){
		boolean changed = false;
		{
			ShortArrayList r = e.getEffectAddBuffer().getReceiveBuffer();
			for(int i = 0; i < r.size(); i++){
				short s = r.getShort(i);
//				System.err.println("[EFFECTCONFIG] "+entityName+" RECEIVED EFFECT ADD: "+s);
				addEffect((short) Math.abs(s), s > 0);
				changed = true;	
			}
		}
		{
			ShortArrayList r = e.getEffectRemoveBuffer().getReceiveBuffer();
			for(int i = 0; i < r.size(); i++){
				short s = r.getShort(i);
//				System.err.println("[EFFECTCONFIG] "+entityName+" RECEIVED EFFECT REMOVE: "+s);
				removeEffect((short) Math.abs(s), s > 0);
				changed = true;
			}
		
		}
		
		if(changed){
			effectsChanged = true;
			notifyObservers();
		}
	}
	public void addEffect(short s, boolean permanent) {
		if(permanent){
			permanentEffects.add(s);
		}else{
			transientEffects.add(s);
		}
		effectsChanged = true;
	}


	public void initFromNetworkObject(EffectConfigNetworkObjectInterface e){
		updateFromNetworkObject(e);
	}
	private ShortOpenHashSet rmTmp = new ShortOpenHashSet();
	private ShortList addTmp = new ShortArrayList();
	public void addTransientEffects(ConfigManagerInterface e){
		//make sure that a change is only flagged when effects change
		
		transientEffectSources.clear();
		final boolean permanent = false;
		e.registerTransientEffects(transientEffectSources);
		
		//copy to temp set so we know which effects are no longer active after adding
		rmTmp.clear();
		rmTmp.addAll(transientEffects);
		
		final int size = transientEffectSources.size();
		for(int i = 0; i < size; i++){
			ConfigProviderSource s = transientEffectSources.get(i);
			addTmp.clear();
			ShortList p = s.getAppliedConfigGroups(addTmp);
			
			final int sSize = p.size();
			for(int k = 0; k < sSize; k++){
				short g = p.getShort(k);
				if(!transientEffects.contains(g)){
					//at least one effect added
					
					addEffect(g, permanent);
				}
				rmTmp.remove(g);
			}
		}
//		if(transientEffectSources.size() > 0){
//			System.err.println("EFFECT SOURCES: "+e+" "+e.getState()+": "+transientEffectSources.size()+"; Trans: "+transientEffects.size());
//		}
		for(short s : rmTmp){
			//at least one effect removed
			removeEffect(s, permanent);
		}
		
		
	}
	
	public void updateLocal(Timer t, ConfigManagerInterface e){
		if(getConfigPool() == null || getConfigPool().pool.isEmpty()){
			return;
		}
		if(e != null){
			addTransientEffects(e);
		}
		if(effectsChanged){
			recalculateEffects();
			effectsChanged = false;
			notifyObservers();
//			if((transientEffects.size()+permanentEffects.size()) > 0){
//				System.err.println("RECALC EFFECTS: "+entityName+"; TO BE -> ACTIVE: "+(transientEffects.size()+permanentEffects.size())+" -> "+effectAccumulator.getActive().size()+"; not found: "+effectAccumulator.getNotFound().size());
//			}else{
//				System.err.println("RECALC EFFECTS: ZERO EFFECTS");
//			}
		}
	}
	private void recalculateEffects() {
		ShortOpenHashSet total = new ShortOpenHashSet(transientEffects.size()+permanentEffects.size());
		total.addAll(transientEffects);
		total.addAll(permanentEffects);
		effectAccumulator.calculate(total, getConfigPool());
	}
	

	public void addEffectAndSend(ConfigGroup g, boolean permanent, EffectConfigNetworkObjectInterface e){
		addEffect(g.ntId, permanent);
		e.getEffectAddBuffer().add((short) (permanent ? g.ntId : -g.ntId));
		effectsChanged = true;
	}
	public void removeEffectAndSend(ConfigGroup g, boolean permanent, EffectConfigNetworkObjectInterface e){
		removeEffect(g.ntId, permanent);
		e.getEffectRemoveBuffer().add((short) (permanent ? g.ntId : -g.ntId));
		effectsChanged = true;
	}
	public byte[] serializeByID() throws IOException{
		synchronized(buffer){
			FastByteArrayOutputStream b = new FastByteArrayOutputStream(buffer);
			serializeByID(new DataOutputStream(b));
			byte[] a = new byte[(int) b.position()];
			System.arraycopy(buffer, 0, a, 0, a.length);
			return a;
		}
	}
	public void serializeByID(DataOutput out) throws IOException{
		List<ConfigGroup> active = new ObjectArrayList<ConfigGroup>(effectAccumulator.getActive());
		out.writeShort(active.size());
		for(ConfigGroup g : active){
			out.writeUTF(g.id);
		}
	}
	public List<ConfigGroup> loadByID(DataInput out) throws IOException{
		List<ConfigGroup> active = new ObjectArrayList<ConfigGroup>();
		int size = out.readShort();
		for(int i = 0; i < size; i++){
			String id = out.readUTF();
			ConfigGroup configGroup = getConfigPool().poolMapLowerCase.get(id.toLowerCase(Locale.ENGLISH));
			if(configGroup != null){
				active.add(configGroup);
			}
		}
		return active;
	}


	public boolean isActive(ConfigGroup f) {
//		System.err.println("IS ACTI: "+f.id+": "+effectAccumulator.getActive().contains(f)+"; ");
		return effectAccumulator.getActive().contains(f);
	}


	public void removeEffect(short s, boolean permanent) {
		if(permanent){
			permanentEffects.remove(s);
		}else{
			transientEffects.remove(s);
		}
		effectsChanged = true;
	}


	public Map<StatusEffectType, EffectModule> getModules() {
		return effectAccumulator.getModules();
	}
	public List<EffectModule> getModulesList() {
		return effectAccumulator.getModulesList();
	}
	public void addByID(String effectID, boolean permanent) {
		ConfigGroup configGroup = getConfigPool().poolMapLowerCase.get(effectID.toLowerCase(Locale.ENGLISH));
		if(configGroup != null){
			//this happens before inital sending, so no need to send
			addEffect(configGroup.ntId, permanent);
		}else{
			System.err.println("[EFFECT] couldn't add config group '"+effectID+"'. NOT FOUND");
		}
	}
	public List<ConfigGroup> getPermanentEffects() {
		List<ConfigGroup> l = new ObjectArrayList<ConfigGroup>();
		for(short s : permanentEffects){
			ConfigGroup configGroup = getConfigPool().ntMap.get(s);
			if(configGroup != null){
				l.add(configGroup);
			}
		}
		return l;
	}
	@Override
	public String toString(){
		return entityName != null ? entityName : Lng.str("Unknown Entity");
	}

	public boolean hasEffect(StatusEffectType type) {
		return apply(type, false);
	}

	public Vector3f apply(StatusEffectType e, Vector3f input) {
		EffectModule effectModule = getModules().get(e);
		if(effectModule != null){
			if(effectModule.getValueType() != StatusEffectParameterType.VECTOR3f){
				throw new RuntimeException("Unknown Type: "+effectModule+" -> "+effectModule.getValueType().name());	
			}
			Vector3f v = effectModule.getVector3fValue();
			input.x *= v.x;
			input.y *= v.y;
			input.z *= v.z;
		}
		///INSERTED CODE
		ArrayList<StatusEffectApplyListener> listeners = FastListenerCommon.statusEffectApplyListeners;
		if(!listeners.isEmpty()) {
			for (StatusEffectApplyListener listener : listeners) {
				input.set(listener.apply(this, e, input));
			}
		}
		///
		return input;
	}
	public int apply(StatusEffectType e, int input) {
		EffectModule effectModule = getModules().get(e);
		int result;
		if(effectModule != null){
			if(effectModule.getValueType() != StatusEffectParameterType.INT){
				throw new RuntimeException("Unknown Type: "+effectModule+" -> "+effectModule.getValueType().name());	
			}
			result = input * effectModule.getIntValue();
		}else{
			result = input;
		}
		///INSERTED CODE
		//Get result and fire through listeners
		ArrayList<StatusEffectApplyListener> listeners = FastListenerCommon.statusEffectApplyListeners;
		if(!listeners.isEmpty()) {
			for (StatusEffectApplyListener listener : listeners) {
				result = listener.apply(this, e, input);
			}
		}
		return result;
		///
	}
	public boolean apply(StatusEffectType e, boolean input) {
		EffectModule effectModule = getModules().get(e);
		boolean result;
		if(effectModule != null){
			if(effectModule.getValueType() != StatusEffectParameterType.BOOLEAN){
				throw new RuntimeException("Unknown Type: "+effectModule+" -> "+effectModule.getValueType().name());	
			}
			result = effectModule.getBooleanValue();
		}else{
			result = input;
		}
		///INSERTED CODE
		ArrayList<StatusEffectApplyListener> listeners = FastListenerCommon.statusEffectApplyListeners;
		if(!listeners.isEmpty()) {
			for (StatusEffectApplyListener listener : listeners) {
				result = (listener.apply(this, e, input));
			}
		}
		return result;
		///
	}
	public double apply(StatusEffectType e, double input) {
		EffectModule effectModule = getModules().get(e);
		double result;
		if(effectModule != null){
			if(effectModule.getValueType() != StatusEffectParameterType.FLOAT){
				throw new RuntimeException("Unknown Type: "+effectModule+" -> "+effectModule.getValueType().name());	
			}
			result = input * effectModule.getFloatValue();
		}else{
			result = input;
		}
		///INSERTED CODE
		ArrayList<StatusEffectApplyListener> listeners = FastListenerCommon.statusEffectApplyListeners;
		if(!listeners.isEmpty()) {
			for (StatusEffectApplyListener listener : listeners) {
				result = (listener.apply(this, e, input));
			}
		}
		return result;
		///
	}

	public float apply(StatusEffectType e, float input) {
		EffectModule effectModule = getModules().get(e);
		if(effectModule != null){
			if(effectModule.getValueType() != StatusEffectParameterType.FLOAT){
				throw new RuntimeException("Unknown Type: "+effectModule+" -> "+effectModule.getValueType().name());	
			}
			return input * effectModule.getFloatValue();
		}
		return input;
	}

	public float apply(StatusEffectType e, DamageDealerType weaponType, float input) {
		EffectModule effectModule = getModules().get(e);
		float result;
		if(effectModule != null){
//			if(effectModule.getValueType() != StatusEffectParameterType.WEAPON_TYPE){
//				throw new RuntimeException("Unknown Type: "+effectModule+" -> "+effectModule.getValueType().name());
//			}
			EffectModule weMod = effectModule.getWeaponType().get(weaponType);
			
			if(weMod == null){
				//no value for that weapon
				return input;
			}
			if(weMod.getValueType() != StatusEffectParameterType.FLOAT){
				throw new RuntimeException("Unknown Type for weapon: "+effectModule+" -> "+effectModule.getValueType().name());	
			}
			result = input * weMod.getFloatValue();
		}else{
			result = input;
		}
		///INSERTED CODE
		ArrayList<StatusEffectApplyListener> listeners = FastListenerCommon.statusEffectApplyListeners;
		if(!listeners.isEmpty()) {
			for (StatusEffectApplyListener listener : listeners) {
				result = (listener.apply(this, e, input));
			}
		}
		return result;
		///
	}
	public double apply(StatusEffectType e, DamageDealerType weaponType, double input) {
		EffectModule effectModule = getModules().get(e);
		double result;
		if(effectModule != null){
//			if(effectModule.getValueType() != StatusEffectParameterType.WEAPON_TYPE){
//				throw new RuntimeException("Unknown Type: "+effectModule+" -> "+effectModule.getValueType().name());
//			}
			EffectModule weMod = effectModule.getWeaponType().get(weaponType);
			
			if(weMod == null){
				//no value for that weapon
				return input;
			}
			if(weMod.getValueType() != StatusEffectParameterType.FLOAT){
				throw new RuntimeException("Unknown Type for weapon: "+effectModule+" -> "+effectModule.getValueType().name());	
			}
			result = input * weMod.getFloatValue();
		}else{
			result = input;
		}
		///INSERTED CODE
		ArrayList<StatusEffectApplyListener> listeners = FastListenerCommon.statusEffectApplyListeners;
		if(!listeners.isEmpty()) {
			for (StatusEffectApplyListener listener : listeners) {
				result = (listener.apply(this, e, input));
			}
		}
		return result;
		///
	}

	///INSERTED CODE
	//By Ithirahad (type defense fix, new method)
	public float getAsDefense(StatusEffectType e){
		EffectModule effectModule = getModules().get(e);
		if(effectModule != null){
			if(effectModule.getValueType() != StatusEffectParameterType.FLOAT){
				throw new RuntimeException("Unknown Type: "+effectModule+" -> "+effectModule.getValueType().name());
			}
			return effectModule.getFloatValue();
		}
		return 0.0f; //if no EffectModule is present, defense is 0
	}
	///

	public ShortList applyMergeTo(boolean permanent, boolean trans, ShortList out) {
		if(permanent){
			out.addAll(this.permanentEffects);
		}
		if(trans){
			out.addAll(this.transientEffects);
		}
		return out;
	}

	//INSERTED CODE

	public long getEntityId() {
		return entityId;
	}

	///
}
