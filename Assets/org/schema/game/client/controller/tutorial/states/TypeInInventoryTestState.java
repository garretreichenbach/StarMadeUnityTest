package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.common.language.Lng;

public class TypeInInventoryTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private short type;
	private int count;
	private String mg;

	public TypeInInventoryTestState(AiEntityStateInterface gObj, String message, GameClientState state, short type, int count) {
		super(gObj, message, state);
		skipIfSatisfiedAtEnter = true;
		this.type = type;
		this.count = count;
		this.mg = message;

	}

	@Override
	protected boolean checkSatisfyingCondition() {
		return getGameState().getPlayer().getInventory().getOverallQuantity(type) >= count;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onEnter()
	 */
	@Override
	public boolean onEnter() {
		getGameState().getController().getTutorialMode().highlightType = type;
		return super.onEnter();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onUpdate()
	 */
	@Override
	public boolean onUpdate() throws FSMException {
		setMessage(Lng.str("%sGet %s more %s!", mg, (count - getGameState().getPlayer().getInventory().getOverallQuantity(type)), ElementKeyMap.getInfo(type).getName() ));
		return super.onUpdate();
	}

}
