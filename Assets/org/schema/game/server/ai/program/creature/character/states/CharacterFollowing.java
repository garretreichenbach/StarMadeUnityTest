package org.schema.game.server.ai.program.creature.character.states;

import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.AIGameCreatureConfiguration;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

public class CharacterFollowing extends CharacterState {

	/**
	 *
	 */
	

	public CharacterFollowing(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
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

		if (getEntityState().getFollowTarget() != null) {

			if (getEntityState().getFollowTarget().isHidden()) {
				try {
					((AIConfiguationElements<?>) getEntity().getAiConfiguration().get(Types.ORDER)).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, true);
				} catch (StateParameterNotFoundException e) {
					e.printStackTrace();
				}
				stateTransition(Transition.RESTART);
			} else {

				if (getEntityState().canPlotPath()) {
					try {
						getEntityState().plotSecondaryAbsolutePath(getEntityState().getFollowTarget().getWorldTransform().origin);
					} catch (FSMException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return false;
	}

}
