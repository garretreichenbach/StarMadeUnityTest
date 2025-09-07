package api.listener.fastevents.segmentpiece;

import org.schema.game.common.data.world.Segment;

public interface SegmentPieceRemoveListener {

    void onBlockRemove(
            short type,
            int segmentSize,
            byte x,
            byte y,
            byte z,
            byte orientation,
            Segment segment,
            boolean preserveControl,
            boolean isServer
    );
}
