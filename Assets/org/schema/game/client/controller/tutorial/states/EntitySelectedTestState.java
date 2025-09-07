package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.ai.AiEntityStateInterface;

public class EntitySelectedTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private Class<? extends SimpleTransformableSendableObject> clazz;

	public EntitySelectedTestState(AiEntityStateInterface gObj, String message, GameClientState state, Class<? extends SimpleTransformableSendableObject> clazz) {
		super(gObj, message, state);
		this.clazz = clazz;
		skipIfSatisfiedAtEnter = true;
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		SimpleTransformableSendableObject selectedEntity = getGameState().getGlobalGameControlManager().getIngameControlManager().
				getPlayerGameControlManager().getPlayerIntercationManager().
				getSelectedEntity();
		return selectedEntity != null && clazz.isInstance(selectedEntity);
	}

}
