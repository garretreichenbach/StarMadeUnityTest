package org.schema.game.common.data.missile;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.MissileUpdateListener;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Random;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;
import org.schema.game.common.data.missile.updates.MissileSpawnUpdate;
import org.schema.game.common.data.missile.updates.MissileSpawnUpdate.MissileType;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public abstract class TargetChasingMissile extends Missile {
	private final Vector3f tmpDir = new Vector3f();
	private final Vector3f dirScaled = new Vector3f();
	private final Vector3f tmp0 = new Vector3f();
	private SimpleTransformableSendableObject target;
	private Transform t = new Transform();
	private Transform tmp = new Transform();
	private Vector3f projected = new Vector3f();
	private Vector3f dd = new Vector3f();
	private Random random;
	private int updateCount = 0;
	//start with 2nd step (first one is initial update)
	public int steps = 1;
	public int ticks = 0;
	
	public static long UPDATE_MS = 8;
	public static final long UPDATE_LEN = UPDATE_MS*35;
	private static float TSTP = UPDATE_MS * 0.001f;
	
	
	public final List<MissileTargetPosition> targetPositions = new ObjectArrayList<MissileTargetPosition>();
	private MissileTargetPosition currentTar;
	private long accumulatedTime;
	private long lastUpdate;
	private long lastReferenceTime;
	int startTicks;
	Vector3f relativeTargetPos = new Vector3f();
	private boolean canTurnFromBack = true;
	private int stalling;
	
	
	public TargetChasingMissile(StateInterface state) {
		super(state);
		t.setIdentity();

		this.random = new Random();
		lastUpdate = System.currentTimeMillis();
		
		
	}

	private boolean resetDebug() {
		if(debug && isOnServer()) {
			synchronized(debugClient) {
				debugClient.put(getId(), new ObjectArrayList<Vector3f>());
				debugServer.put(getId(), new ObjectArrayList<Vector3f>());
			}
		}
		return true;
	}

	@Override
	public void setId(short id) {
		super.setId(id);
		assert(resetDebug());
	}

	@Override
	public MissileType getType() {
				return null;
	}

	/**
	 * @return the target
	 */
	public SimpleTransformableSendableObject getTarget() {
		return target;
	}

	@Override
	public void onSpawn() {
	}

	public void setTarget(int targetId) {
		if (targetId > 0) {
			Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(targetId);
			if (sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				setTarget((SimpleTransformableSendableObject) sendable);
			} else {
			}
		} else {
			setTarget( null);
		}

	}

	public abstract void updateTarget(Timer timer);

	
	public Vector3f getTargetRelativePosition(Vector3f out){
		assert(isOnServer());

		
		
		if(target == null){
			out.set(0,0,0);
		}else{
			random.setSeed(target.getId() + getId());
			target.getAimingAtRelativePos(out, getOwner(), target, random, 0);
		}
		
		return out;
	}
	public Transform getTargetPosition(Transform out){
		assert(isOnServer());
		
		out.setIdentity();
		if(target == null){
			out.origin.set(getWorldTransform().origin);
			Vector3f dd = getDirection(new Vector3f());
			dd.normalize();
			dd.scale(1000);
			out.origin.add(dd);
		}else{
			Sector ownSector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
			if (ownSector != null) {
				target.calcWorldTransformRelative(ownSector.getId(), ownSector.pos);
				return target.getClientTransformCenterOfMass(out);
			
			} else {
				setAlive(false);
			}
		}
		
		return out;
	}
	private boolean onTSEmpty(){
		MissileTargetManager missileManager;
		if(isOnServer()){
			missileManager = ((GameServerState)getState()).getController().getMissileController().getMissileManager().targetManager;
		}else{
			missileManager = ((GameClientState)getState()).getController().getClientMissileManager().targetManager;
		}
		Vector3f positionFor = null;
		if(target != null){
			//no need to check the previous tick because
			//this is executed for teh future on (tick-1) to begin with
			final int tick = startTicks+ticks; 
			if(!missileManager.hasPosForTick(target.getId(), tick)){
				stalling++;
				if(stalling % 500 == 0) {
					System.err.println(getState()+" Missile "+getId()+" stalling for position: amount: "+stalling);
				}
				if(isOnServer() && stalling > 1000) {
					System.err.println("[SERVER] MISSILE STALLING TOO LONG. KILLING MISSILE. MISSING TICK: "+tick+"; next tick available: "+missileManager.hasPosForTick(target.getId(), tick+1));
					setAlive(false);
				}
				return false;
			}
			stalling = 0;
			positionFor = missileManager.getPositionFor(target.getId(), relativeTargetPos, tick, getSectorId(), getState());
			
		}
		if(positionFor == null){
			positionFor = new Vector3f(getWorldTransform().origin);
			Vector3f dr = getDirection(new Vector3f());
			dr.scale(500);
			positionFor.add(dr);
//			System.err.println("DDIR: "+getState()+"; "+steps+": "+getDirection(new Vector3f()));
		}
		MissileTargetPosition p = new MissileTargetPosition();
		p.targetPosition = positionFor;
		p.time = spawnTime + ((steps+1) * UPDATE_LEN);
		
//		System.err.println("MISSILE "+getState()+"("+getId()+") ADDED POS ON Step: "+steps+"; TICK "+ticks+" (started "+startTicks+"): "+positionFor+"; "+target+"; ");
		steps++;
		targetPositions.add(p);
		return true;
	}
	@Override
	public float updateTransform(Timer timer, Transform worldTransform, Vector3f direction, Transform out, boolean prediction) {

		accumulatedTime += (timer.currentTime - lastUpdate);
		lastUpdate = timer.currentTime;
		out.set(worldTransform);
		
		//we grab a new position from what the interface provides
		//server directly grabs positions and puts them in the list
		//clinet gets identical position from server
		//to ensure determinism with low amount of target updates
		if(currentTar == null || currentTar.isExecuted(lastReferenceTime)){
			if(isOnServer()){
				updateTarget(timer);
			}
			if(targetPositions.isEmpty()){
				if(isOnServer()){
					((GameServerState)getState()).getController().
					getMissileController().getMissileManager().stalling.add(getId());
				}else{
					((GameClientState)getState()).getController().
					getClientMissileManager().stalling.add(getId());
				}
//				System.err.println(getState()+" Missile Stalling for targetPositions");
				onTSEmpty();
				return 0;
			}else{
				if(currentTar == null){
					//first
					lastReferenceTime = spawnTime;
				}else{
					lastReferenceTime = currentTar.time;
				}
				currentTar = targetPositions.remove(0);
				
				
				
				if(targetPositions.isEmpty()){
					boolean onTSEmpty = onTSEmpty();
					if(!onTSEmpty){
						return 0;
					}
				}
//				System.err.println("MISSILE "+getState()+"("+getId()+") RETRIEVED POS AT TICK "+ticks+" (started "+startTicks+"): "+currentTar.targetPosition+"; REL POS: "+relativeTargetPos);
				ticks++;
			}
		}
		//how long to update the missile based on that position reference
		long updateTimeSpan = currentTar.time - lastReferenceTime;
		assert(updateTimeSpan == UPDATE_LEN):updateTimeSpan+"; "+UPDATE_LEN;
		
		
		
		long timeLeft = updateTimeSpan - currentTar.executedTime;
//		if(isOnServer()){
//			System.err.println("UPDATE: "+getState()+" startTicks: "+startTicks+"; Ticks: "+ticks+"; Tar: "+currentTar.targetPosition);
//		}
		if(timeLeft <= 0){
			assert(currentTar.isExecuted(lastReferenceTime));
		}
		float len = 0;
		if(timeLeft > 0){
			while(accumulatedTime >= UPDATE_MS && !currentTar.isExecuted(lastReferenceTime)){
				

				dd.sub(currentTar.targetPosition, out.origin);
				
				
				if (dd.lengthSquared() != 0) {
					float dist = dd.length();
					Vector3f dir = new Vector3f(direction);
					dir.normalize();
					dd.normalize();
					float d = dd.dot(dir);
					GlUtil.project(dir, new Vector3f(dd), projected);
					
					assert(!Float.isNaN(projected.x));
					if(projected.lengthSquared() > 0){
						//projected len can be 0 if dir and goalDir are equal
						//no need to correct in that case
						projected.normalize();
						assert(!Float.isNaN(projected.x));
						float correctionPower = TSTP;
						float correctionFallOff = 0.0003f;
//						if(Math.abs(1f-d) < correctionFallOff){
//							
//							correctionPower *= Math.abs(1f-d) / correctionFallOff;
////							System.err.println("FALLOFF "+correctionPower);
//						}
						boolean proj = false;
						if (d < 0) {
							
							if(dist > 300.0) {
								canTurnFromBack = true;
							}
							if(canTurnFromBack) {
								projected.scale(correctionPower * DumbMissileElementManager.CHASING_TURN_SPEED_WITH_TARGET_IN_BACK); //1.1f
								proj = true;
							}
						} else {
							
							projected.scale(correctionPower * DumbMissileElementManager.CHASING_TURN_SPEED_WITH_TARGET_IN_FRONT);
							canTurnFromBack = false;
							proj = true;
						}
						if(proj) {
							assert(!Float.isNaN(projected.x));
							direction.normalize();
							direction.add(projected);
						}
					}
					direction.normalize();

					dirScaled.set(direction);
					dirScaled.normalize();
					dirScaled.scale(TSTP * getSpeed());
					assert(!Float.isNaN(dirScaled.x));
					out.origin.add(dirScaled);
					len += dirScaled.length();
					direction.set(dirScaled);
					
					//set to represet the normal speed (not the speed in the update-step using TSTP e.g. for point defense)
					direction.normalize();
					direction.scale(getSpeed());
					setDirection(direction);
					
					if(isOnServer() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
						DebugDrawer.points.add(new DebugPoint(out.origin, new Vector4f(0,1,0,1)));
					}
				}
				
				accumulatedTime -= UPDATE_MS;
				currentTar.executedTime += UPDATE_MS;
				
				
//				System.err.println("MISSILE POS "+updateCount+" based on "+currentTar.targetPosition+"; : ON "+(isOnServer() ? "SERVER" : "CLIENT")+": "+out.origin+"; TARGET: "+getTarget()+"; "+(isOnServer() ? getTargetPosition(new Transform()).origin : "ddd"));
				assert(checkUpdate(out));
			
				updateCount++;
			}
		}else{
			if(isOnServer()){
				System.err.println("SERVER: NO TIME -> MISSILE DIE: "+timeLeft+"; exec: "+currentTar.executedTime+"; time: "+currentTar.time+"; last ref: "+lastReferenceTime);
				setAlive(false);
			}
		}
		
		return len;
	}
	private boolean checkUpdate(Transform out) {
		if(debug) {
			synchronized(debugClient) {
				if(isOnServer()) {
					debugServer.get(getId()).add(new Vector3f(out.origin));
				}else {
					debugClient.get(getId()).add(new Vector3f(out.origin));
				}
				
				int size = Math.min(debugClient.get(getId()).size(),debugServer.get(getId()).size());
				for(int i = 0; i < size; i++) {
					Vector3f c = debugClient.get(getId()).get(i);
					Vector3f s = debugServer.get(getId()).get(i);
					if(!s.epsilonEquals(c, 0.3f)) {
						System.err.println("ASSERT FAIL: Failed on update "+(updateCount+1)+": Client: "+c+", Server: "+s);
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static boolean debug = false;
	private final static Short2ObjectOpenHashMap<ObjectArrayList<Vector3f>> debugClient = new Short2ObjectOpenHashMap<ObjectArrayList<Vector3f>>();
	private final static Short2ObjectOpenHashMap<ObjectArrayList<Vector3f>> debugServer = new Short2ObjectOpenHashMap<ObjectArrayList<Vector3f>>();
	@Override
	public void setFromSpawnUpdate(MissileSpawnUpdate missileSpawnUpdate) {
		super.setFromSpawnUpdate(missileSpawnUpdate);
		setTarget(missileSpawnUpdate.target);
		
		MissileTargetPosition p = new MissileTargetPosition();
		p.time = spawnTime + UPDATE_LEN;
		p.targetPosition = new Vector3f(missileSpawnUpdate.targetPos);
		this.relativeTargetPos.set(missileSpawnUpdate.relativePos);
		this.targetPositions.add(p);
		this.startTicks = missileSpawnUpdate.startTicks;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.missile.Missile#updateClient(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateClient(Timer timer) {
		//INSERTED CODE
		if(!FastListenerCommon.missileUpdateListeners.isEmpty()) {
			for (MissileUpdateListener listener : FastListenerCommon.missileUpdateListeners) {
				listener.updateClient(this, timer);
			}
		}
		///
		distanceMade += updateTransform(timer, getWorldTransform(), getDirection(tmpDir), t, false);
		setTransformMissile(t);

		//INSERTED CODE
		if(!FastListenerCommon.missileUpdateListeners.isEmpty()) {
			for (MissileUpdateListener listener : FastListenerCommon.missileUpdateListeners) {
				listener.updateClientPost(this, timer);
			}
		}
		///
		//		System.err.println("[CLIENT][MISSILE] TARGET "+(target != null ? target.getWorldTransform().origin : "N/A"));
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateServer(Timer timer) {
		distanceMade += updateTransform(timer, getWorldTransform(), getDirection(tmpDir), t, false);
		setTransformMissile(t);
		super.updateServer(timer);

		//		System.err.println("[SERVER][MISSILE] TARGET "+(target != null ? target.getWorldTransform().origin : "N/A"));
	}

	public void setTarget(SimpleTransformableSendableObject<?> target) {
		MissileTargetManager targetManager;
		if(isOnServer()){
			targetManager = ((GameServerState)getState()).getController().
			getMissileController().getMissileManager().targetManager;
		}else{
			targetManager = ((GameClientState)getState()).getController().
			getClientMissileManager().targetManager;
		}
		targetManager.registerTarget(this.target, target, getState());
		this.target = target;
		
		
	}


	@Override
	protected void onDeadServer() {
		assert(isOnServer());
		super.onDeadServer();
		if(target != null){
			((GameServerState) getState()).getController().getMissileController()
			.getMissileManager().targetManager.unregisterOne(target.getId());
		}
		((GameServerState) getState()).getController().getMissileController()
		.getMissileManager().targetManager.checkAllAlive(getState());
		
		
		
	}

	private boolean removeClientDebug() {
		if(debug) {
			synchronized(debugClient) {
				debugClient.remove(getId());
				debugServer.remove(getId());
			}
		}
		return true;
	}

	@Override
	public void onClientDie(int hitId) {
		super.onClientDie(hitId);
		if(target != null){
			((GameClientState)getState()).getController().getClientMissileManager().targetManager.unregisterOne(target.getId());
		}
		((GameClientState)getState()).getController().getClientMissileManager().targetManager.checkAllAlive(getState());
		
		assert(removeClientDebug());
	}

}
