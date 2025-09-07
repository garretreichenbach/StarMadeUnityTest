package org.schema.game.common.controller;

import java.util.List;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.thrust.ThrusterElementManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.NetworkShip;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteFloat;
import org.schema.schine.network.objects.remote.RemoteVector4f;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class ThrustConfiguration {
	private static final byte VERSION = 0;
	private boolean automaticReacivateDampeners = true;
	private boolean automaticDampeners = true;
	private Vector4f lastAxisBalanceSent = new Vector4f();
	public final Vector4f queuedThrustBuffer = new Vector4f();
	private float lastRepulsorSent;
	private float lastRepulsorBuffered;
	public float queuedRepulsorBuffer;
	private long queuedThrustBufferTime;
	private Vector4f lastAxisBalanceBuffered = new Vector4f();
	public boolean thrustSharing;
	private boolean automaticDampenersOnExit;
	private final Ship ship;
	
	public ThrustConfiguration(Ship ship){
		this.ship = ship;
	}
	
	public void updateLocal(Timer timer) {
		float timeOut = ship.getConfigManager().apply(StatusEffectType.THRUSTER_CONFIG_CHANGE_TIMEOUT, 1f);
		
		long t = (long) (timeOut*ThrusterElementManager.THRUST_CHANGE_APPLY_TIME_IN_SEC*1000f);

		
		
		if(ship.isClientOwnObject() && queuedThrustBufferTime > 0){
			 if(timer.currentTime - queuedThrustBufferTime < t){
				((GameClientState)getState()).getController()
					.popupGameTextMessage(Lng.str("Recalibrating thrusters.\n%s sec left", (t - (timer.currentTime - queuedThrustBufferTime)) / 1000L), "cBufTh", 0);
			 }else{
				 ((GameClientState)getState()).getController()
					.popupGameTextMessage(Lng.str("Recalibration successfull!", (t - (timer.currentTime - queuedThrustBufferTime)) / 1000L), "cBufTh", 0);
				 System.out.println("Sending buffer " + queuedRepulsorBuffer);
				 sendThrusterBalanceChange(new Vector3f(
						 queuedThrustBuffer.x,
						 queuedThrustBuffer.y,
						 queuedThrustBuffer.z), queuedThrustBuffer.w, queuedRepulsorBuffer);
				 queuedThrustBufferTime = 0;
			 }
		}		
	}

	private StateInterface getState() {
		return ship.getState();
	}

	public void initFromNetworkObject(NetworkShip n) {
		getManagerContainer().getThrusterElementManager().thrustBalanceAxis.set(
				n.thrustBalanceAxis.getX(),
				n.thrustBalanceAxis.getY(),
				n.thrustBalanceAxis.getZ()
				);
		getManagerContainer().getThrusterElementManager().rotationBalance = 
				n.thrustBalanceAxis.getW();
//		System.out.println("NetworkObject: initFromNetworkObject " + getManagerContainer().getRepulseManager().thrustToRepul + " " + n.thrustRepulsorBalance.getFloat());
		getManagerContainer().getRepulseManager().setThrustToRepul(n.thrustRepulsorBalance.getFloat());
		queuedRepulsorBuffer = n.thrustRepulsorBalance.getFloat();
		queuedThrustBuffer.set(
				getManagerContainer().getThrusterElementManager().thrustBalanceAxis.x, 
				getManagerContainer().getThrusterElementManager().thrustBalanceAxis.y, 
				getManagerContainer().getThrusterElementManager().thrustBalanceAxis.z, 
				n.thrustBalanceAxis.getW());
		automaticDampeners = n.automaticDampeners.getBoolean();
		automaticReacivateDampeners = n.automaticDampenersReactivate.getBoolean();
		
		thrustSharing = n.thrustSharing.getBoolean();
	}

	private ShipManagerContainer getManagerContainer() {
		return ship.getManagerContainer();
	}

	public void updateFromNetworkObject(NetworkShip n, int senderId) {
		
		if(!isOnServer()){
			getManagerContainer().getThrusterElementManager().thrustBalanceAxis.set(
					n.thrustBalanceAxis.getX(),
					n.thrustBalanceAxis.getY(),
					n.thrustBalanceAxis.getZ()
					);
			getManagerContainer().getThrusterElementManager().rotationBalance = 
					n.thrustBalanceAxis.getW();
			
			
			boolean automaticDampenersBefore = automaticDampeners;
			automaticDampeners = n.automaticDampeners.getBoolean();
			
			if(automaticDampenersBefore != automaticDampeners){
				ship.flagupdateMass();
			}
			boolean tShareBef = thrustSharing;
			thrustSharing = n.thrustSharing.getBoolean();
			if(tShareBef != thrustSharing){
				GameClientState s = (GameClientState) getState();
				s.getController().notifyCollectionManagerChanged(this.getManagerContainer().getThrusterElementManager().getCollection());
			}
			
			automaticReacivateDampeners = n.automaticDampenersReactivate.getBoolean();
			
			getManagerContainer().getRepulseManager().setThrustToRepul(n.thrustRepulsorBalance.getFloat());
//			System.out.println("NetworkObject: updateFromNetworkObject if " + getManagerContainer().getRepulseManager().thrustToRepul + " " + n.thrustRepulsorBalance.getFloat());
		}else{
			RemoteBuffer<RemoteVector4f> c = n.thrustBalanceAxisChangeBuffer;
			for(int i = 0; i < c.getReceiveBuffer().size(); i++){
				RemoteVector4f r = c.getReceiveBuffer().get(i);
				Vector4f a = r.getVector(new Vector4f());
				
				getManagerContainer().getThrusterElementManager().thrustBalanceAxis.set(
						a.x,
						a.y,
						a.z
						);
				getManagerContainer().getThrusterElementManager().rotationBalance = 
						a.w;
				
			}
			
			for(int i = 0; i < n.automaticDampenersReq.getReceiveBuffer().size(); i++){
				automaticDampeners = n.automaticDampenersReq.getReceiveBuffer().get(i).get();
				ship.flagupdateMass();
			}
			for(int i = 0; i < n.thrustSharingReq.getReceiveBuffer().size(); i++){
				thrustSharing = n.thrustSharingReq.getReceiveBuffer().get(i).get();
			}
			
			for(int i = 0; i < n.automaticDampenersReactivateReq.getReceiveBuffer().size(); i++){
				automaticReacivateDampeners = n.automaticDampenersReactivateReq.getReceiveBuffer().get(i).get();
			}
			
			for(int i = 0; i < n.thrustRepulsorBalanceBuffer.getReceiveBuffer().size(); i++){
				getManagerContainer().getRepulseManager().setThrustToRepul(n.thrustRepulsorBalanceBuffer.getReceiveBuffer().get(i).get());
//				System.out.println("NetworkObject: updateFromNetworkObject else " + getManagerContainer().getRepulseManager().thrustToRepul + " " + n.thrustRepulsorBalanceBuffer.getReceiveBuffer().get(i).get());
			}
		}
	}
	public void bufferThrusterBalanceChange(Vector3f balanceAxis, float rotBalance, float repulBalance){
		assert(!isOnServer());
		if(!lastAxisBalanceBuffered.equals(
				new Vector4f(balanceAxis.x, balanceAxis.y, balanceAxis.z, rotBalance)) || lastRepulsorBuffered != repulBalance){
		
			queuedThrustBufferTime = System.currentTimeMillis()+100l;
			queuedThrustBuffer.set(balanceAxis.x, balanceAxis.y, balanceAxis.z, rotBalance);
//			System.out.println("NetworkObject: bufferThrusterBalanceChange " + queuedRepulsorBuffer + " " + repulBalance);
			queuedRepulsorBuffer = repulBalance;
			
			lastRepulsorBuffered = queuedRepulsorBuffer;
			lastAxisBalanceBuffered.set(queuedThrustBuffer);
		}
	}
	
	public void sendThrusterBalanceChange(Vector3f balanceAxis, float rotBalance, float repulBalance){
		assert(!isOnServer());
		if(!lastAxisBalanceSent.equals(new Vector4f(balanceAxis.x, balanceAxis.y, balanceAxis.z, rotBalance)) ){
			lastAxisBalanceSent.set(balanceAxis.x, balanceAxis.y, balanceAxis.z, rotBalance);
			RemoteVector4f r = new RemoteVector4f(isOnServer());
			r.set(balanceAxis.x, balanceAxis.y, balanceAxis.z, rotBalance);
			
			getNetworkObject().thrustBalanceAxisChangeBuffer.add(r);			
		}
		if(lastRepulsorSent != repulBalance){
			lastRepulsorSent = repulBalance;
			RemoteFloat f = new RemoteFloat(repulBalance, getNetworkObject());
			f.set(repulBalance);
//			System.out.println("NetworkObject: sendThrusterBalanceChange " + repulBalance);
			
			//getNetworkObject().thrustRepulsorBalance = f;
			getNetworkObject().thrustRepulsorBalanceBuffer.add(f);
			
		}
	}
	public void updateToFullNetworkObject(NetworkShip n) {
		if(isOnServer()){
			Vector3f thrustBalanceAxis = getManagerContainer().getThrusterElementManager().thrustBalanceAxis;
			float rotationBalance = getManagerContainer().getThrusterElementManager().rotationBalance;
			getNetworkObject().thrustBalanceAxis.set(thrustBalanceAxis.x, thrustBalanceAxis.y, thrustBalanceAxis.z, rotationBalance);
//			System.out.println("NetworkObject: updateToFullNetworkObject " + n.thrustRepulsorBalance.getFloat() + " " + getManagerContainer().getRepulseManager().thrustToRepul);
			n.thrustRepulsorBalance.set(getManagerContainer().getRepulseManager().getThrustToRepul());
			n.thrustSharing.set(thrustSharing);
			n.automaticDampeners.set(automaticDampeners);
			n.automaticDampenersReactivate.set(automaticReacivateDampeners);
		}
		
	}
	private NetworkShip getNetworkObject() {
		return ship.getNetworkObject();
	}

	private boolean isOnServer() {
		return ship.isOnServer();
	}

	public void updateToNetworkObject(NetworkShip n) {
		if(isOnServer()){
			n.thrustSharing.set(thrustSharing);
			n.automaticDampeners.set(automaticDampeners);
			n.automaticDampenersReactivate.set(automaticReacivateDampeners);
			
			Vector3f thrustBalanceAxis = getManagerContainer().getThrusterElementManager().thrustBalanceAxis;
			float rotationBalance = getManagerContainer().getThrusterElementManager().rotationBalance;
			n.thrustBalanceAxis.set(thrustBalanceAxis.x, thrustBalanceAxis.y, thrustBalanceAxis.z, rotationBalance);
//			System.out.println("NetworkObject: updateToNetworkObject " + n.thrustRepulsorBalance.getFloat() + " " + getManagerContainer().getRepulseManager().thrustToRepul);
			n.thrustRepulsorBalance.set(getManagerContainer().getRepulseManager().getThrustToRepul());
		}
	}

	public void onAttachPlayer(PlayerState playerState) {
		if(isOnServer()){
			if(automaticReacivateDampeners){
				automaticDampeners = automaticDampenersOnExit;
			}		
		}
	}

	public void onDetachPlayer(PlayerState playerState, List<PlayerState> attachedPlayers) {
		if(isOnServer()){
			if(attachedPlayers.isEmpty()){
				if(automaticReacivateDampeners){
					automaticDampenersOnExit = automaticDampeners;
					automaticDampeners = true;
					ship.flagupdateMass();
				}
			}
		}
	}

	public void readFromOldTag(Tag[] ts) {
		automaticDampeners = ts[1].getByte() != 0;
		
		automaticReacivateDampeners = (Byte)ts[2].getValue() != 0;
		
		getManagerContainer().getThrusterElementManager()
			.thrustBalanceAxis.set((Vector3f)ts[3].getValue());
		getManagerContainer().getThrusterElementManager()
		.rotationBalance = (Float)ts[4].getValue();
		
		if(ts.length > 5 && ts[5].getType() == Type.BYTE){
			automaticDampenersOnExit = (Byte)ts[5].getValue() != 0;
			thrustSharing = (Byte)ts[6].getValue() != 0;
		}		
		try {
			throw new Exception("OLD auto "+automaticDampeners+"; exit "+automaticDampenersOnExit+"; "+getManagerContainer().getThrusterElementManager()
					.thrustBalanceAxis+"; "+getManagerContainer().getThrusterElementManager()
					.rotationBalance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readFromTag(Tag tag) {
		Tag[] ts = tag.getStruct();
		byte version = ts[0].getByte();
		automaticDampeners = ts[1].getByte() != 0;
		
		automaticReacivateDampeners = ts[2].getByte()  != 0;
		
		getManagerContainer().getThrusterElementManager().thrustBalanceAxis.set(ts[3].getVector3f());
		getManagerContainer().getThrusterElementManager().rotationBalance = ts[4].getFloat();
		automaticDampenersOnExit = ts[5].getByte() != 0;
		thrustSharing = ts[6].getByte() != 0;
		if(ts.length > 7 && ts[7].getType() == Type.FLOAT){
			getManagerContainer().getRepulseManager().setThrustToRepul(ts[7].getFloat());
		}
		//getManagerContainer().getRepulseManager().thrustToRepul = ts[7].getFloat();
//		try {
//			throw new Exception("NEW auto "+automaticDampeners+"; exit "+automaticDampenersOnExit+"; "+getManagerContainer().getThrusterElementManager()
//					.thrustBalanceAxis+"; "+getManagerContainer().getThrusterElementManager()
//					.rotationBalance);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public Tag toTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, VERSION),
				new Tag(Type.BYTE, null, (byte) (automaticDampeners ? 1 : 0)),
				new Tag(Type.BYTE, null, (byte) (automaticReacivateDampeners ? 1 : 0)),
				new Tag(Type.VECTOR3f, null, getManagerContainer().getThrusterElementManager()
						.thrustBalanceAxis),
				new Tag(Type.FLOAT, null, getManagerContainer().getThrusterElementManager()
						.rotationBalance),
				new Tag(Type.BYTE, null, (byte) (automaticDampenersOnExit ? 1 : 0)),
				new Tag(Type.BYTE, null, (byte) (thrustSharing ? 1 : 0)),
				new Tag(Type.FLOAT, null, getManagerContainer().getRepulseManager()
						.getThrustToRepul()),
				FinishTag.INST,
		});
	}

	public boolean isAutomaticReacivateDampeners() {
		return automaticReacivateDampeners;
	}

	public boolean isAutomaticDampeners() {
		return automaticDampeners;
	}


}
