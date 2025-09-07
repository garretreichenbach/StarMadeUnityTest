package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerSegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.Segment;

import javax.vecmath.Vector3f;
import java.util.Collection;

@Deprecated
public class DamageBeamHitEvent extends Event {
    private final DamageBeamHitHandlerSegmentController inst;
    private final SegmentController hitSegment;
    private BeamState beam;
    private final int damage;
    private final BeamHandlerContainer<?> unknownVar1;
    private final SegmentPiece unknownVar2;
    private final Vector3f origin;
    private final Vector3f hitPos;
    private final Collection<Segment> updatedSegments;

    public DamageBeamHitEvent(DamageBeamHitHandlerSegmentController inst, SegmentController hitSegment, BeamState beam, int damage, BeamHandlerContainer<?> unknownVar1, SegmentPiece unknownVar2, Vector3f origin, Vector3f hitPos, Collection<Segment> updatedSegments) {

        this.inst = inst;
        this.hitSegment = hitSegment;
        this.beam = beam;
        this.damage = damage;
        this.unknownVar1 = unknownVar1;
        this.unknownVar2 = unknownVar2;
        this.origin = origin;
        this.hitPos = hitPos;
        this.updatedSegments = updatedSegments;
        hitSegment.getEffectContainer().get(HitReceiverType.BLOCK);
    }

    public DamageBeamHitHandlerSegmentController getInst() {
        return inst;
    }

    public SegmentController getHitSegment() {
        return hitSegment;
    }

    public BeamState getBeam() {
        return beam;
    }

    public int getDamage() {
        return damage;
    }

    public BeamHandlerContainer<?> getUnknownVar1() {
        return unknownVar1;
    }

    public SegmentPiece getUnknownVar2() {
        return unknownVar2;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getHitPos() {
        return hitPos;
    }

    public Collection<Segment> getUpdatedSegments() {
        return updatedSegments;
    }
}
