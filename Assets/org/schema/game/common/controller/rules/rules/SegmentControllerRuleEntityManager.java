package org.schema.game.common.controller.rules.rules;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.actions.seg.SegmentControllerPopupMessageInBuildModeAction.PopupBuildModeActionUpdate;
import org.schema.game.common.controller.rules.rules.conditions.TimedCondition;
import org.schema.game.common.controller.rules.rules.conditions.seg.SegmentControllerCondition;
import org.schema.game.common.controller.rules.rules.conditions.seg.SegmentControllerDurationCondition;

public class SegmentControllerRuleEntityManager extends RuleEntityManager<SegmentController>{

	public SegmentControllerRuleEntityManager(SegmentController segmentController) {
		super(segmentController);
	}

	public void triggerOnBlockBuild() {
		trigger(SegmentControllerCondition.TRIGGER_ON_BUILD_BLOCK);
	}

	public void triggerOnRemoveBlock() {
		trigger(SegmentControllerCondition.TRIGGER_ON_REMOVE_BLOCK);
	}
	public void triggerOnCollectionUpdate() {
		trigger(SegmentControllerCondition.TRIGGER_ON_COLLECTION_UPDATE);
	}
	public void triggerOnMassUpdate() {
		trigger(SegmentControllerCondition.TRIGGER_ON_MASS_UPDATE);
	}
	public void triggerOnBBUpdate() {
		trigger(SegmentControllerCondition.TRIGGER_ON_BB_UPDATE);
	}
	public void triggerOnReactorActivityChange() {
		trigger(SegmentControllerCondition.TRIGGER_ON_REACTOR_ACTIVITY_CHANGE);
	}
	
	public void triggerOnDockingChange() {
		trigger(SegmentControllerCondition.TRIGGER_ON_DOCKING_CHANGED);
	}
	public void triggerOnFleetChange() {
		trigger(SegmentControllerCondition.TRIGGER_ON_FLEET_CHANGE);
	}
	public void triggerAdminFlagChanged() {
		trigger(SegmentControllerCondition.TRIGGER_ON_ADMIN_FLAG_CHANGED);		
	}
	public void triggerSectorSwitched() {
		trigger(SegmentControllerCondition.TRIGGER_ON_SECTOR_SWITCHED);		
	}
	public void triggerHomebaseChanged() {
		trigger(SegmentControllerCondition.TRIGGER_ON_HOMEBASE_CHANGE);		
	}
	public void triggerSectorEntitiesChanged() {
		trigger(SegmentControllerCondition.TRIGGER_ON_SECTOR_ENTITIES_CHANGED);		
	}
	public void triggerAnyEntitySectorSwitched() {
		trigger(SegmentControllerCondition.TRIGGER_ON_ANY_SECTOR_SWITCHED);		
	}
	@Override
	public byte getEntitySubType() {
		return (byte)entity.getType().ordinal();
	}



	

}
