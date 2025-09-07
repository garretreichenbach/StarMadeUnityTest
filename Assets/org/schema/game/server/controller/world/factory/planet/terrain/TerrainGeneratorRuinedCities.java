package org.schema.game.server.controller.world.factory.planet.terrain;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.data.IcosahedronHelper;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.world.factory.planet.FastNoise;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;

import javax.vecmath.Vector3f;

public class TerrainGeneratorRuinedCities extends TerrainGenerator {
	
	FastNoise buildingHeightNoise;
	FastNoise buildingDamageNoise;
	
	public TerrainGeneratorRuinedCities(int seed, float planetRadius) {
		super(seed, planetRadius);
		
		buildingHeightNoise = new FastNoise(seed);// + planet.getSideId());
		buildingHeightNoise.SetFrequency(0.015f);
		
		buildingDamageNoise = new FastNoise(seed);// + planet.getSideId());
		buildingDamageNoise.SetFrequency(0.03f);
	}
	
	final short ROCK = ElementKeyMap.TERRAIN_ROCK_BLACK;
	final short BUILDING = ElementKeyMap.HULL_ID;
	
	@Override
	public TerrainStructureList generateSegment(Segment w, RequestData requestData) {		
		
		if (!IcosahedronHelper.isSegmentInSide(w.pos))
			return null;
				
		SegmentData segData = w.getSegmentData();
		Vector3f blockPos = new Vector3f();	
		byte side = ((PlanetIco)w.getSegmentController()).getSideId();
		Transform sideTransform = IcosahedronHelper.getSideTransform(side);
		Vector3f sideUp = new Vector3f(0f,1f,0f);
		//sideTransform.transform(sideUp);
		
		float radius = planetRadius;				
		boolean airAbove = false;
		boolean allInSide = IcosahedronHelper.isSegmentAllInSide(w.pos);
		
		Vector3i basePos = new Vector3i(w.pos);
		basePos.y = FastMath.fastFloor(radius) + 8;
		boolean building = IcosahedronHelper.isSegmentAllInSide(basePos);
		float buildingHeight = 0;
		blockPos.set(basePos.x, basePos.y, basePos.z);
		FastMath.normalizeCarmack(blockPos);
		
		if (building){
			buildingHeight = buildingHeightNoise.GetSimplexFractal(basePos.x, basePos.z) * 1.5f;
			buildingHeight += ((blockPos.y * blockPos.y * blockPos.y * blockPos.y) - 0.3f) * 2f;
			buildingHeight *= 30f;
			
			if (buildingHeight < 15f)
				building = false;
		}
		try{
		for (byte x = 0; x < SegmentData.SEG; x++){
			for (byte z = 0; z < SegmentData.SEG; z++){
				airAbove = false;
				for (byte y = SegmentData.SEG - 1; y >= 0; y--){
					blockPos.set(x + w.pos.x - 8,y + w.pos.y - 8 , z + w.pos.z - 8);
					
					if (allInSide || IcosahedronHelper.isPointInSide(blockPos)){
						
						if (blockPos.y <= radius){
							segData.setInfoElementForcedAddUnsynched(x, y, z, ROCK, false);
							continue;
						}
						
						if (building){						
							
							if (blockPos.y < radius + buildingHeight - 1f){						
							
								if (x >= 5 && z >= 5 && 
										(x == 5 || z == 5 || x == SegmentData.SEG - 1 || z == SegmentData.SEG - 1) &&
										buildingDamageNoise.GetPerlinFractal(blockPos.x, blockPos.y, blockPos.z) < 0.4f - (blockPos.y - radius) * 0.01f)
									segData.setInfoElementForcedAddUnsynched(x, y, z, BUILDING, false);								
							
							}
							else if (blockPos.y < radius + buildingHeight &&
										buildingDamageNoise.GetPerlinFractal(blockPos.x, blockPos.y, blockPos.z) < 0.3f - (blockPos.y - radius) * 0.01f){
								
								if (x >= 5 && z >= 5)
									segData.setInfoElementForcedAddUnsynched(x, y, z, BUILDING, false);
							}
						}
						
					}
				}
			}
		}		
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
			throw new RuntimeException("this should be never be thrown, as generation should use a normal segment data", e);
		}
		segData.getSegmentController().getSegmentBuffer()
				.updateBB(segData.getSegment());

		return null;
	}
	
	static int FastFloor(float f) { return (f >= 0.0f ? (int)f : (int)f - 1); }
	static float Lerp(float a, float b, float t) { return a + t * (b - a); }

	@Override
	public void generateLOD() {

	}

	@Override
	public boolean isEmptyTerrain(int x, int y, int z) {
		return false;
	}

	@Override
	public TerrainGeneratorTypeI getType() {
		return TerrainGeneratorType.RUINED_CITY;
	}
}