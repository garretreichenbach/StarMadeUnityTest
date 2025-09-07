package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;
import java.util.Map.Entry;

public class ActivateGravityTestState extends SatisfyingCondition {

	private String uidStartsWith;

	public ActivateGravityTestState(AiEntityStateInterface gObj, String message, GameClientState state, String uidStartsWith) {
		super(gObj, message, state);
		this.uidStartsWith = uidStartsWith;

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {

		synchronized (getGameState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Entry<String, Sendable> e : getGameState().getLocalAndRemoteObjectContainer().getUidObjectMap().entrySet()) {
				if (e.getKey().startsWith(uidStartsWith)) {
					if (getGameState().getPlayer().getAssingedPlayerCharacter().getGravity().source == e.getValue()) {
						return true;
					}
					if (e.getValue() instanceof SegmentController && ((SegmentController) e.getValue()).getSectorId() == getGameState().getPlayer().getCurrentSectorId()) {
						SegmentController s = (SegmentController) e.getValue();

						getGameState().getPlayer().getAssingedPlayerCharacter().scheduleGravityWithBlockBelow(new Vector3f(0, -9.81f, 0), s);
						return super.onEnter();
					} else {
					}
					if (getGameState().getPlayer().getAssingedPlayerCharacter().getGravity().source == e.getValue()) {
						return true;
					}
				} else {
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

		return super.onEnter();
	}

}