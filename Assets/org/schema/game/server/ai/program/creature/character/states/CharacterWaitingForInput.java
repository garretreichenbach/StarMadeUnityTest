package org.schema.game.server.ai.program.creature.character.states;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.AIGameCreatureConfiguration;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.ai.AlreadyAtTargetException;
import org.schema.game.server.ai.CannotReachTargetException;
import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.network.server.ServerMessage;

public class CharacterWaitingForInput extends CharacterState {

	/**
	 *
	 */
	
	private Vector3f currentlyPlotted;

	public CharacterWaitingForInput(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
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

		AIConfiguationElements<String> setting = (AIConfiguationElements<String>) getEntity().getAiConfiguration().get(Types.ORDER);

		Integer originX = ((AIConfiguationElements<Integer>) getEntity().getAiConfiguration().get(Types.ORIGIN_X)).getCurrentState();
		Integer originY = ((AIConfiguationElements<Integer>) getEntity().getAiConfiguration().get(Types.ORIGIN_Y)).getCurrentState();
		Integer originZ = ((AIConfiguationElements<Integer>) getEntity().getAiConfiguration().get(Types.ORIGIN_Z)).getCurrentState();

		String s = setting.getCurrentState();

		if (s.equals(AIGameCreatureConfiguration.BEHAVIOR_ROAMING)) {
			stateTransition(Transition.ROAM);
			return true;
		} else if (s.equals(AIGameCreatureConfiguration.BEHAVIOR_ATTACKING)) {
			getEntityState().setAttackTarget(((AIConfiguationElements<String>) getEntity().getAiConfiguration().get(Types.ATTACK_TARGET)).getCurrentState());
			stateTransition(Transition.ATTACK);
			return true;
		} else if (s.equals(AIGameCreatureConfiguration.BEHAVIOR_IDLING)) {
			return true;
		} else if (s.equals(AIGameCreatureConfiguration.BEHAVIOR_FOLLOWING)) {
			getEntityState().setFollowTarget(((AIConfiguationElements<String>) getEntity().getAiConfiguration().get(Types.FOLLOW_TARGET)).getCurrentState());
			stateTransition(Transition.FOLLOW);
			return true;
		} else if (s.equals(AIGameCreatureConfiguration.BEHAVIOR_GOTO)) {

			try {
				getEntityState().makeMoveTarget();

				Vector3f goToTarget = getEntityState().getGotoTarget();

				//				System.err.println("CHECKING: "+getEntityState().canPlotPath()+"; "+currentlyPlotted+" / "+goToTarget);
				//				if(!getEntityState().canPlotPath() && (currentlyPlotted == null || !currentlyPlotted.equals(goToTarget))){
				//					System.err.println("[CHARACTERMOVING] overriding move command");
				//					try {
				//						getEntityState().cancelMoveCommad();
				//					} catch (FSMException e) {
				//						e.printStackTrace();
				//					}
				//				}

				stateTransition(Transition.MOVE);

				currentlyPlotted = new Vector3f(goToTarget);
			} catch (CannotReachTargetException e) {
				e.printStackTrace();

				try {
					PlayerState ps = ((GameServerState) getEntity().getState()).getPlayerFromName(((AIConfiguationElements<String>) getEntity().getAiConfiguration().get(Types.OWNER)).getCurrentState().replaceAll("ENTITY_PLAYERSTATE_", ""));
					ps.sendServerMessage(new ServerMessage(Lng.astr("AI command failed:\nCannot reach target"), ServerMessage.MESSAGE_TYPE_ERROR));
				} catch (PlayerNotFountException e2) {
					e2.printStackTrace();
				}
				try {
					((AIConfiguationElements<?>) getEntity().getAiConfiguration().get(Types.ORDER)).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, true);
				} catch (StateParameterNotFoundException e1) {
					e1.printStackTrace();
				}
			} catch (AlreadyAtTargetException e) {
				try {
					((AIConfiguationElements<?>) getEntity().getAiConfiguration().get(Types.ORDER)).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, true);
				} catch (StateParameterNotFoundException e1) {
					e1.printStackTrace();
				}
			}
			return true;
		} else {
			throw new NullPointerException();
		}

	}

}
