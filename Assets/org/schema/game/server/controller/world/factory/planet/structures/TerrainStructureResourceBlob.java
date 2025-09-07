package org.schema.game.server.controller.world.factory.planet.structures;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementInformation.ResourceInjectionType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.GenerationElementMap;
import org.schema.game.server.controller.RequestDataStructureGen;
import org.schema.game.server.controller.world.factory.planet.FastNoise;

public class TerrainStructureResourceBlob extends TerrainStructure {

	static FastNoise oreNoise;

	static {

		oreNoise = new FastNoise();
		oreNoise.SetFrequency(0.4f);

		/*registerAllResources(ElementKeyMap.TERRAIN_ROCK_NORMAL);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_MARS);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_BLUE);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_ORANGE);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_YELLOW);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_WHITE);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_PURPLE);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_RED);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_GREEN);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_BLACK);*/
		
		for(short type : ElementKeyMap.keySet){
			ElementInformation infoFast = ElementKeyMap.getInfoFast(type);
			if(infoFast.resourceInjection == ResourceInjectionType.ORE){
				registerAllResources(type);
			}
		}
	}

	private static void registerAllResources(short block) {
		//resources are including 16 since 0 is no resource and there are 16 resources in total
		for (byte i = 0; i <= 16; i++) {
			GenerationElementMap.getBlockDataIndex(SegmentData.makeDataInt(block, i));
		}
		
	}

	// MetaData0 mineableBlockID
	// MetaData1 replacableBlockID
	// MetaData2 oreLumpDiameter

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short mineableBlockID, short replacableBlockID, short oreLumpDiameter) throws SegmentDataWriteException {

		float size = toFloat(oreLumpDiameter) * 0.5f;
		int sizeI = FastMath.fastCeil(size * 2);
		SegmentData segData = seg.getSegmentData();

		
		int blockTypeIndex = GenerationElementMap.getBlockDataIndex(
				SegmentData.makeDataInt(replacableBlockID, ElementKeyMap.resIDToOrientationMapping[mineableBlockID]));

		for (byte iX = (byte) Math.max(0, x - sizeI); iX <= Math.min(SegmentData.SEG - 1, x + sizeI); iX++) {
			for (byte iY = (byte) Math.max(0, y - sizeI); iY <= Math.min(SegmentData.SEG - 1, y + sizeI); iY++) {
				for (byte iZ = (byte) Math.max(0, z - sizeI); iZ <= Math.min(SegmentData.SEG - 1, z + sizeI); iZ++) {

					if (segData.getType(iX, iY, iZ) != replacableBlockID)
						continue;

					float fx = iX - x;
					float fy = iY - y;
					float fz = iZ - z;
					float mag = FastMath.carmackSqrt(fx * fx + fy * fy + fz * fz);

					mag += oreNoise.GetPerlin(iX + seg.pos.x, iY + seg.pos.y, iZ + seg.pos.z) * size;

					if (mag < size) {
						assert (ElementKeyMap.resIDToOrientationMapping[mineableBlockID] > 0 && ElementKeyMap.resIDToOrientationMapping[mineableBlockID] < 17);

						reqData.currentChunkCache.placeBlock(
							blockTypeIndex,
							iX, iY, iZ,
							segData);

						//segData.setInfoElementForcedAddUnsynched(iX, iY, iZ, replacableBlockID, orientation, (byte) 0, false);
					}
				}
			}
		}
	}
	
	/*@Override
	public Vector3i getMinBounds(short metaData0, short metaData1, short metaData2, Vector3i out) {
		
		int min = -FastMath.fastCeil(toFloat(metaData2));
		out.set(min, min, min);
		return out;
	}
	@Override
	public Vector3i getMaxBounds(short metaData0, short metaData1, short metaData2, Vector3i out) {
		
		int max = FastMath.fastCeil(toFloat(metaData2));
		out.set(max, max, max);
		return out;
	}*/
}
