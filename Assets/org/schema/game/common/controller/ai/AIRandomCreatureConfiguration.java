package org.schema.game.common.controller.ai;

import org.schema.game.common.data.creature.AICompositeCreature;
import org.schema.game.common.data.creature.AICompositeCreaturePlayer;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.schine.network.StateInterface;

//												   AIGameCreatureConfiguration<CreatureAIEntity<AICompositeCreaturePlayer, AICompositeCreature>, AICompositeCreature>
public class AIRandomCreatureConfiguration extends AIGameCreatureConfiguration<CreatureAIEntity<AICompositeCreaturePlayer, AICompositeCreature>, AICompositeCreature> {

	public AIRandomCreatureConfiguration(StateInterface state, AICompositeCreature owner) {
		super(state, owner);
	}


	@Override
	protected boolean isForcedHitReaction() {
		return false;
	}

}
