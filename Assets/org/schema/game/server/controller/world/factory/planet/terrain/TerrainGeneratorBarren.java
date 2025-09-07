package org.schema.game.server.controller.world.factory.planet.terrain;

import org.schema.common.FastMath;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.data.IcosahedronHelper;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.RequestDataIcoPlanet;
import org.schema.game.server.controller.TerrainChunkCacheElement;
import org.schema.game.server.controller.world.factory.planet.FastNoise;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureCrater;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.Random;

public class TerrainGeneratorBarren extends TerrainGenerator {

	static final int ROCK = registerBlock(ElementKeyMap.TERRAIN_ROCK_NORMAL);

	static final short BIOME_BARREN = 0;

	private final FastNoiseSIMD mainFNS;
	private final FastNoiseSIMD intensityFNS;
	private final FastNoise biomePerturbNoiseFN;

	private static final float layersDepth = 20.0f;
	private static final float terrainAmplitude = 20.0f;

	public TerrainGeneratorBarren(int seed, float planetRadius) {
		super(seed, planetRadius);

		mainFNS = new FastNoiseSIMD(seed);
		mainFNS.SetFrequency(0.01f);
		mainFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Cellular);
		mainFNS.SetCellularReturnType(FastNoiseSIMD.CellularReturnType.NoiseLookup);

		mainFNS.SetPerturbType(FastNoiseSIMD.PerturbType.Gradient);
		mainFNS.SetPerturbAmp(2.0f);
		mainFNS.SetPerturbFrequency(0.5f);

		mainFNS.SetCellularNoiseLookupType(FastNoiseSIMD.NoiseType.SimplexFractal);
		mainFNS.SetCellularNoiseLookupFrequency(0.1f);
		mainFNS.SetFractalType(FastNoiseSIMD.FractalType.FBM);
		mainFNS.SetFractalLacunarity(2.0f);
		mainFNS.SetFractalGain(0.7f);
		mainFNS.SetFractalOctaves(3);

		intensityFNS = new FastNoiseSIMD(seed);
		intensityFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Cellular);
		intensityFNS.SetCellularReturnType(FastNoiseSIMD.CellularReturnType.Distance2Mul);
		intensityFNS.SetFrequency(0.007f);

		biomePerturbNoiseFN = new FastNoise(seed);
		biomePerturbNoiseFN.SetGradientPerturbAmp(80.0f);

		addBiomesRandom(35000.0f, new Biome(BIOME_BARREN));
	}

	@Override
	public int isSolidTerrain(int x, int y, int z) {
		float minDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);

		minDensity -= terrainAmplitude;

		if(minDensity > 0.0f)
			return ROCK;

		return -1;
	}

	@Override
	public boolean isEmptyTerrain(int x, int y, int z) {
		float maxDensity = planetRadius - FastMath.carmackSqrt(x * x + y * y + z * z);

		maxDensity += terrainAmplitude;

		return maxDensity <= 0.0f;
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
		float[] terrainNoise = rd.noiseSet0;
		float[] intensityNoise = rd.noiseSet1;

		sideTransform.transform(blockPos);
		mainFNS.FillSampledNoiseSet(terrainNoise, IcosahedronHelper.getVectorSet(side, 2), blockPos.x, blockPos.y, blockPos.z);
		intensityFNS.FillSampledNoiseSet(intensityNoise, IcosahedronHelper.getVectorSet(side, 2), blockPos.x, blockPos.y, blockPos.z);

		for(byte x = 0; x < SegmentData.SEG; x++) {
			for(byte z = 0; z < SegmentData.SEG; z++) {
				boolean airAbove = false;
				for(byte y = SegmentData.SEG; y >= 0; y--) {
					blockPos.set(x + w.pos.x - SegmentData.SEG_HALF, y + w.pos.y - SegmentData.SEG_HALF, z + w.pos.z - SegmentData.SEG_HALF);
					if(!allInSide && !IcosahedronHelper.isPointInSide(blockPos)) {
						if(y != Segment.DIM) tcce.placeAir();
						break;
					}

					sideTransform.transform(blockPos);
					float density = planetRadius - FastMath.carmackLength(blockPos);
					biomePerturbNoiseFN.GradientPerturb(blockPos);
					getBiome(bbd, blockPos);

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
//						if(density < layersDepth) {
							if(bbd.id == BIOME_BARREN) {
								float craterNoise = cellularNoise[index];
								if(r.nextFloat() < 0.0005f - craterNoise) tsl = TerrainStructureCrater.add(r, tsl, x, (short) (y - 1), z, Element.TYPE_NONE, 5.0f, 15.0f);
								tcce.placeBlock(ROCK, x, y, z, segData);
								break;
							}
//						} airAbove = false;
					} else {
						airAbove = true;
						tcce.placeAir();
					}
					index++;
				}
			}
		}
		segData.getSegmentController().getSegmentBuffer().updateBB(segData.getSegment());
		return tsl;
	}

	@Override
	public void generateLOD() {

	}

	@Override
	public TerrainGeneratorTypeI getType() {
		return TerrainGeneratorType.BARREN;
	}
}