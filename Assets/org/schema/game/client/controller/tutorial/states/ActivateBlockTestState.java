package org.schema.game.client.controller.tutorial.states;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ActivateBlockTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private Boolean active;
	private Vector3i pos;
	private SegmentPiece cc;
	private boolean before;
	private SimpleTransformableSendableObject gravSource;

	public ActivateBlockTestState(AiEntityStateInterface gObj, String message, GameClientState state,
	                              Vector3i pos, Boolean active) {
		super(gObj, message, state);
		this.pos = pos;
		skipIfSatisfiedAtEnter = true;
		this.active = active;
		setMarkers(new ObjectArrayList<TutorialMarker>());
		if (active == null) {
			getMarkers().add(new TutorialMarker(pos, "Press " + KeyboardMappings.ACTIVATE.getKeyChar() + " here to activate/decativate this block"));
		} else {
			getMarkers().add(new TutorialMarker(pos, "Press " + KeyboardMappings.ACTIVATE.getKeyChar() + " here to " + (active ? "activate" : "deactivate") + " this block"));
		}

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		EditableSendableSegmentController s = getGameState().getController().getTutorialMode().currentContext;
		if (s != null) {
			System.err.println("CHECK "+cc+"; "+active);
			if (cc == null) {
				cc = s.getSegmentBuffer().getPointUnsave(pos);
				if (cc != null) {
					if (!ElementKeyMap.isValidType(cc.getType()) || !ElementKeyMap.getInfo(cc.getType()).canActivate()) {
						System.err.println("[ERROR] Block invalid or cannot be activated: " + cc + "!");
						getEntityState().getMachine().getFsm().stateTransition(Transition.RESTART);
						return false;
					}
					before = cc.isActive();
					gravSource = getGameState().getCharacter().getGravity().source;
				}
			} else {
				cc.refresh();

				if (cc.getType() == ElementKeyMap.GRAVITY_ID) {
					if (active == null) {
//						System.err.println("GGG "+gravSource+"; "+getGameState().getCharacter().getGravity().source);
						return gravSource != getGameState().getCharacter().getGravity().source;
					} else {
						
//						System.err.println("GGG "+gravSource+"; "+getGameState().getCharacter().getGravity().source+"; "+getGameState().getCharacter().getGravity().
//								acceleration);
						if (active) {
							return getGameState().getCharacter().getGravity().source != null &&
									!getGameState().getCharacter().getGravity().
									accelerationEquals(0.0F, 0.0F, 0.0F);
						} else {
							return getGameState().getCharacter().getGravity().
									accelerationEquals(0.0F, 0.0F, 0.0F);
						}
					}
				}
				if (active == null) {
					return cc.isActive() != before;
				} else {
					return cc.isActive() == active;
				}
			}
		} else {
			System.err.println("[TUTORIAL] ACTIVATE BLOCK HAS NO SegmentController CONTEXT: " + pos);
		}
		return false;
	}

}
