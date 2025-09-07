package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.ShieldLocal;
import org.schema.game.common.controller.elements.ShieldLocalAddOn;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.common.language.Lng;

public class SegmentControllerShieldCapacityCondition extends SegmentControllerMoreLessCondition{

	
	
	@RuleValue(tag = "Capacity")
	public double capacity;
	
	public SegmentControllerShieldCapacityCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_COLLECTION_UPDATE;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_SHIELD_CAPACITY_CONDITION;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		if(a instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)a).getManagerContainer() instanceof ShieldContainerInterface) {
			ShieldAddOn shieldAddOn = ((ShieldContainerInterface)((ManagedSegmentController<?>)a).getManagerContainer()).getShieldAddOn();
			
			ShieldLocalAddOn shieldLocalAddOn = shieldAddOn.getShieldLocalAddOn();
			for(ShieldLocal s : shieldLocalAddOn.getAllShields()) {
				if(moreThan ? (s.getShieldCapacity() > capacity) : (s.getShieldCapacity() <= capacity)) {
					return true;
				}
			}
			return false;
		}else {
			return false;
		}
		
	}

	@Override
	public String getCountString() {
		return String.valueOf(capacity);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("Shield Capacity");
	}
	
}
