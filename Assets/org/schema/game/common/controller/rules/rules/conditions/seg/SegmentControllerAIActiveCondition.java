package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class SegmentControllerAIActiveCondition extends SegmentControllerCondition{

	
	@RuleValue(tag = "IsActive")
	public boolean active;

	
	public SegmentControllerAIActiveCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_AI_ACTIVE_CHANGE;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_IS_AI_ACTIVE;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return active == a.isAIControlled();
	}

	
	@Override
	public String getDescriptionShort() {
		
		return Lng.str("Is entity AI %s", StringTools.getOnOff(active));
	}
}
