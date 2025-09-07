package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.data.world.Segment;

public class SegmentPieceRemoveEvent extends Event {
    private final short type;
    private final int segmentSize;
    private final byte x;
    private final byte y;
    private final byte z;
    private final byte orientation;
    private final Segment segment;
    private final boolean preserveControl;

    public SegmentPieceRemoveEvent(short type, int segmentSize, byte x, byte y, byte z,
                                   byte orientation, Segment segment, boolean preserveControl){

        this.type = type;
        this.segmentSize = segmentSize;
        this.x = x;
        this.y = y;
        this.z = z;
        this.orientation = orientation;
        this.segment = segment;
        this.preserveControl = preserveControl;
    }

    public short getType() {
        return type;
    }

    public int getSegmentSize() {
        return segmentSize;
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

    public byte getOrientation() {
        return orientation;
    }

    public Segment getSegment() {
        return segment;
    }

    public boolean isPreserveControl() {
        return preserveControl;
    }
}
