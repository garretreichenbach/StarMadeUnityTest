package api.listener.events.entity.rail;

import api.listener.events.Event;
import org.schema.game.common.controller.rails.RailController;
import org.schema.game.common.data.SegmentPiece;

/**
 * Created by Jake on 2021-10-01
 * Fired when a SegmentController wants to dock. It may or may not work, depending on the lastDockRequest
 */

public class RailConnectAttemptClientEvent extends Event {
    private final RailController railController;
    private final SegmentPiece dockedPiece;
    private final SegmentPiece toRailPiece;
    private final long lastDockRequest;

    public RailConnectAttemptClientEvent(RailController railController, SegmentPiece dockedPiece, SegmentPiece toRailPiece, long lastDockRequest) {

        this.railController = railController;
        this.dockedPiece = dockedPiece;
        this.toRailPiece = toRailPiece;
        this.lastDockRequest = lastDockRequest;
    }

    public RailController getRailController() {
        return railController;
    }

    public SegmentPiece getDockedPiece() {
        return dockedPiece;
    }

    public SegmentPiece getToRailPiece() {
        return toRailPiece;
    }

    public long getLastDockRequest() {
        return lastDockRequest;
    }
}
