package org.schema.game.common.data.world;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public interface SegmentDataInterface {

	void translateModBlocks() throws SegmentDataWriteException;

	byte[] getAsOldByteBuffer();

	void migrateTo(int fromVersion, SegmentDataInterface segmentData);

	void setType(int index, short type) throws SegmentDataWriteException;

	boolean isIntDataArray();

	void setHitpointsByte(int index, int hp) throws SegmentDataWriteException;

	void setActive(int index, boolean active) throws SegmentDataWriteException;

	void setOrientation(int index, byte orientation) throws SegmentDataWriteException;

	short getType(int index);

	short getHitpointsByte(int index);

	boolean isActive(int index);

	byte getOrientation(int index);

	void setExtra(int index, byte extra) throws SegmentDataWriteException;

	int getExtra(int index);

	Segment getSegment();

	SegmentController getSegmentController();

	void resetFast() throws SegmentDataWriteException;

	void setInfoElementForcedAddUnsynched(byte x, byte y, byte z, short type, boolean updateSegmentBB) throws SegmentDataWriteException;

	void setInfoElementForcedAddUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB) throws SegmentDataWriteException;

	short getType(byte x, byte y, byte z);

	Vector3i getSegmentPos();

	int inflate(Inflater inflater, byte[] byteFormatBuffer) throws SegmentInflaterException, DataFormatException, SegmentDataWriteException;

	int getSize();

	void setSize(int size);

	SegmentData doBitmapCompressionCheck(RemoteSegment seg);

	void setDataAt(int i, int data) throws SegmentDataWriteException;

	int readFrom(ByteBuffer uncompressed) throws SegmentDataWriteException;
}
