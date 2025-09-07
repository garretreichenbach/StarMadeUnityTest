package org.schema.game.common.data.missile;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.updates.MissileTargetPositionUpdate;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.TransformaleObjectTmpVars;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class MissileTargetManager {
	private final Int2ObjectOpenHashMap<MissileTargetObject> register = new Int2ObjectOpenHashMap<MissileTargetObject>();
	protected final TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();
	public Vector3f getPositionFor(int targetId, Vector3f relativePos, int ticks, int missileSectorId, StateInterface state) {
		MissileTargetObject missileTargetObject = register.get(targetId);
		if(missileTargetObject != null){
			MissileTargetTransform targetTrans = missileTargetObject.transforms.get(ticks);
			assert(targetTrans != null):ticks;
			
			Transform transform = new Transform();
			
			if(targetTrans.sectorId > 0){
				transform.setIdentity();
				
				Vector3i sectorPos = new Vector3i();
				
				if(state instanceof GameServerState){
					GameServerState s = (GameServerState)state;
					Sector sector = s.getUniverse().getSector(missileSectorId);
					if(sector != null){
						sectorPos.set(sector.pos);
					}else{
						System.err.println("[SERVER] NO SECTOR FOR MISSILE");
						return null;
					}
				}else{
					GameClientState s = (GameClientState)state;
					Sendable sendable = s.getLocalAndRemoteObjectContainer().getLocalObjects().get(missileSectorId);
					if(sendable != null && sendable instanceof RemoteSector){
						sectorPos.set(((RemoteSector)sendable).clientPos());
					}else{
						System.err.println("[CLIENT] NO SECTOR FOR MISSILE");
						return null;
					}
				}
				
				SimpleTransformableSendableObject.calcWorldTransformRelative
				(missileSectorId, sectorPos, targetTrans.sectorId, targetTrans.t, state, (state instanceof ServerStateInterface), 
						transform, v);
				
//				Sendable s = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(missileTargetObject.id);
//				if(s != null) {
//					System.err.println("POS::: "+((SimpleTransformableSendableObject)s).getWorldTransform().origin+"; "+targetTrans.t.origin+"; "+missileSectorId+"; "+targetTrans.sectorId+"; "+((SimpleTransformableSendableObject)s).getSectorId()+"; tt "+transform.origin+"; rel "+relativePos);
//					if(Vector3fTools.diffLength(((SimpleTransformableSendableObject)s).getWorldTransform().origin, targetTrans.t.origin) > 16) {
//						assert(false):((SimpleTransformableSendableObject)s).getWorldTransform().origin+"; "+targetTrans.t.origin;
//					}
//				}
			}else{
//				System.err.println("NO TARGET SECTOR ID");
				transform.set(targetTrans.t);
			}
			Vector3f pp = new Vector3f(relativePos);
			transform.transform(pp);
			return pp;
		}else{
			System.err.println("NO TAR "+targetId);
		}
		return null;
	}
	public boolean hasPosForTick(int targetId, int ticks) {
		MissileTargetObject missileTargetObject = register.get(targetId);
		if(missileTargetObject != null){
			return missileTargetObject.transforms.containsKey(ticks);
		}
		return false;
	}
	public void registerTarget(SimpleTransformableSendableObject<?> from,
			SimpleTransformableSendableObject<?> to, StateInterface state) {
		if(from != to){
			if(from != null){
				MissileTargetObject missileTargetObject = register.get(from.getId());
				if(missileTargetObject != null){
					missileTargetObject.count--;
					if(missileTargetObject.count <= 0){
						assert(missileTargetObject.count == 0);
						register.remove(from.getId());
					}
				}
				
			}
			if(to != null){
				MissileTargetObject missileTargetObject = register.get(to.getId());
				if(missileTargetObject == null){
					missileTargetObject = new MissileTargetObject();
					missileTargetObject.id = to.getId();
					if(state instanceof GameServerState){
						GameServerState s = (GameServerState)state;
						missileTargetObject.recordServerTrans(
								s.getController().getMissileController().getMissileManager().getTicks(), s);
					}
					register.put(missileTargetObject.id, missileTargetObject);
				}
				missileTargetObject.count++;
//				System.err.println("REGISTER: "+missileTargetObject.id+": "+missileTargetObject.count);
			}
		}
		
	}
	public void sendPending(GameServerState state) {
		for(MissileTargetObject o : register.values()){
			if(o.hasPending()){
				for (PlayerState p : state.getPlayerStatesByName().values()) {
					RegisteredClientOnServer c = state.getClients().get(
							p.getClientId());
					if (c != null) {
	
						Sendable sendable = c.getLocalAndRemoteObjectContainer()
								.getLocalObjects().get(0);
						if (sendable != null && sendable instanceof ClientChannel) {
							ClientChannel cc = (ClientChannel) sendable;
							o.sendPending(cc);
						} else {
							System.err
									.println("[SERVER] BROADCAST MISSILE UPDATE FAILED FOR "
											+ p + ": NO CLIENT CHANNEL");
						}
					} else {
						System.err.println("[SEVRER][MISSILEMAN] client for player not found: " + p);
					}
				}
				o.clearPending();
			}
		}		
	}
	public void record(GameServerState state, int ticks) {
		for(MissileTargetObject o : register.values()){
			o.recordServerTrans(ticks, state);
		}		
	}
	public void receivedPosUpdate(MissileTargetPositionUpdate m) {
		MissileTargetObject missileTargetObject = register.get(m.objId);
		if(missileTargetObject == null){
			missileTargetObject = new MissileTargetObject();
			missileTargetObject.id = m.objId;
			register.put(missileTargetObject.id, missileTargetObject);
		}
		missileTargetObject.handle(m);		
	}
	public void unregisterOne(int id) {
		MissileTargetObject missileTargetObject = register.get(id);
		if(missileTargetObject != null){
			missileTargetObject.count--;
//			System.err.println("UNREGISTER: "+missileTargetObject.id+": "+missileTargetObject.count);
			if(missileTargetObject.count <= 0){
				register.remove(id);
//				System.err.println("MISSILE TARGET MANAGER: UNREGISTERED "+id);
			}
		}
		
	}
	public void checkAllAlive(StateInterface state) {
		ObjectIterator<MissileTargetObject> iterator = register.values().iterator();
		while(iterator.hasNext()){
			MissileTargetObject next = iterator.next();
			if(!state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(next.id)){
				iterator.remove();
//				System.err.println("MISSILE TARGET MANAGER: UNREGISTERED "+next.id);
			}
		}
	}
}
