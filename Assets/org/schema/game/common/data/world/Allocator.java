package org.schema.game.common.data.world;

import org.agrona.UnsafeApi;
import org.schema.game.common.data.world.nat.terra.server.MemoryAllocator;
import org.schema.game.common.data.world.nat.terra.server.MemoryUseListener;
import org.schema.game.common.data.world.nat.terra.server.Pointer;

public class Allocator implements MemoryAllocator {

	private MemoryUseListener memListener;
	
//	private static final Memory mem = OS.memory();
	
	public Allocator(MemoryUseListener memListener) {
		super();
		this.memListener = memListener;
	}

	@Override
	public @Pointer long allocate(long length) {
		memListener.onAllocate(length);
//		return mem.allocate(length);
		return UnsafeApi.allocateMemory(length);
	}

	@Override
	public void free(@Pointer long addr, long length) {
//		mem.freeMemory(addr, length);
		UnsafeApi.freeMemory(addr);
		memListener.onFree(length);
	}
}
