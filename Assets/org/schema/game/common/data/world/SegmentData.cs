using System;

public abstract class SegmentData : SegmentDataInterface
{
    public const int typeBitCount = 11; //11 bits -> 2048
    public const int hitpointsBitCount = 7; //7 bits -> 128
    public const int activeBitCount = 1; //1 bit -> 1
    public const int orientationBitCount = 5; //5 bit -> 16

    public const int typeIndexStart = 0;
    public const int hitpointsIndexStart = typeIndexStart + typeBitCount;
    public const int activeIndexStart = hitpointsIndexStart + hitpointsBitCount;
    public const int orientationStart = activeIndexStart + activeBitCount;

    public static int UnsignedRightShift(int n, int s)
    {
        return (int)((uint)n >> s);
    }

    public const int typeMask = ((~0) >> (32 - typeBitCount)) << typeIndexStart;

    public const int typeMaskNot = ~typeMask;

    public const int hpMask = ((~0) >> (32 - hitpointsBitCount)) << hitpointsIndexStart;

    public const int hpMaskNot = ~hpMask;

    public const int activeMask = ((~0) >> (32 - activeBitCount)) << activeIndexStart;

    public const int activeMaskNot = ~activeMask;

    public const int orientMask = ((~0) >> (32 - orientationBitCount)) << orientationStart;

    public const int orientMaskNot = ~orientMask;
    public const int SEG = Segment.DIM;
    public const float SEGf = SEG;
    public const byte ANTI_BYTE = 240; // -16 -> 0xf0 -> 11110000;
    public const int SEG_MINUS_ONE = SEG - 1;
    public const int SEG_TIMES_SEG = SEG * SEG;
    public const int SEG_TIMES_SEG_TIMES_SEG = SEG * SEG * SEG;
    public const int BLOCK_COUNT = SEG_TIMES_SEG_TIMES_SEG;
    public const int TOTAL_SIZE = BLOCK_COUNT;
    public const int PIECE_ADDED = 0;
    public const int PIECE_REMOVED = 1;
    public const int PIECE_CHANGED = 2;
    public const int PIECE_UNCHANGED = 3;
    public const int PIECE_ACTIVE_CHANGED = 4;
    public const int SEG_HALF = Segment.HALF_DIM;
    public const int VERSION = 7;
    public const short MAX_TYPE_ID = 8191;
    private const long yDelim = (short.MaxValue + 1) * 2;
    private const long zDelim = yDelim * yDelim;

    public abstract void TranslateModBlocks();
    public abstract byte[] GetAsOldByteBuffer();
    public abstract void MigrateTo(int fromVersion, SegmentDataInterface segmentData);
    public abstract void SetType(int index, short type);
    public abstract bool IsIntDataArray();
    public abstract void SetHitpointsByte(int index, int hp);
    public abstract void SetActive(int index, bool active);
    public abstract void SetOrientation(int index, byte orientation);
    public abstract short GetType(int index);
    public abstract short GetHitpointsByte(int index);
    public abstract bool IsActive(int index);
    public abstract byte GetOrientation(int index);
    public abstract void SetExtra(int index, byte extra);
    public abstract int GetExtra(int index);
    public abstract Segment GetSegment();
    public abstract SegmentController GetSegmentController();
    public abstract void ResetFast();
    public abstract void SetInfoElementForcedAddUnsynched(byte x, byte y, byte z, short type, bool updateSegmentBB);
    public abstract void SetInfoElementForcedAddUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, bool updateSegmentBB);
    public abstract short GetType(byte x, byte y, byte z);
    public abstract UnityEngine.Vector3Int GetSegmentPos();
    public abstract int Inflate(System.IO.Compression.DeflateStream inflater, byte[] byteFormatBuffer);
    public abstract int GetSize();
    public abstract void SetSize(int size);
    public abstract SegmentData DoBitmapCompressionCheck(RemoteSegment seg);
    public abstract void SetDataAt(int i, int data);
    public abstract int ReadFrom(System.IO.MemoryStream uncompressed);
}
