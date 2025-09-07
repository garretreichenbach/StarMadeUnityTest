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

public class TerrainGeneratorEarth extends TerrainGenerator {
	static final int DIRT_TOP = registerBlock(ElementKeyMap.TERRAIN_EARTH_TOP_DIRT);
	static final int DIRT = registerBlock(ElementKeyMap.TERRAIN_DIRT_ID);
	static final int ROCK = registerBlock(ElementKeyMap.TERRAIN_ROCK_NORMAL);
	static final int ROCK_TOP = registerBlock(ElementKeyMap.TERRAIN_EARTH_TOP_ROCK);
	static final int WATER = registerBlock(ElementKeyMap.TERRAIN_WATER);
	static final int LAVA = registerBlock(ElementKeyMap.TERRAIN_LAVA_ID);
	static final int BLACK_ROCK = registerBlock(ElementKeyMap.TERRAIN_ROCK_BLACK);
	static final short BIOME_HILLS = 0;
	static final short BIOME_PLAINS = 1;
	static final short BIOME_FOREST = 2;
	static final short BIOME_OCEAN = 3;
	static final short BIOME_LAVA = 4;
	static final short BIOME_SET_PIECE = 5;
	private final FastNoiseSIMD mainFNS;
	private final FastNoiseSIMD intensityFNS;
	private final FastNoise biomePerturbNoiseFN;
	private final FastNoise surfaceDirtFN;
	private final FastNoise treesFN;
	private final FastNoise lavaPoolFN;

