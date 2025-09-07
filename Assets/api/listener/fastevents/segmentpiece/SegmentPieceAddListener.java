package api.listener.fastevents.segmentpiece;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Segment;

/**
 * Created by Jake on 3/6/2021.
 * <insert description here>
 */
public interface SegmentPieceAddListener {
    void onAdd(
            SegmentController segmentController,
            short newType,
            byte orientation,
            byte x,
            byte y,
            byte z,
            Segment segment,
            boolean updateSegmentBuffer,
            long absIndex,
            boolean isServer
    );
}
