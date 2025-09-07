package org.schema.game.client.controller.tutorial.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class AssignWeaponTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private short type;
	private int slot;

	public AssignWeaponTestState(AiEntityStateInterface gObj, String message, GameClientState state, short type, int slot) {
		super(gObj, message, state);
		skipIfSatisfiedAtEnter = true;
		this.slot = slot - 1;
		this.type = type;
		if (slot == 0) {
			this.slot = 10;
		}
	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {

		if (getGameState().getController().getTutorialMode().lastSpawnedShip == null) {
			getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
			return false;
		}
		SlotAssignment shipConfiguration = getGameState().getController().getTutorialMode().lastSpawnedShip.getSlotAssignment();
		if (shipConfiguration != null) {
			Vector3i assigned = shipConfiguration.get(slot);
			if (assigned != null) {
				SegmentPiece pointUnsave = getGameState().getController().getTutorialMode().lastSpawnedShip.getSegmentBuffer().getPointUnsave(assigned);
				return pointUnsave != null && pointUnsave.getType() == type;
			}
		}
		return false;
	}

}
