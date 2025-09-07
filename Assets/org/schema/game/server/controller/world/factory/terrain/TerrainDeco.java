package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataInterface;
import org.schema.game.common.data.world.SegmentDataWriteException;

public abstract class TerrainDeco {

	public TerrainDeco() {

	}

	public abstract boolean generate(SegmentDataInterface data, final int xMax, final int yMax, final int zMax, Random randomContext)throws SegmentDataWriteException;

	public int getRangeX(Random rand) {
		return rand.nextInt(SegmentData.SEG);
	}

	public int getRangeY(Random rand) {
		return rand.nextInt(SegmentData.SEG);
	}

	public int getRangeZ(Random rand) {
		return rand.nextInt(SegmentData.SEG);
	}
}
