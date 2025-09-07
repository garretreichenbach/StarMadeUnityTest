package org.schema.game.common.controller.ai;

import org.schema.game.common.data.creature.AICharacter;
import org.schema.game.common.data.creature.AICharacterPlayer;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.schine.network.StateInterface;

public class AICharacterConfiguration extends AIGameCreatureConfiguration<CreatureAIEntity<AICharacterPlayer, AICharacter>, AICharacter> {

	public AICharacterConfiguration(StateInterface state, AICharacter owner) {
		super(state, owner);
	}


	@Override
	protected boolean isForcedHitReaction() {
				return false;
	}

}
