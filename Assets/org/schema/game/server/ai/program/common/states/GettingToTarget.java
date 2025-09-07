package org.schema.game.server.ai.program.common.states;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.turret.states.ShootingProcessInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Timer;

public class GettingToTarget extends ShipGameState implements ShootingProcessInterface {

	private Vector3f movingDir;
	private Vector3f targetMod;
	private long inDistanceTime;
	private boolean notInrange = true;
	private float randomDistMinus = 0;
	private boolean checkedRange = false;
	private long waitExtraRange = 0;

	public GettingToTarget(AiEntityStateInterface gObj) {
		super(gObj);
		movingDir = new Vector3f();
		targetMod = new Vector3f();
	}

	/**
	 * @return the movingDir
	 */
	public Vector3f getMovingDir() {
		return movingDir;
	}



	@Override
	public boolean onEnter() {
		double random = Math.random();

		randomDistMinus = (float) Math.random();

		SimpleGameObject target = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget();

		checkedRange = Math.random() < 0.006;
		waitExtraRange = 0;

		if (random > 0.9) {
			targetMod.set(0, 1, 0);
		} else if (random > 0.7) {
			targetMod.set(0, -1, 0);
		} else if (random > 0.6) {
			targetMod.set(1, 0, 0);
		} else if (random > 0.5) {
			targetMod.set(0, 0, 1);
		} else if (random > 0.4) {
			targetMod.set(0, 0, -1);
		} else if (random > 0.3) {
			targetMod.set(-1, 0, 0);
		}

		movingDir.set(0, 0, 0);
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
			return false;
		}
		if (target != null) {
			target.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);

			if (isTargetValid(target)) {

				Vector3f from = getEntity().getWorldTransform().origin;

				Vector3f to = new Vector3f(target.getClientTransformCenterOfMass(serverTmp).origin);

				movingDir.sub(to, from);

				if (!checkedRange && movingDir.length() > getEntityState().getShootingRange() * 0.8f) {
					waitExtraRange = System.currentTimeMillis() + 300;
				}
				checkedRange = true;
				//			System.err.println("[AI] "+getEntity()+" MOVING TO "+getMovingDir());

				if (System.currentTimeMillis() > waitExtraRange && movingDir.length() < getEntityState().getShootingRange() * (1f - randomDistMinus * 0.18f)) {
					if (notInrange) {
						inDistanceTime = System.currentTimeMillis();
					}
					notInrange = false;
					//				System.err.println("[AI] "+((SimpleSearchAndDestroyProgram)getGObj().getCurrentProgram()).getShip()+((SimpleSearchAndDestroyProgram)getGObj().getCurrentProgram()).getShip().getRealName()+" IS IN RANGE: resetting: "+from+" -> "+to);
					movingDir.set(0, 0, 0);
					stateTransition(Transition.TARGET_IN_RANGE);
				} else {
					notInrange = true;
				}
			} else {
				//				System.err.println("[AI] "+getEntity()+" HAS NO TARGET: resetting");
				stateTransition(Transition.RESTART);
			}
		} else {
			//			System.err.println("[AI] "+getEntity()+" TARGET NULL: resetting");
			stateTransition(Transition.RESTART);
		}

		return false;
	}
	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer,
			Ship entity, ShipAIEntity s) throws FSMException {
		getEntity().getNetworkObject().orientationDir.set(0,0,0,0);
		getEntity().getNetworkObject().targetPosition.set(new Vector3f(0, 0, 0));

		Vector3f moveDir = new Vector3f();
		moveDir.set(movingDir);
		getEntity().getNetworkObject().moveDir.set(moveDir);
		if(moveDir.lengthSquared() > 0){
			s.moveTo(timer, moveDir, true);		
		}
	}

}
