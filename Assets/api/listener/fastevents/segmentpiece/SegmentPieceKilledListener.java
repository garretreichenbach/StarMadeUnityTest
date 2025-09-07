package api.listener.fastevents.segmentpiece;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.SegmentPiece;

import javax.annotation.Nullable;

public interface SegmentPieceKilledListener {

    void onBlockKilled(SegmentPiece piece, SendableSegmentController controller, @Nullable Damager damager, boolean isServer);
}
