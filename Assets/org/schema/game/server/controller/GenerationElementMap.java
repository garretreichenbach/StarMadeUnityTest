//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.schema.game.server.controller;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Original class by Jordan on 16/02/2017.
 * Stores all possible block ints used in generation terrain/structures
 * Allows easy counting of block types used
 * <p>
 * New version by Jake allows dynamic resize of blocks
 */
public class GenerationElementMap {

	private static final Int2IntOpenHashMap indexMap = new Int2IntOpenHashMap();
	public static int[] blockDataLookup;

	static {
		indexMap.defaultReturnValue(-1);
		getBlockDataIndex(0);
	}

	public static int getBlockDataIndex(int blockData) {

		int index = indexMap.get(blockData);

		if(index == -1) {
			index = indexMap.size();
			indexMap.put(blockData, index);
			updateLookupArray();
		}

		return index;
	}

	public static void updateLookupArray() {

		blockDataLookup = new int[indexMap.size()];
		for(Int2IntMap.Entry v : indexMap.int2IntEntrySet()) {
			blockDataLookup[v.getIntValue()] = v.getIntKey();
		}

	}

	private boolean[] containsBlockIndex = new boolean[blockDataLookup.length];
	public IntArrayList containsBlockIndexList = new IntArrayList();

	public void addBlock(int blockTypeIndex) {
		try {
			if(blockTypeIndex >= blockDataLookup.length || blockTypeIndex >= containsBlockIndex.length) {
				// Resize the array
				boolean[] newContainsBlockIndex = new boolean[blockTypeIndex + 1];
				System.arraycopy(containsBlockIndex, 0, newContainsBlockIndex, 0, containsBlockIndex.length);
				containsBlockIndex = newContainsBlockIndex;
			}
			if(!containsBlockIndex[blockTypeIndex]) {
				containsBlockIndex[blockTypeIndex] = true;
				containsBlockIndexList.add(blockTypeIndex);
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			System.err.println("[GenerationElementMap] [non-fatal exception] INDEX OUT OF BOUNDS: " + blockTypeIndex + " of total array length: " + containsBlockIndex.length);
			containsBlockIndex = new boolean[blockDataLookup.length];
			e.printStackTrace();
		}
	}

	public void clear() {
		for(int i : containsBlockIndexList) {
			containsBlockIndex[i] = false;
		}

		containsBlockIndexList.clear();
	}

	public int getBlockDataFromList(int index) {
		return GenerationElementMap.blockDataLookup[containsBlockIndexList.getInt(index)];
	}
}