	public TerrainGeneratorEarth(int seed, float planetRadius) {
		super(seed, planetRadius);
		mainFNS = new FastNoiseSIMD(seed);
		mainFNS.SetFrequency(0.003f);
		mainFNS.SetNoiseType(FastNoiseSIMD.NoiseType.SimplexFractal);
		mainFNS.SetFractalType(FastNoiseSIMD.FractalType.RigidMulti);
		mainFNS.SetFractalLacunarity(2.0f);
		mainFNS.SetFractalGain(0.7f);
		mainFNS.SetFractalOctaves(3);
		mainFNS.SetPerturbType(FastNoiseSIMD.PerturbType.Gradient);
		mainFNS.SetPerturbAmp(2.0f);
		mainFNS.SetPerturbFrequency(0.5f);
		intensityFNS = new FastNoiseSIMD(seed);
		intensityFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Cellular);
		intensityFNS.SetCellularReturnType(FastNoiseSIMD.CellularReturnType.Distance2Mul);
		intensityFNS.SetFrequency(0.005f);
		intensityFNS.SetPerturbType(FastNoiseSIMD.PerturbType.Normalise);
		intensityFNS.SetPerturbNormaliseLength(planetRadius);
		biomePerturbNoiseFN = new FastNoise(seed);
		biomePerturbNoiseFN.SetGradientPerturbAmp(80.0f);
		surfaceDirtFN = new FastNoise(seed);
		surfaceDirtFN.SetFrequency(0.03f);
		surfaceDirtFN.SetNoiseType(FastNoise.NoiseType.PerlinFractal);
		surfaceDirtFN.SetFractalOctaves(2);
		surfaceDirtFN.SetFractalGain(0.65f);
		treesFN = new FastNoise(seed);
		treesFN.SetFrequency(0.005f);
		treesFN.SetNoiseType(FastNoise.NoiseType.Cellular);
		treesFN.SetCellularReturnType(FastNoise.CellularReturnType.Distance2Mul);
		lavaPoolFN = new FastNoise(seed);
		lavaPoolFN.SetFrequency(0.01f);
		lavaPoolFN.SetNoiseType(FastNoise.NoiseType.Simplex);
		addSetPieceBiomes(seed, BIOME_SET_PIECE, 0.1f);
		addBiomesRandom(35000.0f * biomeSizeModifier, new Biome(BIOME_HILLS), new Biome(BIOME_PLAINS), new Biome(BIOME_FOREST), new Biome(BIOME_OCEAN), new Biome(BIOME_LAVA, 0.5f));
	}

	@Override
	public int isSolidTerrain(int x, int y, int z) {
		float minDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);
		minDensity -= hillsTerrainIntensity * hillsTerrainIntensityCap;
		if(minDensity > 0.0f) return ROCK;
		return -1;
	}

	@Override
	public boolean isEmptyTerrain(int x, int y, int z) {
		float maxDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);
		maxDensity += hillsTerrainIntensity * hillsTerrainIntensityCap;
		return maxDensity <= 0.0f;
	}

	private static final float riverWidth = 5.0f;
	private static final float riverDepth = 8.0f;
	private static final float riverBedIntensity = 3.5f;
	private static final float riverBedFade = 1.1f;
	private static final float riverSurfaceIntensity = 1.5f;
	private static final float riverTerrainIntensityFade = 0.05f;
	private static final float riverAreaSinkAmount = 5.0f;
	private static final float hillsTerrainIntensity = 200.0f;
	private static final float hillsTerrainIntensityCap = 0.35f;
	private static final float plainsForrestTerrainIntensity = 7.0f;
	private static final float lavaNoiseRatio = -0.4f;
	private static final float oceanFloorIntensity = 8.0f;
	private static final float oceanFloorOffset = 6.0f;
	private static final float oceanFloorCenterDepth = 0.5f;

	@Override
	public TerrainStructureList generateSegment(Segment w, RequestData requestData) throws SegmentDataWriteException {
		//long start,end;
		//start = System.nanoTime();
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
		boolean setPiecePlaced = false;
		int index = 0;
		float[] terrainNoise = rd.noiseSet0;
		float[] intensityNoise = null;
		float[] caveNoise = null;
		sideTransform.transform(blockPos);
		float noiseX = blockPos.x;
		float noiseY = blockPos.y;
		float noiseZ = blockPos.z;
		mainFNS.FillSampledNoiseSet(terrainNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
		Vector3f up = new Vector3f(0, 1, 0);
		for(byte x = 0; x < SegmentData.SEG; x++) {
			for(byte z = 0; z < SegmentData.SEG; z++) {
				boolean airAbove = false;
				for(byte y = SegmentData.SEG; y >= 0; y--) {
					blockPos.set(x + w.pos.x - SegmentData.SEG_HALF, y + w.pos.y - SegmentData.SEG_HALF, z + w.pos.z - SegmentData.SEG_HALF);
					//float density = planetRadius - blockPos.y;
					if(!allInSide && !IcosahedronHelper.isPointInSide(blockPos)) {
						tcce.outOfSide();
						index += y + 1;
						break;
					}
					sideTransform.transform(blockPos);
					float density = planetRadius - FastMath.carmackLength(blockPos);
					float planetMag = density;
					float terrainIntensity = 0, mainTerrain;
					biomePerturbNoiseFN.GradientPerturb(blockPos);
					getBiome(bbd, blockPos);
					float riverProximity = clamp01((bbd.edgeDistance - (riverWidth + 1)) * riverTerrainIntensityFade);
					density += riverProximity * riverAreaSinkAmount;
					switch(bbd.id) {
						case BIOME_HILLS -> {
							if(intensityNoise == null) {
								intensityNoise = rd.noiseSet1;
								intensityFNS.FillSampledNoiseSet(intensityNoise, IcosahedronHelper.getVectorSet(side, 3), noiseX, noiseY, noiseZ);
							}
							terrainIntensity = Math.min(intensityNoise[index], hillsTerrainIntensityCap);
							terrainIntensity = lerp(riverSurfaceIntensity / hillsTerrainIntensity, terrainIntensity, riverProximity);
							mainTerrain = terrainNoise[index] * hillsTerrainIntensity * terrainIntensity;
							density -= mainTerrain;
							if(density > 0.0f && (bbd.edgeDistance > riverWidth * 2 || density >= riverDepth * 2 - Math.max(Math.min(bbd.edgeDistance, 35.0f) + 0.5f + terrainNoise[index] * riverBedIntensity, 0.0f) * riverBedFade)) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_LAVA -> {
							mainTerrain = terrainNoise[index] * lerp(riverSurfaceIntensity, plainsForrestTerrainIntensity, riverProximity);
							density -= mainTerrain;
							if(density < 5.0f && density > -2.6f) {
								terrainIntensity = Math.min((riverProximity + 0.24f) * 8.0f, Math.max(0.0f, (lavaPoolFN.GetNoise(blockPos.x, blockPos.y, blockPos.z) + lavaNoiseRatio + bbd.edgeDistance * 0.001f) * 20.0f));
								if(terrainIntensity > 2.5f && density < 3.5f) terrainIntensity = 0.5f;
								density += terrainIntensity;// * terrainIntensity;
							}
							if(terrainIntensity <= 0.0f && density > 0.0f && (bbd.edgeDistance > riverWidth * 2 || density >= riverDepth * 2 - Math.max(Math.min(bbd.edgeDistance, 35.0f) + 0.5f + terrainNoise[index] * riverBedIntensity, 0.0f) * riverBedFade)) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_PLAINS -> {
							mainTerrain = terrainNoise[index] * lerp(riverSurfaceIntensity, plainsForrestTerrainIntensity, riverProximity);
							density -= mainTerrain;
							if(density > 0.0f && (bbd.edgeDistance > riverWidth * 2 || density >= riverDepth * 2 - Math.max(Math.min(bbd.edgeDistance, 35.0f) + 0.5f + terrainNoise[index] * riverBedIntensity, 0.0f) * riverBedFade)) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_FOREST, BIOME_SET_PIECE -> {
							mainTerrain = terrainNoise[index] * lerp(riverSurfaceIntensity, plainsForrestTerrainIntensity, riverProximity);
							density -= mainTerrain;
							if(density >= riverDepth * 2) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_OCEAN -> {
							mainTerrain = terrainNoise[index] * riverSurfaceIntensity * (1.0f - riverProximity);
							density -= mainTerrain;
							density -= riverProximity * riverAreaSinkAmount;
							if(density > 0.0f) {
								terrainIntensity = density - lerp(riverDepth - Math.max(0.5f + terrainNoise[index] * riverBedIntensity, 0.0f) * riverBedFade, (bbd.edgeDistance * oceanFloorCenterDepth) + oceanFloorOffset + (terrainNoise[index] * oceanFloorIntensity), //Ocean floor
									riverProximity);
								if(terrainIntensity > riverDepth) {
									if(caveNoise == null) {
										caveNoise = rd.noiseSet2;
										caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
									}
									if(caveNoise[index] > caveRatio) density = -1.0f;
								}
							}
						}
					}
					// This is the extra block at the top of the chunk to check air, not actually placed.
					if(y == Segment.DIM) {
						airAbove = density <= 0.0f;
						index++;
						continue;
					}

					PlanetIco planet = (PlanetIco) w.getSegmentController();
					if(planet.isInPlanetCore((int) blockPos.x, (int) blockPos.y, (int) blockPos.z)) {
						tcce.placeAir();
						index++;
						continue;
					}

					if(density > 0.0f) {
						//Ocean
						if(bbd.id == BIOME_OCEAN) {
							if(terrainIntensity > 0.0f) tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
							else if(!airAbove) tcce.placeBlock(WATER, x, y, z, segData);
							else tcce.placeAir();
						} else if(bbd.edgeDistance <= riverWidth && density < riverDepth - Math.max(Math.min(bbd.edgeDistance, 35.0f) + 0.5f + terrainNoise[index] * riverBedIntensity, 0.0f) * riverBedFade) {
							//Rivers
							if(!airAbove) tcce.placeBlock(WATER, x, y, z, segData);
							else tcce.placeAir();
						} else {
							int xPos = x + w.pos.x - SegmentData.SEG_HALF;
							int zPos = z + w.pos.z - SegmentData.SEG_HALF;
							if(airAbove && density < riverDepth * 2) switch(bbd.id) {
								case BIOME_HILLS:
									float surfaceDirt = surfaceDirtFN.GetNoise(blockPos.x, blockPos.y, blockPos.z);
									if(density > riverDepth * 1.6f) tcce.placeBlock(DIRT, x, y, z, segData);
									else if(surfaceDirt < 0.3f) {
										tcce.placeBlock(DIRT_TOP, x, y, z, segData);
										if(r.nextFloat() < (0.012f - terrainIntensity * 0.1f)) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.TreeEarthSmall);
										else if(r.nextFloat() < 0.01f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_GRASS_LONG_SPRITE);
									} else if(density < (2.5f - surfaceDirt * surfaceDirt * 10.0f)) {
										tcce.placeBlock(DIRT, x, y, z, segData);
										if(r.nextFloat() < 0.01f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_ROCK_SPRITE);
									} else {
										tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
										if(r.nextFloat() < 0.03f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_ROCK_SPRITE);
									}
									if(r.nextFloat() < (0.0003f - terrainIntensity * 0.001f)) {
										tsl = TerrainStructureRock.add(r, tsl, x, y, z, ElementKeyMap.TERRAIN_ROCK_NORMAL, 2.0f, 6.0f);
									}
									break;
								case BIOME_SET_PIECE:
									tcce.placeBlock(DIRT_TOP, x, y, z, segData);
									if(xPos == 0 && zPos == 0) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.BP_TownTest);
									else if(xPos * xPos + zPos * zPos > 2178) { // 2178 = (33^2)*2
										if(r.nextFloat() < 0.0015f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.TreeEarthSmall, TerrainStructure.random(r));
										else if(r.nextFloat() < 0.01f) {
											if(r.nextFloat() < 0.8f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_GRASS_LONG_SPRITE);
											else tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_BERRY_BUSH_SPRITE);
										}
									}
									break;
								case BIOME_PLAINS:
									if(density > riverDepth * 1.6f) tcce.placeBlock(DIRT, x, y, z, segData);
									else {
										tcce.placeBlock(DIRT_TOP, x, y, z, segData);
										float treeNoise = treesFN.GetNoise(blockPos.x, blockPos.y, blockPos.z) + 1.0f;
										if(xPos == 0 && zPos == 0) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.BP_TownTest);
										else if(r.nextFloat() < 0.0035f * treeNoise) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.TreeEarthSmall);
										else if(r.nextFloat() < 0.04f * treeNoise) {
											if(r.nextFloat() < 0.8f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_GRASS_LONG_SPRITE);
											else tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_BERRY_BUSH_SPRITE);
										}
									}
									break;
								case BIOME_FOREST:
									tcce.placeBlock(DIRT_TOP, x, y, z, segData);
									float treeNoise2 = treesFN.GetNoise(blockPos.x, blockPos.y, blockPos.z);
									if(r.nextFloat() < Math.min(0.006f, 0.004f * (1.0f - treeNoise2))) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.TreeEarthForest);
									else if(r.nextFloat() < Math.min(0.25f, 0.075f * (treeNoise2 + 1.0f))) {
										if(r.nextFloat() < 0.005f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.TreeEarthSmall, TerrainStructure.random(r)); //Todo: Generate more trees in forests
										else if(r.nextFloat() < 0.01f) {
											if(r.nextFloat() < 0.8f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_GRASS_LONG_SPRITE);
											else tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_BERRY_BUSH_SPRITE);
										} else {
											if(r.nextBoolean()) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_FLOWERS_BLUE_SPRITE);
											else tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_FLOWERS_YELLOW_SPRITE);
										}
									}
									break;
								case BIOME_LAVA:
									if(terrainIntensity > 0.0f) {
										if(terrainIntensity == 0.5f) tcce.placeBlock(LAVA, x, y, z, segData);
										else tcce.placeBlock(BLACK_ROCK, x, y, z, segData);
									} else {
										if(riverProximity < -(lavaPoolFN.GetNoise(blockPos.x, blockPos.y, blockPos.z) + lavaNoiseRatio) * 0.75f) {
											tcce.placeBlock(DIRT_TOP, x, y, z, segData);
											if(r.nextFloat() < 0.03f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_GRASS_LONG_SPRITE);
										} else {
											tcce.placeBlock(DIRT, x, y, z, segData);
											if(r.nextFloat() < (0.0003f)) {
												tsl = TerrainStructureRock.add(r, tsl, x, y, z, ElementKeyMap.TERRAIN_ROCK_NORMAL, 2.0f, 6.0f);
											} else if(r.nextFloat() < (0.0015f)) {
												tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_ROCK_SPRITE);
											} else if(r.nextFloat() < (0.001f)) {
												if(r.nextFloat() < (0.8f)) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.TreeDead);
												else tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.TreeDeadLarge);
											}
										}
									}
									break;
							} else switch(bbd.id) {
								case BIOME_LAVA:
									if(terrainIntensity > 0.0f) {
										if(terrainIntensity == 0.5f) tcce.placeBlock(LAVA, x, y, z, segData);
										else tcce.placeBlock(BLACK_ROCK, x, y, z, segData);
										break;
									}
								default:
									if(density < 4.0f) tcce.placeBlock(DIRT, x, y, z, segData);
									else tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
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
		//end = System.nanoTime();
		//System.out.println("EARTHCHUNK - " + (end - start));
		segData.getSegmentController().getSegmentBuffer().updateBB(segData.getSegment());
		return tsl;
	}

	static float InterpHermiteFunc(float t) {
		return (t * t * (3 - 2 * t));
	}

	static float InterpQuinticFunc(float t) {
		return t * t * t * (t * (t * 6 - 15) + 10);
	}

	static float InterpSq(float t) {
		float f = t * 2.0f - 1.0f;
		f *= FastMath.abs(f);
		return (f + 1.0f) * 0.5f;
	}

	@Override
	public void generateLOD() {
	}

	@Override
	public TerrainGeneratorTypeI getType() {
		return TerrainGeneratorType.EARTH;
	}
}