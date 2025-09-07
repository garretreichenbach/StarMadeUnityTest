package org.schema.game.common.data.element.beam;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

public interface BeamLatchTransitionInterface {

	/**
	 * selects next block after current block dies
	 * @param oldType
	 * @param absoluteIndex
	 * @param from
	 * @param to
	 * @return next block to latch on
	 */

	public <E extends SimpleTransformableSendableObject> SegmentPiece selectNextToLatch(BeamState bState,
			short oldType, long firstLatchAbsIndex, long absoluteIndex, Vector3f from, Vector3f to,
			AbstractBeamHandler<E> abstractBeamHandler, SegmentController segmentController);

}
