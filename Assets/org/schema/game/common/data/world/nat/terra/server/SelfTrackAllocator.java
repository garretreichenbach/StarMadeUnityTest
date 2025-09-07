package org.schema.game.common.data.world.nat.terra.server;

import org.agrona.UnsafeApi;

/**
 * Tracks memory that it allocates, but not with memory use listener.
 * Not thread safe!
 */
public class SelfTrackAllocator implements MemoryAllocator {

//    private static final Memory mem = OS.memory();

	private int used;

	private final boolean zero;

	public SelfTrackAllocator(boolean zero) {
		this.zero = zero;
	}

	@Override
	public long allocate(long length) {
		used += (int) length;
//        long addr = mem.allocate(length);
//        if (zero) { // Zero the memory if requested
//            mem.setMemory(addr, length, (byte) 0);
//        }
		long addr = UnsafeApi.allocateMemory(length);
		if(zero) {
			UnsafeApi.setMemory(addr, length, (byte) 0);
		}
		return addr;
	}

	@Override
	public void free(long addr, long length) {
//        mem.freeMemory(addr, length);
		UnsafeApi.freeMemory(addr);
		used -= (int) length;
	}

	public int getMemoryUsed() {
		return used;
	}
}
