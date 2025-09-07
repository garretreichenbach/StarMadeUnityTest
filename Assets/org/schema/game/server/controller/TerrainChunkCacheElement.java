package org.schema.game.server.controller;

import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;

/**
 * this class conatins all necessary data (e.g. list of structures and their position)
 * of a chunk to help fully create a neighbor chunk
 */
public class TerrainChunkCacheElement {

	/**
	 * this is the raw terrain. on creating the chunk based on this data,
	 * this is purged (because it is only needed once)
	 */
	public RemoteSegment preData;
	public GenerationElementMap generationElementMap = new GenerationElementMap();
	private TerrainStructureList structureList;
	public boolean allInSide = true;

	public boolean isEmpty() {
		assert (generationElementMap.containsBlockIndexList.size() != 0) : "Empty type list";

		return (generationElementMap.containsBlockIndexList.size() == 1 &&
				generationElementMap.containsBlockIndexList.getInt(0) == 0);
	}

	public boolean isFullyFilledWithOneType() {
		return generationElementMap.containsBlockIndexList.size() == 1;
	}

	/**
	 * This is the structure information usable by adjacent chunks
	 */
	public TerrainStructureList getStructureList() {
		return this.structureList;
	}

	/**
	 * this is set during the raw terrain generation
	 *
	 * @param structureList
	 */
	public void setStructureList(TerrainStructureList structureList) {
		this.structureList = structureList;
	}

	public int placeBlock(int blockTypeIndex, byte x, byte y, byte z, SegmentData segData) throws SegmentDataWriteException {

		//assert (blockTypeIndex > 0);

		generationElementMap.addBlock(blockTypeIndex);

		int block = GenerationElementMap.blockDataLookup[blockTypeIndex];

		segData.setInfoElementForcedAddUnsynched(SegmentData.getInfoIndex(x, y, z), block);
		//segData.setBlockAddedForced(true);

		return block;
	}

	public void placeAir() {
		generationElementMap.addBlock(0);
	}

	public void placeAirAt(int x, int y, int z, SegmentData segData) throws SegmentDataWriteException {
		segData.setInfoElementForcedAddUnsynched(SegmentData.getInfoIndex((byte) x, (byte) y, (byte) z), 0);
	}

	public void outOfSide() {
		allInSide = false;
	}

	public void clear() {
		generationElementMap.clear();
		allInSide = true;
	}
}
