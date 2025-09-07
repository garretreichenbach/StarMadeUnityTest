package org.schema.game.client.view.cubes;

import org.schema.game.common.data.world.SegmentData;

public class CubeInfo {

	public static final int CUBE_VERTICES_COUNT = 24;
	public static final int CUBE_COUNT_PER_SEGMENT = SegmentData.SEG * SegmentData.SEG * SegmentData.SEG;
	public static final int CUBE_VERTICES_COUNT_SIDE = 4 * (CUBE_COUNT_PER_SEGMENT / 2);
	public static final int INDEX_BUFFER_SIZE = CUBE_VERTICES_COUNT_SIDE * 24;
	public static final int CUBE_SIDE_STRIDE = (CUBE_COUNT_PER_SEGMENT) * 4 * 3;
	public static final int CUBE_VERTICES_FLOAT_COUNT = 6 * CUBE_SIDE_STRIDE;

}
