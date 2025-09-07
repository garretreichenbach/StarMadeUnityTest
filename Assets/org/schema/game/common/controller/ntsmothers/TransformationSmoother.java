package org.schema.game.common.controller.ntsmothers;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.CompareTools;
import org.schema.common.util.PersistentRingBuffer;
import org.schema.common.util.linAlg.Quat4Util;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.controller.manager.DebugControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.RemoteTransformable;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerState;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

public class TransformationSmoother {
	public static final int STEP_CODE_PASSIVE = 0;
	public static final int STEP_PHYSICS_NOT_INITIALIZED = 1;
	public static final int STEP_SNAP = 2;
	public static final int STEP_NORMAL = 3;

	//	private static TimedTransformationPool pool = new TimedTransformationPool();
	public static final int STEP_SERVER_LIST_EMPTY = 4;
	/**
	 * Sets the value of this quaternion to the rotational component of
	 * the passed matrix.
	 *
	 * @param m1 the Matrix4f
	 */
	//	private static final float EPS2 = 0.000001f;
	final static double EPS2 = 1.0e-30;
	private static boolean DEBUG_STEP_OFF = false;
	private final Transform tmpTransform = new Transform();
	boolean reset = true;
	Vector3f linVelo = new Vector3f();
	private Transform currentTransformation = new Transform();
	private Transform targetTransformation = new Transform();
	private boolean equalsEpsilon;
	private RemoteTransformable transformable;
	private boolean active = true;
	private Vector3f localPos = new Vector3f();
	private Vector3f remotePos = new Vector3f();
	private Vector3f predictPos = new Vector3f();
	private Matrix3f localRot = new Matrix3f();
	private Matrix3f localMat = new Matrix3f();
	private Transform predictTrans = new Transform();
	private ReceivedTrans rt = null; 
	private long lastRecording;
	
	private final PersistentRingBuffer<RecordedTrans> record = new PersistentRingBuffer<RecordedTrans>(updatesHeld){

		@Override
		public RecordedTrans[] create(int capacity) {
			RecordedTrans[] s = new RecordedTrans[capacity];
			
			for(int i = 0; i < capacity; i++){
				s[i] = new RecordedTrans();
			}
			return s;
		}
		
	};
	
	private static final long UPDATE_MS = 30;
	private static final int updatesHeld = 30; //900 ms backlog
	
	public TransformationSmoother(RemoteTransformable obj) {
		this.transformable = obj;
	}

	public void doInstantStep(Transform transform) {
		transformable.getPhysicsDataContainer().addCenterOfMassToTransform(transform);
		setWorldTransform(transform);
		
	}
	
	private class ReceivedTrans{
		
		
		private RecordedTrans receivedTransform = new RecordedTrans();
		
		private float processedPos = 0;
		private float processedRot = 0;
		public RecordedTrans recordTransform = new RecordedTrans();
	}
	
	private class RecordedTrans implements Comparable<RecordedTrans>{
		Vector3f pos = new Vector3f();
		Matrix3f rot = new Matrix3f();
		long time;
		
		@Override
		public int compareTo(RecordedTrans o) {
			return CompareTools.compare(time, o.time);
		}

		public void set(RecordedTrans comp) {
			this.pos.set(comp.pos);
			this.rot.set(comp.rot);
			this.time = comp.time;
		}
		
	}
	
	private void recordCurrent(long currentTime){
		if(currentTime - lastRecording > UPDATE_MS){
			if (isAttachedToAnother()) {
				PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) transformable.getPhysicsDataContainer().getObject());
		
				localPos.set(o.localWorldTransform.origin);
				localRot.set(o.localWorldTransform.basis);
				localMat.set(o.localWorldTransform.basis);
			} else {
				Transform originalTransform = transformable.getPhysicsDataContainer().getOriginalTransform();
				localPos.set(originalTransform.origin);
				localRot.set(originalTransform.basis);
				localMat.set(originalTransform.basis);
			}
			
			RecordedTrans t = record.add();
	
