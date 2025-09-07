package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.Segment;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Created by Jake on 11/20/2020.
 * Whenever a SegmentPiece is added by metadata, usually on entityspawn
 */
public class SegmentPieceAddByMetadataEvent extends Event {
    private final short type;
    private final byte x;
    private final byte y;
    private final byte z;
    private final byte orientation;
    private final Segment segment;
    private long absIndex;

    public SegmentPieceAddByMetadataEvent(short type, byte x, byte y, byte z,
                                   byte orientation, Segment segment, long absIndex){

        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.orientation = orientation;
        this.segment = segment;
        this.absIndex = absIndex;
    }

    public short getType() {
        return type;
    }


    /**
     * Gets the chunk local X
     */
    public byte getX() {
        return x;
    }

    /**
     * Gets the chunk local Y
     */
    public byte getY() {
        return y;
    }

    /**
     * Gets the chunk local Z
     */
    public byte getZ() {
        return z;
    }

    /**
     * Gets the orientation byte
     */
    public byte getOrientation() {
        return orientation;
    }

    public Segment getSegment() {
        return segment;
    }
    public long getIndexAndOrientation(){
        return  ElementCollection.getIndex4(absIndex, orientation);
    }

    public long getAbsIndex() {
        return absIndex;
    }

    /**
     * Gets the added segment as a segmentpiece, which allows setting of orientation, etc.
     *
     * @param segmentPieceTmp A segmentpiece wrapper to use, pass null for a new one.
     */
    @NotNull
    public SegmentPiece getAsSegmentPiece(@Nullable SegmentPiece segmentPieceTmp){
        if(segmentPieceTmp == null){
            segmentPieceTmp = new SegmentPiece();
        }

        segmentPieceTmp.setByReference(segment, x, y, z);

        return segmentPieceTmp;
    }
}
