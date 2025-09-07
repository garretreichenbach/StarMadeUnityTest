package org.schema.game.client.controller.tutorial.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SelectProductionTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private Vector3i pos;
	private short production;

	public SelectProductionTestState(AiEntityStateInterface gObj, String message, GameClientState state,
	                                 Vector3i pos, short production) {
		super(gObj, message, state);
		this.pos = pos;
		this.production = production;
		skipIfSatisfiedAtEnter = true;
		setMarkers(new ObjectArrayList<TutorialMarker>());

		getMarkers().add(new TutorialMarker(pos, Lng.str("Press %s here to open", KeyboardMappings.ACTIVATE.getKeyChar())));

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		EditableSendableSegmentController s = getGameState().getController().getTutorialMode().currentContext;
		if (s != null) {

			InventoryControllerManager iv = getGameState().getGlobalGameControlManager().getIngameControlManager()
					.getPlayerGameControlManager().getInventoryControlManager();
			return iv.isTreeActive() && iv.getSecondInventory() != null && iv.getSecondInventory().getParameter() == ElementCollection.getIndex(pos)
					&& iv.getSecondInventory().getProduction() == production;

		} else {
			System.err.println("[TUTORIAL] SELECT PRODUCTION HAS NO SegmentController CONTEXT: " + pos + "; prod " + production);
			getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
		}
		return false;
	}

}
