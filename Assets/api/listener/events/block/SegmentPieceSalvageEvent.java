package api.listener.events.block;

import api.listener.events.Event;
import api.listener.type.ServerEvent;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.Segment;

import javax.vecmath.Vector3f;
import java.util.Collection;

@ServerEvent
public class SegmentPieceSalvageEvent extends Event {

    private final BeamState beamState;
    private final int salvagePower;
    private final Vector3f direction;
    private final SegmentPiece blockInternal;
    private final Collection<Segment> hitSegments;
    private final SegmentController segmentController;
    private byte orientation;
    private boolean cancelBlockGive = false;

    public SegmentPieceSalvageEvent(BeamState beamState, int salvagePower, Vector3f direction, SegmentPiece blockInternal, Collection<Segment> hitSegments, ManagedUsableSegmentController<?> segmentController, byte orientation) {
        this.beamState = beamState;
        this.salvagePower = salvagePower;
        this.direction = direction;
        this.blockInternal = blockInternal;
        this.hitSegments = hitSegments;
        this.segmentController = segmentController;
        this.orientation = orientation;
    }

    public BeamState getBeamState() {
        return beamState;
    }

    public int getSalvagePower() {
        return salvagePower;
    }

    public Vector3f getDirection() {
        return direction;
    }

    //Shitty name
    @Deprecated
    public SegmentPiece getBlockInternal() {
        return blockInternal;
    }
    public SegmentPiece getSegmentPiece(){
        return blockInternal;
    }

    public SegmentController getSegmentController() {
        return segmentController;
    }

    public Collection<Segment> getHitSegments() {
        return hitSegments;
    }

    public void setCancelBlockGive(boolean cancelBlockGive) {
        this.cancelBlockGive = cancelBlockGive;
    }

    public byte getOrientation() {
        return orientation;
    }

    public boolean isCancelBlockGive() {
        return cancelBlockGive;
    }
}
