package org.schema.game.server.data.simulation.npc.news;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.remote.RemoteNPCFactionNews;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NPCFactionNews{
	
	public enum NPCFactionNewsEventType{
		GROWN(NPCFactionNewsEventGrown::new),
		WAR(NPCFactionNewsEventWar::new),
		PEACE(NPCFactionNewsEventNeutral::new),
		ALLIES(NPCFactionNewsEventAlly::new),
		TRADING(NPCFactionNewsEventTrading::new),
		LOST_STATION(NPCFactionNewsEventLostStation::new),
		LOST_TERRITORY(NPCFactionNewsEventLostSystem::new),
		;
		private interface CC{
			public NPCFactionNewsEvent inst();
		}

		private CC c;
		public NPCFactionNewsEvent instance() {
			NPCFactionNewsEvent inst = c.inst();
			if(inst.getType() != this){
				throw new IllegalArgumentException("Wrong instance: "+inst.getType()+"; ;; "+this);
			}
			return inst;
		}
		
		private NPCFactionNewsEventType(CC c){
			this.c = c;
		}
	}
	private final FactionManager man;
	public NPCFactionNews(FactionManager man){
		this.man = man;
	}
	public void grown(int factionId, Vector3i system){
		NPCFactionNewsEventGrown c = new NPCFactionNewsEventGrown();
		c.factionId = factionId;
		c.time = System.currentTimeMillis();
		
		c.system.set(system);
		addEvent(c);
	}
	public void lostSystem(int factionId, Vector3i system){
		NPCFactionNewsEventLostSystem c = new NPCFactionNewsEventLostSystem();
		c.factionId = factionId;
		c.time = System.currentTimeMillis();
		
		c.system.set(system);
		addEvent(c);
	}
	public void trading(int factionId, Vector3i from, Vector3i to){
		NPCFactionNewsEventTrading c = new NPCFactionNewsEventTrading();
		c.factionId = factionId;
		c.time = System.currentTimeMillis();
		assert(from != null);
		assert(to != null);
		c.from.set(from);
		c.to.set(to);
		addEvent(c);
	}
	public void war(int factionId, String otherEnt){
		NPCFactionNewsEventWar c = new NPCFactionNewsEventWar();
		c.factionId = factionId;
		c.time = System.currentTimeMillis();
		
		c.otherEnt = otherEnt;
		addEvent(c);
	}
	public void peace(int factionId, String otherEnt){
		NPCFactionNewsEventNeutral c = new NPCFactionNewsEventNeutral();
		c.factionId = factionId;
		c.time = System.currentTimeMillis();
		
		c.otherEnt = otherEnt;
		addEvent(c);
	}
	public void ally(int factionId, String otherEnt){
		NPCFactionNewsEventAlly c = new NPCFactionNewsEventAlly();
		c.factionId = factionId;
		c.time = System.currentTimeMillis();
		
		c.otherEnt = otherEnt;
		addEvent(c);
	}
	public void lostStation(int factionId, String otherEnt){
		NPCFactionNewsEventLostStation c = new NPCFactionNewsEventLostStation();
		c.factionId = factionId;
		c.time = System.currentTimeMillis();
		
		c.otherEnt = otherEnt;
		addEvent(c);
	}
	
	private void addEvent(NPCFactionNewsEvent c) {
		events.add(0, c);
		
		while(events.size() > 100){
			events.remove(events.size()-1);
		}
		if(man.isOnServer()){
			send(c);
		}
		
		obs.notifyObservers();
	}


	private void send(NPCFactionNewsEvent c) {
		man.getGameState().getNetworkObject().npcFactionNewsBuffer.add(new RemoteNPCFactionNews(c, man.getGameState().getNetworkObject()));		
	}


	public final ObjectArrayList<NPCFactionNewsEvent> events = new ObjectArrayList<NPCFactionNewsEvent>();
	private final ObjectArrayList<NPCFactionNewsEvent>  toAddClient = new ObjectArrayList<NPCFactionNewsEvent> ();
	public final GUIObservable obs = new GUIObservable();
	
	
	
	public void fromTag(Tag tag){
		Tag[] t = tag.getStruct();
		
		byte version = t[0].getByte();
		Tag[] v = t[1].getStruct();
		
		for(int i = 0; i < v.length-1; i++){
			events.add((NPCFactionNewsEvent) v[i].getValue());
		}
	}
	public Tag toTag(){
		Tag[] t = new Tag[events.size()+1];
		t[t.length-1] = FinishTag.INST;
		
		for(int i = 0; i < events.size(); i++){
			t[i] = new Tag(Type.SERIALIZABLE, null, events.get(i));
		}
		
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, (byte)0),
				new Tag(Type.STRUCT, null, t),
				FinishTag.INST});
		
	}
	public void updateFromNetworkObject(NetworkGameState networkObject) {
		ObjectArrayList<RemoteNPCFactionNews> r = networkObject.npcFactionNewsBuffer.getReceiveBuffer();
		for(int i = 0; i < r.size(); i++){
			toAddClient.add(r.get(i).get());
		}
	}
	
	public void updateLocal(Timer t){
		for(int i = 0; i < toAddClient.size(); i++){
			addEvent(toAddClient.get(i));
		}
		toAddClient.clear();
	}
	public void initFromNetworkObject(NetworkGameState networkObject) {
		ObjectArrayList<RemoteNPCFactionNews> r = networkObject.npcFactionNewsBuffer.getReceiveBuffer();
		for(int i = 0; i < r.size(); i++){
			toAddClient.add(r.get(i).get());
		}
	}
	public void updateToFullNetworkObject(NetworkGameState networkObject) {
		for(int i = events.size()-1; i >= 0; i--){
			NPCFactionNewsEvent e = events.get(i);
			send(e);
		}
	}
	public void updateToNetworkObject(NetworkGameState networkObject) {
		
	}
	
}
