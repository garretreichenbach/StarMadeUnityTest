package org.schema.game.server.ai.program.creature;

import java.util.HashMap;

import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.creature.character.AICreatureProgramInterface;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Transition;

public class NPCProgram<E extends CreatureAIEntity<A, F>, A extends AIPlayer, F extends AICreature<A>> extends TargetProgram<CreatureAIEntity<A, F>> implements AICreatureProgramInterface {
	private static final String CHAR = "CHAR";
	private static final String MOVE = "MOVE";
	private static final String ATT = "ATT";

	public NPCProgram(CreatureAIEntity<A, F> entityState, boolean startSuspended) {
		super(entityState, startSuspended);

		assert (super.getMachine() instanceof NPCMachine) : super.getMachine() + "; " + CHAR + "; " + machines;
	}

	@Override
	public void onAISettingChanged(AIConfiguationElementsInterface setting) throws FSMException {
		AIConfiguationElements<?> e = (AIConfiguationElements<?>) setting;
		if (e.getType() == Types.ORDER) {
			changedOrder(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.MachineProgram#getMachine()
	 */
	@Override
	public FiniteStateMachine getMachine() {
		assert (super.getMachine() instanceof NPCMachine) : super.getMachine();
		return super.getMachine();
	}

	@Override
	protected String getStartMachine() {
		return CHAR;
	}

	@Override
	protected void initializeMachines(
			HashMap<String, FiniteStateMachine<?>> machines) {
		machines.put(CHAR, new NPCMachine(getEntityState(), this));
		machines.put(MOVE, new NPCMoveMachine(getEntityState(), this));
		machines.put(ATT, new NPCAttackMachine(getEntityState(), this));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.MachineProgram#updateOtherMachines()
	 */
	@Override
	public void updateOtherMachines() throws FSMException {
		super.updateOtherMachines();

		getOtherMachine(ATT).update();
		getOtherMachine(MOVE).update();
	}

	@Override
	public void underFire(SimpleTransformableSendableObject from) throws FSMException {
		if (getEntityState().getEntity().getAiConfiguration().isAttackOnAttacked()) {
			if (getTarget() == null) {
				setTarget(from);

				getOtherMachine(ATT).getFsm().getCurrentState().stateTransition(Transition.ENEMY_FIRE);
			}
		}
	}

	@Override
	public void changedOrder(SimpleTransformableSendableObject from) throws FSMException {
		getOtherMachine(CHAR).getFsm().getCurrentState().stateTransition(Transition.STOP);
	}

	@Override
	public void enemyProximity(SimpleTransformableSendableObject from) throws FSMException {
		if (getEntityState().getEntity().getAiConfiguration().isAttackOnProximity()) {
			if (getTarget() == null) {
				setTarget(from);
				getOtherMachine(ATT).getFsm().getCurrentState().stateTransition(Transition.ENEMY_PROXIMITY);
			}
		}
	}

	@Override
	public void stopCurrent(SimpleTransformableSendableObject from) throws FSMException {
		setTarget(from);
		getMachine().getFsm().getCurrentState().stateTransition(Transition.STOP);
	}

	@Override
	public void onNoPath() throws FSMException {
		try {
			getOtherMachine(MOVE).getFsm().getCurrentState().stateTransition(Transition.PATH_FAILED);
		} catch (FSMException e) {
			e.printStackTrace();
		}

	}

	public void attack(SimpleTransformableSendableObject attackTarget) throws FSMException {
		setTarget(attackTarget);
		getOtherMachine(ATT).getFsm().getCurrentState().stateTransition(Transition.ENEMY_FIRE);

	}

}
