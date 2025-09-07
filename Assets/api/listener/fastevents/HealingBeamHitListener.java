package api.listener.fastevents;

import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;
import java.util.Collection;

public interface HealingBeamHitListener {
    boolean handle(BeamState hittingBeam, int beamHits, BeamHandlerContainer<?> container, SegmentPiece hitPiece, Vector3f from, Vector3f to, Timer timer, Collection<Segment> updatedSegments);
}
