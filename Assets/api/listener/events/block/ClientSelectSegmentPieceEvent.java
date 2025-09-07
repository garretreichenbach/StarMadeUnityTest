package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.data.SegmentPiece;

/**
 * Created by Jake on 7/20/2021.
 * When a client presses C on a segmentpiece
 */
public class ClientSelectSegmentPieceEvent extends Event {
    private SegmentPiece selectedBlock;
    private Context context;

    public ClientSelectSegmentPieceEvent(SegmentPiece selectedBlock, Context context) {
        this.selectedBlock = selectedBlock;
        this.context = context;
    }

    public SegmentPiece getSelectedBlock() {
        return selectedBlock;
    }

    public Context getContext() {
        return context;
    }

    public enum Context {
        PLAYER,
        BUILD_MODE
    }
}
