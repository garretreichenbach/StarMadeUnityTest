package org.schema.game.server.ai.program.creature.character;

import org.schema.game.common.data.creature.AICharacter;
import org.schema.game.common.data.creature.AICharacterPlayer;
import org.schema.game.server.ai.CharacterAIEntity;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.game.server.ai.program.creature.NPCProgram;

//<E extends CreatureAIEntity<A,F>, A extends AIPlayer, F extends AICreature<A>>
public class CharacterNPCProgram extends NPCProgram<CharacterAIEntity, AICharacterPlayer, AICharacter> {

	public CharacterNPCProgram(
			CreatureAIEntity<AICharacterPlayer, AICharacter> entityState,
			boolean startSuspended) {
		super(entityState, startSuspended);
	}

}
