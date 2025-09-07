package org.schema.game.common.controller.ai;

import api.utils.ai.CustomAITargetUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.graphicsengine.core.settings.states.StaticStates;
import org.schema.schine.network.StateInterface;

import java.util.ArrayList;

public abstract class AIGameSegmentControllerConfiguration<E extends AiEntityState, A extends SegmentController> extends AIGameConfiguration<E, A> {

	public AIGameSegmentControllerConfiguration(StateInterface state,
	                                            A owner) {
		super(state, owner);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.ai.AIGameConfiguration#initialize(it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap)
	 */
	@Override
	public void initialize(
			Int2ObjectOpenHashMap<AIConfiguationElements<?>> elements) {
		//INSERTED CODE
//		System.err.println("[AIGameSegmentControllerConfiguration] Registering custom mod targeting programs");
		ArrayList<String> aimAtStates = new ArrayList<>(CustomAITargetUtil.getCustomPrograms().keySet());
		//Add all the vanilla states
		aimAtStates.add("Any");
		aimAtStates.add("Selected Target");
		aimAtStates.add("Ships");
		aimAtStates.add("Stations");
		aimAtStates.add("Missiles");
		aimAtStates.add("Astronauts");
		///
		elements.put(Types.AIM_AT.ordinal(), new AIConfiguationElements(Types.AIM_AT, "Any", new StaticStates(aimAtStates.toArray(new String[0])), this));
		
		elements.put(Types.TYPE.ordinal(), new AIConfiguationElements(Types.TYPE, "Ship", new StaticStates("Turret", "Ship", "Fleet"), this));

		elements.put(Types.ACTIVE.ordinal(), new AIConfiguationElements(Types.ACTIVE, false, new StaticStates(false, true), this));
		
		elements.put(Types.MANUAL.ordinal(), new AIConfiguationElements(Types.MANUAL, false, new StaticStates(false, true), this));
		
		elements.put(Types.PRIORIZATION.ordinal(), new AIConfiguationElements(Types.PRIORIZATION, "Highest", new StaticStates("Highest", "Lowest", "Random"), this));

		elements.put(Types.FIRE_MODE.ordinal(), new AIConfiguationElements(Types.FIRE_MODE, "Simultaneous", new StaticStates("Simultaneous", "Volley"), this));
		
	}

}
