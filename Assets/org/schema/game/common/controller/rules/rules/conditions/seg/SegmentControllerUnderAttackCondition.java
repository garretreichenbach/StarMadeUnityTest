package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SegmentControllerUnderAttackCondition extends SegmentControllerCondition{

	
	@RuleValue(tag = "UnderAttack")
	public boolean underAttack;
	
	@RuleValue(tag = "SecondsSinceLastDamage")
	public int secsSinceLastDamage = 120;
	
	public SegmentControllerUnderAttackCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_ATTACK;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_UNDER_ATTACK;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		boolean ua = (a.getState().getUpdateTime() - a.lastAnyDamageTakenServer) < ((long)secsSinceLastDamage * 1000L);
		
		return underAttack ? ua : !ua;
	}
	

	@Override
	public String getDescriptionShort() {
		return underAttack ? Lng.str("Entity under attack") : Lng.str("Entity not under attack");
	}
}
