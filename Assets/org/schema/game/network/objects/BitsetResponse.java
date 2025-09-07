package org.schema.game.network.objects;

import org.schema.common.util.linAlg.Vector3i;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class BitsetResponse {
	public EWAHCompressedBitmap bitmap;
	public boolean data;
	public Vector3i pos;
	public long segmentBufferIndex;
	public BitsetResponse() {

	}
	public BitsetResponse(long segmentBufferIndex, EWAHCompressedBitmap bitMap, Vector3i pos) {
		this.segmentBufferIndex = segmentBufferIndex;
		this.pos = pos;
		if (bitMap != null) {
			this.bitmap = bitMap;
			data = true;
		}
	}
}
