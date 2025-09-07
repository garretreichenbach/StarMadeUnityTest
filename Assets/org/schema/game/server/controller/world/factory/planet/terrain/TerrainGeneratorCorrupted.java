package org.schema.game.server.controller.world.factory.planet.terrain;

import org.schema.common.FastMath;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.data.IcosahedronHelper;
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
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureRock;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.Random;

public class TerrainGeneratorCorrupted extends TerrainGenerator {

	final static int[] CRYSTAL_DAM = registerBlockDamaged(ElementKeyMap.CRYS_GREEN);
	final static int CRYSTAL = registerBlock(ElementKeyMap.CRYS_GREEN);
	final static int ROCK_PURPLE = registerBlock(ElementKeyMap.TERRAIN_ROCK_PURPLE);
	final static int PURPLE = registerBlock(ElementKeyMap.TERRAIN_PURPLE_ALIEN_ROCK);
	final static int PURPLE_TOP = registerBlock(ElementKeyMap.TERRAIN_PURPLE_ALIEN_TOP);
	final static short BIOME_SET_PIECE = 0;

	private FastNoiseSIMD mainFNS;
	private FastNoiseSIMD detailFNS;
	private FastNoise crystalFN;
	private FastNoise crystalPerturbFN;
	private FastNoise surfaceDirtFN;

	public TerrainGeneratorCorrupted(int seed, float planetRadius) {
		super(seed, planetRadius);

		mainFNS = new FastNoiseSIMD(seed);
		mainFNS.SetFrequency(0.015f);
		mainFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Cellular);
		mainFNS.SetCellularReturnType(FastNoiseSIMD.CellularReturnType.NoiseLookup);
		mainFNS.SetCellularDistanceFunction(FastNoiseSIMD.CellularDistanceFunction.Natural);

		mainFNS.SetPerturbType(FastNoiseSIMD.PerturbType.Gradient);
		mainFNS.SetPerturbAmp(2.0f);
		mainFNS.SetPerturbFrequency(0.5f);

		mainFNS.SetCellularNoiseLookupType(FastNoiseSIMD.NoiseType.SimplexFractal);
		mainFNS.SetCellularNoiseLookupFrequency(0.3f);
		mainFNS.SetFractalType(FastNoiseSIMD.FractalType.FBM);
		mainFNS.SetFractalLacunarity(2f);
		mainFNS.SetFractalGain(0.5f);
		mainFNS.SetFractalOctaves(2);

