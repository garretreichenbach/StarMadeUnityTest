package org.schema.game.client.controller.tutorial.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class EnterLastSpawnedShipTestState extends SatisfyingCondition {

	/**
	 *
	 */
	

	public EnterLastSpawnedShipTestState(AiEntityStateInterface gObj, String message, GameClientState state) {
		super(gObj, message, state);
		skipIfSatisfiedAtEnter = true;

		setMarkers(new ObjectArrayList<TutorialMarker>(1));
		getMarkers().add(new TutorialMarker(
				new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF), "Press " + KeyboardMappings.ACTIVATE.getKeyChar() + " here to enter"));
	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {

		if (getGameState().getController().getTutorialMode().lastSpawnedShip == null) {
			getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
			return false;
		}

		getMarkers().get(0).context = getGameState().getController().getTutorialMode().lastSpawnedShip;

		SegmentPiece entered = getGameState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().
				getPlayerIntercationManager().getInShipControlManager().getEntered();

		if (entered != null && entered.getSegment().getSegmentController() != null) {
			return entered.getSegment().getSegmentController() == getGameState().getController().getTutorialMode().lastSpawnedShip;
		}
		return false;
	}

}
