package org.schema.game.client.controller.tutorial.states;

import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.linearmath.Transform;

public class TeleportToTestState extends TimedState {

	/**
	 *
	 */
	
	private String uidStartsWith;
	private Vector3i pos;
	private int rightTime = 0;

	public TeleportToTestState(AiEntityStateInterface gObj, String message, GameClientState state, String uidStartsWith, Vector3i pos) {
		super(gObj, message, state);
		this.uidStartsWith = uidStartsWith;
		this.pos = pos;

	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.TimedState#checkSatisfyingCondition()
	 */
	@Override
	protected boolean checkSatisfyingCondition() {
		Transform t = null;
		synchronized (getGameState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Entry<String, Sendable> e : getGameState().getLocalAndRemoteObjectContainer().getUidObjectMap().entrySet()) {
				if (e.getKey().startsWith(uidStartsWith)) {

					if (e.getValue() instanceof SegmentController && ((SegmentController) e.getValue()).getSectorId() == getGameState().getPlayer().getCurrentSectorId()) {

						SegmentController s = (SegmentController) e.getValue();
						if (s.getSegmentBuffer().getPointUnsave(
								new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF)) != null) {
							t = new Transform();
							t.setIdentity();
							s.getAbsoluteElementWorldPositionShifted(pos, t.origin);

							getGameState().getPlayer().getAssingedPlayerCharacter().getGhostObject().setWorldTransform(t);

						}
					} else {
					}
				} else {
					System.err.println("NOT MATCHED: " + e.getValue());
				}
			}
		}
		if (t != null && !getGameState().getPlayer().getAssingedPlayerCharacter().isHidden()) {
			Vector3f dist = new Vector3f();
			dist.sub(getGameState().getPlayer().getAssingedPlayerCharacter().getGhostObject().getWorldTransform(new Transform()).origin, t.origin);

			if (dist.length() < 0.5f) {
				if (rightTime > 100) {
					System.err.println("SATITSFIED ON POSITION: " + getGameState().getPlayer().getAssingedPlayerCharacter().getGhostObject().getWorldTransform(new Transform()).origin + "; " + getGameState().getPlayer().getAssingedPlayerCharacter().getWorldTransform().origin);
					return true;
				} else {
					rightTime++;
					return false;
				}
			} else {
				return false;
			}

		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onEnter()
	 */
	@Override
	public boolean onEnter() {
		synchronized (getGameState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Entry<String, Sendable> e : getGameState().getLocalAndRemoteObjectContainer().getUidObjectMap().entrySet()) {
				if (e.getKey().startsWith(uidStartsWith)) {

					if (e.getValue() instanceof SegmentController && ((SegmentController) e.getValue()).getSectorId() == getGameState().getPlayer().getCurrentSectorId()) {

						SegmentController s = (SegmentController) e.getValue();
						Transform t = new Transform();
						t.setIdentity();
						s.getAbsoluteElementWorldPositionShifted(pos, t.origin);

						getGameState().getPlayer().getAssingedPlayerCharacter().getGhostObject().setWorldTransform(t);
						return super.onEnter();
					} else {
					}
				} else {
					System.err.println("NOT MATCHED: " + e.getValue());
				}
			}
		}
		return super.onEnter();
	}

	@Override
	public int getDurationInMs() {
		return 5000;
	}

}