		detailFNS = new FastNoiseSIMD(seed);
		detailFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Value);
		detailFNS.SetFrequency(0.05f);

		crystalFN = new FastNoise(seed);
		crystalFN.SetFrequency(0.005f);
		crystalFN.SetNoiseType(FastNoise.NoiseType.Cellular);
		crystalFN.SetCellularReturnType(FastNoise.CellularReturnType.Distance2Div);
		crystalFN.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Natural);

		crystalPerturbFN = new FastNoise(seed);
		crystalPerturbFN.SetFrequency(0.03f);
		crystalPerturbFN.SetGradientPerturbAmp(10f);

		surfaceDirtFN = new FastNoise(seed);
		surfaceDirtFN.SetFrequency(0.03f);
		surfaceDirtFN.SetNoiseType(FastNoise.NoiseType.PerlinFractal);
		surfaceDirtFN.SetFractalOctaves(2);
		surfaceDirtFN.SetFractalGain(0.65f);
	}

	@Override
	public int isSolidTerrain(int x, int y, int z) {
		float minDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);

		minDensity -= terrainAmplitude + detailAmplitude;

		if (minDensity > 0f) return PURPLE;

		return -1;
	}

	@Override
	public boolean isEmptyTerrain(int x, int y, int z) {
		float maxDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);

		maxDensity += terrainAmplitude + detailAmplitude;

		return maxDensity <= 0.0f;
	}

	private static final float terrainAmplitude = 80f;
	private static final float detailAmplitude = 6f;

	private static final float crystalRatio = -0.02f;


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

		float[] terrainNoise = rd.noiseSet0;
		float[] detailNoise = rd.noiseSet1;
		float[] caveNoise = null;

		sideTransform.transform(blockPos);
		float noiseX = blockPos.x;
		float noiseY = blockPos.y;
		float noiseZ = blockPos.z;
		mainFNS.FillSampledNoiseSet(terrainNoise, IcosahedronHelper.getVectorSet(side, 1), noiseX, noiseY, noiseZ);
		detailFNS.FillSampledNoiseSet(detailNoise, IcosahedronHelper.getVectorSet(side, 1), noiseX, noiseY, noiseZ);

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
					//float planetMag = density;
					float mainTerrain;

					mainTerrain = terrainNoise[index] * terrainAmplitude;// * edgeDistance;

					density -= mainTerrain + detailNoise[index] * detailAmplitude;

					if (density > 0f) {
						if(caveNoise == null) {
							caveNoise = rd.noiseSet2;
							caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
						}

						if (caveNoise[index] > caveRatio)
							density = -1.0f;
					}


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
						if (airAbove && density < 40f) {

							if (r.nextFloat() < 0.0005f) {
								tsl = TerrainStructureRock.add(r, tsl, x, (short)(y-1), z, ElementKeyMap.TERRAIN_ROCK_PURPLE, 3f, 5f);
							}

							crystalPerturbFN.GradientPerturb(blockPos);
							float crystalNoise = crystalFN.GetNoise(blockPos.x, blockPos.y, blockPos.z);
							if (crystalNoise > crystalRatio)
								tcce.placeBlock(CRYSTAL, x, y, z, segData);
//								tcce.placeBlock(damagedCrystal(crystalNoise), x, y, z, segData);

							else {
								float surfaceDirt = surfaceDirtFN.GetNoise(blockPos.x, blockPos.y, blockPos.z);

								if (surfaceDirt < 0.3f) {
									tcce.placeBlock(PURPLE_TOP, x, y, z, segData);

									if (r.nextFloat() < 0.004f) {
										tsl = switch(r.nextInt(4)) {
											case 0 -> addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_FLOWER_FAN_PURPLE_SPRITE);
											case 1 -> addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_GLOW_TRAP_SPRITE);
											case 2 -> addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_WEEDS_PURPLE_SPRITE);
											case 3 -> addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_YHOLE_PURPLE_SPRITE);
											default -> tsl;
										};
									}
								}
								else if (surfaceDirt < 0.4f)
									tcce.placeBlock(ROCK_PURPLE, x, y, z, segData);
								else
									tsl = placeSolidAndResources(PURPLE, r, tsl, tcce, x, y, z, segData);

							}
						}

						// No air above
						else {
							if (density < 40f) {
								crystalPerturbFN.GradientPerturb(blockPos);
								float crystalNoise = crystalFN.GetNoise(blockPos.x, blockPos.y, blockPos.z);
								if (crystalNoise > crystalRatio)
									tcce.placeBlock(CRYSTAL, x, y, z, segData);
//									tcce.placeBlock(damagedCrystal(crystalNoise), x, y, z, segData);

								else
									tsl = placeSolidAndResources(PURPLE, r, tsl, tcce, x, y, z, segData);
							} else
								tsl = placeSolidAndResources(PURPLE, r, tsl, tcce, x, y, z, segData);
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

	private int damagedCrystal(float noise) {
		int i = FastMath.fastRound((noise - crystalRatio) * (8f / -crystalRatio));

		i = Math.max(7 - i, 0);

		return CRYSTAL_DAM[i];
	}

	@Override
	public void generateLOD() {
		
	}
	@Override
	public TerrainGeneratorTypeI getType() {
		return TerrainGeneratorType.CORRUPTED;
	}
}