			t.pos.set(localPos);
			t.rot.set(localRot);
			t.time = currentTime;
		}
		
	}
	public StateInterface getState() {
		return transformable.getState() ;
	}
	
	public boolean isOnServer() {
		return (getState() instanceof ServerState);
	}
	public boolean isSelectedOnClient() {
		return !isOnServer() && transformable.getIdentifiable() == ((GameClientState)getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
	}
	public void update(Timer timer){
		if(!isOnServer() && DebugControlManager.requestShipMissalign == transformable.getIdentifiable()) {
			
			Transform t;
			if (isAttachedToAnother()) {
				PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) transformable.getPhysicsDataContainer().getObject());
				t = new Transform(o.localWorldTransform);
			} else {
				t = new Transform(transformable.getPhysicsDataContainer().getOriginalTransform());
			}
			transformable.getPhysicsDataContainer().addCenterOfMassToTransform(t);
			t.origin.y += 3;
			t.basis.setIdentity();
			setWorldTransform(t);
			
			((GameClientState)getState()).getController().popupAlertTextMessage("[DEBUG] Ship "+transformable.getIdentifiable()+"; Rotation reset for client only");
			DebugControlManager.requestShipMissalign = null;
		}
		if(!(transformable.isSmoothingGeneral() && transformable.isSmoothingEntity())) {
			rt = null;
		}
		if(rt != null){
			
//			if(transformable.isOnServer()){
//				System.err.println("CORRECTING "+transformable.getIdentifiable());
//			}
			Transform t;
			if (isAttachedToAnother()) {
				PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) transformable.getPhysicsDataContainer().getObject());
				t = new Transform(o.localWorldTransform);
			} else {
				t = new Transform(transformable.getPhysicsDataContainer().getOriginalTransform());
			}
			
			
			float step = timer.getDelta()*0.5f;
			
			Transform predictAdd = new Transform();
			
			
			if(rt.processedPos < 1.0f) {
				Vector3f mv = new Vector3f();
				mv.sub(rt.receivedTransform.pos, rt.recordTransform.pos);
				
				Vector3f realDist = new Vector3f();
				realDist.sub(rt.receivedTransform.pos, t.origin);
				float relLen = realDist.length();
	//			if(isSelectedOnClient()) {
	//				System.err.println("LEN: "+mv.length()+"; RELLEN: "+relLen);
	//			}
				if(relLen > 0) {
					if(realDist.length() < 0.5f) {
						realDist.normalize();
						realDist.scale(timer.getDelta()*0.5f);
						if(realDist.length() >= relLen) {
							t.origin.set(rt.receivedTransform.pos);
						}else {
							t.origin.add(realDist);
						}
					}else {
						mv.scale(step);
						predictAdd.setIdentity();
						predictAdd.origin.set(mv);
						t.origin.add(predictAdd.origin);
					}
				}
				rt.processedPos += step;
			}
			transformable.getPhysicsDataContainer().addCenterOfMassToTransform(t);
			assert(!isOnServer());
			
			
			
			
//			if(transformable.getState() instanceof GameClientState && Keyboard.isKeyDown(GLFW.GLFW_KEY_COMMA)) {
//				Quat4Util.slerp(fq, tq,rt.processed, res);
//				
//				if(transformable.getIdentifiable().toString().contains("68099")){
//					System.err.println("ENTITY: "+transformable.getIdentifiable());
//					System.err.println("RECORD: \n"+rt.recordTransform.rot);
//					System.err.println("RECEIVED: \n"+rt.receivedTransform.rot);
//					System.err.println("FROM: \n"+from);
//					System.err.println("TO: \n"+to);
//					System.err.println("STEP "+step+"; poc: "+rt.processed+"; RES: "+res);
//				}
//			}else {
				
//			}
//				predictAdd.basis.set(res);
//			MatrixUtil.setRotation(predictAdd.basis, res);
			
//			from.invert();
//			predictAdd.basis.mul(from);
			
			
			Matrix3f from = new Matrix3f(t.basis);
			Matrix3f to = new Matrix3f(rt.receivedTransform.rot);
			
			
			
			Quat4f fq = new Quat4f();
			Quat4f tq = new Quat4f();
			
			Quat4fTools.set(from, fq);
			Quat4fTools.set(to, tq);
			rt.processedRot += step * 0.075f;
			
			
			Quat4f res = new Quat4f();
			Quat4Util.slerp(fq, tq, Math.min(1f, rt.processedRot), res);
