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

public class TerrainGeneratorFrozen extends TerrainGenerator {
	final static int ROCK = registerBlock(ElementKeyMap.TERRAIN_ICEPLANET_ROCK);
	final static int ROCK_TOP = registerBlock(ElementKeyMap.TERRAIN_ICEPLANET_SURFACE);
	final static int WATER = registerBlock(ElementKeyMap.WATER);
	final static int CRYSTAL = registerBlock(ElementKeyMap.TERRAIN_ICEPLANET_CRYSTAL);
	final static int ICE = registerBlock(ElementKeyMap.TERRAIN_ICE_ID);

	final static short BIOME_TUNDRA = 0;
	final static short BIOME_JAGGED = 1;
	final static short BIOME_ICE_WATER = 2;

	private FastNoiseSIMD mainFNS;
	private FastNoise biomePerturbNoiseFN;

	public TerrainGeneratorFrozen(int seed, float planetRadius) {
		super(seed, planetRadius);

		mainFNS = new FastNoiseSIMD(seed);
		mainFNS.SetFrequency(0.005f);
		mainFNS.SetNoiseType(FastNoiseSIMD.NoiseType.SimplexFractal);
		mainFNS.SetFractalType(FastNoiseSIMD.FractalType.RigidMulti);
		mainFNS.SetFractalLacunarity(2f);
		mainFNS.SetFractalGain(0.7f);
		mainFNS.SetFractalOctaves(3);
		mainFNS.SetPerturbType(FastNoiseSIMD.PerturbType.Gradient);
		mainFNS.SetPerturbAmp(2.0f);
		mainFNS.SetPerturbFrequency(0.5f);
		biomePerturbNoiseFN = new FastNoise(seed);
		biomePerturbNoiseFN.SetGradientPerturbAmp(80.f);

		addBiomesRandom(25000f * biomeSizeModifier,
						new Biome(BIOME_TUNDRA),
						new Biome(BIOME_JAGGED),
						new Biome(BIOME_ICE_WATER)
					   );
	}

	@Override
	public int isSolidTerrain(int x, int y, int z) {
		float minDensity = planetRadius - FastMath.carmackSqrt(x*x+y*y+z*z);

		minDensity -= jaggedAmplitude;

		if (minDensity > 0f)
			return ROCK;

		return -1;
	}
	@Override
	public boolean isEmptyTerrain(int x, int y, int z) {
		float maxDensity = planetRadius - FastMath.carmackSqrt(x*x+y*y+z*z);

		maxDensity += jaggedAmplitude;

		return maxDensity <= 0.0f;
	}

	private static final float biomeSeparatorWidth = 5f;
	private static final float biomeSeparatorIntensity = 6f;
	private static final float biomeSeparatorIntensityFade = 0.05f;

	private static final float jaggedAmplitude = 30f;
	private static final float iceWaterAmplitude = 1.5f;
	private static final float iceWaterRatio = 0.3f;
	private static final float iceWaterDepth = 30f;

	private static final float lowestStructure = 15f;


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

		float[] terrainNoise = rd.noiseSet0;
		float[] caveNoise = null;

		sideTransform.transform(blockPos);
		float noiseX = blockPos.x;
		float noiseY = blockPos.y;
		float noiseZ = blockPos.z;
		mainFNS.FillSampledNoiseSet(terrainNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);

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

					biomePerturbNoiseFN.GradientPerturb(blockPos);
					getBiome(bbd, blockPos);

					// Biome Edge Distance 0f-1f (near-far)
					float separatorDistance = clamp01((bbd.edgeDistance - (biomeSeparatorWidth + 1)) * biomeSeparatorIntensityFade);

