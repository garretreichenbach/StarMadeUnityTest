package org.schema.game.common.controller.ai;

import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.network.StateInterface;

public class EmptyAIEntityState extends AiEntityState {

	/**
	 *
	 */
	
	private final AIGameConfiguration<?, ?> config;

	public EmptyAIEntityState(String name, StateInterface state, AIGameConfiguration<?, ?> config) {
		super(name, state);
		this.config = config;
	}

	/**
	 * @return the config
	 */
	public AIGameConfiguration<?, ?> getConfig() {
		return config;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.AiEntityState#isActive()
	 */
	@Override
	public boolean isActive() {
		//		if(isOnServer()){
		return false;
		//		}else{
		//			return config.get(Types.ACTIVE).isOn();
		//		}
	}

	@Override
	public String toString() {
		return "EMPTY_AI_STATE";
	}

}
