package org.schema.game.common.data.world.nat.terra.server;

import org.agrona.UnsafeApi;
import org.agrona.concurrent.UnsafeBuffer;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;

/**
 * A memory area with optional bounds checks.
 *
 */
public class MemoryArea {

	// TODO warn about memory leaks created by forgotten release()

//    private static final Memory mem = Memory.memory();

	private static final VarHandle releaseAllowedVar;

	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			releaseAllowedVar = lookup.findVarHandle(MemoryArea.class, "releaseAllowed", boolean.class);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new Error(e); // Cannot use MemoryAreas safely without this
		}
	}

	/**
	 * Creates a new memory area by allocating more memory.
	 * @param allocator Memory allocator to use.
	 * @param length How much memory is needed.
	 * @return A new memory area.
	 */
	public static MemoryArea create(MemoryAllocator allocator, long length) {
		return new MemoryArea(allocator.allocate(length), length, allocator, true);
	}

	/**
	 * Creates a new memory area by wrapping a pointer.
	 * Releasing it must be done manually, i.e. calling {@link #release()}
	 * will throw an exception.
	 * @param start Start address.
	 * @param length Wanted length.
	 * @return A new memory area wrapping a pointer.
	 */
	public static MemoryArea wrap(@Pointer long start, long length) {
		return new MemoryArea(start, length, null, false);
	}

	public MemoryArea wrapFromStart(@Pointer long start, long length) {
		return wrap(this.start + start, length);
	}

	/**
	 * Start memory address of this.
	 */
	@Pointer
	private final long start;

	/**
	 * Length of this.
	 */
	private final long length;

	/**
	 * Memory allocator used to allocate this area.
	 * May be null if this was just wrapped.
	 */
	private final MemoryAllocator allocator;

	/**
	 * Whether or not releasing this memory are is allowed.
	 */
	@SuppressWarnings("unused") // VarHandle
	private volatile boolean releaseAllowed;

	protected MemoryArea(long start, long length, MemoryAllocator allocator, boolean releaseAllowed) {
		this.start = start;
		this.length = length;
		this.allocator = allocator;
		this.releaseAllowed = releaseAllowed;
	}

	public long length() {
		return length;
	}

	public void fill(long index, long size, byte fill) {
//		mem.setMemory(BuildConfig.inBounds(start + index, 1, start, length), size, fill);
		UnsafeApi.setMemory(BuildConfig.inBounds(start + index, 1, start, length), size, fill);
	}

	public byte readByte(long index) {
//		return mem.readByte(BuildConfig.inBounds(start + index, 1, start, length));
		return UnsafeApi.getByte(BuildConfig.inBounds(start + index, 1, start, length));
	}

	public void readAllBytes(long index, byte[] out) {
//		mem.readBytes(BuildConfig.inBounds(start + index, 1, start, length), out, 0, out.length);
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(out.length);
		UnsafeBuffer buffer = new UnsafeBuffer(byteBuffer);
		UnsafeApi.copyMemory(BuildConfig.inBounds(start + index, 1, start, length), buffer.addressOffset(), out.length);
	}

	public void writeAllBytes(byte[] byteBuffer) {
//		mem.writeBytes(start, byteBuffer, 0, byteBuffer.length);
		ByteBuffer buffer = ByteBuffer.allocateDirect(byteBuffer.length);
		UnsafeBuffer unsafeBuffer = new UnsafeBuffer(buffer);
		UnsafeApi.copyMemory(unsafeBuffer.addressOffset(), start, byteBuffer.length);
	}

	public void writeByte(long index, byte value) {
//		mem.writeByte(BuildConfig.inBounds(start + index, 1, start, length), value);
		UnsafeApi.putByte(BuildConfig.inBounds(start + index, 1, start, length), value);
	}

	public byte readVolatileByte(long index) {
//		return mem.readVolatileByte(BuildConfig.inBounds(start + index, 1, start, length));
		return UnsafeApi.getByte(BuildConfig.inBounds(start + index, 1, start, length));
	}

	public void writeVolatileByte(long index, byte value) {
//		mem.writeVolatileByte(BuildConfig.inBounds(start + index, 1, start, length), value);
		UnsafeApi.putByte(BuildConfig.inBounds(start + index, 1, start, length), value);
	}

	public short readShort(long index) {
//		return mem.readShort(BuildConfig.inBounds(start + index, 2, start, length));
		return UnsafeApi.getShort(BuildConfig.inBounds(start + index, 2, start, length));
	}

	public void writeShort(long index, short value) {
//		mem.writeShort(BuildConfig.inBounds(start + index, 2, start, length), value);
		UnsafeApi.putShort(BuildConfig.inBounds(start + index, 2, start, length), value);
	}

	public short readVolatileShort(long index) {
//		return mem.readVolatileShort(BuildConfig.inBounds(start + index, 2, start, length));
		return UnsafeApi.getShort(BuildConfig.inBounds(start + index, 2, start, length));
	}

	public void writeVolatileShort(long index, short value) {
//		mem.writeVolatileShort(BuildConfig.inBounds(start + index, 2, start, length), value);
		UnsafeApi.putShort(BuildConfig.inBounds(start + index, 2, start, length), value);
	}

	public int readInt(long index) {
//		return mem.readInt(BuildConfig.inBounds(start + index, 4, start, length));
		return UnsafeApi.getInt(BuildConfig.inBounds(start + index, 4, start, length));
	}

	public void writeInt(long index, int value) {
//		mem.writeInt(BuildConfig.inBounds(start + index, 4, start, length), value);
		UnsafeApi.putInt(BuildConfig.inBounds(start + index, 4, start, length), value);
	}

	public int readIntIndex(long index) {
//		return mem.readInt(BuildConfig.inBounds(start + index * 4, 4, start, length));
		return UnsafeApi.getInt(BuildConfig.inBounds(start + index * 4, 4, start, length));
	}

	public void writeIntIndex(long index, int value) {
//		mem.writeInt(BuildConfig.inBounds(start + index * 4, 4, start, length), value);
		UnsafeApi.putInt(BuildConfig.inBounds(start + index * 4, 4, start, length), value);
	}

	public int readVolatileInt(long index) {
//		return mem.readVolatileInt(BuildConfig.inBounds(start + index, 4, start, length));
		return UnsafeApi.getInt(BuildConfig.inBounds(start + index, 4, start, length));
	}

	public void writeVolatileInt(long index, int value) {
//		mem.writeVolatileInt(BuildConfig.inBounds(start + index, 4, start, length), value);
		UnsafeApi.putInt(BuildConfig.inBounds(start + index, 4, start, length), value);
	}

	public long readLong(long index) {
//		return mem.readLong(BuildConfig.inBounds(start + index, 8, start, length));
		return UnsafeApi.getLong(BuildConfig.inBounds(start + index, 8, start, length));
	}

	public void writeLong(long index, long value) {
//		mem.writeLong(BuildConfig.inBounds(start + index, 8, start, length), value);
		UnsafeApi.putLong(BuildConfig.inBounds(start + index, 8, start, length), value);
	}

	public long readVolatileLong(long index) {
//		return mem.readVolatileLong(BuildConfig.inBounds(start + index, 8, start, length));
		return UnsafeApi.getLong(BuildConfig.inBounds(start + index, 8, start, length));
	}

	public void writeVolatileLong(long index, long value) {
//		mem.writeVolatileLong(BuildConfig.inBounds(start + index, 8, start, length), value);
		UnsafeApi.putLong(BuildConfig.inBounds(start + index, 8, start, length), value);
	}

	public int getAndAddInt(long index, int increment) {
//		return mem.addInt(BuildConfig.inBounds(start + index, 4, start, length), increment);
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public long getAndAddLong(long index, int increment) {
//		return mem.addLong(BuildConfig.inBounds(start + index, 8, start, length), increment);
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/**
	 * Releases this memory area. Do not call this more than once!
	 */
	public void release() {
		// Atomic: only one release() ever gets through
		if(((boolean) releaseAllowedVar.getAndSet(this, false))) {
			throw new IllegalStateException("release not allowed");
		}
		allocator.free(start, length);
	}

	/**
	 * Gets the memory address of this memory area.
	 * @return Memory address.
	 */
	@Pointer
	public long memoryAddress() {
		return start;
	}

	public void copyTo(MemoryArea data) {
		assert (data.length == length);
//		mem.copyMemory(start, data.start, data.length);
		UnsafeApi.copyMemory(start, data.start, length);
	}

	public void fillFully(byte b) {
//		mem.setMemory(BuildConfig.inBounds(start, 1, start, length), length, b);
		UnsafeApi.setMemory(BuildConfig.inBounds(start, 1, start, length), length, b);
	}

	public void copyTo(ByteBuffer buffer) {
		for(int i = 0; i < length; i++) {
//			buffer.put(mem.readByte(start + i));
			buffer.put(readByte(i));
		}
	}

	public void putAll(ByteBuffer b) {
		for(int i = 0; i < length; i++) {
//			mem.writeByte(start + i, b.get());
			writeByte(i, b.get());
		}
	}
}
