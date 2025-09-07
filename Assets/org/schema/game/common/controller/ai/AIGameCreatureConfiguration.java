package org.schema.game.common.controller.ai;

import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.schine.graphicsengine.core.settings.states.FloatStates;
import org.schema.schine.graphicsengine.core.settings.states.IntegerStates;
import org.schema.schine.graphicsengine.core.settings.states.StaticStates;
import org.schema.schine.graphicsengine.core.settings.states.StringStates;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public abstract class AIGameCreatureConfiguration<E extends CreatureAIEntity<?, A>, A extends AICreature<? extends AIPlayer>> extends AIGameConfiguration<E, A> {

	public static final String BEHAVIOR_ROAMING = "Roaming";
	public static final String BEHAVIOR_ATTACKING = "Attacking";
	public static final String BEHAVIOR_IDLING = "Idling";
	public static final String BEHAVIOR_FOLLOWING = "Following";
	public static final String BEHAVIOR_GOTO = "GoTo";

	public static final int AGGRO_PROXIMITY = 1;
	public static final int AGGRO_ATTACKED = 2;
	public static final int ATTACK_STRUCTURES = 4;
	public static final int STOP_ATTACKING = 8;

	public AIGameCreatureConfiguration(StateInterface state,
	                                   A owner) {
		super(state, owner);
	}

	private void addSetting(Int2ObjectOpenHashMap<AIConfiguationElements<?>> elements, AIConfiguationElements<?> e) {
		elements.put(e.getType().ordinal(), e);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.ai.AIGameConfiguration#initialize(it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap)
	 */
	@Override
	public void initialize(
			Int2ObjectOpenHashMap<AIConfiguationElements<?>> elements) {
		addSetting(elements, new AIConfiguationElements(Types.ACTIVE, true, new StaticStates(false, true), this));
		addSetting(elements, new AIConfiguationElements(Types.ORDER, BEHAVIOR_IDLING, new StaticStates(BEHAVIOR_ROAMING, BEHAVIOR_ATTACKING, BEHAVIOR_IDLING, BEHAVIOR_FOLLOWING, BEHAVIOR_GOTO), this));
		addSetting(elements, new AIConfiguationElements(Types.OWNER, "none", new StringStates("none"), this));
		addSetting(elements, new AIConfiguationElements(Types.AGGRESIVENESS, AGGRO_ATTACKED | AGGRO_PROXIMITY | STOP_ATTACKING, new IntegerStates(AGGRO_ATTACKED | AGGRO_PROXIMITY | STOP_ATTACKING, 0, 100), this));
		addSetting(elements, new AIConfiguationElements(Types.FEAR, 0, new IntegerStates(0, 0, 100), this));
		addSetting(elements, new AIConfiguationElements(Types.ORIGIN_X, 0, new IntegerStates(0, 0, 100), this));
		addSetting(elements, new AIConfiguationElements(Types.ORIGIN_Y, 0, new IntegerStates(0, 0, 100), this));
		addSetting(elements, new AIConfiguationElements(Types.ORIGIN_Z, 0, new IntegerStates(0, 0, 100), this));
		addSetting(elements, new AIConfiguationElements(Types.ROAM_X, 0, new IntegerStates(0, 0, 100), this));
		addSetting(elements, new AIConfiguationElements(Types.ROAM_Y, 0, new IntegerStates(0, 0, 100), this));
		addSetting(elements, new AIConfiguationElements(Types.ROAM_Z, 0, new IntegerStates(0, 0, 100), this));
		addSetting(elements, new AIConfiguationElements(Types.ATTACK_TARGET, "none", new StringStates("none"), this));
		addSetting(elements, new AIConfiguationElements(Types.FOLLOW_TARGET, "none", new StringStates("none"), this));
		addSetting(elements, new AIConfiguationElements(Types.TARGET_X, 0f, new FloatStates(0f, Float.MIN_VALUE, Float.MAX_VALUE), this));
		addSetting(elements, new AIConfiguationElements(Types.TARGET_Y, 0f, new FloatStates(0f, Float.MIN_VALUE, Float.MAX_VALUE), this));
		addSetting(elements, new AIConfiguationElements(Types.TARGET_Z, 0f, new FloatStates(0f, Float.MIN_VALUE, Float.MAX_VALUE), this));
		addSetting(elements, new AIConfiguationElements(Types.TARGET_AFFINITY, "none", new StringStates("none"), this));

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.ai.AIGameConfiguration#getIdleEntityState()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected E getIdleEntityState() {
		CreatureAIEntity<? extends AIPlayer, ?> instantiateAIEntity = getOwner().instantiateAIEntity();
		assert (instantiateAIEntity != null);
		return (E) instantiateAIEntity;
	}

	@Override
	protected void prepareActivation() {
		getAiEntityState().start();
	}

	public boolean isAttackOnProximity() {
		return (((AIConfiguationElements<Integer>) get(Types.AGGRESIVENESS)).getCurrentState().intValue() & AGGRO_PROXIMITY) == AGGRO_PROXIMITY;
	}

	public boolean isAttackOnAttacked() {
		return (((AIConfiguationElements<Integer>) get(Types.AGGRESIVENESS)).getCurrentState().intValue() & AGGRO_ATTACKED) == AGGRO_ATTACKED;
	}

	public boolean isAttackStructures() {
		return (((AIConfiguationElements<Integer>) get(Types.AGGRESIVENESS)).getCurrentState().intValue() & ATTACK_STRUCTURES) == ATTACK_STRUCTURES;
	}

	public boolean isStopAttacking() {
		return (((AIConfiguationElements<Integer>) get(Types.AGGRESIVENESS)).getCurrentState().intValue() & STOP_ATTACKING) == STOP_ATTACKING;
	}

	public void setAttack(boolean v, int valMask, boolean send) {
		if (v) {
			((AIConfiguationElements<Integer>) get(Types.AGGRESIVENESS)).setCurrentState(((AIConfiguationElements<Integer>) get(Types.AGGRESIVENESS)).getCurrentState().intValue() | valMask, send);
		} else {
			((AIConfiguationElements<Integer>) get(Types.AGGRESIVENESS)).setCurrentState(((AIConfiguationElements<Integer>) get(Types.AGGRESIVENESS)).getCurrentState().intValue() & ~valMask, send);
		}
	}

	public void setAttackOnProximity(boolean v, boolean send) {
		setAttack(v, AGGRO_PROXIMITY, send);
	}

	public void setAttackOnAttacked(boolean v, boolean send) {
		setAttack(v, AGGRO_ATTACKED, send);
	}

	public void setAttackStructures(boolean v, boolean send) {
		setAttack(v, ATTACK_STRUCTURES, send);
	}

	public void setStopAttacking(boolean v, boolean send) {
		setAttack(v, STOP_ATTACKING, send);
	}
}
