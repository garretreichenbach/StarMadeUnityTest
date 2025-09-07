package org.schema.game.server.controller.world.factory.planet.terrain;

import org.schema.common.FastMath;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.data.IcosahedronHelper;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.RequestDataIcoPlanet;
import org.schema.game.server.controller.TerrainChunkCacheElement;
import org.schema.game.server.controller.world.factory.planet.FastNoise;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.Random;

public class TerrainGeneratorVolcanic extends TerrainGenerator {

	final static int ROCK = registerBlock(ElementKeyMap.TERRAIN_ROCK_BLACK);
	final static int LAVA = registerBlock(ElementKeyMap.TERRAIN_LAVA_ID);
	final static int CRYSTAL = registerBlock(ElementKeyMap.CRYS_RED);

	final static int[] DAM_ROCK = registerBlockDamaged(ElementKeyMap.TERRAIN_ROCK_BLACK);

	public static short[] caveResources = {
		ElementKeyMap.RESS_ORE_THRENS,
		ElementKeyMap.RESS_ORE_JISPER,
		ElementKeyMap.RESS_GAS_ZERCANER,
		ElementKeyMap.RESS_ORE_SERTISE,
		ElementKeyMap.RESS_ORE_HYLAT,
		ElementKeyMap.RESS_ORE_FERTIKEEN,
		ElementKeyMap.RESS_ORE_SAPSUN,
		ElementKeyMap.RESS_ORE_METAL_COMMON,
		ElementKeyMap.RESS_CRYS_HATTEL,
		ElementKeyMap.RESS_CRYS_SINTYR,
		ElementKeyMap.RESS_CRYS_MATTISE,
		ElementKeyMap.RESS_CRYS_RAMMET,
		ElementKeyMap.RESS_CRYS_VARAT,
		ElementKeyMap.RESS_GAS_BASTYN,
		ElementKeyMap.RESS_CRYS_CRYSTAL_COMMON,
		ElementKeyMap.RESS_CRYS_NOCX,
	};

	private FastNoiseSIMD mainFNS;
	private FastNoise damageFN;

