package org.schema.game.server.ai.program.creature.character.states;

import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class CharacterEngaging extends CharacterState {

	/**
	 *
	 */
	
	private int shootTime;

	public CharacterEngaging(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj, mic);
	}

	@Override
	public boolean onEnter() {
		shootTime = 3000;
		if (getEntityState().canPlotPath() && getEntityState().isMoveRandomlyWhenEngaging()) {
			try {
				getEntityState().plotSecondaryPath();
			} catch (FSMException e) {
				e.printStackTrace();
			}
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
		if (target == null) {
			stateTransition(Transition.RESTART);
			return false;
		} else {
			synchronized (target.getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {

				if (!target.existsInState()) {
					((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);
					System.err.println("RESET TARGET: didn't exist anymore: " + target);
					target = null;
					stateTransition(Transition.RESTART);
					return false;
				}
			}
			if (getEntityState().getEntity().getAiConfiguration().isStopAttacking() &&
					System.currentTimeMillis() - ((TargetProgram<?>) getEntityState().getCurrentProgram()).getTargetAquiredTime() > shootTime + ((TargetProgram<?>) getEntityState().getCurrentProgram()).getTargetHoldTime()) {
				((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);
				stateTransition(Transition.RESTART);
				return false;
			}
			if (getEntity().isInShootingRange(target)) {

				if (target.isHidden()) {
//					try {
//						((AIConfiguationElements<?>) getEntity().getAiConfiguration().get(Types.ORDER)).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, true);
//					} catch (StateParameterNotFoundException e) {
//						e.printStackTrace();
//					}
					stateTransition(Transition.RESTART);
					((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);
					return false;
				} else {

					if (getEntityState().canPlotPath() && getEntityState().isMoveUpToTargetWhenEngaging()) {
						try {
							getEntityState().plotSecondaryAbsolutePath(target.getWorldTransform().origin);
						} catch (FSMException e) {
							e.printStackTrace();
						}
					}
				}

				stateTransition(Transition.TARGET_IN_RANGE);
				return false;
			} else {
				stateTransition(Transition.TARGET_OUT_OF_RANGE);
				return false;
			}

		}
	}

}
