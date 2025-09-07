package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.ConditionList;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.schine.common.language.Lng;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;


public class SegmentControllerConditionGroup extends SegmentControllerCondition implements ConditionGroup{

	@RuleValue(tag = "AllTrue")
	public boolean allTrue;
	
	@RuleValue(tag = "Conditions")
	public ConditionList conditions = new ConditionList();
	
	@RuleValue(tag = "Inverse")
	public boolean inverse;

	private long groupTrigger;
	
	public SegmentControllerConditionGroup() {
		super();
	}

	@Override
	public long getTrigger() {
		return groupTrigger;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_CONDITION_GROUP;
	}
	public void createStateChange(short conditionIndex, RuleStateChange stateChange) {
		stateChange.changeLogCond.add(isSatisfied() ? (short)(conditionIndex +1) : (short)-(conditionIndex +1));
		stateChange.changeLogCond.add((short)conditions.size());
		for(short i = 0; i < conditions.size(); i++) {
			Condition<?> c = conditions.get(i);
			c.createStateChange(i, stateChange);
		}
	}
	public void addToList(ConditionList all) {
		all.add(this);
		getAllConditions(all);
	}
	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		int changed = 0;
		
		int currentLogIndex = stateChange.changeLogCond.size();
		for(short i = 0; i < conditions.size(); i++) {
			Condition<?> c = conditions.get(i);
			final boolean bef = c.isSatisfied();
			final boolean now = ((SegmentControllerCondition)c).checkSatisfied(i, stateChange, a, trigger, forceTrue);
			
			if(bef != now) {
				changed++;
			}
		}
		if(changed > 0) {
			//add amount of conditions that have changed for this group
			stateChange.changeLogCond.add(currentLogIndex, (short)changed);
		}
		
		if(allTrue) {
			boolean ok = true;
			for(short i = 0; i < conditions.size(); i++) {
				Condition<?> c = conditions.get(i);
				if(!c.isSatisfied()) {
					ok = false;
				}
			}
			return inverse ? !ok : ok;
		}else {
			boolean ok = false;
			for(short i = 0; i < conditions.size(); i++) {
				Condition<?> c = conditions.get(i);
				if(c.isSatisfied()) {
					ok = true;
				}
			}
			return inverse ? !ok : ok;
		}
	}

	@Override
	public String getDescriptionShort() {
		return allTrue ? Lng.str("All of the %s subconditions must be true", conditions.size()) : Lng.str("Either of the %s subconditions must be true", conditions.size());
	}

	@Override
	public ConditionList getConditions() {
		return conditions;
	}

	@Override
	public boolean isAllTrue() {
		return allTrue;
	}

	@Override
	public void setAllTrue(boolean b) {
		allTrue = b;
	}

	@Override
	public void addCondition(Condition<?> c) {
		c.parent = this;
		conditions.add(c);
		
		groupTrigger = calculateGroupTriggerRec(0);
	}

	@Override
	public void removeCondition(Condition<?> c) {
		c.parent = null;
		conditions.remove(c);
		groupTrigger = calculateGroupTriggerRec(0);
	}

	@Override
	public long calculateGroupTriggerRec(long t) {
		for(Condition<?> l : conditions) {
			t |= l.calculateGroupTriggerRec(t);
		}
		return t;
	}
	
	@Override
	public boolean isConditionsAvailable() {
		return true;
	}
	public int processReceivedState(int cIndex, ShortArrayList changeLogCond, short cIndexValue) {
		cIndex = super.processReceivedState(cIndex, changeLogCond, cIndexValue);
		
		cIndex++; //next is the amount of conditions that have been changed for this group 
		
		int amountChanged = changeLogCond.get(cIndex);
		for(int i = 0; i < amountChanged; i++) {
			cIndex++;
			short subConditionIndex = changeLogCond.getShort(cIndex);
			Condition<?> condition = conditions.get(Math.abs(subConditionIndex)-1);
			
			cIndex = condition.processReceivedState(cIndex, changeLogCond, subConditionIndex);
		}
		//cIndex here should be the last index before a condition from a higher level gets processed
		
		//only condition groups process additional indices
		return cIndex;
	}
	public void resetCondition(boolean b) {
		for(Condition<?> c : conditions) {
			c.resetCondition(b);
		}
		super.resetCondition(b);
	}

	@Override
	public void getAllConditions(ConditionList all) {
		for(Condition<?> c : conditions) {
			c.addToList(all);
		}		
	}
}
