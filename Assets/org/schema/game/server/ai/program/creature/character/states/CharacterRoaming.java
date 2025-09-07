package org.schema.game.server.ai.program.creature.character.states;

import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class CharacterRoaming extends CharacterState {

	/**
	 *
	 */
	

	public CharacterRoaming(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj, mic);
	}

	@Override
	public boolean onEnter() {
		if (getEntity().getOwnerState().getConversationPartner() != null) {
			//System.err.println("[AI] Not Roaming while in conversation");
		} else {
			if (getEntityState().canPlotPath()) {
				try {
					getEntityState().plotSecondaryPath();
					//				System.err.println("PLOT PATH REQUESTED");
				} catch (FSMException e) {
					e.printStackTrace();
				}
			} else {
				//			System.err.println("CANNOT PLOT PATH");
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

		if (getEntity().getAffinity() != null) {

		}

		//		Vector3f e = new Vector3f((float)Math.random()-0.5f, (float)Math.random()-0.5f, (float)Math.random()-0.5f);
		//		e.scale((float)Math.random()*8.f);
		//		e.add(getEntity().getWorldTransform().origin);
		//
		//		getEntityState().getCurrentMoveTarget().set(e);
		stateTransition(Transition.RESTART);
		return false;

	}

}
