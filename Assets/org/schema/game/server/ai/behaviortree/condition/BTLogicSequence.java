package org.schema.game.server.ai.behaviortree.condition;

import org.schema.game.server.ai.behaviortree.BTNode;
import org.schema.game.server.ai.behaviortree.action.BTAction;


public class BTLogicSequence {
	private BTCondition condition;
	private BTAction action;
	
	
	public void execute(BTNode node){
		if(condition.state == BTConditionState.READY){
			condition.isSatisfied(node);
		}
		
		if(condition.state == BTConditionState.SUCCESS){
			action.update(node);
		}
	}
}
