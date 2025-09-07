package org.schema.game.server.ai.program.creature.critter;

import org.schema.game.common.data.creature.AICompositeCreature;
import org.schema.game.common.data.creature.AICompositeCreaturePlayer;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.game.server.ai.CreatureDefaultAIEntity;
import org.schema.game.server.ai.program.creature.NPCProgram;

//<E extends CreatureAIEntity<A,F>, A extends AIPlayer, F extends AICreature<A>>
public class CritterNPCProgram extends NPCProgram<CreatureDefaultAIEntity, AICompositeCreaturePlayer, AICompositeCreature> {

	public CritterNPCProgram(
			CreatureAIEntity<AICompositeCreaturePlayer, AICompositeCreature> entityState,
			boolean startSuspended) {
		super(entityState, startSuspended);
	}

}
