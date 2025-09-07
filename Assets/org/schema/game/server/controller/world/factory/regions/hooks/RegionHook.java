package org.schema.game.server.controller.world.factory.regions.hooks;

import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.world.factory.regions.UsableRegion;

public abstract class RegionHook<E extends UsableRegion> {

	protected E region;
	protected Segment onCreatedSegment;

	public abstract void execute();

	public void initialize(E region, Segment onCreatedSegment) {
		this.region = region;
		this.onCreatedSegment = onCreatedSegment;
	}
}
