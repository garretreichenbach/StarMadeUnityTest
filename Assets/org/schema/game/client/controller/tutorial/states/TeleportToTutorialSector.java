package org.schema.game.client.controller.tutorial.states;

import java.util.Map.Entry;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.linearmath.Transform;

public class TeleportToTutorialSector extends SatisfyingCondition {

	/**
	 *
	 */
	
	private String uidStartsWith;
	private Vector3i pos;

	public TeleportToTutorialSector(AiEntityStateInterface gObj, String message, GameClientState state, String uidStartsWith, Vector3i pos) {
		super(gObj, message, state);
		skipIfSatisfiedAtEnter = true;
		endIfSatisfied = true;
		this.uidStartsWith = uidStartsWith;
		this.pos = pos;

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		if (!getGameState().getPlayer().isInTutorial()) {
			System.err.println("[TUT] toTutorialSector: not yet in tutorial sector");
			return false;
		}
		synchronized (getGameState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Entry<String, Sendable> e : getGameState().getLocalAndRemoteObjectContainer().getUidObjectMap().entrySet()) {
				if (e.getKey().startsWith(uidStartsWith)) {

					if (e.getValue() instanceof SegmentController && ((SegmentController) e.getValue()).getSectorId() == getGameState().getPlayer().getCurrentSectorId()) {
						SegmentController s = (SegmentController) e.getValue();
						if (s.getSegmentBuffer().getPointUnsave(new Vector3i(
								SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF)) != null) {
							Transform t = new Transform();
							t.setIdentity();
							s.getAbsoluteElementWorldPositionShifted(pos, t.origin);

							getGameState().getPlayer().getAssingedPlayerCharacter().getGhostObject().setWorldTransform(t);
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				} else {
					System.err.println("[TUT] toTutorialSector: " + uidStartsWith + " NOT MATCHED: " + e.getValue());
				}
			}
		}
		return false;

	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onEnter()
	 */
	@Override
	public boolean onEnter() {
		if (getGameState().getPlayer().getFirstControlledTransformableWOExc() == null || !(getGameState().getPlayer().getFirstControlledTransformableWOExc() instanceof PlayerCharacter)) {
			try {
				getGameState().getController().popupAlertTextMessage(Lng.str("You can't be in a structure\nto do this tutorial."), 0);
				getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
			} catch (FSMException e) {
				e.printStackTrace();
			}
			return false;
		}
		getGameState().getPlayer().warpToTutorialSectorClient();
		return super.onEnter();
	}
}
