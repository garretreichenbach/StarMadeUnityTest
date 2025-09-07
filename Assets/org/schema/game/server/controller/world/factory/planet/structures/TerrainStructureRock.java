package org.schema.game.server.controller.world.factory.planet.structures;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.GenerationElementMap;
import org.schema.game.server.controller.RequestDataStructureGen;
import org.schema.game.server.controller.world.factory.planet.FastNoise;
import org.schema.game.server.controller.world.factory.planet.FastNoise.FractalType;
import org.schema.game.server.controller.world.factory.planet.terrain.TerrainGenerator;

import java.util.Random;

public class TerrainStructureRock extends TerrainStructure {

	static FastNoise rockNoise;

	static {
		registerBlock(ElementKeyMap.TERRAIN_ROCK_NORMAL);
		registerBlock(ElementKeyMap.TERRAIN_ROCK_BLACK);
		registerBlock(ElementKeyMap.TERRAIN_ROCK_PURPLE);
		registerBlock(ElementKeyMap.TERRAIN_ICEPLANET_ROCK);

		rockNoise = new FastNoise();
		rockNoise.SetFrequency(0.05f);
		rockNoise.SetFractalType(FractalType.Billow);
		rockNoise.SetFractalOctaves(2);
	}

	public TerrainStructureRock() {

	}

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short block, short rockSize, short ore) throws SegmentDataWriteException {

		float size = toFloat(rockSize);
		int sizeI = FastMath.fastCeil(size) + 2;
		SegmentData segData = seg.getSegmentData();

		int blockTypeIndex = GenerationElementMap.getBlockDataIndex(SegmentData.makeDataInt(block));

		float fx, fy, fz, mag;

		for(byte iX = (byte) Math.max(0, x - sizeI); iX < Math.min(SegmentData.SEG, x + sizeI + 1); iX++) {
			for(byte iY = (byte) Math.max(0, y - sizeI); iY < Math.min(SegmentData.SEG, y + sizeI + 1); iY++) {
				for(byte iZ = (byte) Math.max(0, z - sizeI); iZ < Math.min(SegmentData.SEG, z + sizeI + 1); iZ++) {

					if(segData.getType(iX, iY, iZ) == block)
						continue;

					fx = iX - x;
					fy = (iY - y) * 1.3f;
					fz = iZ - z;

					mag = fx * fx + fy * fy + fz * fz;
					mag = FastMath.carmackInvSqrt(mag) * mag;

					mag += rockNoise.GetSimplexFractal(iX + seg.pos.x, iY + seg.pos.y, iZ + seg.pos.z) * 3.0f;

					if(mag < size)
						reqData.currentChunkCache.placeBlock(
								blockTypeIndex,
								iX, iY, iZ,
								segData);
				}
			}
		}

		if(ore > 0) {
			Type.ResourceBlob.terrainStructure.build(seg, reqData, x, y, z, ore, block, rockSize);
		}
	}

	public static TerrainStructureList add(Random r, TerrainStructureList tsl, short x, short y, short z, short blockType, float minSize, float maxSize) {
		float size = r.nextFloat() * (maxSize - minSize) + minSize;
		short ore = 0;
		if(r.nextFloat() < TerrainGenerator.rockStructureResourceChance)
			ore = TerrainGenerator.randomStandardResource(r);

		return TerrainGenerator.addStructure(tsl, x, y, z, TerrainStructure.Type.Rock, blockType, TerrainStructure.toHalfFloat(size), ore);
	}
}
