package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Segment;

public class SegmentPieceAddEvent extends Event {
    private SegmentController segmentController;
    private final short newType;
    private final byte orientation;
    private final byte x;
    private final byte y;
    private final byte z;
    private final Segment segment;
    private final boolean updateSegmentBuffer;
    private final long absIndex;

    public SegmentController getSegmentController() {
        return segmentController;
    }

    public SegmentPieceAddEvent(SegmentController segmentController, short newType, byte orientation, byte x,
                                byte y, byte z, Segment segment, boolean updateSegmentBuffer, long absIndex) {
        this.segmentController = segmentController;
        this.newType = newType;
        this.orientation = orientation;
        this.x = x;
        this.y = y;
        this.z = z;
        this.segment = segment;
        this.updateSegmentBuffer = updateSegmentBuffer;
        this.absIndex = absIndex;
    }

    public short getNewType() {
        return newType;
    }

    public byte getOrientation() {
        return orientation;
    }

    public byte getX() {
        return x;
    }

    public byte getY() {
        return y;
    }

    public byte getZ() {
        return z;
    }

    public Segment getSegment() {
        return segment;
    }

    public boolean isUpdateSegmentBuffer() {
        return updateSegmentBuffer;
    }

    public long getAbsIndex() {
        return absIndex;
    }
}
