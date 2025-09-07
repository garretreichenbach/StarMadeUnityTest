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

public class TerrainGeneratorDesert extends TerrainGenerator {

	final static int DIRT_TOP = registerBlock(ElementKeyMap.TERRAIN_EARTH_TOP_DIRT);
	final static int DIRT = registerBlock(ElementKeyMap.TERRAIN_DIRT_ID);
	final static int ROCK = registerBlock(ElementKeyMap.TERRAIN_ROCK_NORMAL);
	final static int ROCK_TOP = registerBlock(ElementKeyMap.TERRAIN_EARTH_TOP_ROCK);
	final static int WATER = registerBlock(ElementKeyMap.TERRAIN_WATER);
	final static int LAVA = registerBlock(ElementKeyMap.TERRAIN_LAVA_ID);
	final static int SAND = registerBlock(ElementKeyMap.TERRAIN_SAND_ID);
	final static int ROCK_BLACK = registerBlock(ElementKeyMap.TERRAIN_ROCK_BLACK);
	final static int ROCK_YELLOW = registerBlock(ElementKeyMap.TERRAIN_ROCK_YELLOW);
	final static int ROCK_ORANGE = registerBlock(ElementKeyMap.TERRAIN_ROCK_ORANGE);

	final static int[] DAM_ROCK = registerBlockDamaged(ElementKeyMap.TERRAIN_ROCK_NORMAL);

	final static short BIOME_DUNES = 0;
	final static short BIOME_LAVA = 1;
	final static short BIOME_BLACK_ROCK = 2;
	final static short BIOME_SET_PIECE = 3;
	final static short BIOME_CAVERNS = 4;

	private FastNoiseSIMD cellularFNS;
	private FastNoiseSIMD simplexFNS;
	private FastNoise biomePerturbNoiseFN;
	private FastNoise lavaPoolFN;
	private FastNoise layersFN;
	private FastNoise damageFN;

	public TerrainGeneratorDesert(int seed, float planetRadius) {
		super(seed, planetRadius);

		cellularFNS = new FastNoiseSIMD(seed);
		cellularFNS.SetFrequency(0.0175f);
		cellularFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Cellular);
		cellularFNS.SetCellularReturnType(FastNoiseSIMD.CellularReturnType.Distance2Mul);
		cellularFNS.SetCellularDistanceFunction(FastNoiseSIMD.CellularDistanceFunction.Euclidean);

		cellularFNS.SetPerturbType(FastNoiseSIMD.PerturbType.Normalise);
		cellularFNS.SetPerturbNormaliseLength(planetRadius);

		simplexFNS = new FastNoiseSIMD(seed);
		simplexFNS.SetNoiseType(FastNoiseSIMD.NoiseType.SimplexFractal);
		simplexFNS.SetFrequency(0.01f);
		simplexFNS.SetFractalOctaves(2);

		biomePerturbNoiseFN = new FastNoise(seed);
		biomePerturbNoiseFN.SetGradientPerturbAmp(biomePerturb);

		lavaPoolFN = new FastNoise(seed);
		lavaPoolFN.SetFrequency(0.006f);
		lavaPoolFN.SetNoiseType(FastNoise.NoiseType.Simplex);

		layersFN = new FastNoise(seed);
		layersFN.SetFrequency(0.006f);
		layersFN.SetNoiseType(FastNoise.NoiseType.Value);

