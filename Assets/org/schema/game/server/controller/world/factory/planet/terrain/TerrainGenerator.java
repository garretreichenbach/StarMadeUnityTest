package org.schema.game.server.controller.world.factory.planet.terrain;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.schema.common.FastMath;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.data.IcosahedronHelper;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.*;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public abstract class TerrainGenerator {

	public static short[] standardResources = {
			ElementKeyMap.RESS_ORE_THRENS,
			ElementKeyMap.RESS_ORE_JISPER,
			ElementKeyMap.RESS_GAS_ZERCANER,
			ElementKeyMap.RESS_ORE_SERTISE,
			ElementKeyMap.RESS_ORE_HYLAT,
			ElementKeyMap.RESS_ORE_FERTIKEEN,
			ElementKeyMap.RESS_ORE_SAPSUN,
			ElementKeyMap.RESS_ORE_METAL_COMMON,
	};

	public static short[] veinResources = {
			ElementKeyMap.RESS_CRYS_HATTEL,
			ElementKeyMap.RESS_CRYS_SINTYR,
			ElementKeyMap.RESS_CRYS_MATTISE,
			ElementKeyMap.RESS_CRYS_RAMMET,
			ElementKeyMap.RESS_CRYS_VARAT,
			ElementKeyMap.RESS_GAS_BASTYN,
			ElementKeyMap.RESS_CRYS_CRYSTAL_COMMON,
			ElementKeyMap.RESS_CRYS_NOCX,
	};

	public interface TerrainGeneratorTypeI {
		public int getId();

		public TerrainGenerator inst(int seed, float planetRadius);

		public static TerrainGeneratorTypeI getFromId(int id) {
			return TerrainGeneratorType.values()[id];
		}
	}

	private interface TInst {
		public TerrainGenerator inst(int seed, float planetRadius);
	}

	public enum TerrainGeneratorType implements TerrainGeneratorTypeI {
		BARREN(TerrainGeneratorBarren::new),
		EARTH(TerrainGeneratorEarth::new),
		DESERT(TerrainGeneratorDesert::new),
		FROZEN(TerrainGeneratorFrozen::new),
		VOLCANIC(TerrainGeneratorVolcanic::new),
		CORRUPTED(TerrainGeneratorCorrupted::new),
		RUINED_CITY(TerrainGeneratorRuinedCities::new),
		MESA(TerrainGeneratorMesa::new);

		private TInst t;

		TerrainGeneratorType(TInst t) {
			this.t = t;
		}

		@Override
		public int getId() {
			return ordinal();
		}

		@Override
		public TerrainGenerator inst(int seed, float planetRadius) {
			return t.inst(seed, planetRadius);
		}

	}

	public abstract TerrainGeneratorTypeI getType();

	public static final float biomeSizeModifier = 1f;
	public static final float rockStructureResourceChance = 0.4f;

	public static final float caveRatio = 0.88f;
	public static final float caveDepth = 130f;

	public static float resourceChance = 0.0003f;
	public static short standardResourceSize = TerrainStructure.toHalfFloat(2.5f);
	public static short veinResourceSize = TerrainStructure.toHalfFloat(30f);
	public static float veinResourceChance = 0.03f;

	public final float planetRadius;
	protected final int seed;
	protected FastNoiseSIMD caveFNS;

	private Vector3f[] biomeVectors;
	private short[] biomeIDs;

	public TerrainGenerator(int seed, float planetRadius) {

		biomeVectors = new Vector3f[0];
		biomeIDs = new short[0];

		this.seed = seed;
		this.planetRadius = planetRadius;

		caveFNS = new FastNoiseSIMD(seed);
		caveFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Cellular);
		caveFNS.SetCellularReturnType(FastNoiseSIMD.CellularReturnType.Distance2Cave);
		caveFNS.SetCellularDistanceFunction(FastNoiseSIMD.CellularDistanceFunction.Euclidean);
		caveFNS.SetFrequency(0.003f);
		caveFNS.SetCellularJitter(0.3f);

		caveFNS.SetPerturbType(FastNoiseSIMD.PerturbType.GradientFractal);
		caveFNS.SetPerturbFractalOctaves(2);
		caveFNS.SetPerturbAmp(0.3f);
		caveFNS.SetPerturbFrequency(3.0f);
		caveFNS.SetPerturbFractalLacunarity(12f);
		caveFNS.SetPerturbFractalGain(0.08f);
	}

	abstract public TerrainStructureList generateSegment(Segment w, RequestData requestData) throws SegmentDataWriteException;

	public void generateStructures(Segment w, RequestDataStructureGen requestData) {
		TerrainChunkCacheElement[] elements = requestData.rawChunks;

		try {
			int i = 0;
			for(int zi = 0; zi < 3; zi++) {
				for(int yi = 0; yi < 3; yi++) {
					for(int xi = 0; xi < 3; xi++) {

						TerrainStructureList tsl = elements[i++].getStructureList();

						if(tsl != null)
							tsl.buildAll(w, requestData, (xi - 1) * SegmentData.SEG, (yi - 1) * SegmentData.SEG, (zi - 1) * SegmentData.SEG);
					}
				}
			}
		} catch(SegmentDataWriteException e) {
			throw new RuntimeException("Cannot write to chunk: " + e.data.getClass().toString(), e);
		}
	}

	abstract public void generateLOD();

	public int isSolidTerrain(int x, int y, int z) {
		return -1;
	}

	public boolean isEmptyTerrain(int x, int y, int z) {
		return false;
	}

	protected void spherePos(Vector3f p) {
		p.scale(planetRadius * FastMath.carmackInvSqrt(p.x * p.x + p.y * p.y + p.z * p.z));
	}

	protected void getBiome(BlockBiomeData bbd, Vector3f position) {

		float maxDot = -Float.MAX_VALUE;
		float maxDot2 = -Float.MAX_VALUE;
		short biomeID = -1;
		short biomeID2 = -1;

		for(int i = 0; i < biomeVectors.length; i++) {
			float dot = biomeVectors[i].dot(position);

			if(dot > maxDot) {
				maxDot2 = maxDot;
				biomeID2 = biomeID;

				maxDot = dot;
				biomeID = biomeIDs[i];
			} else if(dot > maxDot2) {
				maxDot2 = dot;
				biomeID2 = biomeIDs[i];
			}
		}

		bbd.id = biomeID;
		bbd.id2 = biomeID2;
		bbd.centerDistance = maxDot;
		bbd.edgeDistance = maxDot - maxDot2;
	}

	protected void addBiome(short biomeID, Vector3f position) {

		FastMath.normalizeCarmack(position);

		ArrayList<Vector3f> biomeVectorsList = new ArrayList<Vector3f>(Arrays.asList(biomeVectors));
		ShortArrayList biomeIDsList = new ShortArrayList(biomeIDs);

		biomeVectorsList.add(position);
		biomeIDsList.add(biomeID);

		biomeVectors = biomeVectorsList.toArray(new Vector3f[biomeVectorsList.size()]);
		biomeIDs = biomeIDsList.toShortArray();
	}

	protected class Biome {
		public short id;
		public float chance;
		public float sizeModifier;

		public Biome(short id, float chance, float sizeModifier) {
			this.id = id;
			this.chance = chance;
			this.sizeModifier = sizeModifier;
		}

		public Biome(short id, float chance) {
			this.id = id;
			this.chance = chance;
			this.sizeModifier = 1f;
		}

		public Biome(short id) {
			this.id = id;
			this.chance = 1f;
			this.sizeModifier = 1f;
		}
	}

	protected void addBiomesRandom(float minBiomeSize, Biome... biomes) {

		ArrayList<Vector3f> biomeVectorsList = new ArrayList<Vector3f>(Arrays.asList(biomeVectors));
		ShortArrayList biomeIDsList = new ShortArrayList(biomeIDs);
		Random rand = new Random(seed);

		int biomeI;
		while(rand.nextFloat() > biomes[biomeI = rand.nextInt(biomes.length)].chance) {
		}

		minBiomeSize = planetRadius * planetRadius - minBiomeSize;
		int toCloseCount = 0;
		Vector3f v3 = new Vector3f();

		while(toCloseCount < 500) {
			v3.set(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
			FastMath.normalizeCarmack(v3);

			boolean toClose = false;
			float modifier = planetRadius * planetRadius;

			for(Vector3f bv : biomeVectorsList) {
				if(bv.dot(v3) * modifier > minBiomeSize) {
					toClose = true;
					toCloseCount++;
					break;
				}
			}

			if(toClose)
				continue;

			toCloseCount = 0;
			biomeVectorsList.add(v3);
			v3 = new Vector3f();
			biomeIDsList.add(biomes[biomeI].id);

			while(rand.nextFloat() > biomes[biomeI = rand.nextInt(biomes.length)].chance) {
			}
		}

		biomeVectors = biomeVectorsList.toArray(new Vector3f[biomeVectorsList.size()]);
		biomeIDs = biomeIDsList.toShortArray();

		//Evenly space biomes
		/*if (biomeVectors.length < 4)
			return;			
		
		for (int smoothCount = 0; smoothCount < 3; smoothCount++){
			
			for (biomeI = 0; biomeI < biomeVectors.length; biomeI++){
				Vector3f[] closeBiomes = new Vector3f[3];
				
				for (int compareI = 0; compareI < biomeVectors.length; compareI++){
					if (compareI == biomeI)
						continue;
					
					for (int i = 0; i < closeBiomes.length; i++)
					{
						if (closeBiomes[i] == null ||
							biomeVectors[biomeI].dot(biomeVectors[compareI]) > biomeVectors[biomeI].dot(closeBiomes[i])){
							
							closeBiomes[i] = biomeVectors[compareI];
							break;
						}						
					}
				}
				
				//Average biome vec from 3 closest biomes
				biomeVectors[biomeI].set(0, 0, 0);
				for (int i = 0; i < closeBiomes.length; i++)				
					biomeVectors[biomeI].add(closeBiomes[i]);					
				
				FastMath.normalizeCarmack(biomeVectors[biomeI]);
			}
		}*/
	}

	protected void addSetPieceBiomes(int seed, short biomeID, float chance) {
		Random r = new Random(seed);

		for(byte i = 0; i < 20; i++) {
			if(r.nextFloat() < chance) {

				Vector3f v3 = new Vector3f(0f, 1f, 0f);
				IcosahedronHelper.getSideTransform(i).basis.transform(v3);
				addBiome(biomeID, v3);
			}
		}
	}

	public TerrainStructureList generateSegmentUnderground(Segment w, RequestData requestData, int solidBlockIndex) throws SegmentDataWriteException {

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

		float[] caveNoise = rd.noiseSet0;

		sideTransform.transform(blockPos);
		caveFNS.FillSampledNoiseSet(caveNoise, IcosahedronHelper.getVectorSet(side, 2), blockPos.x, blockPos.y, blockPos.z);

		for(byte x = 0; x < SegmentData.SEG; x++) {
			for(byte z = 0; z < SegmentData.SEG; z++) {
				index++;// Skip usual extra top y block
				for(byte y = SegmentData.SEG - 1; y >= 0; y--) {

					// Skip all remaining -y blocks if out of side
					if(!allInSide && !IcosahedronHelper.isPointInSide(x + w.pos.x - SegmentData.SEG_HALF, y + w.pos.y - SegmentData.SEG_HALF, z + w.pos.z - SegmentData.SEG_HALF)) {
						tcce.outOfSide();
						index += y + 1;
						break;
					}

					float density;// = planetRadius - FastMath.carmackLength(blockPos);

					//if (planetMag < caveDepth)
					density = (caveNoise[index] - caveRatio) * -30f;

					if(density > 0f) {
						tsl = placeSolidAndResources(solidBlockIndex, r, tsl, tcce, x, y, z, segData);

					} else {
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

	public static TerrainStructureList addStructure(TerrainStructureList tsl, short x, short y, short z, TerrainStructure.Type structureType, short metaData0, short metaData1, short metaData2) {

		if(tsl == null)
			tsl = new TerrainStructureList();

		tsl.add(x, y, z, structureType, metaData0, metaData1, metaData2);

		return tsl;
	}

	protected static TerrainStructureList addStructure(TerrainStructureList tsl, short x, short y, short z, TerrainStructure.Type structureType, short metaData0, short metaData1) {
		return addStructure(tsl, x, y, z, structureType, metaData0, metaData1, (short) 0);
	}

	protected static TerrainStructureList addStructure(TerrainStructureList tsl, short x, short y, short z, TerrainStructure.Type structureType, short metaData0) {
		return addStructure(tsl, x, y, z, structureType, metaData0, (short) 0, (short) 0);
	}

	protected static TerrainStructureList addStructure(TerrainStructureList tsl, short x, short y, short z, TerrainStructure.Type structureType) {
		return addStructure(tsl, x, y, z, structureType, (short) 0, (short) 0, (short) 0);
	}

	protected static TerrainStructureList addStructureNotNear(TerrainStructureList tsl, short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType, short metaData0, short metaData1, short metaData2) {

		if(tsl == null)
			tsl = new TerrainStructureList();

		tsl.addNotNear(x, y, z, minNearDistance, structureType, metaData0, metaData1, metaData2);

		return tsl;
	}

	protected static TerrainStructureList addStructureNotNear(TerrainStructureList tsl, short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType, short metaData0, short metaData1) {
		return addStructureNotNear(tsl, x, y, z, minNearDistance, structureType, metaData0, metaData1, (short) 0);
	}

	protected static TerrainStructureList addStructureNotNear(TerrainStructureList tsl, short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType, short metaData0) {
		return addStructureNotNear(tsl, x, y, z, minNearDistance, structureType, metaData0, (short) 0, (short) 0);
	}

	protected static TerrainStructureList addStructureNotNear(TerrainStructureList tsl, short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType) {
		return addStructureNotNear(tsl, x, y, z, minNearDistance, structureType, (short) 0, (short) 0, (short) 0);
	}

	public static float lerp(float a, float b, float t) {
		return a + t * (b - a);
	}

	public static float clamp01(float v) {
		return Math.max(0f, Math.min(1f, v));
	}

	public static float cubeDistance(Vector3f v3) {
		return Math.max(Math.abs(v3.x), Math.max(Math.abs(v3.y), Math.abs(v3.z)));
	}

	public static int registerBlock(short block) {
		return GenerationElementMap.getBlockDataIndex(SegmentData.makeDataInt(block));
	}

	public static int[] registerBlockDamaged(short block) {
		int[] damagedIDs = new int[8];
		int maxHP = ElementKeyMap.getInfoFast(block).getMaxHitPointsFull();
		int stepHP = maxHP / 8;

		for(int i = 0; i < 8; i++) {
			damagedIDs[i] = GenerationElementMap.getBlockDataIndex(SegmentData.makeDataInt(block, (byte) 0, false, (byte) (maxHP - stepHP * i)));
		}
		return damagedIDs;
	}

	public static short randomStandardResource(Random r) {
		return standardResources[r.nextInt(standardResources.length)];
	}

	protected TerrainStructureList placeSolidAndResources(int solidBlockIndex, Random r, TerrainStructureList tsl, TerrainChunkCacheElement tcce, byte x, byte y, byte z, SegmentData segData) throws SegmentDataWriteException {

		int blockData = tcce.placeBlock(solidBlockIndex, x, y, z, segData);

		if(r.nextFloat() < resourceChance) {
			assert (ElementKeyMap.getInfoFast(SegmentData.getTypeFromIntData(blockData)).resourceInjection == ElementInformation.ResourceInjectionType.ORE);

			if(r.nextFloat() < veinResourceChance) {
				tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.ResourceVein,
						veinResources[r.nextInt(veinResources.length)], SegmentData.getTypeFromIntData(blockData), veinResourceSize);

			} else {
				tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.ResourceBlob,
						randomStandardResource(r), SegmentData.getTypeFromIntData(blockData), standardResourceSize);
			}
		}

		return tsl;
	}

}

//package org.schema.game.server.controller.world.factory.planet.terrain;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Random;
//
//import javax.vecmath.Matrix3f;
//import javax.vecmath.Vector3f;
//
//import org.schema.common.FastMath;
//import org.schema.game.common.controller.PlanetIco;
//import org.schema.game.common.data.Icosahedron;
//import org.schema.game.common.data.element.ElementInformation;
//import org.schema.game.common.data.element.ElementKeyMap;
//import org.schema.game.common.data.world.Segment;
//import org.schema.game.common.data.world.SegmentData;
//import org.schema.game.common.data.world.SegmentDataWriteException;
//import org.schema.game.server.controller.GenerationElementMap;
//import org.schema.game.server.controller.RequestData;
//import org.schema.game.server.controller.RequestDataIcoPlanet;
//import org.schema.game.server.controller.RequestDataStructureGen;
//import org.schema.game.server.controller.TerrainChunkCacheElement;
//import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;
//import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
//import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
//
//import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//import it.unimi.dsi.fastutil.shorts.ShortArrayList;
//
//public abstract class TerrainGenerator {
//
//	public static short[] standardResources = {
//		ElementKeyMap.RESS_ORE_THRENS,
//		ElementKeyMap.RESS_ORE_JISPER,
//		ElementKeyMap.RESS_ORE_ZERCANER,
//		ElementKeyMap.RESS_ORE_SERTISE,
//		ElementKeyMap.RESS_ORE_HITAL,
//		ElementKeyMap.RESS_ORE_FERTIKEEN,
//		ElementKeyMap.RESS_ORE_PARSTUN,
//		ElementKeyMap.RESS_ORE_NACHT,
//	};
//
//	public static short[] veinResources = {
//		ElementKeyMap.RESS_CRYS_HATTEL,
//		ElementKeyMap.RESS_CRYS_SINTYR,
//		ElementKeyMap.RESS_CRYS_MATTISE,
//		ElementKeyMap.RESS_CRYS_RAMMET,
//		ElementKeyMap.RESS_CRYS_VARAT,
//		ElementKeyMap.RESS_CRYS_BASTYN,
//		ElementKeyMap.RESS_CRYS_PARSEN,
//		ElementKeyMap.RESS_CRYS_NOCX,
//	};
//
//	public static final float biomeSizeModifier = 1f;
//	public static final float rockStructureResourceChance = 0.4f;
//
//	public static final float caveRatio = 0.88f;
//	public static final float caveDepth = 130f;
//
//	public static float resourceChance = 0.0003f;
//	public static short standardResourceSize = TerrainStructure.toHalfFloat(2.5f);
//	public static short veinResourceSize = TerrainStructure.toHalfFloat(30f);
//	public static float veinResourceChance = 0.03f;
//
//	public final float planetRadius;
//	protected final int seed;
//	protected FastNoiseSIMD caveFNS;
//
//	private float[] biomeVectors;
//	private short[] biomeIDs;
//
//	public TerrainGenerator(int seed, float planetRadius) {
//
//		biomeVectors = new float[0];
//		biomeIDs = new short[0];
//
//		this.seed = seed;
//		this.planetRadius = planetRadius;
//
//		caveFNS = new FastNoiseSIMD(seed);
//		caveFNS.SetNoiseType(FastNoiseSIMD.NoiseType.Cellular);
//		caveFNS.SetCellularReturnType(FastNoiseSIMD.CellularReturnType.Distance2Cave);
//		caveFNS.SetCellularDistanceFunction(FastNoiseSIMD.CellularDistanceFunction.Euclidean);
//		caveFNS.SetFrequency(0.003f);
//		caveFNS.SetCellularJitter(0.3f);
//
//		caveFNS.SetPerturbType(FastNoiseSIMD.PerturbType.GradientFractal);
//		caveFNS.SetPerturbFractalOctaves(2);
//		caveFNS.SetPerturbAmp(0.3f);
//		caveFNS.SetPerturbFrequency(3.0f);
//		caveFNS.SetPerturbFractalLacunarity(12f);
//		caveFNS.SetPerturbFractalGain(0.08f);
//	}
//
//	abstract public TerrainStructureList generateSegment(Segment w, RequestData requestData) throws SegmentDataWriteException;
//
//	public void generateStructures(Segment w, RequestDataStructureGen requestData) {
//		TerrainChunkCacheElement[] elements = requestData.rawChunks;
//
//		try {
//			int i = 0;
//			for (int zi = 0; zi < 3; zi++) {
//				for (int yi = 0; yi < 3; yi++) {
//					for (int xi = 0; xi < 3; xi++) {
//
//						TerrainStructureList tsl = elements[i++].getStructureList();
//
//						if (tsl != null)
//							tsl.buildAll(w, requestData, (xi - 1) * SegmentData.SEG, (yi - 1) * SegmentData.SEG, (zi - 1) * SegmentData.SEG);
//					}
//				}
//			}
//		} catch (SegmentDataWriteException e) {
//			throw new RuntimeException("Cannot write to chunk: " + e.data.getClass().toString(), e);
//		}
//	}
//
//	abstract public void generateLOD();
//
//	public int isSolidTerrain(int x, int y, int z) {
//		return -1;
//	}
//
//	public boolean isEmptyTerrain(int x, int y, int z) {
//		return false;
//	}
//
//	protected void spherePos(Vector3f p) {
//		p.scale(planetRadius * FastMath.carmackInvSqrt(p.x * p.x + p.y * p.y + p.z * p.z));
//	}
//
//	protected void getBiome(BlockBiomeData bbd, int x, int y, int z) {
//
//		float maxDot = -Float.MAX_VALUE;
//		float maxDot2 = -Float.MAX_VALUE;
//		short biomeID = -1;
//		short biomeID2 = -1;
//
//		int bin = 0;
//		for (int i = 0; i < biomeVectors.length; i+=3) {
//			float dot = (x*biomeVectors[i] + y*biomeVectors[i+1] + z*biomeVectors[i+2]);//biomeVectors[i].dot(position);
//
//			if (dot > maxDot) {
//				maxDot2 = maxDot;
//				biomeID2 = biomeID;
//
//				maxDot = dot;
//				biomeID = biomeIDs[bin];
//			} else if (dot > maxDot2) {
//				maxDot2 = dot;
//				biomeID2 = biomeIDs[bin];
//			}
//			bin++;
//		}
//
//		bbd.id = biomeID;
//		bbd.id2 = biomeID2;
//		bbd.centerDistance = maxDot;
//		bbd.edgeDistance = maxDot - maxDot2;
//	}
//
//	protected void addBiome(short biomeID, Vector3f position) {
//
//		FastMath.normalizeCarmack(position);
//
//		int index = biomeVectors.length;
//		biomeVectors = Arrays.copyOf(biomeVectors, biomeVectors.length+3);
//		ShortArrayList biomeIDsList = new ShortArrayList(biomeIDs);
//
//		biomeVectors[index] = position.x;
//		biomeVectors[index+1] = position.y;
//		biomeVectors[index+2] = position.z;
//		biomeIDsList.add(biomeID);
//
//		biomeIDs = biomeIDsList.toShortArray();
//	}
//
//	protected class Biome {
//		public short id;
//		public float chance;
//		public float sizeModifier;
//
//		public Biome(short id, float chance, float sizeModifier) {
//			this.id = id;
//			this.chance = chance;
//			this.sizeModifier = sizeModifier;
//		}
//
//		public Biome(short id, float chance) {
//			this.id = id;
//			this.chance = chance;
//			this.sizeModifier = 1f;
//		}
//
//		public Biome(short id) {
//			this.id = id;
//			this.chance = 1f;
//			this.sizeModifier = 1f;
//		}
//	}
//
//	private List<Vector3f> getBiomeVectorListInstance(){
//		List<Vector3f> biomeVectorsList = new ObjectArrayList<Vector3f>(biomeVectors.length/3);
//		
//		for(int i = 0; i < biomeVectors.length; i+=3) {
//			biomeVectorsList.add(new Vector3f(biomeVectors[i], biomeVectors[i+1], biomeVectors[i+2]));
//		}
//		return biomeVectorsList;
//	}
//	private float[] getBiomeVectorsFromList(List<Vector3f> bs){
//		float[] b = new float[bs.size()*3];
//		
//		
//		for(int i = 0; i < bs.size(); i++) {
//			b[i*3] = bs.get(i).x;
//			b[i*3+1] = bs.get(i).y;
//			b[i*3+2] = bs.get(i).z;
//		}
//		return b;
//	}
//	protected void addBiomesRandom(float minBiomeSize, Biome... biomes) {
//
//		List<Vector3f> biomeVectorsList = getBiomeVectorListInstance();
//		ShortArrayList biomeIDsList = new ShortArrayList(biomeIDs);
//		Random rand = new Random(seed);
//
//		int biomeI;
//		while (rand.nextFloat() > biomes[biomeI = rand.nextInt(biomes.length)].chance) {
//		}
//
//		minBiomeSize = planetRadius * planetRadius - minBiomeSize;
//		int toCloseCount = 0;
//		Vector3f v3 = new Vector3f();
//
//		while (toCloseCount < 500) {
//			v3.set(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
//			FastMath.normalizeCarmack(v3);
//
//			boolean toClose = false;
//			float modifier = planetRadius * planetRadius;
//
//			for (Vector3f bv : biomeVectorsList) {
//				if (bv.dot(v3) * modifier > minBiomeSize) {
//					toClose = true;
//					toCloseCount++;
//					break;
//				}
//			}
//
//			if (toClose)
//				continue;
//
//			toCloseCount = 0;
//			biomeVectorsList.add(v3);
//			v3 = new Vector3f();
//			biomeIDsList.add(biomes[biomeI].id);
//
//			while (rand.nextFloat() > biomes[biomeI = rand.nextInt(biomes.length)].chance) {
//			}
//		}
//
//		biomeVectors = getBiomeVectorsFromList(biomeVectorsList);
//		biomeIDs = biomeIDsList.toShortArray();
//
//		//Evenly space biomes
//		/*if (biomeVectors.length < 4)
//			return;			
//		
//		for (int smoothCount = 0; smoothCount < 3; smoothCount++){
//			
//			for (biomeI = 0; biomeI < biomeVectors.length; biomeI++){
//				Vector3f[] closeBiomes = new Vector3f[3];
//				
//				for (int compareI = 0; compareI < biomeVectors.length; compareI++){
//					if (compareI == biomeI)
//						continue;
//					
//					for (int i = 0; i < closeBiomes.length; i++)
//					{
//						if (closeBiomes[i] == null ||
//							biomeVectors[biomeI].dot(biomeVectors[compareI]) > biomeVectors[biomeI].dot(closeBiomes[i])){
//							
//							closeBiomes[i] = biomeVectors[compareI];
//							break;
//						}						
//					}
//				}
//				
//				//Average biome vec from 3 closest biomes
//				biomeVectors[biomeI].set(0, 0, 0);
//				for (int i = 0; i < closeBiomes.length; i++)				
//					biomeVectors[biomeI].add(closeBiomes[i]);					
//				
//				FastMath.normalizeCarmack(biomeVectors[biomeI]);
//			}
//		}*/
//	}
//
//	protected void addSetPieceBiomes(int seed, short biomeID, float chance) {
//		Random r = new Random(seed);
//
//		for (byte i = 0; i < 20; i++) {
//			if (r.nextFloat() < chance) {
//
//				Vector3f v3 = new Vector3f(0f, 1f, 0f);
//				Icosahedron.getSideTransform(i).basis.transform(v3);
//				addBiome(biomeID, v3);
//			}
//		}
//	}
//
//	public TerrainStructureList generateSegmentUnderground(Segment w, RequestData requestData, int solidBlockIndex) throws SegmentDataWriteException {
//
//		SegmentData segData = w.getSegmentData();
//		byte side = ((PlanetIco) w.getSegmentController()).getSideId();
//		Matrix3f sideTransform = Icosahedron.getSideTransform(side).basis;
//		RequestDataIcoPlanet rd = (RequestDataIcoPlanet) requestData;
//		TerrainChunkCacheElement tcce = rd.currentChunkCache;
//		Vector3f blockPos = rd.vector3f;
//		//BlockBiomeData bbd = rd.blockBiomeData;
//		Random r = rd.random;
//
//		blockPos.set(w.pos.x, w.pos.y, w.pos.z);
//		r.setSeed(blockPos.hashCode() + seed);
//
//		TerrainStructureList tsl = null;
//
//		boolean allInSide = Icosahedron.isSegmentAllInSide(w.pos);
//		int index = 0;
//
//		float[] caveNoise = rd.noiseSet0;
//
//		sideTransform.transform(blockPos);
//		caveFNS.FillSampledNoiseSet(caveNoise, Icosahedron.getVectorSet(side, 2), blockPos.x, blockPos.y, blockPos.z);
//
//		for (byte x = 0; x < SegmentData.SEG; x++) {
//			for (byte z = 0; z < SegmentData.SEG; z++) {
//				index++;// Skip usual extra top y block
//				for (byte y = SegmentData.SEG - 1; y >= 0; y--) {
//
//					// Skip all remaining -y blocks if out of side
//					if (!allInSide && !Icosahedron.isPointInSide(x + w.pos.x - SegmentData.SEG_HALF, y + w.pos.y - SegmentData.SEG_HALF, z + w.pos.z - SegmentData.SEG_HALF)) {
//						tcce.outOfSide();
//						index += y + 1;
//						break;
//					}
//
//					float density;// = planetRadius - FastMath.carmackLength(blockPos);
//
//					//if (planetMag < caveDepth)
//					density = (caveNoise[index] - caveRatio) * -30f;
//
//					if (density > 0f) {
//						tsl = placeSolidAndResources(solidBlockIndex, r, tsl, tcce, x, y, z, segData);
//
//					} else {
//						tcce.placeAir();
//					}
//
//					index++;
//				}
//			}
//		}
//
//		segData.getSegmentController().getSegmentBuffer()
//			.updateBB(segData.getSegment());
//
//		return tsl;
//	}
//
//	public static TerrainStructureList addStructure(TerrainStructureList tsl, short x, short y, short z, TerrainStructure.Type structureType, short metaData0, short metaData1, short metaData2) {
//
//		if (tsl == null)
//			tsl = new TerrainStructureList();
//
//		tsl.add(x, y, z, structureType, metaData0, metaData1, metaData2);
//
//		return tsl;
//	}
//
//	protected static TerrainStructureList addStructure(TerrainStructureList tsl, short x, short y, short z, TerrainStructure.Type structureType, short metaData0, short metaData1) {
//		return addStructure(tsl, x, y, z, structureType, metaData0, metaData1, (short) 0);
//	}
//
//	protected static TerrainStructureList addStructure(TerrainStructureList tsl, short x, short y, short z, TerrainStructure.Type structureType, short metaData0) {
//		return addStructure(tsl, x, y, z, structureType, metaData0, (short) 0, (short) 0);
//	}
//
//	protected static TerrainStructureList addStructure(TerrainStructureList tsl, short x, short y, short z, TerrainStructure.Type structureType) {
//		return addStructure(tsl, x, y, z, structureType, (short) 0, (short) 0, (short) 0);
//	}
//
//	protected static TerrainStructureList addStructureNotNear(TerrainStructureList tsl, short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType, short metaData0, short metaData1, short metaData2) {
//
//		if (tsl == null)
//			tsl = new TerrainStructureList();
//
//		tsl.addNotNear(x, y, z, minNearDistance, structureType, metaData0, metaData1, metaData2);
//
//		return tsl;
//	}
//
//	protected static TerrainStructureList addStructureNotNear(TerrainStructureList tsl, short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType, short metaData0, short metaData1) {
//		return addStructureNotNear(tsl, x, y, z, minNearDistance, structureType, metaData0, metaData1, (short) 0);
//	}
//
//	protected static TerrainStructureList addStructureNotNear(TerrainStructureList tsl, short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType, short metaData0) {
//		return addStructureNotNear(tsl, x, y, z, minNearDistance, structureType, metaData0, (short) 0, (short) 0);
//	}
//
//	protected static TerrainStructureList addStructureNotNear(TerrainStructureList tsl, short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType) {
//		return addStructureNotNear(tsl, x, y, z, minNearDistance, structureType, (short) 0, (short) 0, (short) 0);
//	}
//
//	public static float lerp(float a, float b, float t) {
//		return a + t * (b - a);
//	}
//
//	public static float clamp01(float v) {
//		return Math.max(0f, Math.min(1f, v));
//	}
//
//	public static float cubeDistance(Vector3f v3) {
//		return Math.max(Math.abs(v3.x), Math.max(Math.abs(v3.y), Math.abs(v3.z)));
//	}
//
//	public static int registerBlock(short block) {
//		return GenerationElementMap.getBlockDataIndex(SegmentData.makeDataInt(block));
//	}
//
//	public static int[] registerBlockDamaged(short block) {
//		int[] damagedIDs = new int[8];
//		int maxHP = ElementKeyMap.getInfoFast(block).getMaxHitPointsFull();
//		int stepHP = maxHP / 8;
//
//		for (int i = 0; i < 8; i++) {
//			damagedIDs[i] = GenerationElementMap.getBlockDataIndex(SegmentData.makeDataInt(block, (byte) 0, false, (byte) (maxHP - stepHP * i)));
//		}
//		return damagedIDs;
//	}
//
//	public static short randomStandardResource(Random r) {
//		return standardResources[r.nextInt(standardResources.length)];
//	}
//
//	protected TerrainStructureList placeSolidAndResources(int solidBlockIndex, Random r, TerrainStructureList tsl, TerrainChunkCacheElement tcce, byte x, byte y, byte z, SegmentData segData) throws SegmentDataWriteException {
//
//		int blockData = tcce.placeBlock(solidBlockIndex, x, y, z, segData);
//
//		if (r.nextFloat() < resourceChance) {
//			assert (ElementKeyMap.getInfoFast(SegmentData.getTypeFromIntData(blockData)).resourceInjection == ElementInformation.ResourceInjectionType.ORE);
//
//			if (r.nextFloat() < veinResourceChance) {
//				tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.ResourceVein,
//								   veinResources[r.nextInt(veinResources.length)], SegmentData.getTypeFromIntData(blockData), veinResourceSize);
//
//			} else {
//				tsl = addStructure(tsl, x, y, z, TerrainStructure.Type.ResourceBlob,
//								   randomStandardResource(r), SegmentData.getTypeFromIntData(blockData), standardResourceSize);
//			}
//		}
//
//		return tsl;
//	}
//}


