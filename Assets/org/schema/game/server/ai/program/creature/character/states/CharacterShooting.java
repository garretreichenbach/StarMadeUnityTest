package org.schema.game.server.ai.program.creature.character.states;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.ShootingStateInterface;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class CharacterShooting extends CharacterState implements ShootingStateInterface {
	/**
	 *
	 */
	
	private final Vector3f targetPosition = new Vector3f();
	private final Vector3f targetVelocity = new Vector3f();
	private Vector3f dist = new Vector3f();
	private int targetid;
	private byte targetType;
	private Vector3f tmp = new Vector3f();

	public CharacterShooting(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj, mic);
	}

	@Override
	public boolean onEnter() {

		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {

		SimpleGameObject target = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget();

		if (target != null) {

			target.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);

			if (target == getEntity() || !checkTarget(target)) {
				stateTransition(Transition.RESTART);
				return false;
			}
			Vector3f linearVelocity = target.getLinearVelocity(tmp);

			if (linearVelocity != null) {

				Vector3f to = new Vector3f(target.getClientTransformCenterOfMass(serverTmp).origin);

				Vector3f fromTo = new Vector3f(getEntity().getWorldTransform().origin);
				fromTo.sub(to);
				AiEntityStateInterface aiEntityState = ((AiInterface) getEntity()).getAiConfiguration().getAiEntityState();
				float difficulty = 10;
				if (aiEntityState instanceof ShipAIEntity) {
					difficulty = ((ShipAIEntity) aiEntityState).getShootingDifficulty(target);
				}
				if (target instanceof Ship && ((Ship) target).isJammingFor(getEntity())) {
					difficulty = Math.max(1, difficulty * 0.1f);
				}

				to.x = (float) ((Math.random() - 0.5f) * (fromTo.length() / difficulty));
				to.y = (float) ((Math.random() - 0.5f) * (fromTo.length() / difficulty));
				to.z = (float) ((Math.random() - 0.5f) * (fromTo.length() / difficulty));

				targetPosition.set(to);
				targetVelocity.set(linearVelocity);
				targetid = target.getAsTargetId();
				targetType = target.getTargetType();
			} else {
				stateTransition(Transition.RESTART);
				return false;
			}

		} else {
			System.err.println("SHOOTING null");
		}
		return false;
	}

	public boolean checkTarget(SimpleGameObject s) {

		if (!checkAsTarget(s)) {
			return false;
		}

		dist.sub(s.getClientTransform().origin, getEntity().getWorldTransform().origin);

		if (s instanceof Ship && ((Ship) s).isCloakedFor(getEntity())) {
			return false;
		}
		if (s instanceof Ship && ((Ship) s).isJammingFor(getEntity()) && dist.length() > getEntityState().getShootingRange() / 4f) {
			return false;
		}
		if (dist.length() > getEntityState().getShootingRange()) {
			return false;
		}
		return true;
	}

	private boolean checkAsTarget(SimpleGameObject s) {
		if (!s.isInPhysics()) {
			return false;
		}
		if (s.isHidden()) {
			return false;
		}
		return true;
	}

	@Override
	public int getTargetId() {
		return targetid;
	}

	@Override
	public byte getTargetType() {
		return targetType;
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
}
