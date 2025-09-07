package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.conditions.Condition;

public abstract class SegmentControllerCondition extends Condition<SegmentController> {

	public SegmentControllerCondition() {
		super();
	}
	
	public static final long TRIGGER_ON_BUILD_BLOCK = 2048L;
	public static final long TRIGGER_ON_REMOVE_BLOCK = 4096L;
	public static final long TRIGGER_ON_COLLECTION_UPDATE = 8192L;
	public static final long TRIGGER_ON_MASS_UPDATE = 16384L;
	public static final long TRIGGER_ON_BB_UPDATE = 32768L;
	public static final long TRIGGER_ON_REACTOR_ACTIVITY_CHANGE = 65536L;
	public static final long TRIGGER_ON_DOCKING_CHANGED = 131072L;
	public static final long TRIGGER_ON_ADMIN_FLAG_CHANGED = 262144L;
	public static final long TRIGGER_ON_SECTOR_SWITCHED = 262144L;
	public static final long TRIGGER_ON_ANY_SECTOR_SWITCHED = 1048576L;
	public static final long TRIGGER_ON_FLEET_CHANGE = 2097152L;
	public static final long TRIGGER_ON_HOMEBASE_CHANGE = 4194304L;
	
	
	
	
	
	
	public boolean isTriggeredOnBuildBlock() {
		return isTriggeredOn(TRIGGER_ON_BUILD_BLOCK);
	}
	public boolean isTriggeredOnRemoveBlock() {
		return isTriggeredOn(TRIGGER_ON_REMOVE_BLOCK);
	}
	public boolean isTriggeredOnCollectionUpdate() {
		return isTriggeredOn(TRIGGER_ON_COLLECTION_UPDATE);
	}
	public boolean isTriggeredOnMassUpdate() {
		return isTriggeredOn(TRIGGER_ON_MASS_UPDATE);
	}
	public boolean isTriggeredOnBBUpdate() {
		return isTriggeredOn(TRIGGER_ON_BB_UPDATE);
	}
	public boolean isTriggeredOnReactorActivityChange() {
		return isTriggeredOn(TRIGGER_ON_REACTOR_ACTIVITY_CHANGE);
	}
	public boolean isTriggeredOnDockingChange() {
		return isTriggeredOn(TRIGGER_ON_DOCKING_CHANGED);
	}
	public boolean isTriggeredOnSectorSwitch() {
		return isTriggeredOn(TRIGGER_ON_SECTOR_SWITCHED);
	}
	public boolean isTriggeredOnSectorEntitiesChanged() {
		return isTriggeredOn(TRIGGER_ON_SECTOR_ENTITIES_CHANGED);
	}
	public boolean isTriggeredOnRuleStateChanged() {
		return isTriggeredOn(TRIGGER_ON_RULE_STATE_CHANGE);
	}
	public boolean isTriggeredOnTimedCondition() {
		return isTriggeredOn(TRIGGER_ON_TIMED_CONDITION);
	}
	public boolean isTriggeredOnFleetChange() {
		return isTriggeredOn(TRIGGER_ON_FLEET_CHANGE);
	}
	public boolean isTriggeredOnAIActiveChange() {
		return isTriggeredOn(TRIGGER_ON_AI_ACTIVE_CHANGE);
	}
	public boolean isTriggeredOnFactionChange() {
		return isTriggeredOn(TRIGGER_ON_FACTION_CHANGE);
	}
	public boolean isTriggeredOnAttack() {
		return isTriggeredOn(TRIGGER_ON_ATTACK);
	}
	
	public boolean isTriggeredOnHomebaseChange() {
		return isTriggeredOn(TRIGGER_ON_HOMEBASE_CHANGE);
	}
	
	
	
	

}
