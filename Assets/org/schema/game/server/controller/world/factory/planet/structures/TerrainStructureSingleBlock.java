package org.schema.game.server.controller.world.factory.planet.structures;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.GenerationElementMap;
import org.schema.game.server.controller.RequestDataStructureGen;

public class TerrainStructureSingleBlock extends TerrainStructure {

	private static int spriteOrient = SegmentData.makeDataInt((byte) 0, (byte) 2, false, (byte) 1);

	static {
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_FLOWERS_BLUE_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_GRASS_LONG_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_BERRY_BUSH_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_FLOWERS_YELLOW_SPRITE);

		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_CACTUS_SMALL_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_FLOWERS_DESERT_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_ROCK_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE);

		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_SHROOM_RED_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_FUNGAL_GROWTH_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_CORAL_RED_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_FUNGAL_TRAP_SPRITE);

		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_FLOWER_FAN_PURPLE_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_WEEDS_PURPLE_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_GLOW_TRAP_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_YHOLE_PURPLE_SPRITE);

		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_CORAL_ICE_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_ICE_CRAG_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_SNOW_BUD_SPRITE);
		GenerationElementMap.getBlockDataIndex(spriteOrient | ElementKeyMap.TERRAIN_FAN_FLOWER_ICE_SPRITE);
	}

	public TerrainStructureSingleBlock(){
		bbMin.set(0,1,0);
		bbMax.set(0,1,0);
	}

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short blockType, short metaData1, short metaData2) throws SegmentDataWriteException {

		y++;

		if (x < 0 || x >= SegmentData.SEG ||
			y < 0 || y >= SegmentData.SEG ||
			z < 0 || z >= SegmentData.SEG)
			return;

		SegmentData segData = seg.getSegmentData();

		if (segData.getType((byte) x, (byte) y, (byte) z) != Element.TYPE_NONE)
			return;

		reqData.currentChunkCache.placeBlock(
			GenerationElementMap.getBlockDataIndex(spriteOrient | blockType),
			(byte) x, (byte) y, (byte) z,
			segData);

		//segData.setInfoElementForcedAddUnsynched(dataIndex, spriteOrient | blockType);

	}

}
