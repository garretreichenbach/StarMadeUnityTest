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

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class TerrainGeneratorMesa extends TerrainGenerator {

	static final short BIOME_SET_PIECE = 0;
	static final short BIOME_DUNES = 1;
	static final short BIOME_CAVERNS = 2;
	static final short BIOME_HILLS = 3;

	static final int[] DAM_ROCK = registerBlockDamaged(ElementKeyMap.TERRAIN_ROCK_MARS);
	static final int ROCK = registerBlock(ElementKeyMap.TERRAIN_ROCK_MARS);
	static final int ROCK_YELLOW = registerBlock(ElementKeyMap.TERRAIN_ROCK_YELLOW);
	static final int ROCK_ORANGE = registerBlock(ElementKeyMap.TERRAIN_ROCK_ORANGE);
	static final int DIRT = registerBlock(ElementKeyMap.TERRAIN_MARS_DIRT);
	static final int DIRT_TOP = registerBlock(ElementKeyMap.TERRAIN_MARS_DIRT);

	private final FastNoiseSIMD cellularFNS;
	private final FastNoiseSIMD intensityFNS;
	private final FastNoiseSIMD simplexFNS;
	private final FastNoise biomePerturbNoiseFN;
	private final FastNoise surfaceDirtFN;
	private final FastNoise layersFN;
	private final FastNoise damageFN;

	private static final float biomePerturb = 80.0f;
	private static final float biomeSeparatorWidth = 50.0f;
	private static final float biomeSeparatorIntensityFade = 0.05f;
	private static final float cavernDensity = 16.0f;
	private static final float layersAmp = 50.0f;
	private static final float layersDepth = 20.0f;
	private static final float hillsTerrainIntensity = 200.0f;
	private static final float hillsTerrainIntensityCap = 0.35f;

	public TerrainGeneratorMesa(int seed, float planetRadius) {
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

		intensityFNS = new FastNoiseSIMD(seed);
		intensityFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Cellular);
		intensityFNS.SetCellularReturnType(FastNoiseSIMD.CellularReturnType.Distance2Mul);
		intensityFNS.SetFrequency(0.005f);
		intensityFNS.SetPerturbType(FastNoiseSIMD.PerturbType.Normalise);
		intensityFNS.SetPerturbNormaliseLength(planetRadius);

		surfaceDirtFN = new FastNoise(seed);
		surfaceDirtFN.SetFrequency(0.03f);
		surfaceDirtFN.SetNoiseType(FastNoise.NoiseType.PerlinFractal);
		surfaceDirtFN.SetFractalOctaves(2);
		surfaceDirtFN.SetFractalGain(0.65f);

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
		addBiomesRandom(50000.0f * biomeSizeModifier, new Biome(BIOME_DUNES, 1.0f), new Biome(BIOME_CAVERNS, 0.7f), new Biome(BIOME_HILLS, 0.5f));
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

	@Override
	public TerrainGeneratorTypeI getType() {
		return TerrainGeneratorType.MESA;
	}

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
		float[] intensityNoise = null;
		float[] terrainNoise = rd.noiseSet0;

		sideTransform.transform(blockPos);
		float noiseX = blockPos.x;
		float noiseY = blockPos.y;
		float noiseZ = blockPos.z;
		cellularFNS.FillSampledNoiseSet(cellularNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
		simplexFNS.FillSampledNoiseSet(simplexNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);

		for(byte x = 0; x < SegmentData.SEG; x++) {
			for(byte z = 0; z < SegmentData.SEG; z++) {
				boolean airAbove = false;
				for(byte y = SegmentData.SEG; y >= 0; y--) {
					blockPos.set(x + w.pos.x - SegmentData.SEG_HALF, y + w.pos.y - SegmentData.SEG_HALF, z + w.pos.z - SegmentData.SEG_HALF);
					if(!allInSide && !IcosahedronHelper.isPointInSide(blockPos)) {
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
					if(bbd.edgeDistance <= biomeSeparatorWidth) bbd.id = BIOME_DUNES;
					switch(bbd.id) {
						case BIOME_HILLS -> {
							if(intensityNoise == null) {
								intensityNoise = rd.noiseSet1;
								intensityFNS.FillSampledNoiseSet(intensityNoise, IcosahedronHelper.getVectorSet(side, 3), noiseX, noiseY, noiseZ);
							}
							terrainIntensity = Math.min(intensityNoise[index], hillsTerrainIntensityCap);
							mainTerrain = terrainNoise[index] * hillsTerrainIntensity * terrainIntensity;
							density -= mainTerrain;
							if(caveNoise == null) {
								caveNoise = rd.noiseSet2;
								caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
							}
							if(caveNoise[index] > caveRatio) density = -1.0f;
						}
						case BIOME_DUNES -> {//min 40 max 10
							mainTerrain = Math.min(cellularNoise[index], (0.45f + simplexNoise[index] * 0.05f));
							mainTerrain *= mainTerrain;
							mainTerrain -= 0.2f;
							mainTerrain *= 200.0f;
							density += mainTerrain;
							if(density > layersDepth) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_SET_PIECE -> {
							density += (cellularNoise[index] - 0.5f) * 10.0f;
							if(density > layersDepth) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_CAVERNS -> {//min 40 max 20
							if(baseDensity > 15.0f) {
								float dunes = Math.min(cellularNoise[index], (0.45f + simplexNoise[index] * 0.05f));
								dunes *= dunes;
								dunes -= 0.2f;
								density -= lerp(dunes * -200.0f, simplexNoise[index] * 20.0f, separatorDistance);
							} else density -= lerp(1.0f, simplexNoise[index], separatorDistance) * 20.0f;
							density -= Math.max(0.0f, baseDensity - Math.abs(baseDensity - cavernDensity) + (20.0f - cavernDensity)) * separatorDistance;
							if(density > 0.0f) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
					}

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
						if(airAbove && density < layersDepth) {
							switch(bbd.id) {
								case BIOME_HILLS:
									float surfaceDirt = surfaceDirtFN.GetNoise(blockPos.x, blockPos.y, blockPos.z);
									if(surfaceDirt < 0.3f) {
										tcce.placeBlock(DIRT_TOP, x, y, z, segData);
										if(r.nextFloat() < (0.012f - terrainIntensity * 0.1f))  tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_FUNGAL_TRAP_SPRITE);
										else if(r.nextFloat() < 0.01f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_FUNGAL_GROWTH_SPRITE);
									} else if(density < (2.5f - surfaceDirt * surfaceDirt * 10.0f)) {
										tcce.placeBlock(DIRT, x, y, z, segData);
										if(r.nextFloat() < 0.01f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CORAL_RED_SPRITE);
									} else {
										tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
										if(r.nextFloat() < 0.03f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_SHROOM_RED_SPRITE);
									}
									if(r.nextFloat() < (0.0003f - terrainIntensity * 0.001f)) {
										tsl = TerrainStructureRock.add(r, tsl, x, y, z, ElementKeyMap.TERRAIN_ROCK_NORMAL, 2.0f, 6.0f);
									}
									break;
								case BIOME_DUNES:
									float cactusNoise = cellularNoise[index];
									if(r.nextFloat() < 0.002f - cactusNoise * 0.004f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.Cactus, TerrainStructure.random(r));
									else if(r.nextFloat() < 0.005f - cactusNoise * 0.004f) {
										if(r.nextBoolean()) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_SMALL_SPRITE);
										else tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE);
									}
									tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);
									break;
								case BIOME_SET_PIECE:
									int xPos = x + w.pos.x - SegmentData.SEG_HALF;
									int zPos = z + w.pos.z - SegmentData.SEG_HALF;
									if(xPos == 0 && zPos == 0) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.BP_TownTest);
									else if(xPos * xPos + zPos * zPos > 2178) {// 2178 = (33^2)*2
										if(r.nextFloat() < 0.0015f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.Cactus, TerrainStructure.random(r));
										else if(r.nextFloat() < 0.003f) {
											if(r.nextBoolean()) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_SMALL_SPRITE);
											else tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE);
										}
									}
									tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);
									break;
								case BIOME_CAVERNS:
									if(baseDensity > cavernDensity) {
										tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);
										if(r.nextFloat() < 0.002f) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.Cactus, TerrainStructure.random(r));
										else if(r.nextFloat() < 0.003f) {
											if(r.nextBoolean()) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_FLOWERS_DESERT_SPRITE);
											else tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE);
										}
									} else {
										float damagedRockNoise = damageFN.GetNoise(baseX, baseY, baseZ);
										tsl = placeSolidAndResources(damagedRock(damagedRockNoise, density), r, tsl, tcce, x, y, z, segData);
										if(r.nextFloat() < 0.02f + 0.02f * damagedRockNoise) tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_ROCK_SPRITE);
									}
									break;
							}
						} else {
							switch(bbd.id) {
								case BIOME_CAVERNS:
									if(density < layersDepth) {
										if(baseDensity > cavernDensity) tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);
										else tsl = placeSolidAndResources(damagedRock(damageFN.GetNoise(baseX, baseY, baseZ), density), r, tsl, tcce, x, y, z, segData);
									} else tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
									break;
								case BIOME_HILLS:
								default:
									if(density < layersDepth) tcce.placeBlock(layerSand(magnitude + layersFN.GetNoise(baseX, baseY, baseZ) * layersAmp), x, y, z, segData);
									else tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
							}
							airAbove = false;
							index++;
						}
					} else {
						airAbove = true;
						tcce.placeAir();
					}
				}
			}
		}
		return tsl;
	}

	@Override
	public void generateLOD() {

	}

	private int layerSand(float mag) {
		return switch(FastMath.fastFloor(mag * 0.25f) & 15) {
			case 0, 1, 2, 7, 8 -> ROCK_YELLOW;
			case 3, 4, 5, 6 -> ROCK_ORANGE;
			default -> DIRT;
		};
	}

	private int damagedRock(float noise, float density) {
		int i = FastMath.fastFloor(noise * -24 + density * (layersDepth / 8.0f));
		i = Math.max(7 - i, 0);
		return DAM_ROCK[i];
	}
}