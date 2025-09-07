package org.schema.game.server.ai.program.creature.character.states;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.server.ai.AlreadyAtTargetException;
import org.schema.game.server.ai.CannotReachTargetException;
import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class CharacterMovingToPosition extends CharacterState {

	/**
	 *
	 */
	
	private Vector3f currentlyPlotted;
	private float targetX;
	private float targetY;
	private float targetZ;
	private String targetAffinity = "none";

	public CharacterMovingToPosition(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj, mic);
	}

	@Override
	public boolean onEnter() {

		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		Vector3f goToTarget = getEntityState().getGotoTarget();

		float targetX = ((AIConfiguationElements<Float>) getEntity().getAiConfiguration().get(Types.TARGET_X)).getCurrentState();
		float targetY = ((AIConfiguationElements<Float>) getEntity().getAiConfiguration().get(Types.TARGET_Y)).getCurrentState();
		float targetZ = ((AIConfiguationElements<Float>) getEntity().getAiConfiguration().get(Types.TARGET_Z)).getCurrentState();
		String targetAffinity = ((AIConfiguationElements<String>) getEntity().getAiConfiguration().get(Types.TARGET_AFFINITY)).getCurrentState();

		if (targetX != this.targetX || targetY != this.targetY || targetZ != this.targetZ || (targetAffinity != null && this.targetAffinity != null && !targetAffinity.equals(this.targetAffinity))) {
			try {
				getEntityState().makeMoveTarget();
			} catch (CannotReachTargetException e1) {
				e1.printStackTrace();
			} catch (AlreadyAtTargetException e1) {
				e1.printStackTrace();
			}

			//			System.err.println("CHECKING: "+getEntityState().canPlotPath()+"; "+currentlyPlotted+" / "+goToTarget);
			if (!getEntityState().canPlotPath() && (currentlyPlotted == null || !currentlyPlotted.equals(goToTarget))) {
				System.err.println("[CHARACTERMOVING] Overriding move command");
				try {
					getEntityState().cancelMoveCommad();
				} catch (FSMException e) {
					e.printStackTrace();
				}
			}
		}
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
		this.targetAffinity = targetAffinity;

		if (getEntityState().canPlotPath()) {
			try {
				System.err.println("[CHARACTERMOVING] PLOTTING MOVE " + goToTarget);
				getEntityState().plotSecondaryAbsolutePath(goToTarget);
				currentlyPlotted = new Vector3f(goToTarget);
				stateTransition(Transition.RESTART);
			} catch (FSMException e) {
				e.printStackTrace();
			}

		}
		return false;
	}

}
