package org.schema.game.common.data.creature;

import org.schema.game.server.ai.program.creature.character.AICreatureProgramInterface;

public abstract interface AICreatureMessage {
	public void handle(AICreatureProgramInterface p);

	public MessageType getType();

	enum MessageType {
		UNDER_FIRE,
		PROXIMITY,
		STOP,
		NO_PATH
	}
}
