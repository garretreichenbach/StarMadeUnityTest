package org.schema.game.server.ai;

import org.schema.game.common.data.creature.AICharacter;
import org.schema.game.common.data.creature.AICharacterPlayer;
import org.schema.game.server.ai.program.creature.character.CharacterNPCProgram;

public class CharacterAIEntity extends CreatureAIEntity<AICharacterPlayer, AICharacter> implements MovableAICreature {

	public CharacterAIEntity(String name, AICharacter s) {
		super(name, s);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.AIGameEntityState#update(org.schema.schine.graphicsengine.core.Timer)
	 */

	@Override
	public void start() {
		CharacterNPCProgram p = new CharacterNPCProgram(this, false);
		setCurrentProgram(p);
	}

}
