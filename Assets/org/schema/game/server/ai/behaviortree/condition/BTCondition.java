package org.schema.game.server.ai.behaviortree.condition;

import org.schema.game.server.ai.behaviortree.BTNode;

public abstract class BTCondition {

	public boolean NOT = false;
	public BTConditionState state = BTConditionState.READY;
	public boolean isSatisfied(BTNode node){
		state = BTConditionState.RUNNIG;
		if(isSatisfiedImpl(node)){
			state = BTConditionState.SUCCESS;
			return true;
		}else{
			state = BTConditionState.FAILURE;
			return false;
		}
	}
	
	protected abstract boolean isSatisfiedImpl(BTNode node);
}
