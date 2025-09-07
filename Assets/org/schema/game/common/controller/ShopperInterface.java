package org.schema.game.common.controller;

import java.util.Set;

import org.schema.schine.physics.Physical;

public interface ShopperInterface extends Physical {
	public int getSectorId();

	public Set<ShopInterface> getShopsInDistance();

}
