package org.schema.game.common.controller;

import org.schema.game.common.data.SegmentPiece;

public interface BlockLogicReplaceInterface {

	boolean isBlockNextToLogicOkTuUse(SegmentPiece fromBlockSurround, SegmentPiece toReplace);

	void afterReplaceBlock(SegmentPiece fromBlockSurroundOriginal, SegmentPiece toReplace);

	boolean fromBlockOk(SegmentPiece fromBlockSurround);

	boolean equalsBlockData(SegmentPiece fromBlockSurround,
			SegmentPiece toReplace);
	public void modifyReplacement(SegmentPiece fromBlockSurround,
			SegmentPiece toReplace);
}
