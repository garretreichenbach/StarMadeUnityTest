package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.controller.rules.rules.conditions.TimedCondition;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SegmentControllerDurationCondition extends SegmentControllerCondition implements TimedCondition{

	
	@RuleValue(tag = "DurationInSecs")
	public int durationInSecs;
	
	public long firstFired = -1;

	public boolean flagTriggered;

	private boolean flagEndTriggered;
	
	public SegmentControllerDurationCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_RULE_STATE_CHANGE | TRIGGER_ON_TIMED_CONDITION;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_DURATION_ACTIVE;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		if(stateChange.lastSatisfied) {
			if(firstFired == -1) {
				firstFired = a.getState().getUpdateTime();
				a.getRuleEntityManager().addDurationCheck(this);
			}
			long serverTime = a.getUpdateTime();
			return isTimeToFire(serverTime);
		}else {
			a.getRuleEntityManager().removeDurationCheck(this);
			firstFired = -1;
			flagTriggered = false;
			return true;
		}
	}
	

	@Override
	public boolean isTimeToFire(long time) {
		return time < firstFired + (long)durationInSecs*1000L;
	}

	@Override
	public String getDescriptionShort() {
		return Lng.str("becomes false after %s secs of Rule becoming active (resets on reload)", durationInSecs);
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
