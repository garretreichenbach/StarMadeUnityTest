package org.schema.game.common.controller.rules.rules.actions.seg;

import java.util.Set;

import org.schema.common.util.TranslatableEnum;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.actions.ActionList;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.TopLevelType;

public class SegmentControllerActionList extends ActionList<SegmentController, SegmentControllerAction>{

	private static final long serialVersionUID = 1L;

	@Override
	public TopLevelType getEntityType() {
		return TopLevelType.SEGMENT_CONTROLLER;
	}

	@Override
	public void add(Action<?> c) {
		add((SegmentControllerAction)c);
	}

	
}