//			if(isSelectedOnClient()) {
//				System.err.println("FROM: "+fq+" -> "+tq+" = "+res+"; "+rt.processedRot);
//			}
			
			
			
			MatrixUtil.setRotation(predictAdd.basis, res);
			t.basis.set(predictAdd.basis);
			
			setWorldTransform(t);
			
			
			
			if(rt.processedRot >= 1f){
				rt = null;
			}
		}
		
		
		recordCurrent(timer.currentTime);
	}
	
	public void recordStep(Transform transform, long timestampSentRaw) {
		if (DEBUG_STEP_OFF) {
			return;
		}
		
		if (!transformable.getPhysicsDataContainer().isInitialized()) {
			return;
		}
		
		recordCurrent(getState().getUpdateTime());
		
		
		if(record.size() > 0){
			int serverTimeDifference = (transformable.getState()).getServerTimeDifference();
			Transform latest = transform;
			long timestampOfSending = timestampSentRaw - serverTimeDifference;

		
		
			rt = new ReceivedTrans();
			
			
			
			rt.receivedTransform.pos.set(transform.origin);
			rt.receivedTransform.rot.set(transform.basis);
			rt.receivedTransform.time = timestampOfSending;
			
			long ts = System.currentTimeMillis();
			RecordedTrans comp = record.get(0);
			int index = 0;
			for(int i = record.size()-2; i >= 0; i--){
				RecordedTrans recordedTrans = record.get(i);
				
				if(timestampOfSending >= recordedTrans.time && timestampOfSending <= ts){
					comp = recordedTrans;
					index = i;
					break;
				}else{
					ts = recordedTrans.time;
				}
			}
			rt.recordTransform.set(comp);
			
			if(comp == record.get(0) && timestampOfSending < comp.time){
//				System.err.println("LAGGED "+transformable.getIdentifiable()+"; "+transformable.getState());
			}else if(comp == record.get(record.size()-1)){
				//this is in front of newest
				//correct from teh latest record to the received transform, since it's newest
//				System.err.println("NEWEST "+transformable.getIdentifiable()+"; "+transformable.getState());
			}else{
				//this is somewhere in the middle
				//interpolate where that transform was at that point
				RecordedTrans next = record.get(index+1);
				long top = next.time;
				long bottom = comp.time;
				
				long span = top - bottom;
				long spanToTT = timestampOfSending - bottom;
				double t;
				
				if(span == 0){
					t = 1;
				}else{
					t = Math.max(0, Math.min(1.0d, (double)spanToTT / (double)span));
				}
				
				Vector3f posRes = new Vector3f();
				Vector3fTools.lerp(comp.pos, next.pos, (float)t, posRes);
				
				Quat4f rotRes = new Quat4f();
				
				Quat4f fq = new Quat4f();
				Quat4f tq = new Quat4f();
				
				Quat4fTools.set(comp.rot, fq);
				Quat4fTools.set(next.rot, tq);
				
				Quat4Util.slerp(fq, tq, (float)t, rotRes);
				
				rt.recordTransform.pos.set(posRes);
				
				MatrixUtil.setRotation(rt.recordTransform.rot, rotRes);
//				rt.recordTransform.rot.set(rotRes);
				rt.recordTransform.time = bottom + spanToTT;
//				System.err.println("MID "+transformable.getIdentifiable()+"; "+t+"; "+"; "+transformable.getState()+"; span "+span+"; spanTT "+spanToTT);
			}
		}
		
//		int code = STEP_NORMAL;
//
//		if (isAttached()) {
//			PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) transformable.getPhysicsDataContainer().getObject());
//
//			localPos.set(o.localWorldTransform.origin);
//			Quat4fTools.set(o.localWorldTransform.basis, localQuat);
//			localMat.set(o.localWorldTransform.basis);
//		} else {
//			Transform originalTransform = transformable.getPhysicsDataContainer().getOriginalTransform();
//			localPos.set(originalTransform.origin);
//			Quat4fTools.set(originalTransform.basis, localQuat);
//			localMat.set(originalTransform.basis);
//		}
//
//		remotePos.set(latest.origin);
//		Quat4fTools.set(latest.basis, remoteQuat);
//
//		if (DEBUG_INSTANT) {
//			predictTrans.setIdentity();
//			predictTrans.origin.set(latest.origin);
//			predictTrans.basis.set(remoteQuat);
//		} else {
//			float pc = transformable.getIdentifiable() instanceof AbstractCharacter<?> ? 0.85f : 0.70f;
//			VectorUtil.setInterpolate3(predictPos, localPos, remotePos, pc);
//
//			Quat4Util.slerp(localQuat, remoteQuat, pc, predictQuat);
//
//			predictTrans.setIdentity();
//			predictTrans.origin.set(predictPos);
//			predictTrans.basis.set(predictQuat);
//		}
//
//		transformable.getPhysicsDataContainer().addCenterOfMassToTransform(predictTrans);
//		setWorldTransform(predictTrans);
//
//		if (((SimpleTransformableSendableObject) transformable.getIdentifiable()).getDebugMode() == SimpleTransformableSendableObject.DEBUG_NT_SMOOTHER) {
//			System.err.println("DEBUG " + transformable.getState() + ": " + transformable.getIdentifiable() + " S POS: " + remotePos + "; local: " + localPos + " -> " + predictPos + "; " + transformable.getState().getUpdateNumber());
//		}
//
//		return code;
	}

	public Transform getCurrentTransformation() {
		return currentTransformation;
	}

	public void setCurrentTransformation(Transform currentTransformation) {
		this.currentTransformation = currentTransformation;
	}

	public Transform getTargetTransformation() {
		return targetTransformation;
	}

	public void setTargetTransformation(Transform targetTransformation) {
		this.targetTransformation = targetTransformation;
	}

	//	private void release(TimedTransformation localTransform) {
	//		pool.release(localTransform);
	//	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAttachedToAnother() {
		return (transformable.getPhysicsDataContainer().getObject() != null &&
				transformable.getPhysicsDataContainer().getObject() instanceof PairCachingGhostObjectAlignable &&
				((PairCachingGhostObjectAlignable) transformable.getPhysicsDataContainer().getObject()).getAttached() != null);

	}

	public boolean isEqualsEpsilon() {
		return equalsEpsilon;
	}

	public void setEqualsEpsilon(boolean equalsEpsilon) {
		this.equalsEpsilon = equalsEpsilon;
	}

	public void reset() {
		//		while(!localTransformationList.isEmpty()){
		//			release(localTransformationList.remove(localTransformationList.size()-1));
		//		}
		//		while(!serverTransformationList.isEmpty()){
		//			release(serverTransformationList.remove(serverTransformationList.size()-1));
		//		}
	}

	//	public void traceLocalTransform(){
	//		if(localTransformationList.size() > 0 && localTransformationList.get(localTransformationList.size()-1).transform.equals(getCurrent())){
	//			localTransformationList.get(localTransformationList.size()-1).timestamp = System.currentTimeMillis();
	//		}else{
	//			synchronized (localTransformationList) {
	//				while(localTransformationList.size() > MAX_SAVED_STEPS-1){
	//					TimedTransformation remove = localTransformationList.remove(0);
	//					release(remove);
	//				}
	//
	//				localTransformationList.add(pool.get().set(getCurrent(), System.currentTimeMillis()));
	//			}
	//		}
	//	}
	//	public void updateTransformationReceivedFromServer(Transform t, Long timestamp) {
	//		synchronized (serverTransformationList) {
	//			while(serverTransformationList.size() > MAX_SAVED_STEPS-1){
	//				TimedTransformation remove = serverTransformationList.remove(0);
	//				release(remove);
	//			}
	//
	//
	//			serverTransformationList.add(pool.get().set(t, timestamp));
	//		}
	//	}

	private boolean setOutSector(Transform t) {
		if (transformable.getIdentifiable() instanceof SimpleTransformableSendableObject) {
			SimpleTransformableSendableObject<?> s = (SimpleTransformableSendableObject<?>) transformable.getIdentifiable();
			if (!s.isOnServer() && s.getSectorId() != ((GameClientState) s.getState()).getCurrentSectorId()) {

				if (transformable.getPhysicsDataContainer().getObject() != null) {
					transformable.getPhysicsDataContainer().getObject().setWorldTransform(t);
					transformable.getPhysicsDataContainer().getObject().setInterpolationWorldTransform(t);
					transformable.getPhysicsDataContainer().updatePhysical(transformable.getState().getUpdateTime());
				} else {
					transformable.getWorldTransform().set(t);
				}
				return true;
			}
		}
		return false;
	}


	public void setWorldTransform(Transform t) {

		if (transformable.getIdentifiable() instanceof SimpleTransformableSendableObject) {
			SimpleTransformableSendableObject<?> s = (SimpleTransformableSendableObject<?>) transformable
					.getIdentifiable();
		}
		if (transformable.getPhysicsDataContainer().getObject() != null && transformable.getPhysicsDataContainer().isInitialized()) {
			if (isAttachedToAnother()) {
				PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) transformable.getPhysicsDataContainer().getObject());
				o.localWorldTransform.set(t);
			} else {
				if (!setOutSector(t)) {

					transformable.getPhysicsDataContainer().getObject().setWorldTransform(t);
					transformable.getPhysicsDataContainer().getObject().setInterpolationWorldTransform(t);

					transformable.getPhysicsDataContainer().updatePhysical(transformable.getState().getUpdateTime());

					this.transformable.onSmootherSet(transformable.getPhysicsDataContainer().getCurrentPhysicsTransform());
				}
			}

		}
	}

	public void onNoSmooth() {
//		if(isSelectedOnClient()) {
//			System.err.println("ON NO SMOOTH");
//		}
		//cancel all ongoing smoothing
		rt = null;
	}

}
