package org.schema.game.server.ai.program.creature.character.states;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.GlUtil;

public class CharacterPathMovingToPosition extends CharacterState {

	/**
	 *
	 */
	
	boolean up = false;

	public CharacterPathMovingToPosition(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj, mic);
	}

	@Override
	public boolean onEnter() {
		CreatureAIEntity<?, AICreature<?>> c = getEntityState();

		AIPlayer ownerState = c.getEntity().getOwnerState();
		ownerState.setTargetToMove(c.getCurrentMoveTarget(), 10000);
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {

		//		if(getEntity().getOwnerState().getCurrentPath() != null && getEntity().getAffinity() != null && !getEntity().getOwnerState().getCurrentPath().isEmpty()){
		//
		//
		////			Transform affTrans = new Transform(getEntity().getAffinity().getWorldTransform());
		////
		////			long next = getEntity().getOwnerState().getCurrentPath().remove(0);
		//		}

		CreatureAIEntity<?, AICreature<?>> c = getEntityState();
		AIPlayer ownerState = c.getEntity().getOwnerState();
		if (ownerState.isTargetReachLocalTimeout()) {
			Vector3f forwardVector = GlUtil.getForwardVector(new Vector3f(), c.getEntity().getOwnerState().getTarget());
			forwardVector.scale(0.01f);
			if (!up) {
				forwardVector.negate();
			}
			c.getEntity().getOwnerState().getTarget().origin.add(forwardVector);
			up = !up;
		}
		if (ownerState.isTargetReachTimeout()) {
			stateTransition(Transition.TARGET_IN_RANGE);
			return false;
		}

		if (ownerState.isAtTarget()) {
			stateTransition(Transition.TARGET_IN_RANGE);
			return false;
		}

		return false;
	}

}
