using UnityEngine;
using System.IO;
using System.IO.Compression;

// Placeholder for Segment class
public class Segment { public const int DIM = 16; public const int HALF_DIM = 8; }

// Placeholder for SegmentController class
public class SegmentController { }

// Placeholder for RemoteSegment class
public class RemoteSegment { }

public interface SegmentDataInterface
{
    void TranslateModBlocks();

    byte[] GetAsOldByteBuffer();

    void MigrateTo(int fromVersion, SegmentDataInterface segmentData);

    void SetType(int index, short type);

    bool IsIntDataArray();

    void SetHitpointsByte(int index, int hp);

    void SetActive(int index, bool active);

    void SetOrientation(int index, byte orientation);

    short GetType(int index);

    short GetHitpointsByte(int index);

    bool IsActive(int index);

    byte GetOrientation(int index);

    void SetExtra(int index, byte extra);

    int GetExtra(int index);

    Segment GetSegment();

    SegmentController GetSegmentController();

    void ResetFast();

    void SetInfoElementForcedAddUnsynched(byte x, byte y, byte z, short type, bool updateSegmentBB);

    void SetInfoElementForcedAddUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, bool updateSegmentBB);

    short GetType(byte x, byte y, byte z);

    Vector3Int GetSegmentPos();

    int Inflate(DeflateStream inflater, byte[] byteFormatBuffer);

    int GetSize();

    void SetSize(int size);

    SegmentData DoBitmapCompressionCheck(RemoteSegment seg);

    void SetDataAt(int i, int data);

    int ReadFrom(MemoryStream uncompressed);
}