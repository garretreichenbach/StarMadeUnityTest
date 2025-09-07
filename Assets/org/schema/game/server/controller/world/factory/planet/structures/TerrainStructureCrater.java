package org.schema.game.server.controller.world.factory.planet.structures;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.GenerationElementMap;
import org.schema.game.server.controller.RequestDataStructureGen;
import org.schema.game.server.controller.world.factory.planet.terrain.TerrainGenerator;

import java.util.Random;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class TerrainStructureCrater extends TerrainStructure {

	static {
		registerBlock(Element.TYPE_NONE);
		registerBlock(ElementKeyMap.TERRAIN_ROCK_NORMAL);
	}

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short radius, short metaData1, short metaData2) throws SegmentDataWriteException {
		int sizeI = FastMath.fastCeil(radius) + 2;
		SegmentData segData = seg.getSegmentData();
		int blockTypeIndex = GenerationElementMap.getBlockDataIndex(SegmentData.makeDataInt(Element.TYPE_NONE));
		float pi = FastMath.PI;
		for(byte iX = (byte) Math.max(0, x - sizeI); iX < Math.min(SegmentData.SEG, x + sizeI + 1); iX++) {
			for(byte iY = (byte) Math.max(0, y - sizeI); iY < Math.min(SegmentData.SEG, y + sizeI + 1); iY++) {
				for(byte iZ = (byte) Math.max(0, z - sizeI); iZ < Math.min(SegmentData.SEG, z + sizeI + 1); iZ++) {
					if(segData.getType(iX, iY, iZ) == Element.TYPE_NONE) continue;
					float dist = FastMath.sqrt(FastMath.pow(iX - x, 2) + FastMath.pow(iY - y, 2) + FastMath.pow(iZ - z, 2)) - 1.0f;
					if(dist < radius) {
						float craterDist = FastMath.sqrt(FastMath.pow(iX - x, 2) + FastMath.pow(iY - y, 2));
						if(craterDist < radius) {
							float craterHeight = FastMath.sqrt((float) radius * radius - craterDist * craterDist);
							float craterDepth = radius - craterHeight;
							if(craterDepth > 0) {
								float craterDepthFactor = FastMath.sin(craterDist / radius * pi / 2);
								int depth = FastMath.fastCeil(craterDepth * craterDepthFactor);
								for(int i = 0; i < depth; i++) {
									if(segData.getType(iX, iY, iZ) == Element.TYPE_NONE) continue;
									reqData.currentChunkCache.placeBlock(blockTypeIndex, iX, iY, iZ, segData);
								}
							}
						}
					}
				}
			}
		}
	}

	public static TerrainStructureList add(Random r, TerrainStructureList tsl, short x, short y, short z, short blockType, float minSize, float maxSize) {
		float radius = r.nextFloat() * (maxSize - minSize) + minSize;
		return TerrainGenerator.addStructure(tsl, x, y, z, Type.Crater, (short) radius, (short) 0, (short) 0);
	}
}
