package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.SegmentPiece;

import javax.annotation.Nullable;

@Deprecated
public class SegmentPieceKillEvent extends Event {

    private final SegmentPiece piece;
    private final SendableSegmentController controller;
    private final Damager damager;

    public SegmentPieceKillEvent(SegmentPiece piece, SendableSegmentController controller, @Nullable Damager damager){
        this.piece = piece;
        this.controller = controller;
        this.damager = damager;
    }

    public SegmentPiece getPiece() {
        return piece;
    }

    public SendableSegmentController getController() {
        return controller;
    }

    @Nullable
    public Damager getDamager() {
        return damager;
    }
}
