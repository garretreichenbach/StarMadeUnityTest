package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleEntityManager;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.controller.rules.rules.conditions.TimedCondition;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class SegmentControllerAbstractDateTimeCondition extends SegmentControllerCondition implements TimedCondition{

	
	public abstract Date getDate();
	
	public long firstFired = -1;

	public boolean flagTriggered;
	
	@RuleValue(tag = "FalseBeforeTrueAfter")
	public boolean after = true;

	private boolean flagEndTriggered;
	
	public SegmentControllerAbstractDateTimeCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_TIMED_CONDITION;
	}


	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		long serverTime = a.getUpdateTime();
		boolean passed = isTimeToFire(serverTime);
		
		if(!passed && firstFired == -1) {
			firstFired = a.getState().getUpdateTime();
			a.getRuleEntityManager().addDurationCheck(this);
			//no need to add check condition after since it will never change
			onFire();
		}
		
		if(passed) {
			return triggededAfterPassed();
		}else {
			return !triggededAfterPassed();
		}
		
	}
	

	protected boolean triggededAfterPassed() {
		return after;
	}

	protected void onFire() {
		
	}

	@Override
	public boolean isTimeToFire(long time) {
		return time >= getDate().getTime();
	}


	@Override
	public void flagTriggeredTimedCondition() {
		this.flagTriggered = true;
	}

	@Override
	public boolean isTriggeredTimedCondition() {
		return flagTriggered;
	}
	@Override
	public boolean isRemoveOnTriggered() {
		return false;
	}

	@Override
	public boolean isTriggeredTimedEndCondition() {
		return flagEndTriggered;
	}

	@Override
	public void flagTriggeredTimedEndCondition() {
		flagEndTriggered = true;		
	}
	
}
