package org.schema.game.common.controller.rules.rules;

import org.schema.game.common.controller.rules.rules.conditions.sector.SectorCondition;
import org.schema.game.common.data.world.RemoteSector;

public class SectorRuleEntityManager  extends RuleEntityManager<RemoteSector>{

	public SectorRuleEntityManager(RemoteSector entity) {
		super(entity);
	}

	@Override
	public byte getEntitySubType() {
		return 100;
	}
	public void triggerSectorChmod() {
		trigger(SectorCondition.TRIGGER_ON_SECTOR_CHMOD);		
	}
}