	public TerrainGeneratorVolcanic(int seed, float planetRadius) {
		super(seed, planetRadius);

		mainFNS = new FastNoiseSIMD(seed);
		mainFNS.SetFrequency(0.004f);
		mainFNS.SetNoiseType(FastNoiseSIMD.NoiseType.SimplexFractal);

		mainFNS.SetFractalType(FastNoiseSIMD.FractalType.RigidMulti);
		mainFNS.SetFractalLacunarity(2.2f);
		mainFNS.SetFractalGain(0.39f);
		mainFNS.SetFractalOctaves(4);

		//mainFNS.SetPerturbType(FastNoiseSIMD.PerturbType.Gradient);
		mainFNS.SetPerturbAmp(2.0f);
		mainFNS.SetPerturbFrequency(0.5f);

		damageFN = new FastNoise(seed);
		damageFN.SetFrequency(0.015f);
		damageFN.SetNoiseType(FastNoise.NoiseType.Cellular);
		damageFN.SetCellularReturnType(FastNoise.CellularReturnType.Distance2Div);
		damageFN.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Natural);
	}

	@Override
	public int isSolidTerrain(int x, int y, int z) {
		float minDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);

		minDensity -= caveDepth;

		if (minDensity > 0f) return LAVA;

		return -1;
	}

	@Override
	public boolean isEmptyTerrain(int x, int y, int z) {

		//if(true)
		//	return false;

		float maxDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);

		maxDensity += terrainAmplitude * 1.66f + rockLayerHeight;

		return maxDensity <= 0.0f;
	}

	private static final float terrainAmplitude = 45f;
	private static final float rockLayerHeight = 18f;
	private static final float damageDepth = 20f;

	private static final float caveWallDepth = 14f;
	private static final float caveMaxResourceMultiplier = 34f;

	@Override
	public TerrainStructureList generateSegment(Segment w, RequestData requestData) throws SegmentDataWriteException {

		SegmentData segData = w.getSegmentData();
		byte side = ((PlanetIco) w.getSegmentController()).getSideId();
		Matrix3f sideTransform = IcosahedronHelper.getSideTransform(side).basis;
		RequestDataIcoPlanet rd = (RequestDataIcoPlanet) requestData;
		TerrainChunkCacheElement tcce = rd.currentChunkCache;
		Vector3f blockPos = rd.vector3f;
		//BlockBiomeData bbd = rd.blockBiomeData;
		Random r = rd.random;

		blockPos.set(w.pos.x, w.pos.y, w.pos.z);
		r.setSeed(blockPos.hashCode() + seed);

		TerrainStructureList tsl = null;

		boolean allInSide = IcosahedronHelper.isSegmentAllInSide(w.pos);
		int index = 0;

		float[] terrainNoise = null;
		float[] caveNoise = rd.noiseSet1;

		sideTransform.transform(blockPos);
		float noiseX = blockPos.x;
		float noiseY = blockPos.y;
		float noiseZ = blockPos.z;

		caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);

		for (byte x = 0; x < SegmentData.SEG; x++) {
			for (byte z = 0; z < SegmentData.SEG; z++) {
				boolean airAbove = false;
				for (byte y = SegmentData.SEG; y >= 0; y--) {

					blockPos.set(x + w.pos.x - SegmentData.SEG_HALF, y + w.pos.y - SegmentData.SEG_HALF, z + w.pos.z - SegmentData.SEG_HALF);

					// Skip all remaining -y blocks if out of side
					if (!allInSide && !IcosahedronHelper.isPointInSide(blockPos)) {
						tcce.outOfSide();
						index += y + 1;
						break;
					}

					sideTransform.transform(blockPos);

					float density = planetRadius - FastMath.carmackLength(blockPos);
					float planetMag = density;
					float mainTerrain;

					float rockDensity = density + rockLayerHeight;

					if (planetMag < 0f) {

						if (terrainNoise == null) {
							terrainNoise = rd.noiseSet0;
							mainFNS.FillSampledNoiseSet(terrainNoise, IcosahedronHelper.getVectorSet(side, 1), noiseX, noiseY, noiseZ);
						}

						mainTerrain = (terrainNoise[index] + 0.3f - Math.min(caveNoise[index] * caveNoise[index], 0.16f) * 6f) * -terrainAmplitude;
						density = mainTerrain - Math.abs(rockDensity);
					} else
						density += 10f;


					//if (planetMag < caveDepth)
					density = Math.min((caveNoise[index] - caveRatio) * -30f, density);


					// This is the extra block at the top of the chunk to check air, not actually placed.
					if (y == Segment.DIM) {
						airAbove = density <= 0f;
						index++;
						continue;
					}

					PlanetIco planet = (PlanetIco) w.getSegmentController();
					if(planet.isInPlanetCore((int) blockPos.x, (int) blockPos.y, (int) blockPos.z)) {
						tcce.placeAir();
						index ++;
						continue;
					}

					if (density > 0f) {

						// This is a solid block with air directly above
						if (airAbove) {

							/*if (r.nextFloat() < 0.0005f) {
								float size = r.nextFloat() * 4f + 2f;
								tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.Rock, ElementKeyMap.TERRAIN_ROCK_PURPLE, TerrainStructure.toHalfFloat(size));
							}*/

							if (planetMag < 0f) {

								float damageNoise = damageFN.GetNoise(blockPos.x, blockPos.y, blockPos.z);

								if (r.nextFloat() < 0.005f + 0.03f * (1f + damageNoise)) {
									tsl = TerrainGenerator.addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_ROCK_SPRITE);
								}

								tsl = placeSolidAndResources(damagedRock(damageNoise, density), r, tsl, tcce, x, y, z, segData);
							} else if (density < caveWallDepth) {
								float rockNoise = -damageFN.GetNoise(blockPos.x, blockPos.y, blockPos.z);
								rockNoise *= rockNoise;

								if (density < caveWallDepth * rockNoise)
									tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
								else
									tcce.placeAir();
							} else
								tcce.placeAir();
						}

						// No air above
						else {
							if (planetMag < 0f) {
								tsl = placeSolidAndResources(damagedRock(damageFN.GetNoise(blockPos.x, blockPos.y, blockPos.z), density), r, tsl, tcce, x, y, z, segData);

							} else {

								float maxRockDensity = caveWallDepth;

								if (density < caveWallDepth) {
									float rockNoise = -damageFN.GetNoise(blockPos.x, blockPos.y, blockPos.z);
									rockNoise *= rockNoise;
									maxRockDensity = caveWallDepth * rockNoise;
								}

								if (density < maxRockDensity) {
									int blockData = tcce.placeBlock(ROCK, x, y, z, segData);

									if (r.nextFloat() < resourceChance * (1f + Math.min(planetMag * 0.3f, caveMaxResourceMultiplier))) {
										assert (ElementKeyMap.getInfoFast(SegmentData.getTypeFromIntData(blockData)).resourceInjection == ElementInformation.ResourceInjectionType.ORE);

										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.ResourceBlob,
														   caveResources[r.nextInt(caveResources.length)], SegmentData.getTypeFromIntData(blockData), standardResourceSize);
									}
								} else
									tcce.placeBlock(LAVA, x, y, z, segData);
							}
						}

						airAbove = false;
					} else {
						airAbove = true;
						tcce.placeAir();
					}

					index++;
				}
			}
		}

		segData.getSegmentController().getSegmentBuffer()
			.updateBB(segData.getSegment());

		return tsl;
	}

	private int damagedRock(float mag, float density) {
		int i = FastMath.fastFloor(mag * -24 + density * (damageDepth / 8f));

		i = Math.max(7 - i, 0);

		return DAM_ROCK[i];
	}

	@Override
	public void generateLOD() {
		
	}
	@Override
	public TerrainGeneratorTypeI getType() {
		return TerrainGeneratorType.VOLCANIC;
	}
}