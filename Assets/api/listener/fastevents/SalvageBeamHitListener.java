package api.listener.fastevents;

import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.beam.harvest.SalvageBeamHandler;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;
import java.util.Collection;

/**
 * Created by Jake on 3/13/2021.
 * <insert description here>
 */
public interface SalvageBeamHitListener {
    void handle(SalvageBeamHandler handler, BeamState hittingBeam, int hits, BeamHandlerContainer<SegmentController> container, SegmentPiece hitPiece, Vector3f from,
                Vector3f to, Timer timer, Collection<Segment> updatedSegments);
}
