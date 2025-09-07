package org.schema.game.common.controller.io;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.schema.game.common.data.world.*;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.util.lz4.LZ4Frame.LZ4F_getErrorName;

public class SegmentSerializationBuffers implements SegmentDummyProvider {
	private static boolean created;

	private final LZ4Factory factory = LZ4Factory.fastestInstance();
	private final LZ4Compressor compressor = factory.fastCompressor();
	private final LZ4FastDecompressor decompressor = factory.fastDecompressor();

	private static final ObjectArrayList<SegmentSerializationBuffers> pool = new ObjectArrayList<>();
	public final Int2ObjectOpenHashMap<SegmentDataInterface> dummies = new Int2ObjectOpenHashMap<>();

	/**
	 *
	 * @return the number of bytes read from the source buffer (== the compressed size). 
	 * If the source stream is detected malformed, the function stops decoding and return a negative result. 
	 * Destination buffer must be already allocated. Its size must be ≥ originalSize bytes.
	 */
	public void uncompress() {
//		int decompressed = decompressor.decompress(compressed, 0, uncompressed, 0, uncompressed.capacity());
//		if(decompressed < 0) {
//			decompressed is an error code
//			throw new RuntimeException("LZ4 Error (" + decompressed + "): " + LZ4F_getErrorName(decompressed));
//		}
//		return decompressed;
		decompressor.decompress(compressed, uncompressed);
	}

	/**
	 *
	 * @return the number of bytes written into buffer dest (necessarily ≤ maxOutputSize) or 0 if compression fails
	 */
	public void compress() {
//		int compressedSize = compressor.compress(uncompressed, 0, uncompressed.capacity(), compressed, 0, compressed.capacity());
//		if(compressedSize < 0) {
			//compressedSize is an error code
//			throw new RuntimeException("LZ4 Error (" + compressedSize + "): " + LZ4F_getErrorName(compressedSize));
//		}
//		return compressedSize;
		compressor.compress(uncompressed, compressed);
	}

	public ByteBuffer uncompressed = ensure((1024 * 1024) * 4, null);
	public ByteBuffer compressed = ensure((1024 * 1024) * 4, null);

	public static ByteBuffer ensure(int limit, ByteBuffer b) {

		int size = b != null ? b.capacity() : 1024;
		if(b != null && size >= limit) {
			return b;
		}
		while(size < limit) {
			size *= 2;
		}
		if(b != null) {
			b.clear();
			memFree(b);
		}

		b = memAlloc(size);
		return b;
	}

	public void ensureSegmentBufferSize(int compressed, int uncompressed) {
		this.compressed = ensure(compressed, this.compressed);
		this.uncompressed = ensure(uncompressed, this.uncompressed);
	}

	public SegmentSerializationBuffers() {
		createDummies(dummies);
	}

	static {
		create();
	}

	public static void create() {
		synchronized(pool) {
			if(!created) {
				for(int i = 0; i < 15; i++) {
					pool.add(new SegmentSerializationBuffers());
				}
				created = true;
			}
		}
	}

	public static SegmentSerializationBuffers get() {
		synchronized(pool) {
			if(pool.isEmpty()) {
				return new SegmentSerializationBuffers();
			} else {
				return pool.remove(pool.size() - 1);
			}
		}
	}

	public static void free(SegmentSerializationBuffers vc) {
		synchronized(pool) {
			pool.add(vc);
		}
	}

	public static void createDummies(Int2ObjectOpenHashMap<SegmentDataInterface> versionDummies) {
		if(versionDummies.isEmpty()) {
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

	public void clear() {
		uncompressed.clear();
		compressed.clear();
	}

	@Override
	public Int2ObjectOpenHashMap<SegmentDataInterface> getDummies() {
		return dummies;
	}

}