		damageFN = new FastNoise(seed);
		damageFN.SetFrequency(0.015f);
		damageFN.SetNoiseType(FastNoise.NoiseType.Cellular);
		damageFN.SetCellularReturnType(FastNoise.CellularReturnType.Distance2Div);
		damageFN.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Natural);

		// Biomes
		addSetPieceBiomes(seed, BIOME_SET_PIECE, 0.1f);

		addBiomesRandom(50000f * biomeSizeModifier,
						new Biome(BIOME_DUNES, 1.0f),
						new Biome(BIOME_LAVA, 0.4f),
						new Biome(BIOME_BLACK_ROCK, 0.7f),
						new Biome(BIOME_CAVERNS, 0.7f)
					   );
	}

	@Override
	public int isSolidTerrain(int x, int y, int z) {
		float minDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);

		minDensity -= 60;

		if (minDensity > 0f)
			return ROCK;

		return -1;
	}

	@Override
	public boolean isEmptyTerrain(int x, int y, int z) {

		float maxDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);

		maxDensity += 25;

		return maxDensity <= 0.0f;
	}

	private static final float biomePerturb = 80f;
	private static final float biomeSeparatorWidth = 50f;
	private static final float biomeSeparatorIntensity = 5f;
	private static final float biomeSeparatorIntensityFade = 0.05f;

	private static final float lavaNoiseRatio = -0.4f;

	private static final float cavernDensity = 16f;

	private static final float layersAmp = 50f;
	private static final float layersDepth = 20f;


	@Override
	public TerrainStructureList generateSegment(Segment w, RequestData requestData) throws SegmentDataWriteException {

		SegmentData segData = w.getSegmentData();
		byte side = ((PlanetIco) w.getSegmentController()).getSideId();
		Matrix3f sideTransform = IcosahedronHelper.getSideTransform(side).basis;
		RequestDataIcoPlanet rd = (RequestDataIcoPlanet) requestData;
		TerrainChunkCacheElement tcce = rd.currentChunkCache;
		Vector3f blockPos = rd.vector3f;
		BlockBiomeData bbd = rd.blockBiomeData;
		Random r = rd.random;

		blockPos.set(w.pos.x, w.pos.y, w.pos.z);
		r.setSeed(blockPos.hashCode() + seed);

		TerrainStructureList tsl = null;

		boolean allInSide = IcosahedronHelper.isSegmentAllInSide(w.pos);
		int index = 0;

		float[] cellularNoise = rd.noiseSet0;
		float[] simplexNoise = rd.noiseSet1;
		float[] caveNoise = null;

		sideTransform.transform(blockPos);
		float noiseX = blockPos.x;
		float noiseY = blockPos.y;
		float noiseZ = blockPos.z;
		cellularFNS.FillSampledNoiseSet(cellularNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
		simplexFNS.FillSampledNoiseSet(simplexNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);

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

					float magnitude = FastMath.carmackLength(blockPos);
					float baseDensity = planetRadius - magnitude;
					float density = baseDensity;
					float mainTerrain, terrainIntensity = 0;

					float baseX = blockPos.x;
					float baseY = blockPos.y;
					float baseZ = blockPos.z;

					biomePerturbNoiseFN.GradientPerturb(blockPos);
					getBiome(bbd, blockPos);

					// Biome Edge Distance 0f-1f (near-far)
					float separatorDistance = clamp01((bbd.edgeDistance - biomeSeparatorWidth) * biomeSeparatorIntensityFade);

					if (bbd.edgeDistance <= biomeSeparatorWidth)
						bbd.id = BIOME_DUNES;

					// Per biome terrain density
					switch(bbd.id) {
						case BIOME_DUNES -> {//min 40 max 10
							mainTerrain = Math.min(cellularNoise[index], (.45f + simplexNoise[index] * 0.05f));
							mainTerrain *= mainTerrain;
							mainTerrain -= 0.2f;
							mainTerrain *= 200f;// * Math.max(separatorDistance, 0.3f);
							density += mainTerrain;
							if(density > layersDepth) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_LAVA -> {
							mainTerrain = simplexNoise[index] * 10f;
							density -= mainTerrain;
							if(density < 5.0f && density > -2.6f) {
								terrainIntensity = Math.max(0f, (lavaPoolFN.GetNoise(blockPos.x, blockPos.y, blockPos.z) + lavaNoiseRatio + bbd.edgeDistance * 0.003f) * 15f);
								terrainIntensity = Math.min((separatorDistance + 0.24f) * 8.0f, terrainIntensity);
								if(terrainIntensity > 2.5f) terrainIntensity = 0.5f;
								density += terrainIntensity;// * terrainIntensity;
							}
							if(terrainIntensity <= 0f && density > 0f) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_BLACK_ROCK -> {
							density -= 20f + simplexNoise[index] * biomeSeparatorIntensity;
							terrainIntensity = Math.max(0f, cellularNoise[index] - 0.3f) * 40f;
							density += terrainIntensity;
							if((cellularNoise[index]) < 0.03f * separatorDistance - Math.max(0, density * 0.002)) {
								density += 20f;
								terrainIntensity = -1f;
							} else if(density > 0f) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_SET_PIECE -> {
							density += (cellularNoise[index] - 0.5f) * 10f;
							if(density > layersDepth) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_CAVERNS -> {//min 40 max 20
							if(baseDensity > 15f) {
								float dunes = Math.min(cellularNoise[index], (.45f + simplexNoise[index] * 0.05f));
								dunes *= dunes;
								dunes -= 0.2f;
								density -= lerp(dunes * -200f, simplexNoise[index] * 20f, separatorDistance);
							} else//Top
								density -= lerp(1f, simplexNoise[index], separatorDistance) * 20f;
							density -= Math.max(0f, baseDensity - Math.abs(baseDensity - cavernDensity) + (20f - cavernDensity)) * separatorDistance;
							if(density > 0f) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
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

						/*if (bbd.edgeDistance <= biomeSeparatorWidth
							&& bbd.id != BIOME_BLACK_ROCK && bbd.id2 != BIOME_BLACK_ROCK) {

							if (baseDensity > 20f + simplexNoise[index] * biomeSeparatorIntensity){
								if (y != Segment.DIM)
									tcce.placeBlock(ROCK, x, y, z, segData, this);
								airAbove = false;
							}
							else {
								airAbove = true;
								tcce.placeAir();
							}
							index++;
							continue;
						}*/

						// This is a solid block with air directly above
						if (airAbove && density < layersDepth) switch (bbd.id) {
							case BIOME_DUNES:
								float cactusNoise = cellularNoise[index];
								if (r.nextFloat() < 0.002f - cactusNoise * 0.004f)
									tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.Cactus, TerrainStructure.random(r));

								else if (r.nextFloat() < 0.005f - cactusNoise * 0.004f) {
									if (r.nextBoolean())
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_SMALL_SPRITE);
									else
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE);
								}


								tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);
								break;

							case BIOME_LAVA:
								if (terrainIntensity > 0.0f) {
									if (terrainIntensity == 0.5f)
										tcce.placeBlock(LAVA, x, y, z, segData);
									else
										tcce.placeBlock(ROCK_BLACK, x, y, z, segData);
								} else {
									float damagedRockNoise = damageFN.GetNoise(baseX, baseY, baseZ);

									tsl = placeSolidAndResources(damagedRock(damagedRockNoise, density), r, tsl, tcce, x, y, z, segData);

									if (r.nextFloat() < 0.02f + 0.02f * damagedRockNoise)
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_ROCK_SPRITE);

									else if (r.nextFloat() < 0.001f) {
										tsl = TerrainStructureRock.add(r, tsl, x, (short) (y - 1), z, ElementKeyMap.TERRAIN_ROCK_BLACK, 3f, 7f);
									}
								}
								break;

							case BIOME_BLACK_ROCK:
								if (terrainIntensity > 0f) {
									tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);

									float cactusNoise2 = cellularNoise[index];
									if (r.nextFloat() < 0.003f - cactusNoise2 * 0.004f)
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.Cactus, TerrainStructure.random(r));
									else if (r.nextFloat() < 0.003f - cactusNoise2 * 0.004f) {
										if (r.nextBoolean())
											tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_SMALL_SPRITE);
										else
											tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE);
									}

								} else if (terrainIntensity == -1f)
									tsl = placeSolidAndResources(ROCK_BLACK, r, tsl, tcce, x, y, z, segData);
								else {
									float damagedRockNoise = damageFN.GetNoise(baseX, baseY, baseZ);

									tsl = placeSolidAndResources(damagedRock(damagedRockNoise, density), r, tsl, tcce, x, y, z, segData);

									if (r.nextFloat() < 0.02f + 0.02f * damagedRockNoise)
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_ROCK_SPRITE);

									else if (r.nextFloat() < (-0.006f - (cellularNoise[index]-1f) * 0.01f) * separatorDistance) {
										float size = r.nextFloat() * 4f + 2f - (cellularNoise[index]-1f) * 3f;
										short ore = 0;
										if (r.nextFloat() < rockStructureResourceChance)
											ore = randomStandardResource(r);

										tsl = addStructure(tsl, x, y, z,
														   TerrainStructure.Type.Rock,
														   ElementKeyMap.TERRAIN_ROCK_BLACK,
														   TerrainStructure.toHalfFloat(size),
														   ore);
									}
								}
								break;

							case BIOME_SET_PIECE:

								int xPos = x + w.pos.x - SegmentData.SEG_HALF;
								int zPos = z + w.pos.z - SegmentData.SEG_HALF;

								if (xPos == 0 && zPos == 0)
									tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.BP_TownTest);

								else if (xPos * xPos + zPos * zPos > 2178) {// 2178 = (33^2)*2
									if (r.nextFloat() < 0.0015f)
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.Cactus, TerrainStructure.random(r));

									else if (r.nextFloat() < 0.003f) {
										if (r.nextBoolean())
											tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_SMALL_SPRITE);
										else
											tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE);
									}
								}

								tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);
								break;

							case BIOME_CAVERNS:

								if (baseDensity > cavernDensity) {
									tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);

									if (r.nextFloat() < 0.002f)
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.Cactus, TerrainStructure.random(r));

									else if (r.nextFloat() < 0.003f) {
										if (r.nextBoolean())
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_FLOWERS_DESERT_SPRITE);
									else
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE);
									}
								} else {
									float damagedRockNoise = damageFN.GetNoise(baseX, baseY, baseZ);

									tsl = placeSolidAndResources(damagedRock(damagedRockNoise, density), r, tsl, tcce, x, y, z, segData);

									if (r.nextFloat() < 0.02f + 0.02f * damagedRockNoise)
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_ROCK_SPRITE);
								}
								break;
						}

							// No air above
						else switch (bbd.id) {
							case BIOME_LAVA:
								if (terrainIntensity > 0.0f) {
									if (terrainIntensity == 0.5f)
										tcce.placeBlock(LAVA, x, y, z, segData);
									else
										tcce.placeBlock(ROCK_BLACK, x, y, z, segData);
								} else if (density < layersDepth)
									tsl = placeSolidAndResources(damagedRock(damageFN.GetNoise(baseX, baseY, baseZ), density), r, tsl, tcce, x, y, z, segData);
								else
									tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);

								break;

							case BIOME_BLACK_ROCK:

								if (baseDensity < 30f) {
									if (terrainIntensity > 0f)
										tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);
									else if (terrainIntensity == -1f)
										tsl = placeSolidAndResources(ROCK_BLACK, r, tsl, tcce, x, y, z, segData);
									else
										tsl = placeSolidAndResources(damagedRock(damageFN.GetNoise(baseX, baseY, baseZ), density), r, tsl, tcce, x, y, z, segData);
								} else {
									tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
								}

								break;

							case BIOME_CAVERNS:

								if (density < layersDepth) {
									if (baseDensity > cavernDensity)
										tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);
									else
										tsl = placeSolidAndResources(damagedRock(damageFN.GetNoise(baseX, baseY, baseZ), density), r, tsl, tcce, x, y, z, segData);
								} else
									tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
								break;

							default:
								if (density < layersDepth)
									tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);

								else
									tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
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

	private int layerSand(float mag) {
		return switch(FastMath.fastFloor(mag * 0.25f) & 15) {
			case 0, 1, 2, 7, 8 -> ROCK_YELLOW;
			case 3, 4, 5, 6 -> ROCK_ORANGE;
			default -> SAND;
		};
	}

	private int damagedRock(float noise, float density) {
		int i = FastMath.fastFloor(noise * -24 + density * (layersDepth / 8f));

		i = Math.max(7 - i, 0);

		return DAM_ROCK[i];
	}

	@Override
	public void generateLOD() {
		
	}
	@Override
	public TerrainGeneratorTypeI getType() {
		return TerrainGeneratorType.DESERT;
	}
}