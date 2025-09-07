package org.schema.common.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.agrona.UnsafeApi;
import org.lwjgl.system.MemoryUtil;
import org.schema.game.common.data.world.nat.terra.server.MemoryArea;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class MemoryManager {
//	private static final Memory mem = OS.memory();

	private final List<MemoryChunk> chunks = new ObjectArrayList<MemoryChunk>();
	private int totalBytes;
	private long startPointer;

	public <E extends MemoryChunk> E register(E e) {
		chunks.add(e);
		return e;
	}

	public void allocateMemory() {
		totalBytes = 0;
		for(int i = 0; i < chunks.size(); i++) {
			totalBytes += chunks.get(i).lengthBytes();
		}

		startPointer = MemoryUtil.nmemAllocChecked(totalBytes);

		long fieldPointer = 0;
		for(int i = 0; i < chunks.size(); i++) {
			chunks.get(i).resserve(startPointer + fieldPointer);
			fieldPointer += chunks.get(i).lengthBytes();
		}

	}

	public interface MemoryChunk {
		int length();

		void resserve(long l);

		int lengthBytes();
	}

	public interface ManagedMemoryChunk extends MemoryChunk {
		void flip();

		void zero();

		int limit();

		void clear();

		int position();

		ByteBuffer getByteBackingToCurrent();
	}

	public static class MemIntArray implements ManagedMemoryChunk {
		private long startPointer;
		public int position;
		public int limit;
		private final int size;
		private IntBuffer backing;
		private ByteBuffer backingByte;

		public MemIntArray(int size) {
			this.size = size;
			limit = size;
		}

		public int get(int index) {
			return MemoryUtil.memGetInt(startPointer + (long) index * ByteUtil.SIZEOF_INT);
		}

		public void put(int index, int val) {
			MemoryUtil.memPutInt(startPointer + (long) index * ByteUtil.SIZEOF_INT, val);
		}

		public void put(int val) {
			MemoryUtil.memPutInt(startPointer + (long) position * ByteUtil.SIZEOF_INT, val);
			position++;
		}

		public void flip() {
			limit = position;
			position = 0;
		}

		@Override
		public int length() {
			return size;
		}

		@Override
		public int lengthBytes() {
			return size * ByteUtil.SIZEOF_INT;
		}

		@Override
		public void resserve(long pointer) {
			startPointer = pointer;
			backing = MemoryUtil.memIntBuffer(pointer, size);
			backingByte = MemoryUtil.memByteBuffer(pointer, size * ByteUtil.SIZEOF_INT);
		}

		public IntBuffer getBackingToCurrent() {
			backing.position(position);
			backing.limit(limit);
			return backing;
		}

		public ByteBuffer getByteBackingToCurrent() {
			backingByte.position(position * ByteUtil.SIZEOF_INT);
			backingByte.limit(limit * ByteUtil.SIZEOF_INT);
			return backingByte;
		}

		public void zero() {
//			mem.setMemory(startPointer, lengthBytes(), (byte) 0);
			UnsafeApi.setMemory(startPointer, lengthBytes(), (byte) 0);
		}

		public void clear() {
			position = 0;
			limit = size;
		}

		public void rewind() {
			position = 0;
		}

		public void put(MemIntArray b) {

			int len = b.limit - b.position;
			assert (len > 0);
			assert (position * ByteUtil.SIZEOF_INT + len * ByteUtil.SIZEOF_INT < lengthBytes());

//			mem.copyMemory(b.startPointer + b.position * ByteUtil.SIZEOF_INT, startPointer + position * ByteUtil.SIZEOF_INT, len * ByteUtil.SIZEOF_INT);
			UnsafeApi.copyMemory(b.startPointer + (long) b.position * ByteUtil.SIZEOF_INT, startPointer + (long) position * ByteUtil.SIZEOF_INT, (long) len * ByteUtil.SIZEOF_INT);
			position += len;
		}

		public int get() {
			int v = get(position);
			position++;
			return v;
		}

		@Override
		public int limit() {
			return limit;
		}

		public void add(int index, int val) {
			long p = startPointer + (long) index * ByteUtil.SIZEOF_INT;
			MemoryUtil.memPutInt(p, MemoryUtil.memGetInt(p) + val);
		}

		public void scale(int index, int val) {
			long p = startPointer + (long) index * ByteUtil.SIZEOF_INT;
			MemoryUtil.memPutInt(p, MemoryUtil.memGetInt(p) * val);
		}

		public int[] toArray(int wantedSize) {
			assert (wantedSize <= size);
			int[] a = new int[wantedSize];
			for(int i = 0; i < wantedSize; i++) {
				a[i] = get(i);
			}
			return a;
		}

		@Override
		public int position() {
			return position;
		}

		public void copyFrom(MemoryArea b) {
			assert (b.length() <= lengthBytes());
//			mem.copyMemory(b.memoryAddress(), startPointer, b.length());
			UnsafeApi.copyMemory(b.memoryAddress(), startPointer, b.length());
		}

		public void fill(int filledType) {
			long p = startPointer;
			for(int i = 0; i < size; i++) {
				MemoryUtil.memPutInt(p, filledType);
				p += ByteUtil.SIZEOF_INT;
			}
		}
	}

	public static class MemFloatArray implements ManagedMemoryChunk {
		private long startPointer;
		public int position;
		public int limit;
		private final int size;
		private FloatBuffer backing;
		private ByteBuffer backingByte;

		public MemFloatArray(int size) {
			this.size = size;
			limit = size;
		}

		public float get(int index) {
			return MemoryUtil.memGetFloat(startPointer + (long) index * ByteUtil.SIZEOF_FLOAT);
		}

		public void put(int index, float val) {
			MemoryUtil.memPutFloat(startPointer + (long) index * ByteUtil.SIZEOF_FLOAT, val);
		}

		public void put(float val) {
			MemoryUtil.memPutFloat(startPointer + (long) position * ByteUtil.SIZEOF_FLOAT, val);
			position++;
		}

		public void flip() {
			limit = position;
			position = 0;
		}

		public ByteBuffer getByteBackingToCurrent() {
			backingByte.position(position * ByteUtil.SIZEOF_FLOAT);
			backingByte.limit(limit * ByteUtil.SIZEOF_FLOAT);
			return backingByte;
		}

		@Override
		public int length() {
			return size;
		}

		@Override
		public int lengthBytes() {
			return size * ByteUtil.SIZEOF_FLOAT;
		}

		@Override
		public void resserve(long pointer) {
			startPointer = pointer;
			backing = MemoryUtil.memFloatBuffer(pointer, size);
			backingByte = MemoryUtil.memByteBuffer(pointer, size * ByteUtil.SIZEOF_FLOAT);
		}

		public FloatBuffer getBackingToCurrent() {
			backing.position(position);
			backing.limit(limit);
			return backing;
		}

		public void zero() {
//			mem.setMemory(startPointer, lengthBytes(), (byte) 0);
			UnsafeApi.setMemory(startPointer, lengthBytes(), (byte) 0);
		}

		public void clear() {
			position = 0;
			limit = size;
		}

		public void rewind() {
			position = 0;
		}

		public void put(MemFloatArray b) {
			int len = b.limit - b.position;
			assert (len > 0);
			assert (position * ByteUtil.SIZEOF_FLOAT + len * ByteUtil.SIZEOF_FLOAT < lengthBytes());
//			mem.copyMemory(b.startPointer + (long) b.position * ByteUtil.SIZEOF_FLOAT, startPointer + (long) position * ByteUtil.SIZEOF_FLOAT, len * ByteUtil.SIZEOF_FLOAT);
			UnsafeApi.copyMemory(b.startPointer + (long) b.position * ByteUtil.SIZEOF_FLOAT, startPointer + (long) position * ByteUtil.SIZEOF_FLOAT, (long) len * ByteUtil.SIZEOF_FLOAT);
			position += len;
		}

		public float get() {
			float v = get(position);
			position++;
			return v;
		}

		@Override
		public int limit() {
			return limit;
		}

		public void add(int index, float val) {
			long p = startPointer + (long) index * ByteUtil.SIZEOF_FLOAT;
			MemoryUtil.memPutFloat(p, MemoryUtil.memGetFloat(p) + val);
		}

		public void scale(int index, float val) {
			long p = startPointer + (long) index * ByteUtil.SIZEOF_FLOAT;
			MemoryUtil.memPutFloat(p, MemoryUtil.memGetFloat(p) * val);
		}

		public float[] toArray(int wantedSize) {
			assert (wantedSize <= size);
			float[] a = new float[wantedSize];
			for(int i = 0; i < wantedSize; i++) {
				a[i] = get(i);
			}
			return a;
		}

		@Override
		public int position() {
			return position;
		}

		public void fill(float filledType) {
			long p = startPointer;
			for(int i = 0; i < size; i++) {
				MemoryUtil.memPutFloat(p, filledType);
				p += ByteUtil.SIZEOF_INT;
			}
		}
	}

	public static class MemBoolArray implements MemoryChunk {
		private long startPointer;

		private final int size;

		public MemBoolArray(int size) {
			this.size = size;
		}

		public boolean get(int index) {
			return MemoryUtil.memGetByte(startPointer + index) != 0;
		}

		public void put(int index, boolean val) {
			MemoryUtil.memPutByte(startPointer + index, (byte) (val ? 1 : 0));
		}

		@Override
		public int length() {
			return size;
		}

		@Override
		public int lengthBytes() {
			return size;
		}

		@Override
		public void resserve(long pointer) {
			startPointer = pointer;
		}

		public void zero() {
//			mem.setMemory(startPointer, lengthBytes(), (byte) 0);
			UnsafeApi.setMemory(startPointer, lengthBytes(), (byte) 0);
		}
	}

	public static class MemByteArray implements MemoryChunk {
		private long startPointer;

		private final int size;

		public MemByteArray(int size) {
			this.size = size;
		}

		public byte get(int index) {
			return MemoryUtil.memGetByte(startPointer + index);
		}

		public void put(int index, byte val) {
			MemoryUtil.memPutByte(startPointer + index, val);
		}

		@Override
		public int length() {
			return size;
		}

		@Override
		public int lengthBytes() {
			return size;
		}

		@Override
		public void resserve(long pointer) {
			startPointer = pointer;
		}

		public void zero() {
//			mem.setMemory(startPointer, lengthBytes(), (byte) 0);
			UnsafeApi.setMemory(startPointer, lengthBytes(), (byte) 0);
		}
	}

	public static class MemShortArray implements MemoryChunk {
		private long startPointer;

		private final int size;

		public MemShortArray(int size) {
			this.size = size;
		}

		public short get(int index) {
			return MemoryUtil.memGetShort(startPointer + (long) index * ByteUtil.SIZEOF_SHORT);
		}

		public void put(int index, short val) {
			MemoryUtil.memPutShort(startPointer + (long) index * ByteUtil.SIZEOF_SHORT, val);
		}

		@Override
		public int length() {
			return size;
		}

		@Override
		public int lengthBytes() {
			return size * ByteUtil.SIZEOF_SHORT;
		}

		@Override
		public void resserve(long pointer) {
			startPointer = pointer;
		}

		public void zero() {
//			mem.setMemory(startPointer, lengthBytes(), (byte) 0);
			UnsafeApi.setMemory(startPointer, lengthBytes(), (byte) 0);
		}

		public short[] toArray(int wantedSize) {
			assert (wantedSize <= size);
			short[] a = new short[wantedSize];
			for(int i = 0; i < wantedSize; i++) {
				a[i] = get(i);
			}
			return a;
		}
	}

	public MemShortArray shortArray(int size) {
		return register(new MemShortArray(size));
	}

	public MemByteArray byteArray(int size) {
		return register(new MemByteArray(size));
	}

	public MemIntArray intArray(int size) {
		return register(new MemIntArray(size));
	}

	public MemFloatArray floatArray(int size) {
		return register(new MemFloatArray(size));
	}

	public MemBoolArray boolArray(int size) {
		return register(new MemBoolArray(size));
	}
}

