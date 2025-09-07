package org.schema.game.server.ai.program.common.states;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.SegmentControllerAIEntity;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.turret.states.ShootingProcessInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;

public class EngagingTargetFighter extends ShipGameState implements ShootingProcessInterface {

	/**
	 *
	 */
	private final Vector3f rotDir = new Vector3f();

	public EngagingTargetFighter(AiEntityStateInterface gObj) {
		super(gObj);
	}

	

	/**
	 * @return the movingDir
	 */
	public Vector3f getRotDir() {
		return rotDir;
	}

	@Override
	public boolean onEnter() {
		SimpleGameObject target = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget();
		findRotDir(target);
		
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	public boolean findRotDir(SimpleGameObject target){

		if (target != null) {
			target.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);
			if (!checkTargetinRange(target, 0, false)) {
				rotDir.set(0,0,0);
				return false;
			}
			Vector3f from = getEntity().getWorldTransform().origin;

			Vector3f to = new Vector3f(target.getClientTransformCenterOfMass(serverTmp).origin);
			rotDir.sub(to, from);
			return true;
		}
		rotDir.set(0,0,0);
		return false;
	}
	
	@Override
	public boolean onUpdate() throws FSMException {
		if(((SegmentControllerAIEntity<?>)getEntityState()).isTimeout()){
			System.err.println("[AI] TIMEOUT ENGAGING (too far from set sector) for "+getEntity());
			stateTransition(Transition.RESTART);
			return false;
		}
		SimpleGameObject target = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget();
		if(getEntity().isCoreOverheating()){
			stateTransition(Transition.RESTART);
			return false;
		}
		if (findRotDir(target)) {

			
			assert(!Float.isNaN(rotDir.x));
			Vector3f mNorm = new Vector3f(rotDir);
			mNorm.normalize();
			Vector3f forwardVector = GlUtil.getForwardVector(new Vector3f(), getEntity().getWorldTransform());

			Vector3f foN = new Vector3f(forwardVector);
			foN.negate();
			foN.sub(mNorm);
			Vector3f fo = new Vector3f(forwardVector);
			fo.sub(mNorm);

			if (mNorm.epsilonEquals(forwardVector, ShipAIEntity.EPSILON_RANGE)) {
				stateTransition(Transition.IN_SHOOTING_POSITION);
				return true;
			}
		} else {
//			System.err.println("[AI] " + getEntity() + " HAS NO TARGET: resetting");
			stateTransition(Transition.RESTART);
		}
		assert(!Float.isNaN(rotDir.x));
		return false;
	}

	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer,
			Ship entity, ShipAIEntity s) throws FSMException {
		
		getEntity().getNetworkObject().targetPosition.set(new Vector3f(0, 0, 0));

		
		Vector3f up = GlUtil.getUpVector(new Vector3f(), getEntity().getWorldTransform().basis);
		up.cross(up, rotDir);
		
		
		if(up.lengthSquared() > 0){
			up.normalize();
			up.scale(((ShipAIEntity)getEntityState()).getForceMoveWhileShooting());
			
			if(rotDir.length() > getEntityState().getShootingRange()*0.75){
				Vector3f n = new Vector3f(rotDir);
				n.normalize();
				n.scale(10f);
				up.add(n);
			}
			
			getEntity().getNetworkObject().moveDir.set(up);
			s.moveTo(timer, up, false);
			
		}else{
			getEntity().getNetworkObject().moveDir.set(0,0,0);
		}
		
		
		
		s.moveTo(timer, up, false);
		assert(!Float.isNaN(rotDir.x));
		
		s.orientate(timer, Quat4fTools.getNewQuat(rotDir.x, rotDir.y, rotDir.z, 0));
		getEntity().getNetworkObject().orientationDir.set(rotDir.x, rotDir.y, rotDir.z, 0);
	}

}
