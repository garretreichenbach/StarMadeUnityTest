package org.schema.game.common.data.player.dialog;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class VoidState extends State {

	/**
	 *
	 */
	

	public VoidState(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnter() {
		try {
			stateTransition(Transition.NEXT);
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
				return false;
	}

}
