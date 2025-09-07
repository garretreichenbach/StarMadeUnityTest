package org.schema.game.server.ai.behaviortree.condition;

import java.util.List;

import org.schema.game.server.ai.behaviortree.BTNode;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BTConditionGroup extends BTCondition{

	public enum Operator{
		AND,
		OR
	}
	private Operator operator = Operator.AND;
	private final List<BTCondition> conditions = new ObjectArrayList<BTCondition>();
	@Override
	public boolean isSatisfiedImpl(BTNode node) {
		
		if(!conditions.isEmpty()){
			if(operator == Operator.OR){
				
				for(BTCondition c : conditions){
					if(c.isSatisfied(node) == !c.NOT){
						return true;
					}
				}
				return false;
				
			}else{
				assert(operator == Operator.AND);
				
				for(BTCondition c : conditions){
					if(c.isSatisfied(node) != !c.NOT){
						return false;
					}
				}
				return true;
			}
		}
		
		return true;
	}

}
