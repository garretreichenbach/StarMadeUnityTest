package org.schema.game.common.data.missile;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.data.missile.updates.MissileTargetPositionUpdate;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.remote.RemoteMissileUpdate;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;

public class MissileTargetObject {
	public int count;
	public int id;
	public IntArrayList pendingSend = new IntArrayList();
	public Int2ObjectOpenHashMap<MissileTargetTransform> transforms = new Int2ObjectOpenHashMap<MissileTargetTransform>();
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MissileTargetObject)) {
			return false;
		}
		MissileTargetObject other = (MissileTargetObject) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}
	
	public void recordServerTrans(int ticks, GameServerState state){
		if(!transforms.containsKey(ticks)){
//			System.err.println("REGISTERING FOR "+id+" AT TICK "+ticks);
			pendingSend.add(ticks);
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(id);
			if(sendable != null){
				SimpleTransformableSendableObject<?> s = ((SimpleTransformableSendableObject<?>)sendable);
				MissileTargetTransform m = new MissileTargetTransform();
				m.sectorId = s.getSectorId();
				m.t = new Transform(s.getWorldTransform());
				if(s.getPhysicsDataContainer().getObject() != null && s.getPhysicsDataContainer().getObject() instanceof RigidBody){
					Vector3f linearVelocity = ((RigidBody)s.getPhysicsDataContainer().getObject()).getLinearVelocity(new Vector3f());
					//scale linear velocity by how much we should be ahead
					linearVelocity.scale((TargetChasingMissile.UPDATE_LEN / 1000f)* ServerConfig.MISSILE_TARGET_PREDICTION_SEC.getFloat());
					m.t.origin.add(linearVelocity);
				}
				transforms.put(ticks, m);
			}else{
				MissileTargetTransform m = new MissileTargetTransform();
				m.sectorId = -1;
				m.t = TransformTools.ident;
				transforms.put(ticks, m);
			}
		}
	}
	public boolean hasPending() {
		return !pendingSend.isEmpty();
	}
	public void clearPending() {
		pendingSend.clear();
	}
	public void sendPending(ClientChannel cc) {
		for(int tick : pendingSend){
			MissileTargetPositionUpdate pp = new MissileTargetPositionUpdate((short)-1);
			pp.ticks = tick;
			pp.objectTrans = transforms.get(tick).t;
			pp.targetSectorId = transforms.get(tick).sectorId;
			pp.objId = id;
			cc.getNetworkObject().missileUpdateBuffer.add(new RemoteMissileUpdate(pp, cc.getNetworkObject()));
		}
	}
	public void handle(MissileTargetPositionUpdate m) {
		MissileTargetTransform ms = new MissileTargetTransform();
		ms.sectorId = m.targetSectorId;
		ms.t = m.objectTrans;
		transforms.put(m.ticks, ms);
	}
}
