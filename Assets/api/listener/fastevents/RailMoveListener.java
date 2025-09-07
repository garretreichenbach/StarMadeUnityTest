package api.listener.fastevents;

import org.schema.game.common.controller.rails.RailController;
import org.schema.game.common.data.SegmentPiece;

public interface RailMoveListener {

    /**
     * Called when a rail is rotated
     * @param railController
     * @param railBlock
     * @param railDocker
     */
    void onRailRotate(RailController railController, SegmentPiece railBlock, SegmentPiece railDocker);

    /**
     * Called when a rail undocks
     * @param railController
     * @param railBlock
     * @param railDocker
     */
    void onRailUndock(RailController railController, SegmentPiece railBlock, SegmentPiece railDocker);

    /**
     * Called when a rail docks
     * @param railController
     * @param railBlock
     * @param railDocker
     */
    void onRailDock(RailController railController, SegmentPiece railBlock, SegmentPiece railDocker);
}
