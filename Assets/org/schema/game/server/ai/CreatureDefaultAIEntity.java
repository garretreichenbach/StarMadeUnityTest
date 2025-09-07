package org.schema.game.server.ai;

import org.schema.game.common.data.creature.AICompositeCreature;
import org.schema.game.common.data.creature.AICompositeCreaturePlayer;
import org.schema.game.server.ai.program.creature.critter.CritterNPCProgram;

public class CreatureDefaultAIEntity extends CreatureAIEntity<AICompositeCreaturePlayer, AICompositeCreature> {

	public CreatureDefaultAIEntity(String name, AICompositeCreature s) {
		super(name, s);
	}

	@Override
	public void start() {
		CritterNPCProgram p = new CritterNPCProgram(this, false);
		setCurrentProgram(p);
	}

}
