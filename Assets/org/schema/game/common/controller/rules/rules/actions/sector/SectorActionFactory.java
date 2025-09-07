package org.schema.game.common.controller.rules.rules.actions.sector;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.ActionFactory;
import org.schema.game.common.controller.rules.rules.conditions.ConditionFactory;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.schine.network.TopLevelType;

public abstract class SectorActionFactory implements ActionFactory<RemoteSector, SectorAction> {

	@Override
	public TopLevelType getType() {
		return TopLevelType.SECTOR;
	}
	
}
