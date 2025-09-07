package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.ai.AiEntityStateInterface;

public class ShipBuildModeSelectedTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private short clazz;

	public ShipBuildModeSelectedTestState(AiEntityStateInterface gObj, String message, GameClientState state, short clazz) {
		super(gObj, message, state);
		this.clazz = clazz;
		skipIfSatisfiedAtEnter = true;
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		SegmentPiece selectedBlock = getGameState().getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().
						getInShipControlManager().getShipControlManager().getSegmentBuildController().getSelectedBlock();
		return selectedBlock != null && selectedBlock.getType() == (clazz);
	}

}
