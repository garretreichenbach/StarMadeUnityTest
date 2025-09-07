package api.listener.fastevents;

import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerSegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;
import java.util.Collection;

/**
 * Created by Jake on 9/26/2021.
 * <insert description here>
 */
public interface DamageBeamHitListener {
    void handle(BeamState hittingBeam, int hits, BeamHandlerContainer<?> container, SegmentPiece segmentPiece, Vector3f from, Vector3f to, Timer timer, Collection<Segment> updatedSegments, DamageBeamHitHandlerSegmentController damageBeamHitHandlerSegmentController);
}
