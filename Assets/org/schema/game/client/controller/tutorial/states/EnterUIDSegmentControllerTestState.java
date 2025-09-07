package org.schema.game.client.controller.tutorial.states;

import java.util.Map.Entry;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class EnterUIDSegmentControllerTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private String uidStartsWith;

	public EnterUIDSegmentControllerTestState(AiEntityStateInterface gObj, String message, GameClientState state, String uidStartsWith, Vector3i enterPos) {
		super(gObj, message, state);
		skipIfSatisfiedAtEnter = true;
		this.uidStartsWith = uidStartsWith;

		setMarkers(new ObjectArrayList<TutorialMarker>(1));
		getMarkers().add(new TutorialMarker(enterPos, "Press " + KeyboardMappings.ACTIVATE.getKeyChar() + " here to enter"));
	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {

		synchronized (getGameState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Entry<String, Sendable> e : getGameState().getLocalAndRemoteObjectContainer().getUidObjectMap().entrySet()) {
				if (e.getKey().startsWith(uidStartsWith)) {
					if (e.getValue() instanceof SegmentController && ((SegmentController) e.getValue()).getSectorId() == getGameState().getPlayer().getCurrentSectorId()) {
						getMarkers().get(0).context = (SegmentController) e.getValue();

						SegmentPiece entered = getGameState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().
								getPlayerIntercationManager().getInShipControlManager().getEntered();

						if (entered != null && entered.getSegment().getSegmentController() != null) {
							return entered.getSegment().getSegmentController().getUniqueIdentifier().startsWith(uidStartsWith);
						}
						return false;
					} else {
						getGameState().getController().popupAlertTextMessage(Lng.str("TUTORIAL ERROR:\nNOT A SEGCONTROLLER\n%s",  e.getValue()), 0);
						stateTransition(Transition.TUTORIAL_FAILED);
						return false;
					}
				}
			}
		}
		getGameState().getController().popupAlertTextMessage(Lng.str("TUTORIAL ERROR:\nSEGCONTROLLER NOT FOUND (Starting UID with)\n%s", uidStartsWith), 0);
		stateTransition(Transition.TUTORIAL_FAILED);
		return false;

	}

}
