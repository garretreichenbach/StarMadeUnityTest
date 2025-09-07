package org.schema.game.server.ai.program.common.states;

import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.ShootingStateInterface;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.turret.states.ShootingProcessInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;

public abstract class ShootAtTarget extends ShipGameState implements ShootingStateInterface, ShootingProcessInterface {

	private final Vector3f targetPosition = new Vector3f();
	private final Vector3f targetVelocity = new Vector3f();
	private Vector3f dist = new Vector3f();
	private int targetid;
	private byte targetType;
	private Vector3f tmp = new Vector3f();
	protected final Vector3f fromTo = new Vector3f();

	public ShootAtTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@Override
	public int getTargetId() {
		return targetid;
	}

	@Override
	public byte getTargetType() {
		return targetType;
	}
	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer, Ship entity,
			ShipAIEntity s) throws FSMException {
		
		Vector3f targetPosition = new Vector3f();
		Vector3f targetVelocity = new Vector3f();

		targetPosition.set(this.targetPosition);
		targetVelocity.set(this.targetVelocity);
		int targetId = (targetid);
		byte targetType = (this.targetType);

		if (targetPosition.length() > 0) {
			getEntity().getNetworkObject().targetPosition.set(targetPosition);
			getEntity().getNetworkObject().targetVelocity.set(targetVelocity);
			getEntity().getNetworkObject().targetId.set(targetId);
			getEntity().getNetworkObject().targetType.set(targetType);
			s.doShooting(unit, timer);
			
			getEntityState().onShot(this);
			
			this.targetPosition.set(0, 0, 0);
			stateTransition(Transition.SHOOTING_COMPLETED);
		}
		
		//continue strafing
		
		Vector3f orientateDir = new Vector3f();
		orientateDir.set(fromTo);
		getEntity().getNetworkObject().orientationDir.set(orientateDir, 0);
		
		Vector3f up = GlUtil.getUpVector(new Vector3f(), getEntity().getWorldTransform().basis);
		
		up.cross(up, orientateDir);
		
		
		if(up.lengthSquared() > 0){
			up.normalize();
			up.scale(((ShipAIEntity)getEntityState()).getForceMoveWhileShooting());
			
			if(orientateDir.length() > getEntityState().getShootingRange()*0.75){
				Vector3f n = new Vector3f(orientateDir);
				n.normalize();
				n.scale(10f);
				up.add(n);
			}
			
			getEntity().getNetworkObject().moveDir.set(up);
			s.moveTo(timer, up, false);
			
		}else{
			getEntity().getNetworkObject().moveDir.set(0,0,0);
		}
		
		
		getEntity().getNetworkObject().orientationDir.set(orientateDir.x, orientateDir.y, orientateDir.z, 0);
		
		s.moveTo(timer, up, false);
		assert(!Float.isNaN(fromTo.x));
		assert(!Float.isNaN(orientateDir.x));
		
		
		s.orientate(timer, Quat4fTools.getNewQuat(orientateDir.x, orientateDir.y, orientateDir.z, 0));
	}
	/**
	 * @return the shootingDir
	 */
	@Override
	public Vector3f getTargetPosition() {
		return targetPosition;
	}

	@Override
	public Vector3f getTargetVelocity() {
		return targetVelocity;
	}

	@Override
	public boolean onEnter() {
		targetPosition.set(0, 0, 0);
		try {
			onUpdate();
		} catch (FSMException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		SimpleGameObject target = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget();
		if(getEntity().isCoreOverheating()){
			stateTransition(Transition.RESTART);
			targetPosition.set(0,0,0);
			return false;
		}
		if (target != null) {

			target.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);

			if (target == getEntity() || !checkTargetinRange(target, 0, false)) {
//				System.err.println("SHOOTING AT INVALID TARGET "+target);
				stateTransition(Transition.RESTART);
				return false;
			}
			Vector3f linearVelocity = target.getLinearVelocity(tmp);

			Vector3f to = new Vector3f(target.getClientTransformCenterOfMass(serverTmp).origin);

			fromTo.sub(to, getEntity().getWorldTransform().origin);
			AiEntityStateInterface aiEntityState = ((SegmentControllerAIInterface) getEntity()).getAiConfiguration().getAiEntityState();
			float difficulty = 10;
			if (aiEntityState instanceof ShipAIEntity) {
				difficulty = ((ShipAIEntity) aiEntityState).getShootingDifficulty(target);
			}
			if (target instanceof Ship && ((Ship) target).isJammingFor(getEntity())) {
				difficulty = Math.max(1, difficulty * 0.1f);
			}
			float deviation = fromTo.length()/difficulty;
			

			getEntityState().random.setSeed(getEntityState().seed);
			target.transformAimingAt(to, getEntityState().getEntity(), target, getEntityState().random, deviation);

			targetPosition.set(to);
			targetVelocity.set(linearVelocity);
			targetid = target.getAsTargetId();
			targetType = target.getTargetType();

		}
		return false;
	}

	

}
