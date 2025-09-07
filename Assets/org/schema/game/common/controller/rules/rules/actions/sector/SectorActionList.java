package org.schema.game.common.controller.rules.rules.actions.sector;

import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.actions.ActionList;
import org.schema.game.common.controller.rules.rules.actions.seg.SegmentControllerAction;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.schine.network.TopLevelType;

public class SectorActionList extends ActionList<RemoteSector, SectorAction>{

	private static final long serialVersionUID = 1L;

	@Override
	public TopLevelType getEntityType() {
		return TopLevelType.SECTOR;
	}
	@Override
	public void add(Action<?> c) {
		add((SectorAction)c);
	}
}
