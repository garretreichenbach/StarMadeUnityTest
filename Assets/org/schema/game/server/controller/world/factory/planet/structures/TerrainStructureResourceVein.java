package org.schema.game.server.controller.world.factory.planet.structures;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.GenerationElementMap;
import org.schema.game.server.controller.RequestDataStructureGen;
import org.schema.game.server.controller.world.factory.planet.FastNoise;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;

public class TerrainStructureResourceVein extends TerrainStructure {

	static FastNoise oreNoise;
	static FastNoiseSIMD oreFNS;

	static {

		oreNoise = new FastNoise();
		oreNoise.SetFrequency(0.1f);
		oreNoise.SetCellularReturnType(FastNoise.CellularReturnType.Distance2Mul);

		oreFNS = new FastNoiseSIMD();
		oreFNS.SetFrequency(0.1f);
		oreFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Cellular);
		oreFNS.SetCellularReturnType(FastNoiseSIMD.CellularReturnType.Distance2Mul);

		/*registerAllResources(ElementKeyMap.TERRAIN_ROCK_NORMAL);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_MARS);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_BLUE);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_ORANGE);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_YELLOW);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_WHITE);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_PURPLE);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_RED);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_GREEN);
		registerAllResources(ElementKeyMap.TERRAIN_ROCK_BLACK);
		
		for(short type : ElementKeyMap.keySet){
			ElementInformation infoFast = ElementKeyMap.getInfoFast(type);
			if(infoFast.resourceInjection == ResourceInjectionType.ORE){
				registerAllResources(type);
			}
		}*/
	}

	

	// MetaData0 mineableBlockID
	// MetaData1 replacableBlockID
	// MetaData2 oreLumpDiameter

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short mineableBlockID, short replacableBlockID, short oreLumpDiameter) throws SegmentDataWriteException {

		float size = toFloat(oreLumpDiameter) * 0.5f;
		int sizeI = FastMath.fastCeil(size*1.5f);
		SegmentData segData = seg.getSegmentData();

		byte xStart = (byte)Math.max(0, x - sizeI);
		byte yStart = (byte)Math.max(0, y - sizeI);
		byte zStart = (byte)Math.max(0, z - sizeI);
		byte xEnd = (byte)Math.min(SegmentData.SEG - 1, x + sizeI);
		byte yEnd = (byte)Math.min(SegmentData.SEG - 1, y + sizeI);
		byte zEnd = (byte)Math.min(SegmentData.SEG - 1, z + sizeI);

		/*if (xStart <= xEnd ||
			yStart <= yEnd ||
			zStart <= zEnd)
			return;

		float[] noiseSet = reqData.noiseSet0;

		oreFNS.FillNoiseSet(noiseSet,
							xStart + seg.pos.x, yStart + seg.pos.y, zStart + seg.pos.z,
							xEnd - xStart, yEnd - yStart, zEnd - zStart);

		int index = 0;*/

		int blockTypeIndex = GenerationElementMap.getBlockDataIndex(
				SegmentData.makeDataInt(replacableBlockID, ElementKeyMap.resIDToOrientationMapping[mineableBlockID]));

		for (byte iX = xStart; iX <= xEnd; iX++) {
			for (byte iY = yStart; iY <= yEnd; iY++) {
				for (byte iZ = zStart; iZ <= zEnd; iZ++) {

					if (segData.getType(iX, iY, iZ) != replacableBlockID)
						continue;

					float fx = iX - x;
					float fy = iY - y;
					float fz = iZ - z;
					float mag = FastMath.carmackSqrt(fx * fx + fy * fy + fz * fz);

					if (oreNoise.GetCellular(iX + seg.pos.x, iY + seg.pos.y, iZ + seg.pos.z) > (mag-size)/size * 0.4f - 0.6) {
						//assert (ElementKeyMap.resIDToOrientationMapping[mineableBlockID] > 0 && ElementKeyMap.resIDToOrientationMapping[mineableBlockID] < 17); // (veins of random rocks should be possible)

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
