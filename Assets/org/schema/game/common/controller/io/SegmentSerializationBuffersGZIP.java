package org.schema.game.common.controller.io;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.data.world.*;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class SegmentSerializationBuffersGZIP implements SegmentDummyProvider{
	private static boolean created;
	
	public final Int2ObjectOpenHashMap<SegmentDataInterface> dummies = new Int2ObjectOpenHashMap<>();
	public final Deflater deflater = new Deflater();
	public final Inflater inflater = new Inflater();
	
	private byte[] byteArray = new byte[4 * 1024];
	public byte[] SEGMENT_BYTE_FORMAT_BUFFER = new byte[SegmentData.BLOCK_COUNT * SegmentData.BYTES_USED];
	public byte[] SEGMENT_BUFFER = new byte[1024 * 1024];
	
	private FastByteArrayOutputStream byteArrayOutput = new FastByteArrayOutputStream(4 * 1024);
	
	public void ensureSegmentBufferSize(int controllerBlockSize) {
		int size = SEGMENT_BUFFER.length;
		while(size < controllerBlockSize) {
			size *= 2;
		}
		SEGMENT_BUFFER = new byte[size];
	}
	
	public FastByteArrayOutputStream getStaticArrayOutput() {
		byteArrayOutput.reset();
		return byteArrayOutput;
	}
	public byte[] getStaticArray(int size) {
		if (byteArray.length < size) {
			int newSize = byteArray.length;
			while (newSize < size) {
				newSize *= 2;
			}
			byteArray = new byte[newSize];
		}
		return byteArray;
	}
	public SegmentSerializationBuffersGZIP() {
		createDummies(dummies);
	}
	private static final ObjectArrayList<SegmentSerializationBuffersGZIP> pool = new ObjectArrayList<SegmentSerializationBuffersGZIP>();
	static {
		create();
	}
	public static void create() {
		synchronized(pool) {
			if(!created) {
				for(int i = 0; i < 15; i++) {
					pool.add(new SegmentSerializationBuffersGZIP());
				}
				created = true;
			}
		}
	}
	public static SegmentSerializationBuffersGZIP get() {
		synchronized(pool) {
			if(pool.isEmpty()) {
				return new SegmentSerializationBuffersGZIP();
			}else {
				return pool.remove(pool.size()-1);
			}
		}
	}
	public static void free(SegmentSerializationBuffersGZIP vc) {
		synchronized(pool) {
			pool.add(vc);
		}
	}
	public static void createDummies(Int2ObjectOpenHashMap<SegmentDataInterface> versionDummies){
		if (versionDummies.isEmpty()) {
			versionDummies.put(0, new SegmentDataOld(false));
			versionDummies.put(1, new SegmentDataRepairAll(false));
			versionDummies.put(2, new SegmentData3Byte(true));
			versionDummies.put(3, new SegmentDataIntArray(true));
			versionDummies.put(4, new SegmentDataIntArray(true));
			versionDummies.put(5, new SegmentDataIntArray(true));
			versionDummies.put(6, new SegmentDataIntArray(true));
			versionDummies.put(7, new SegmentData4Byte(true));
		}
	}

	@Override
	public Int2ObjectOpenHashMap<SegmentDataInterface> getDummies() {
		return dummies;
	}
	
}
