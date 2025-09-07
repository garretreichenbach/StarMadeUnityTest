package org.schema.game.server.data.simulation.npc.diplomacy;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.schema.common.util.LogInterface;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.network.objects.remote.RemoteNPCDiplomacy;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFactionConfig;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction.DiplActionType;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NPCDiplomacy extends GUIObservable implements LogInterface{
	
	private static final long CHANGE_MOD_DURATION = 11000;


	public Long2ObjectOpenHashMap<NPCDiplomacyEntity> entities = new Long2ObjectOpenHashMap<NPCDiplomacyEntity>(); 
	

	private LongOpenHashSet changedEnts = new LongOpenHashSet();
	
	public final NPCFaction faction;

	

	final LongOpenHashSet ntChanged = new LongOpenHashSet();




	private boolean first = true;

	public NPCDiplomacy(NPCFaction faction) {
		super();
		this.faction = faction;
		
		
	}
	public void initialize(){
		if(faction.isOnServer()){
			Collection<Faction> m = ((FactionState) faction.getState()).getFactionManager().getFactionCollection();
			for(Faction f : m){
				if(f != faction){
					onAddedFaction(f);
				}
			}
			
			for(PlayerState p : ((GameServerState) faction.getState()).getPlayerStatesByName().values()){
				onPlayerJoined(p);
			}
		}
		
		execs.add(new TimedExecution() {
			@Override
			public NPCDipleExecType getType() {
				return NPCDipleExecType.STATUS_CALC;
			}
			@Override
			public long getDelay() {
				return getConfig().diplomacyStatusCheckDelay;
			}
			@Override
			public void execute() {
				log("Diplomacy Status Check", LogLevel.NORMAL);
				for(NPCDiplomacyEntity e : entities.values()){
					e.calculateStaticModifiers(getDelay());
				}
			}
		});
		execs.add(new TimedExecution() {
			@Override
			public NPCDipleExecType getType() {
				return NPCDipleExecType.DIPL_APPLY;
			}
			@Override
			public long getDelay() {
				return getConfig().diplomacyTurnEffectDelay;
			}
			@Override
			public void execute() {
				log("Diplomacy Turn apply", LogLevel.NORMAL);
				for(NPCDiplomacyEntity e : entities.values()){
					e.applyDynamicModifiers(getDelay());
				}
			}
		});
		execs.add(new TimedExecution() {
			@Override
			public NPCDipleExecType getType() {
				return NPCDipleExecType.DIPL_CHANGE_CHECK;
			}
			@Override
			public long getDelay() {
				return getConfig().diplomacyTurnEffectChangeDelay;
			}
			@Override
			public void execute() {
				log("Calculate Diplomacy Turn change "+getDelay(), LogLevel.NORMAL);
				for(NPCDiplomacyEntity e : entities.values()){
					e.calculateDiplomacyModifiersFromActions(getDelay());
				}
			}
		});
		execs.add(new TimedExecution() {
			@Override
			public NPCDipleExecType getType() {
				return NPCDipleExecType.DIPL_ON_ACTION;
			}
			@Override
			public long getDelay() {
				return CHANGE_MOD_DURATION;
			}
			@Override
			public void execute() {
				for(long l : changedEnts){
					//only done when change is triggered
					NPCDiplomacyEntity npcDiplomacyEntity = entities.get(l);
					if(npcDiplomacyEntity != null){
						npcDiplomacyEntity.calculateDiplomacyModifiersFromActions(0);
						npcDiplomacyEntity.calculateStaticModifiers(0);
					}
				}
				changedEnts.clear();
			}
		});
		
	}
	public NPCFactionConfig getConfig(){
		return faction.getConfig();
	}
	
	public void diplomacyAction(DiplActionType type, long otherDbId) {
		NPCDiplomacyEntity e = entities.get(otherDbId);
		if(e == null){
			e = new NPCDiplomacyEntity((FactionState) faction.getState(), faction.getIdFaction(), otherDbId);
			e.setPoints(getConfig().diplomacyStartPoints);
			entities.put(otherDbId, e);
		}
		e.diplomacyAction(type);
		changedEnts.add(e.getDbId());
	}
	
	public void trigger(NPCDipleExecType type) {
		for(TimedExecution c : execs){
			if(c.getType() == type){
				c.forceTrigger();
				break;
			}
		}
	}
	
	public enum NPCDipleExecType{
		STATUS_CALC,
		DIPL_APPLY,
		DIPL_CHANGE_CHECK,
		DIPL_ON_ACTION,
	}
	
	private List<TimedExecution> execs = new ObjectArrayList<TimedExecution>();
	
	
	public void update(long time){
		if(first){
			initialize();
			assert(entities.size() > 0);
			first = false;
			
		}
		for(TimedExecution exe : execs){
			exe.update(time);
		}
	}
	
	private abstract class TimedExecution{
		long lastT = -1;
		long timeElapsed;
		public abstract long getDelay();
		public void forceTrigger() {
			timeElapsed = getDelay()+1;
			log("Forced Trigger "+getType().name(), LogLevel.NORMAL);
		}
		public abstract void execute();
		public abstract NPCDipleExecType getType();
		public void update(long time){
			if(lastT < 0){
				lastT = time;
			}
			timeElapsed += (time - lastT);
			
			if(timeElapsed > getDelay()){
				log("Executing "+getType().name(), LogLevel.NORMAL);
				execute();
				timeElapsed -= getDelay();
			}
			
			
			lastT = time;
		}
	}
	
	@Override
	public void log(String string, LogLevel l) {
		faction.log("[DIPLOMACY]"+string, l);
	}
	public Tag toTag(){
		Tag[] t = new Tag[entities.size()+1];
		t[t.length-1] = FinishTag.INST;
		int i = 0;
		for(NPCDiplomacyEntity e : entities.values()){
			t[i] = e.toTag();
			i++;
		}
		
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.BYTE, null, (byte)0),
			new Tag(Type.STRUCT, null, t),
			FinishTag.INST
		});
	}
	public void fromTag(Tag tag){
		Tag[] t = tag.getStruct();
		byte version = t[0].getByte();
		Tag[] ents = t[1].getStruct();
		
		System.err.println("[SERVER][FACTION][NPC] laoding diplomacy from tag. Entries; "+ents.length);
		for(int i = 0; i < ents.length-1; i++){
			NPCDiplomacyEntity e = new NPCDiplomacyEntity((FactionState) faction.getState(), faction.getIdFaction());
			e.fromTag(ents[i]);
			
			entities.put(e.getDbId(), e);
		}
	}

	
	private void sendFor(SendableGameState g, NPCDiplomacyEntity e){
		if(e.isSinglePlayer()){
			PlayerState playerState = ((GameServerState)g.getState()).getPlayerStatesByDbId().get(e.getDbId());
			if(playerState != null && playerState.getClientChannel() != null){
				log("SENDING NT PLAYER DIPLOMACY TO "+playerState+": "+e, LogLevel.DEBUG);
				playerState.getClientChannel().getNetworkObject().npcDiplomacyBuffer.add(new RemoteNPCDiplomacy(e, g.getNetworkObject()));
			}else{
			}
		}else{
			Faction f = ((FactionState)g.getState()).getFactionManager().getFaction((int) e.getDbId());
			if(f != null && f.isPlayerFaction()){
				Map<String, FactionPermission> membersUID = f.getMembersUID();
				for(String mem : membersUID.keySet()){
					PlayerState playerState = ((GameServerState)g.getState()).getPlayerFromNameIgnoreCaseWOException(mem.toLowerCase(Locale.ENGLISH));
					if(playerState != null && playerState.getClientChannel() != null){
						log("SENDING NT FACTION DIPLOMACY TO "+playerState+": "+e, LogLevel.DEBUG);
						playerState.getClientChannel().getNetworkObject().npcDiplomacyBuffer.add(new RemoteNPCDiplomacy(e, g.getNetworkObject()));
					}
				}
			}
		}
	}
	
	public void checkNPCFactionSending(SendableGameState g, boolean force) {
		if(force){
			for(NPCDiplomacyEntity e : entities.values()){
				sendFor(g, e);
			}
		}else{
			for(long k : ntChanged){
				
				NPCDiplomacyEntity e = entities.get(k);
				if(e != null){
					
					sendFor(g, e);
				}
				
//				g.getNetworkObject().npcDiplomacyBuffer.add(new RemoteNPCDiplomacy(entities.get(k), g.getNetworkObject()));
			}
			ntChanged.clear();
		}
	}

	public String printFor(PlayerState player) {
		StringBuffer b = new StringBuffer();
		NPCDiplomacyEntity npcDiplomacyEntity = entities.get(player.getDbId());
		if(npcDiplomacyEntity != null){
			b.append("PLAYER DIPLOMACY: \n");
			b.append(npcDiplomacyEntity+"\n");
		}
		npcDiplomacyEntity = entities.get(player.getFactionId());
		if(npcDiplomacyEntity != null){
			b.append("PLAYER FACTION DIPLOMACY: \n");
			b.append(npcDiplomacyEntity+"\n");
		}
		if(b.length() == 0){
			b.append("No Values yet for "+player.getName());
		}
		return b.toString();
	}

	public void onAddedFaction(Faction f) {
		NPCDiplomacyEntity e = entities.get(f.getIdFaction());
		if(e == null){
			e = new NPCDiplomacyEntity((FactionState) faction.getState(), faction.getIdFaction(), (long)f.getIdFaction());
			e.setPoints(getConfig().diplomacyStartPoints);
			entities.put(f.getIdFaction(), e);
		}
		ntChanged(f.getIdFaction());
		
	}

	public void onPlayerJoined(PlayerState p) {
		NPCDiplomacyEntity e = entities.get(p.getDbId());
		if(e == null){
			e = new NPCDiplomacyEntity((FactionState) faction.getState(), faction.getIdFaction(), p.getDbId());
			e.setPoints(getConfig().diplomacyStartPoints);
			entities.put(p.getDbId(), e);
			e.calculateStaticModifiers(0);
			e.applyDynamicModifiers(0);
		}
		ntChanged(p.getDbId());
		
		((FactionState) faction.getState()).getFactionManager().needsSendAll.add(p);
		
//		System.err.println("SENDING DDDDDDDDDDD PLAYER JOINED: "+ntChanged+"; "+entities);
	}
	public void onDeletedFaction(Faction f) {
		entities.remove(f.getIdFaction());
	}

	public void ntChanged(long dbId) {
//		try {
//			throw new Exception("DS");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		ntChanged.add(dbId);
		((FactionState)faction.getState()).getFactionManager().diplomacyChanged.add(this);
	}
	public void onClientChanged() {
		notifyObservers();
	}
	
}
