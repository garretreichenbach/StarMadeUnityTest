package org.schema.game.client.controller.tutorial.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class OpenChestTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private Vector3i pos;
	private SegmentPiece cc;

	public OpenChestTestState(AiEntityStateInterface gObj, String message, GameClientState state,
	                          Vector3i pos) {
		super(gObj, message, state);
		this.pos = pos;
		skipIfSatisfiedAtEnter = true;
		setMarkers(new ObjectArrayList<TutorialMarker>());

		getMarkers().add(new TutorialMarker(pos, Lng.str("Press %s here to open", KeyboardMappings.ACTIVATE.getKeyChar())));

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		EditableSendableSegmentController s = getGameState().getController().getTutorialMode().currentContext;
		if (s != null) {
			if (cc == null) {
				cc = s.getSegmentBuffer().getPointUnsave(pos);
				if (cc != null) {
					if (!ElementKeyMap.isValidType(cc.getType()) || !(ElementKeyMap.getFactorykeyset().contains(cc.getType()) || cc.getType() != ElementKeyMap.STASH_ELEMENT)) {
						System.err.println("[ERROR] Block invalid or cannot be opened: " + cc + "!");
						getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
						return false;
					}
				}
			} else {
				cc.refresh();
				InventoryControllerManager iv = getGameState().getGlobalGameControlManager().getIngameControlManager()
						.getPlayerGameControlManager().getInventoryControlManager();
				return iv.isTreeActive() && iv.getSecondInventory() != null && iv.getSecondInventory().getParameter() == ElementCollection.getIndex(pos);

			}
		} else {
			System.err.println("[TUTORIAL] ACTIVATE BLOCK HAS NO SegmentController CONTEXT: " + pos);
			getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
		}
		return false;
	}

}
