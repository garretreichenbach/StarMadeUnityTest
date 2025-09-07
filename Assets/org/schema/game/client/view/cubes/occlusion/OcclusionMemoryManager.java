package org.schema.game.client.view.cubes.occlusion;

import java.util.List;

import org.schema.game.common.data.world.Allocator;
import org.schema.game.common.data.world.NativeMemoryManager;
import org.schema.game.common.data.world.nat.terra.server.MemoryArea;
import org.schema.game.common.data.world.nat.terra.server.MemoryUseListener;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class OcclusionMemoryManager {
	public static NativeMemoryManager segmentDataManager;
	private long pointer;
	private final MemoryUseListener memListener = new MemoryUseListener() {
		@Override
		public void onFree(long amount) {
		}
		
		@Override
		public void onAllocate(long amount) {
			
		}
	};
	private MemoryArea mainArea;
	private Allocator allocator;
	private final List<MemoryArea> freeSegments = new ObjectArrayList<MemoryArea>();
	private int blockSize;
	private int areaSize;
	
	
//	public static int minusOneToSeventeenIndices[] = new int[segmentArraySize];
//	public static boolean minusOneToSeventeenValid[] = new boolean[segmentArraySize];
//	public static int minusOneToSeventeenInfoIndexDiv3[] = new int[segmentArraySize];
//	public static int minusOneToSeventeenOGIndex[] = new int[segmentArraySize];
//	public static boolean normalValid[] = new boolean[segmentArraySize];
//	public static boolean allInside[] = new boolean[segmentArraySize];
	
	public OcclusionMemoryManager() {
		
	}
	public static synchronized OcclusionMemoryManager initialize(int areaSize, int blockSize) {
		OcclusionMemoryManager instance = new OcclusionMemoryManager();
		instance.init(areaSize, blockSize);
		return instance;
	}

	
	private void init(int areaSize, int blockSize) {
		
		this.areaSize = areaSize;
		this.blockSize = blockSize;
		
		this.allocator = new Allocator(memListener);
		this.mainArea = MemoryArea.create(this.allocator, blockSize);
		pointer = 0;
	}
	public void freeArea(MemoryArea data) {
		data.fillFully((byte)0);
		synchronized(this) {
			freeSegments.add(data);
		}
	}
	public MemoryArea getMemoryArea() {
		synchronized(this) {
			if(!freeSegments.isEmpty()) {
				return freeSegments.remove(freeSegments.size()-1);
			}
			if(pointer+areaSize < blockSize) {
				
				MemoryArea area = mainArea.wrapFromStart(pointer, areaSize);
				pointer+=areaSize;
				return area;
			}else {
				//all memory of this block used. allocate new one, old areas will still point to the same memory space
				System.err.println("[NATIVE] Native memory block full. Allocating new memory block! (might be the cause of a small lag)");
				init(areaSize, blockSize);
				return getMemoryArea();
			}
			
		}
	}
}
