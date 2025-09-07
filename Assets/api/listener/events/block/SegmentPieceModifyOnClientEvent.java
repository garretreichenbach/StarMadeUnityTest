package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidSegmentPiece;

public class SegmentPieceModifyOnClientEvent extends Event {


    private final VoidSegmentPiece piecef;
    private final SegmentPiece oldPiecef;

    public SegmentPieceModifyOnClientEvent(VoidSegmentPiece newPiece, SegmentPiece oldPiece) {
        piecef = newPiece;
        oldPiecef = oldPiece;
    }

    public VoidSegmentPiece getPiece() {
        return piecef;
    }

    public SegmentPiece getOldPiece() {
        return oldPiecef;
    }
}
