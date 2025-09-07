package org.schema.game.server.ai.program.creature.character.states;

import org.schema.game.common.data.world.Universe;
import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class CharacterRandomWaiting extends CharacterState {

	/**
	 *
	 */
	
	private int min;
	private int max;
	private int waitingTime;
	private long waitingStarted;

	public CharacterRandomWaiting(AiEntityStateInterface gObj, int min, int max, AICreatureMachineInterface mic) {
		super(gObj, mic);
		this.min = min;
		this.max = Math.max(max, min);
	}

	@Override
	public boolean onEnter() {
//		getEntity().getOwnerState().resetTargetToMove();
		waitingTime = min + Universe.getRandom().nextInt(max - min);
		waitingStarted = System.currentTimeMillis();
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
//		if(getEntityState().getAttackTarget() != null){
//			
//		}else 
		if (System.currentTimeMillis() - waitingStarted > waitingTime) {
			stateTransition(Transition.WAIT_COMPLETED);
		}

		return false;
	}

}