					// Per biome terrain density
					switch(bbd.id) {
						case BIOME_TUNDRA -> {
							mainTerrain = terrainNoise[index] * biomeSeparatorIntensity;
							density += mainTerrain;
							if(density > 0f) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_JAGGED -> {
							mainTerrain = lerp(terrainNoise[index] * -biomeSeparatorIntensity, terrainNoise[index] * jaggedAmplitude, separatorDistance);
							density -= mainTerrain;
							if(density > 0f) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) density = -1.0f;
							}
						}
						case BIOME_ICE_WATER -> {
							mainTerrain = terrainNoise[index] * lerp(biomeSeparatorIntensity, iceWaterAmplitude, separatorDistance);
							density += mainTerrain;
							if(density > 30f * Math.min(bbd.edgeDistance * 0.02f, 1f)) {
								if(caveNoise == null) {
									caveNoise = rd.noiseSet2;
									caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), noiseX, noiseY, noiseZ);
								}
								if(caveNoise[index] > caveRatio) {
									if(density - 1.5f <= iceWaterDepth * Math.min(bbd.edgeDistance * 0.02f, 1f)) density = Float.MAX_VALUE;
									else density = -1.0f;
								}
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
						// This is a solid block with air directly above
						if (airAbove && density < lowestStructure) switch (bbd.id) {
							case BIOME_TUNDRA:
									if (r.nextFloat() < 0.0003f)
										tsl = TerrainStructureRock.add(r, tsl, x, (short)(y-2), z, ElementKeyMap.TERRAIN_ICEPLANET_ROCK, 4f, 8f);

									else if (r.nextFloat() < 0.0003f) {
										short width = (short) (r.nextInt(6) + 3);
										float ratio = r.nextFloat()*1.5f + 4;
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.IceShard, width, (short)FastMath.round(width*ratio), TerrainStructure.random(r));

									}else if (r.nextFloat() < 0.004f) {
										if (r.nextBoolean())
											tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CORAL_ICE_SPRITE);
										else
											tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_FAN_FLOWER_ICE_SPRITE);
									}

								tcce.placeBlock(ROCK_TOP, x, y, z, segData);
								break;

							case BIOME_JAGGED:
								if (r.nextFloat() < 0.0005f)
									tsl = TerrainStructureRock.add(r, tsl, x, (short)(y-1), z, ElementKeyMap.TERRAIN_ICEPLANET_ROCK, 3f, 5f);

								else if (r.nextFloat() < 0.004f) {
									if (r.nextBoolean())
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_CORAL_ICE_SPRITE);
									else
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_SNOW_BUD_SPRITE);
								}

								tcce.placeBlock(ROCK_TOP, x, y, z, segData);
								break;


							case BIOME_ICE_WATER:

								if (terrainNoise[index] > iceWaterRatio - 1f){
									if (r.nextFloat() < 0.0005f) {
										short width = (short) (r.nextInt(6) + 3);
										float ratio = r.nextFloat()*1.5f + 4;
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.IceShard, width, (short)FastMath.round(width*ratio), TerrainStructure.random(r));
									}
									else if (r.nextFloat() < 0.001f)
										tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.SingleBlock, ElementKeyMap.TERRAIN_ICE_CRAG_SPRITE);

									tcce.placeBlock(ICE, x, y, z, segData);
								}
								else
									tcce.placeAir();

								break;

						}

						// No air above
						else switch (bbd.id) {
							case BIOME_ICE_WATER:

								if (density < 3f * (terrainNoise[index] - (iceWaterRatio - 1f)) || density == Float.MAX_VALUE)
									tcce.placeBlock(ICE, x, y, z, segData);
								else if (density > iceWaterDepth * Math.min(bbd.edgeDistance * 0.02f, 1f))
									tsl = placeSolidAndResources(ROCK, r, tsl, tcce, x, y, z, segData);
								else
									tcce.placeBlock(WATER, x, y, z, segData);


								break;
							default:
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

	@Override
	public void generateLOD() {
		
	}
	@Override
	public TerrainGeneratorTypeI getType() {
		return TerrainGeneratorType.FROZEN;
	}
}