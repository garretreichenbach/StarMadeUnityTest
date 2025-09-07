package api.listener.fastevents;

import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.beam.repair.RepairBeamHandler;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;
import java.util.Collection;

public interface RepairBeamHitListener {
    void hitFromShip(RepairBeamHandler handler, BeamState hittingBeam, int beamHits,
                     BeamHandlerContainer<SegmentController> container,
                     SegmentPiece hitPiece, Vector3f from, Vector3f to,
                     Timer timer, Collection<Segment> updatedSegments);
}
