package org.schema.game.server.controller.world.factory.terrain;

public class ColumnTerrainGenerator extends TerrainGenerator {

	public ColumnTerrainGenerator(long seed) {
		super(seed);
		this.setFlatness(0.45000000000000001D);
		hasColumns = true;
		defaultMax = 14;
	}

}
